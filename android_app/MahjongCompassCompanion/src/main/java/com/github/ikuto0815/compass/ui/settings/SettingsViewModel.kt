package com.github.ikuto0815.compass.ui.settings

import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.ikuto0815.compass.helper.Bluetooth

class SettingsViewModel : ViewModel() {

    private val _statusText = MutableLiveData<String>().apply {
        value = if (Bluetooth.connected) {
            "connected"
        } else {
            "disconnected"
        }

        Bluetooth.stateChangeCB = {
                state: Int ->
            Log.d("STATE", state.toString())
            when(state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    statusText.postValue("connected")
                    disconnectButton.postValue(true)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    statusText.postValue("disconnected")
                    disconnectButton.postValue(false)
                }
            }
        }
    }

    private val _connectButton = MutableLiveData<Boolean>().apply {
        value = Bluetooth.connected
    }

    val statusText: MutableLiveData<String> = _statusText
    val disconnectButton: MutableLiveData<Boolean> = _connectButton
}