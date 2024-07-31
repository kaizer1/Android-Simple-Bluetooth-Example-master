package com.mcuhq.simplebluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

//
//fun requestPermissions(cont : Context, activity: AppCompatActivity)  {
//
//
//    var dsf : Context = cont
//
//    fun simpleCall() {
//
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            activity.requestMultiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                )
//            )
//        } else {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            requestEnableBluetooth.launch(enableBtIntent)
//        }
//
//    }
//
//
//
//     val requestEnableBluetooth =
//    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            // granted
//        } else {
//            // denied
//        }
//    }
//
// val requestMultiplePermissions =
//    activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//        permissions.entries.forEach {
//            Log.d("MyTag", "${it.key} = ${it.value}")
//        }
//    }
//
//}

