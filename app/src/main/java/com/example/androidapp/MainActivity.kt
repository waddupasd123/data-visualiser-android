package com.example.androidapp

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidapp.ui.theme.AndroidAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allPermissionsGranted = true
            permissions.entries.forEach {
                if (!it.value) allPermissionsGranted = false
            }
            if (allPermissionsGranted) {
                bluetoothManager.enableBluetooth()
            } else {
                Log.e("BLE", "Permissions not granted")
            }
        }

        val enableBluetoothLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        Log.d("BLE","BLUETOOTH ENABLED")
                    }
            }

        bluetoothManager = BluetoothManager(this, permissionsLauncher, enableBluetoothLauncher)
        bluetoothManager.requestPermissions()


        setContent {
            AndroidAppTheme {
                // AddBluetoothDevice();
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AddBluetoothDevice(bluetoothManager)
                    }
                }

            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AddBluetoothDevice(bluetoothManager: BluetoothManager) {
    val bleDevices = remember { bluetoothManager.bleDevices }
    Button(onClick = { bluetoothManager.startBleScan() }) {
        Text(
            text = "Add Bluetooth Device"
        )
    }

    bleDevices.forEach { device ->
        Button(onClick = { bluetoothManager.connectToDevice(device) }) {
            Text(text = "${device.name} (${device.address})")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    lateinit var bluetoothManager: BluetoothManager
    AndroidAppTheme {
        AddBluetoothDevice(bluetoothManager)
    }
}