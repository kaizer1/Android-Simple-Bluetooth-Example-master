package com.mcuhq.simplebluetooth

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(private val mmSocket: BluetoothSocket, private val mHandler: Handler) :
    Thread() {
    private val mmInStream: InputStream?
    private val mmOutStream: OutputStream?

    init {
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null

        try {
            tmpIn = mmSocket.inputStream
            tmpOut = mmSocket.outputStream
        } catch (e: IOException) {
        }

        mmInStream = tmpIn
        mmOutStream = tmpOut
    }

    // @Override
    override fun run() {
        var buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {


                buffer = ByteArray(512)
                    //SystemClock.sleep(1)
                bytes = mmInStream!!.read(buffer)
                mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                    .sendToTarget()

            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("df", " fail to read asnwer " + e.localizedMessage)
                break
            }
        }
    }


    fun write(input: String) {
        val bytes = input.toByteArray()
        try {
            mmOutStream!!.write(bytes)
        } catch (e: IOException) {
            Log.d("df", " write ERROR! " + e.localizedMessage)
        }
    }

    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
        }
    }
}