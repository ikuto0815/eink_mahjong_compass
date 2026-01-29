package com.github.ikuto0815.compass.helper

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
fun WebView.defaults(url: String) {
    this.webChromeClient = WebChromeClient()
    this.addJavascriptInterface(AndroidJSInterface, "Android")
    this.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
    this.isVerticalScrollBarEnabled = true
    this.isScrollbarFadingEnabled = false
    WebView.setWebContentsDebuggingEnabled(true)

    val settings = this.settings
    settings.builtInZoomControls = true
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    CookieManager.getInstance().setAcceptCookie(true)
    this.loadUrl(url)
}

fun WebView.injectCallback() {
    val handler = Handler()

    handler.postDelayed({
        loadUrl("""
            javascript:(function f() {
            const targetNode = document.querySelector("#tyr-root");
            const config = { attributes: true, childList: true, subtree: true };
            
            const callback = (mutationList, observer) => {
            var data = targetNode[Object.keys(targetNode).findLast((s) => s.startsWith("__reactContainer"))]["memoizedState"]["element"]["props"]["state"];

            data = JSON.stringify(data);
            Android.copyData(data); 
            };
            const observer = new MutationObserver(callback);
            observer.observe(targetNode, config);
            })()
        """.trimIndent())
    }, 2000)
}

object AndroidJSInterface {
    var state = String()
    var old_state = String()

    @Suppress("unused") //called from javascript context
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @JavascriptInterface
    fun copyData(s: String) {
        if (s == "null") return

        val o = JSONObject(s)

        if (!o.has("players"))
            return

        val jaPlayers = o.getJSONArray("players")
        val sessionState = o.getJSONObject("sessionState")
        val honbaCount = sessionState.getInt("honbaCount")
        val riichiCount = sessionState.getInt("riichiCount")
        val roundIndex = sessionState.getInt("roundIndex")
        val dealer = sessionState.getInt("dealer")
        var dealerIdx = -1

        Log.d("CopyData", jaPlayers.toString())

        val players = mutableListOf<JSONObject>()
        for (i in 0 until jaPlayers.length()) {
            players.add(jaPlayers.getJSONObject(i))
            if (jaPlayers.getJSONObject((i)).getInt(("id")) == dealer)
                dealerIdx = i
        }

        var i = 0
        state = "$roundIndex $riichiCount $honbaCount\n"
        while (i < players.size) {
            val pl = players[i]
            val idx = (i - dealerIdx + 4) % 4
            state += "$idx\n${pl["score"]}\n${pl["title"]}\n"
            i += 1
        }
        if (old_state == state) {
            state = ""
            return
        }

        val handler: Handler = Handler()
        handler.postDelayed({
            old_state = if (Bluetooth.sendData(state)) {
                state
            } else {
                ""
            }
        }, 50)

        return
    }
}