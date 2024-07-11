package com.example.androidapp

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
        bluetoothManager.loadKnownDevices()


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
                    IconButton(onClick =  {}) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }

        }
    }
}