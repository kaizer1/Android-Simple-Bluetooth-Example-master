package com.mcuhq.simplebluetooth;

import android.app.DialogFragment;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

   // @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

         Log.d("df", " in a RUN ! ");
        while (true) {
            Log.d("df", " try connect ! ");
            try {


                
                //bytes = mmInStream.available();
                //Log.d("df", " Bytes size " + bytes);
                //if(bytes != 0) {


                    Log.d("df", " byte is not null ");
                    buffer = new byte[1024];
                    SystemClock.sleep(100);

                //    bytes = mmInStream.available();

                   // bytes = mmInStream.read(buffer, 0, bytes);
                    bytes = mmInStream.read(buffer);
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                    Log.d("df", " ok read answer ! ");
                //}
            } catch (IOException e) {
                e.printStackTrace();
                   Log.d("df", " fail to read asnwer ");
                break;
              }
             }
        Log.d("df", " while end's ");
    }


    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.d("df", " write ERROR! ");
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}