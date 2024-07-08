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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        BluetoothDevices(bluetoothManager)
                        AddBluetoothDevice(bluetoothManager)
                    }
                }

            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDevices(bluetoothManager: BluetoothManager) {
    // Use connected for now
    val connectedDevices = bluetoothManager.connectedDevices

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Connected Devices:")
        connectedDevices.forEach { device ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column() {
                    Text(
                        text = device.name,
                    )
                    Text(
                        text = device.address,
                        fontSize = 8.sp
                    )
                }
                Button(onClick = {
                    // TO DO
                }) {
                    Text(text = "View data")
                }
                Button(
                    onClick = { bluetoothManager.disconnectFromDevice(device) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(text = "Disconnect")
                }
            }

        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AddBluetoothDevice(bluetoothManager: BluetoothManager) {
    val bleDevices = remember { bluetoothManager.bleDevices }
    val isScanning = remember { bluetoothManager.isScanning }
    Button(onClick = { bluetoothManager.startBleScan() }) {
        Text(
            text = if (isScanning.value) "Stop scanning" else "Add Bluetooth device"
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
    AndroidAppTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Connected Devices:")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column() {
                        Text(
                            text = "ESP32"
                        )
                        Text(
                            text = "(F0:F5:BD:6D:59:A9)",
                            fontSize = 8.sp
                        )
                    }
                    Button(onClick = {
                        // TO DO
                    }) {
                        Text(text = "View data")
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "Disconnect")
                    }
                }

        }
    }
}