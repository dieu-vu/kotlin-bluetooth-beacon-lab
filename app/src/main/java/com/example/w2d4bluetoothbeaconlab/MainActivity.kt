package com.example.w2d4bluetoothbeaconlab

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.w2d4bluetoothbeaconlab.ui.theme.W2D4BluetoothBeaconLabTheme
import kotlinx.coroutines.Dispatchers
import java.util.*

class MainActivity : ComponentActivity() {
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var takePermissions: ActivityResultLauncher<Array<String>>
    lateinit var takeResultLauncher: ActivityResultLauncher<Intent>
    lateinit var bluetoothViewModel: BTViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothViewModel = BTViewModel()
        takePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            {
                it.entries.forEach{
                    Log.d("DBG list permission", "${it.key} = ${it.value}")

                    if (it.value == false) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        takeResultLauncher.launch(enableBtIntent)
                    }
                }
                if (it[Manifest.permission.BLUETOOTH_ADMIN] == true
                    && it[Manifest.permission.ACCESS_FINE_LOCATION] == true){

                    bluetoothAdapter.bluetoothLeScanner.let { scan ->
                        bluetoothViewModel.scanDevices(
                            scan,
                            this
                        )
                    }
                } else {
                    Toast.makeText(applicationContext, "Not all permissions are granted", Toast.LENGTH_SHORT).show()
                }
           }

        takeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback{
            result -> if (result.resultCode == RESULT_OK){
                Log.d("DBG result callback ok", " ${result.resultCode}")
            } else {
                Log.d("DBG result callback NOT OK", " ${result.resultCode}")
            }
        })



        setContent {

            W2D4BluetoothBeaconLabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StartScan()
                        ShowDevices(bluetoothViewModel)
                    }

                }
            }
        }
    }
    @Composable
    fun StartScan() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text("Bluetooth beacon lab", fontSize = 30.sp)
            OutlinedButton(onClick = {
                takePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
            }) {
                Text("Start scan")
            }
            OutlinedButton(onClick = {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter.disable()
                    Toast.makeText(applicationContext, "bluetooth off", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Bluetooth off")
            }
        }
    }

    @Composable
    fun ShowDevices(model: BTViewModel) {
        val context = LocalContext.current
        val value: List<ScanResult>? by model.scanResults.observeAsState(null)
        val fScanning: Boolean by model.fScanning.observeAsState(false)
        Column (modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (fScanning) "Scanning" else "Not scanning")
            Text(if (value?.size ==0) "no devices" else if (value == null) "" else "found ${value?.size}")
            value?.forEach {
                Text("Device: ${it.device}")
            }
        }
    }


}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    W2D4BluetoothBeaconLabTheme {
    }
}