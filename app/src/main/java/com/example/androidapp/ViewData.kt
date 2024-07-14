package com.example.androidapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewData(deviceAddress: String, bluetoothManager: BluetoothManager, navController: NavController) {
    val notificationsEnabled = bluetoothManager.notificationsManager[deviceAddress]
    // REMOVE LATER
    val allDeviceData = bluetoothManager.deviceDataMap

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = deviceAddress) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            bluetoothManager.toggleCharacteristicNotification(deviceAddress)
                        }
                    ) {
                        if (notificationsEnabled == true) {
                            Icon(Icons.Filled.Lock, contentDescription = "Pause Notifications")
                        } else {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Enable Notifications")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = allDeviceData[deviceAddress].toString(), fontSize = 20.sp)
        }
    }
}