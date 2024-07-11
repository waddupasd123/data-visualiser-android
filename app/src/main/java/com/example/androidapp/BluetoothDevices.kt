package com.example.androidapp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Manage main bluetooth screen

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDevices(bluetoothManager: BluetoothManager) {
    val knownDevices = bluetoothManager.knownDevices
    val connectedDevices = bluetoothManager.connectedDevices

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "BLE Devices:")
        knownDevices.forEach { device ->
            var showDialog by remember { mutableStateOf(false) }

            ConfirmationDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                onConfirm = {
                    bluetoothManager.deleteDevice(device)
                    showDialog = false
                            },
                dialogTitle = device.name,
                dialogText = "Remove device and all of its files?"
            )

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
                if (connectedDevices.contains(device)) {
                    Button(
                        onClick = { bluetoothManager.disconnectFromDevice(device) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "Disconnect")
                    }
                } else {
                    Button(
                        onClick = { bluetoothManager.connectToDevice(device) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text(text = "Connect")
                    }
                }
                IconButton(onClick =  {
                    showDialog = true
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
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
