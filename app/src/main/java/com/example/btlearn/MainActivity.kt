package com.example.btlearn

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.btlearn.databinding.ActivityMainBinding
import java.security.Permission

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var receiver: BluetoothReceiver
    private lateinit var discoverability: Discoverability

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager == null) {
            return
        }
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            return
        }
        enableDisableBT()
        bluetoothAdapter = bluetoothManager.adapter
        requestPermissionsIfRequired()
        enableDisableBTDiscoverability()
        receiver = BluetoothReceiver()
        discoverability = Discoverability()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun enableDisableBTDiscoverability() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE)
                    == PackageManager.PERMISSION_GRANTED -> {
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_ADVERTISE) -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                    101
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(this, "Bluetooth is not on", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun enableDisableBT() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED -> {
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_CONNECT) -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.BLUETOOTH),
                    101
                )
            }
        }
        binding.buttonOnBluetooth.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH)
            } else {
                Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
            }
            if (bluetoothAdapter.isEnabled) {
                val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                registerReceiver(receiver, intentFilter)
//                Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonDiscover.setOnClickListener {
            val discoverIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            startActivity(discoverIntent)
            discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            registerReceiver(discoverability, intentFilter)
        }
        binding.buttonPair.setOnClickListener {
            getPairDevice()
        }

        binding.buttonCheck.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when (ContextCompat.checkSelfPermission(
                    baseContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
                )) {
                    PackageManager.PERMISSION_DENIED-> {
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Нужно знать твой местоположение")
                            .setMessage("Как же тебя бить не зная IP?")
                            .setNeutralButton("Ok",DialogInterface.OnClickListener { _, _ ->
                                if(ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED){
                                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 101)
                                }
                            })
                            .show()
                            .findViewById<TextView>(R.id.tvMessage)?.movementMethod = android.text.method.LinkMovementMethod.getInstance()
                    }
                    PackageManager.PERMISSION_GRANTED ->{
                        Toast.makeText(this, "Location is available", Toast.LENGTH_SHORT).show()
                    }
                }
                discoverDevices()
            }
        }
//        binding.buttonCheck.setOnClickListener {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                when (ContextCompat.checkSelfPermission(
//                    baseContext, android.Manifest.permission.BLUETOOTH
//                )) {
//                    PackageManager.PERMISSION_DENIED-> {
//                        androidx.appcompat.app.AlertDialog.Builder(this)
//                            .setTitle("Включи Bluetooth")
//                            .setMessage("смотри заголовок?")
//                            .setNeutralButton("Ok",DialogInterface.OnClickListener { _, _ ->
//                                if(ContextCompat.checkSelfPermission(baseContext, android.Manifest.permission.BLUETOOTH)
//                                    != PackageManager.PERMISSION_GRANTED){
//                                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH), 101)
//                                }
//                            })
//                            .show()
//                            .findViewById<TextView>(R.id.tvMessage)?.movementMethod = android.text.method.LinkMovementMethod.getInstance()
//                    }
//                    PackageManager.PERMISSION_GRANTED ->{
//
//                    }
//                }
//            }
//        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverDevices() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoverDeviceReceiver, filter)
        bluetoothAdapter.startDiscovery()
    }
    private val discoverDeviceReceiver = object :BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent?) {
            var action = ""
            if(intent != null){
                 action = intent.action.toString()
            }
            when(action){
                BluetoothAdapter.ACTION_STATE_CHANGED ->{
                    Toast.makeText(context, "Bluetooth is ${intent?.getIntExtra(BluetoothAdapter.ACTION_STATE_CHANGED, 0)}", Toast.LENGTH_SHORT).show()
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED ->{
                    Toast.makeText(context, "Bluetooth is ${intent?.getIntExtra(BluetoothAdapter.ACTION_DISCOVERY_STARTED, 0)}", Toast.LENGTH_SHORT).show()

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->{
                    Toast.makeText(context, "Bluetooth is ${intent?.getIntExtra(BluetoothAdapter.ACTION_DISCOVERY_FINISHED, 0)}", Toast.LENGTH_SHORT).show()

                }
                BluetoothDevice.EXTRA_DEVICE ->{
                    val device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        Toast.makeText(context, "Permission granted device ${device.name}, ${device.address}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    /*Свойство bondState объекта BluetoothDevice представляет текущее состояние связывания (пары)
    Bluetooth-устройства с устройством, на котором выполняется ваше приложение.
    Свойство bondState имеет одно из следующих значений, определенных в классе BluetoothDevice
    BOND_NONE (0): Устройство не связано с текущим устройством.
    BOND_BONDING (1): Устройство находится в процессе связывания.
    BOND_BONDED (2): Устройство успешно связано с текущим устройством.
    */
    @SuppressLint("MissingPermission")
    private fun getPairDevice() {
        var arrDevices = bluetoothAdapter.bondedDevices
        Toast.makeText(this, "Вокруг меня ${arrDevices.size}", Toast.LENGTH_SHORT).show()

        if (arrDevices.isNotEmpty()) {
            for (device in arrDevices) {
                binding.tvAroud.text = "Вокруг меня такие то устройства: ${device.name}"
                binding.tvAroudName.text = "Вот столько ${arrDevices.size} народу"
                binding.tvAdress.text = "OS девайсов: ${device.uuids}"
                binding.tvUUID.text = "UUID девайсов: ${device.address}"
                binding.tvBoun.text = "Состояние связи: ${device.bondState == BluetoothDevice.BOND_BONDED}"
            }
        } else {
            binding.tvAroud.text = ""
            binding.tvAroudName.text = "Вокруг нет устройств"
            binding.tvAdress.text = ""
            binding.tvUUID.text = ""
            binding.tvBoun.text = ""
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissionsIfRequired() {
        val missingPermissions = mutableListOf<String>()
        val storagePermissions = getStoragePermissions()
        val locationPermissions = getLocationPermissions()

        for (permission in storagePermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(permission)
            }
        }
        for (permission in locationPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(permission)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_PERMISSIONS
            )
        } else {
            enableDisableBT()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterReceiver(discoverability)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.S)
        private val PERMISSIONS_STORAGE = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED
        )

        @RequiresApi(Build.VERSION_CODES.S)
        private val PERMISSIONS_LOCATION = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED
        )

        @RequiresApi(Build.VERSION_CODES.S)
        fun getStoragePermissions(): Array<String> {
            return PERMISSIONS_STORAGE
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun getLocationPermissions(): Array<String> {
            return PERMISSIONS_LOCATION
        }

        const val REQUEST_PERMISSIONS = 101
        const val REQUEST_ENABLE_BLUETOOTH = 102
    }
}


