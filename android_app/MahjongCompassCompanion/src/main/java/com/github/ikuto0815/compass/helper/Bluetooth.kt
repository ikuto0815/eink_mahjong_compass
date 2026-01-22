package com.github.ikuto0815.compass.helper


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID

object Bluetooth {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    var device: BluetoothDevice? = null
    var gatt : BluetoothGatt? = null
    var gameStateCharacteristic : BluetoothGattCharacteristic? = null
    var busy = false
    var bluetoothGatt : BluetoothGatt? = null
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.w("BT", "STATE_CONNECTED.")
                Bluetooth.gatt = gatt
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("BT", "STATE_DISCONNECTED.")
                Bluetooth.gatt = null
                gameStateCharacteristic = null
                device = null
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            busy = false
            Log.w("BT", "char read $status")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            busy = false
            Log.w("BT", "char write $status")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(address: String): Boolean {
        Log.w("BT", "connect")
        adapter?.let { adapter ->
            try {
                device = adapter.getRemoteDevice(address)
                bluetoothGatt = device?.connectGatt(null, false, bluetoothGattCallback)
            } catch (exception: IllegalArgumentException) {
                Log.w("BT", "Device not found with provided address.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w("BT", "BluetoothAdapter not initialized")
            return false
        }
        return true
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bluetoothGatt?.disconnect()
        device = null
        bluetoothGatt = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendData(state : String): Boolean {
        device?.let {
            if (it.address != Settings.getValue("address")?.uppercase()) {
                bluetoothGatt?.disconnect()
            }
        }
        if (device == null) {
            val address = Settings.getValue("address")
            Log.w("BT", "connecting to address $address")
            address?.let { connect(it.uppercase()) }
            if (address == null) return false
        }
        if (gatt != null) {
            if (gameStateCharacteristic == null) {
                gameStateCharacteristic = gatt?.getService(UUID.fromString("bae5e4dd-f2b4-4461-a84c-b7851fb8efd3"))
                    ?.getCharacteristic(UUID.fromString("bab40271-33ea-48dc-a145-638361f54d2b"))
            }
            if (gameStateCharacteristic != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt?.writeCharacteristic(gameStateCharacteristic!!, state.toByteArray(), WRITE_TYPE_DEFAULT)
                } else {
                    gameStateCharacteristic!!.setValue(state)
                    gatt?.writeCharacteristic(gameStateCharacteristic!!)
                }
                while (busy) {
                    Log.w("BT", "wait")
                    SystemClock.sleep(100);
                }
                return true
            }
        }
        return false
    }
}