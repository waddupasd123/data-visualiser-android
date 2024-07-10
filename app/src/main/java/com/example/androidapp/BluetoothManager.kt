package com.example.androidapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

class BluetoothManager(
    private val context: Context,
    private val permissionsLauncher: ActivityResultLauncher<Array<String>>,
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent>
) {

    private val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter

    val bleDevices = mutableStateListOf<BluetoothDevice>()
    val isScanning = mutableStateOf(false)
    val connectedDevices = mutableStateListOf<BluetoothDevice>()
    val knownDevices = mutableStateListOf<BluetoothDevice>()
    private val gattConnections = mutableStateMapOf<String, BluetoothGatt>()

    // Bluetooth Permissions
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
            Log.d("BLE","Requesting bluetooth")
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                if (!bleDevices.contains(device) && !connectedDevices.contains(device)) {
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


    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        Log.d("BLE", "Connecting to device: ${device.address}")

        // Close any existing GATT connection before creating a new one
        gattConnections[device.address]?.let { existingGatt ->
            Log.d("BLE", "Closing existing GATT connection for ${device.address}")
            existingGatt.disconnect()
            existingGatt.close()
            gattConnections.remove(device.address)
        }

        val gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to ${device.address}")
                    connectedDevices.add(device)
                    if (!knownDevices.contains(device)) {
                        knownDevices.add(device)
                        saveKnownDevices()
                    }
                    bleDevices.remove(device)
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from ${device.address}")
                    connectedDevices.remove(device)
                    gattConnections.remove(device.address)
                } else if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e("BLE", "Connection failed with status $status")
                    gatt?.close()
                    gattConnections.remove(device.address)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Services discovered for ${device.address}")
                } else {
                    Log.w("BLE", "onServicesDiscovered received: $status")
                }
            }
        })

        gattConnections[device.address] = gatt
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromDevice(device: BluetoothDevice) {
        gattConnections[device.address]?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            connectedDevices.remove(device)
            gattConnections.remove(device.address)
            Log.d("BLE", "Disconnected from device: ${device.address}")
        }
    }


    // Store and load previous devices
    private fun saveKnownDevices() {
        val sharedPreferences = context.getSharedPreferences("BluetoothManager", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val deviceAddresses = knownDevices.map { it.address }.toSet()
        editor.putStringSet("KnownDevices", deviceAddresses)
        editor.apply()
    }

    fun loadKnownDevices() {
        val sharedPreferences = context.getSharedPreferences("BluetoothManager", Context.MODE_PRIVATE)
        val deviceAddresses = sharedPreferences.getStringSet("KnownDevices", emptySet())
        deviceAddresses?.forEach { address ->
            bluetoothAdapter?.getRemoteDevice(address)?.let { device ->
                knownDevices.add(device)
            }
        }
    }


}