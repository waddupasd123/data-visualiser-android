package com.example.androidapp

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphData(
    context: Context,
    deviceAddress: String,
    fileName: String,
    dataManager: DataManager,
    navController: NavController
) {
    val scrollState = rememberVicoScrollState(autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased)
    val zoomState = rememberVicoZoomState()
    val cartesianChartModelProducer = remember { CartesianChartModelProducer() }

    val xDataPoints = mutableListOf<Long>()
    val yDataPoints = mutableListOf<Float>()
    context.contentResolver.openInputStream(dataManager.getFileUri(fileName))?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readLine() // Skip headers
            var line: String? = reader.readLine()
            while (line != null) {
                val parts = line.split(",")
                if (parts.size == 2) {
                    val time = parts[0].toLong()
                    val value = parts[1].toFloat()
                    xDataPoints.add(time)
                    yDataPoints.add(value)
                }
                line = reader.readLine()
            }
        }
    }

    LaunchedEffect(Unit) {
        cartesianChartModelProducer.runTransaction {
            lineSeries {
                series(x = xDataPoints, y = yDataPoints)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$deviceAddress - $fileName") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(modifier = Modifier
                .fillMaxWidth()
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = rememberStartAxis(
                            title = "Value",
                        ),
                        bottomAxis = rememberBottomAxis(
                            title = "Time",
                        )
                    ),
                    modelProducer = cartesianChartModelProducer,
                    scrollState = scrollState,
                    zoomState = zoomState,
                )
            }


        }
    }
}