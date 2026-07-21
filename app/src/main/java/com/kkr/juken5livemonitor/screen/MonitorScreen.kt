package com.kkr.juken5livemonitor.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.viewmodel.MonitorViewModel

@Composable
fun MonitorScreen(
    navController: NavHostController,
    vm: MonitorViewModel = viewModel()
) {

    val liveData by vm.liveData.collectAsState()
    val status by vm.status.collectAsState()

    LaunchedEffect(Unit) {
        vm.startMonitor()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "Juken 5 Live Monitor",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = status,
                    fontWeight = FontWeight.Bold
                )

            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        DataRow("RPM", "${liveData.rpm}")
        DataRow("TPS", "${liveData.tps} %")
        DataRow("Battery", "${"%.2f".format(liveData.battery)} V")
        DataRow("AFR", "${"%.1f".format(liveData.afr)}")
        DataRow("EOT", "${"%.1f".format(liveData.eot)} °C")
        DataRow("IAT", "${"%.1f".format(liveData.iat)} °C")
        DataRow("MAP", "${liveData.map} %")
        DataRow("Ignition", "${"%.1f".format(liveData.ignition)}°")
        DataRow("Injector", liveData.injector)
        DataRow("Base Map", liveData.baseMap)
        DataRow("Fuel", liveData.fuel)

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                vm.disconnect()

                navController.popBackStack()

            }
        ) {

            Text("DISCONNECT")

        }

    }

}

@Composable
private fun DataRow(

    title: String,

    value: String

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            horizontalArrangement = Arrangement.SpaceBetween

        ) {

            Text(

                text = title,

                fontWeight = FontWeight.SemiBold

            )

            Text(

                text = value

            )

        }

    }

}