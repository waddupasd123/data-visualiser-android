package com.example.androidapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi

class BluetoothManager(
    context: Context,
    private val permissionsLauncher: ActivityResultLauncher<Array<String>>,
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent>
) {

    private val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter

    val bleDevices = mutableListOf<BluetoothDevice>()

    @RequiresApi(Build.VERSION_CODES.S)
    fun requestPermissions() {
        val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )

        permissionsLauncher.launch(permissions)
    }

    fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            Log.e("BLE","Requesting bluetooth")
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }


}