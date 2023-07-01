package com.example.btlearn

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BluetoothReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> {
//                    Toast.makeText(context, "Bluetooth is off", Toast.LENGTH_SHORT).show()
                }

                BluetoothAdapter.STATE_ON -> {
//                    Toast.makeText(context, "Bluetooth is on", Toast.LENGTH_SHORT).show()
                }

                BluetoothAdapter.STATE_TURNING_ON -> {
//                    Toast.makeText(context, "Bluetooth is turning on", Toast.LENGTH_SHORT).show()
                }

                BluetoothAdapter.STATE_TURNING_OFF -> {
//                    Toast.makeText(context, "Bluetooth is turning off", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}