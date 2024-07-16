package com.example.androidapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidapp.ui.theme.AndroidAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var dataManager: DataManager

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialise bluetooth manager
        val permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allPermissionsGranted = true
            permissions.entries.forEach {
                if (!it.value) allPermissionsGranted = false
            }
            if (allPermissionsGranted) {
                bluetoothManager.enableBluetooth()
            } else {
                Log.e("BluetoothManager", "Permissions not granted")
            }
        }

        val enableBluetoothLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
                    if (result.resultCode == RESULT_OK) {
                        Log.d("BluetoothManager","Intent enabled")
                    }
            }

        dataManager = DataManager(this)
        bluetoothManager = BluetoothManager(this, dataManager, permissionsLauncher, enableBluetoothLauncher)
        bluetoothManager.requestPermissions()
        bluetoothManager.loadKnownDevices()


        val context = this


        setContent {
            AndroidAppTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = "devices") {
                        composable("devices") {
                            BluetoothDevicesScreen(innerPadding, bluetoothManager, navController)
                        }
                        composable("viewData/{deviceAddress}") { backStackEntry ->
                            val deviceAddress = backStackEntry.arguments?.getString("deviceAddress")
                            if (deviceAddress != null) {
                                dataManager.loadDeviceFiles(deviceAddress)
                                ViewData(context, deviceAddress, bluetoothManager, dataManager, navController)
                            }
                        }
                    }
                }

            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun MainPreview() {
    AndroidAppTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "BLE Devices:")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ESP32"
                        )
                        Text(
                            text = "(F0:F5:BD:6D:59:A9)",
                            fontSize = 8.sp
                        )
                    }
                    Button(onClick = { }, modifier = Modifier.padding(0.dp) ) {
                        Row(
                            modifier = Modifier.padding(0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "View data")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Notifications Enabled",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "Disconnect")
                    }
                    IconButton(onClick =  {}) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoxPreview() {
    AndroidAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
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
        }
    }
}