package com.example.androidapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class BluetoothManager(
    context: Context,
    private val permissionsLauncher: ActivityResultLauncher<Array<String>>,
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent>
) {

    private val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter

    val bleDevices = mutableStateListOf<BluetoothDevice>()
    val isScanning = mutableStateOf(false)

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

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                if (!bleDevices.contains(device)) {
                    bleDevices.add(device)
                    Log.d("BLE", "Device found: ${device.name} - ${device.address}")
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> onScanResult(0, result) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Scan failed with error: $errorCode")
        }
    }


    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000
    @SuppressLint("MissingPermission")
    fun startBleScan() {
        if (!isScanning.value) {
            handler.postDelayed({
                isScanning.value = false
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }, SCAN_PERIOD)
            isScanning.value = true
            bleDevices.clear()
            Log.d("BLE", "Starting BLE scan")
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        } else {
            isScanning.value = false
            Log.d("BLE", "Stopping BLE scan")
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        Log.d("BLE", "Connecting to device: ${device.address}")
    }


}