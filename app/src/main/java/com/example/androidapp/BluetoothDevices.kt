package com.example.androidapp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.navigation.NavController

// Manage main bluetooth screen
@Composable
fun BluetoothDevicesScreen(innerPadding: PaddingValues, bluetoothManager: BluetoothManager, dataManager: DataManager, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BluetoothDevices(bluetoothManager, dataManager, navController)
        AddBluetoothDevice(bluetoothManager)
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun BluetoothDevices(bluetoothManager: BluetoothManager, dataManager: DataManager, navController: NavController) {
    val knownDevices = bluetoothManager.knownDevices
    val connectedDevices = bluetoothManager.connectedDevices
    val notificationsEnabled = bluetoothManager.notificationsManager

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
                Column {
                    Text(
                        text = device.name,
                    )
                    Text(
                        text = device.address,
                        fontSize = 8.sp
                    )
                }
                Button(onClick = { navController.navigate("viewData/${device.address}") }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "View data",  fontSize = 12.sp)
                        if (notificationsEnabled[device.address] == true && dataManager.selectedFiles[device.address] != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Notifications Enabled",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (connectedDevices.contains(device)) {
                    Button(
                        onClick = { bluetoothManager.disconnectFromDevice(device) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "Disconnect", fontSize = 12.sp)
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
private fun AddBluetoothDevice(bluetoothManager: BluetoothManager) {
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


