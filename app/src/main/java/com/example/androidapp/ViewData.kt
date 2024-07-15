package com.example.androidapp

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewData(
    context: Context,
    deviceAddress: String,
    bluetoothManager: BluetoothManager,
    dataManager: DataManager,
    navController: NavController
) {
    val notificationsEnabled = bluetoothManager.notificationsManager[deviceAddress]
    // REMOVE LATER
    val allDeviceData = bluetoothManager.deviceDataMap

    var selectedDirectoryUri by remember { mutableStateOf<Uri?>(dataManager.getDirectoryUri(deviceAddress)) }
    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { parentUri ->
        if (parentUri != null) {
            selectedDirectoryUri = dataManager.createDeviceFolder(deviceAddress, parentUri)
            Log.d("ViewDataScreen", "Directory selected: $parentUri")
            Toast.makeText(context, "Directory selected: $parentUri", Toast.LENGTH_LONG).show()

        } else {
            Log.e("ViewDataScreen", "Failed to select directory")
            Toast.makeText(context, "Failed to select directory", Toast.LENGTH_LONG).show()
        }
    }

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
                    Text(text = "Directory")
                    IconButton(
                        onClick = {
                            openDocumentTreeLauncher.launch(selectedDirectoryUri)
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Select directory")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (selectedDirectoryUri == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select directory first",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 25.sp,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { dataManager.createCsvFile(deviceAddress) }
                ) {
                    Text(text = "Create New CSV File")
                }

//            dataManager.csvFilesList.forEach { csvFile ->
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                ) {
//                    Text(text = csvFile.name, modifier = Modifier.weight(1f))
//                    IconButton(
//                        onClick = {
//
//                        }
//                    ) {
//                        if (notificationsEnabled == true) {
//                            Icon(Icons.Filled.Lock, contentDescription = "Pause Notifications")
//                        } else {
//                            Icon(
//                                Icons.Filled.PlayArrow,
//                                contentDescription = "Enable Notifications"
//                            )
//                        }
//                    }
//                }
//            }

                Text(text = allDeviceData[deviceAddress].toString(), fontSize = 20.sp)
            }
        }
    }
}