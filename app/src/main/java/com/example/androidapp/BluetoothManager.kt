package com.example.androidapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import java.util.UUID


// Manage bluetooth functionality here

class BluetoothManager(
    private val context: Context,
    private val dataManager: DataManager,
    private val permissionsLauncher: ActivityResultLauncher<Array<String>>,
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent>
) {

    private val bluetoothManager : BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter

    private val serviceUUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val characteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    val bleDevices = mutableStateListOf<BluetoothDevice>()
    val isScanning = mutableStateOf(false)
    val connectedDevices = mutableStateListOf<BluetoothDevice>()
    val knownDevices = mutableStateListOf<BluetoothDevice>()
    private val gattConnections = mutableStateMapOf<String, BluetoothGatt>()

    val notificationsManager = mutableStateMapOf<String, Boolean>()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("BluetoothManager", Context.MODE_PRIVATE)

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
            Log.d("BluetoothManager","Requesting bluetooth")
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                if (!bleDevices.contains(device) && !connectedDevices.contains(device)) {
                    bleDevices.add(device)
                    Log.d("BluetoothManager", "Device found: ${device.name} - ${device.address}")
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> onScanResult(0, result) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothManager", "Scan failed with error: $errorCode")
        }
    }


    private val handler = Handler(Looper.getMainLooper())
    private val scanPeriod: Long = 10000
    @SuppressLint("MissingPermission")
    fun startBleScan() {
        if (!isScanning.value) {
            handler.postDelayed({
                isScanning.value = false
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }, scanPeriod)
            isScanning.value = true
            bleDevices.clear()
            Log.d("BluetoothManager", "Starting BLE scan")
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        } else {
            isScanning.value = false
            Log.d("BluetoothManager", "Stopping BLE scan")
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }


    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        Log.d("BluetoothManager", "Connecting to device: ${device.address}")

        // Close any existing GATT connection before creating a new one
        gattConnections[device.address]?.let { existingGatt ->
            Log.d("BluetoothManager", "Closing existing GATT connection for ${device.address}")
            existingGatt.disconnect()
            existingGatt.close()
            gattConnections.remove(device.address)
        }

        val gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothManager", "Connected to ${device.address}")
                    connectedDevices.add(device)
                    if (!knownDevices.contains(device)) {
                        knownDevices.add(device)
                        saveKnownDevices()
                    }
                    bleDevices.remove(device)
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothManager", "Disconnected from ${device.address}")
                    connectedDevices.remove(device)
                    gattConnections.remove(device.address)
                    notificationsManager[device.address] = false
                } else if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e("BluetoothManager", "Connection failed with status $status")
                    gatt?.close()
                    gattConnections.remove(device.address)
                    notificationsManager[device.address] = false
                }
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    notificationsManager[device.address] = false
                } else {
                    Log.d("BluetoothManager", "onServicesDiscovered received: $status")
                }
            }

            // https://stackoverflow.com/questions/73438580/new-oncharacteristicread-method-not-working
            @Suppress("DEPRECATION")
            @Deprecated(
                "Used natively in Android 12 and lower",
                ReplaceWith("onCharacteristicRead(gatt, characteristic, characteristic.value, status)")
            )
            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int)
            {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (characteristic?.uuid == characteristicUUID) {
                        val data = characteristic?.getIntValue(
                            BluetoothGattCharacteristic.FORMAT_SINT32,
                            0
                        )
                        dataManager.updateDeviceData(gatt?.device?.address ?: "", data)
                        // Log.d("BluetoothManager", "Characteristic read: $data")
                    }
                }
            }

            @Suppress("DEPRECATION")
            @Deprecated(
                "Used natively in Android 12 and lower",
                ReplaceWith("onCharacteristicChanged(gatt, characteristic, characteristic.value)")
            )
            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                if (characteristic?.uuid == characteristicUUID) {
                    val data =
                        characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0)
                    dataManager.updateDeviceData(gatt?.device?.address ?: "", data)
                    // Log.d("BluetoothManager", "Characteristic changed: $data")
                }
            }

        })

        gattConnections[device.address] = gatt
    }


    // Enables auto update
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun toggleCharacteristicNotification(deviceAddress: String) {
        val gatt = gattConnections[deviceAddress]
        val characteristic = gatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        val enable = !notificationsManager[deviceAddress]!!
        gatt?.setCharacteristicNotification(characteristic, enable)
        Log.d("BluetoothManager", "CHARACTERISTIC $enable")
        characteristic?.descriptors?.forEach { descriptor ->
            if (enable) {
                val status = gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                Log.d("BluetoothManager", "DESCRIPTOR ENABLED: $status")
                if (status == 0) notificationsManager[deviceAddress] = true
            } else {
                val status = gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
                Log.d("BluetoothManager", "DESCRIPTOR DISABLED: $status")
                if (status == 0) notificationsManager[deviceAddress] = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromDevice(device: BluetoothDevice) {
        gattConnections[device.address]?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            connectedDevices.remove(device)
            gattConnections.remove(device.address)
            notificationsManager[device.address] = false
            Log.d("BluetoothManager", "Disconnected from device: ${device.address}")
        }
    }


    // Store and load previous devices
    private fun saveKnownDevices() {
        val editor = sharedPreferences.edit()
        val deviceAddresses = knownDevices.map { it.address }.toSet()
        editor.putStringSet("KnownDevices", deviceAddresses)
        editor.apply()
    }

    fun loadKnownDevices() {
        val deviceAddresses = sharedPreferences.getStringSet("KnownDevices", emptySet())
        deviceAddresses?.forEach { address ->
            bluetoothAdapter?.getRemoteDevice(address)?.let { device ->
                knownDevices.add(device)
            }
        }
    }

    fun deleteDevice(device: BluetoothDevice) {
        disconnectFromDevice(device)
        knownDevices.remove(device)
        saveKnownDevices()

        dataManager.deleteFolder(device.address)
    }

}