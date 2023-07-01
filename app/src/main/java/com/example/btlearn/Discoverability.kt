package com.example.btlearn

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class Discoverability: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if(action!=null){
            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
//                        Toast.makeText(context, "Bluetooth is connecting", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
//                        Toast.makeText(context, "Bluetooth is discovering", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothAdapter.SCAN_MODE_NONE -> {
//                        Toast.makeText(context, "Bluetooth is off", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}