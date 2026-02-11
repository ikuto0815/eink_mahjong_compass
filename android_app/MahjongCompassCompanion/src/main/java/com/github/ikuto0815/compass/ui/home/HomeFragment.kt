package com.github.ikuto0815.compass.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.ikuto0815.compass.databinding.FragmentHomeBinding
import com.github.ikuto0815.compass.helper.Settings
import com.github.ikuto0815.compass.helper.defaults
import com.github.ikuto0815.compass.helper.injectCallback
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val myVersion = 1
    private var newVersion = myVersion

    override fun onResume() {
        super.onResume()

        val web = binding.web

        if (newVersion > myVersion) {
            web.defaults("https://9001.ovh/compass/")
            return
        }

        val url = Settings.getValue("url", "https://riichi.berlin-mahjong.club")

        Log.d("URL", "weburl ${web.url} settings url $url")
        if (url != null && web.url?.startsWith(url) != true) {
            url.apply {
                web.defaults(url)
                web.injectCallback()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        thread {
            try {
                val j = JSONObject(URL("https://9001.ovh/compass/update.json").readText())

                newVersion = j.getInt("version")
            } catch (_: Exception) {
                // just silently fail :shrug:
            }

        }.join()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}