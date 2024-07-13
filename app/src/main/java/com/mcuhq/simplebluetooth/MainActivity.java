package com.mcuhq.simplebluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID BT_MODULE_UUID = UUID.fromString("9c57b849-30c1-4282-80d1-0915b3109ccd");
    private final static int REQUEST_ENABLE_BT = 1;
    public final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;

    private Button sendButton;
    private Button closeButton;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    private TextView textFouyn;
    private boolean connection = false;

    private boolean foundRaspberryOk = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
        JWT jwt = new JWT(token);

        String issuer = jwt.getIssuer(); //get registered claims
        String claim = jwt.getClaim("isAdmin").asString(); //get custom claims
        boolean isExpired = jwt.isExpired(10); // Do time validation with 10 seconds leeway

             if(ContextCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION }, 3);


        sendButton = (Button) findViewById(R.id.send_idd);
        textFouyn = (TextView) findViewById(R.id.text_fouynd);
        assert textFouyn != null;
        textFouyn.setText(R.string.forun_ste);
        closeButton = (Button) findViewById(R.id.closeid);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectedThread.cancel();
                textFouyn.setText(R.string.forun_ste);
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                       JSONObject df = new JSONObject();
                        try {
                            df.put("header","connection?");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        //String dsf = ""
                        //
                        mConnectedThread.write(df.toString());
                        Log.d(" df", " in ok write data");
                     //   mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, device.getName())
                     //           .sendToTarget();




            }
        });


        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    Log.d("df", " mess Read ! LOS");
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                   Log.d("df", " mes = " + readMessage);
                }

                if(msg.what == CONNECTING_STATUS){

                     Log.d("d", "Connection status LOS ");
                }

            }
        };


       mPairedDevices = mBTAdapter.getBondedDevices();
       if(mBTAdapter.isEnabled()) {
           for (BluetoothDevice device : mPairedDevices) {
               mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

               if (device.getName().equals("raspberrypi")) {

                    foundRaspberryOk = true;

                    TryConnection(device);

                    sendButton.setVisibility(View.VISIBLE);
                    closeButton.setVisibility(View.VISIBLE);

               }
           }

       }



        if(!foundRaspberryOk){
            discover();
        }

    }


    private void seeButtonSend( Boolean rt){
        if (rt)
        sendButton.setVisibility(View.VISIBLE);
        else
        sendButton.setVisibility(View.GONE);
    }

    private void TryConnection(BluetoothDevice device){


            new Thread() {
                       @Override
                       public void run() {
                           boolean fail = false;

                           BluetoothDevice deviceLocal = mBTAdapter.getRemoteDevice(device.getAddress());

                           try {
                               mBTSocket = createBluetoothSocket(deviceLocal);
                               Log.d("df", " ok create Socket blue");
                           } catch (IOException e) {
                               fail = true;
                              // Toast.makeText(getBaseContext(), getString(R.string.ErrSockCrea), Toast.LENGTH_SHORT).show();
                           }

                           Log.d("df", " connection blue 222 ");
                           try {

                               mBTSocket.connect();
                               textFouyn.setText(R.string.ok_ok);

                               Log.d("df", " connect soccker 22 ! ");

                           } catch (IOException e) {
                               Log.d(" df", " microCather1 " + e.getLocalizedMessage());
                               try {
                                   fail = true;
                                   mBTSocket.close();
                                   Log.d("df", " Ok CLose 1! ");
                                   mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                           .sendToTarget();


                                   currentThread().sleep(2000);
                                   TryConnection(device);


                               } catch (IOException | InterruptedException e2) {

                                   // TryConnection
                              }
                           }
                           if (!fail) {
                               mConnectedThread = new ConnectedThread(mBTSocket, mHandler);

                               Log.d("df", " connection thread sdf23");

                                  JSONObject df = new JSONObject();
                               try {
                                   df.put("header", "connection?");
                               } catch (JSONException e) {
                                   throw new RuntimeException(e);
                               }
                               //String dsf = ""
                               //
                               mConnectedThread.write(df.toString());


                               mConnectedThread.start();


                               Log.d(" df", " start in Connectrion  ");
                               mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, device.getName())
                                       .sendToTarget();
                           }
                       }
                   }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){

        if (requestCode == REQUEST_ENABLE_BT) {
        }

        //  Log.d("df"," activity Result !!! !!! ");
        discover();
    }

    private void discover(){


        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
        }
        else{


            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear();
                mBTAdapter.startDiscovery();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
            }
        }
    }



    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice deviceOld = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(Objects.equals(deviceOld.getName(), "raspberrypi")){
                    Log.d("df"," ok found raspberry ! ");
                    TryConnection(deviceOld);
                 }
            }
        }
    };



    @Override
public void onRequestPermissionsResult(
        int requestCode,
        String permissions[],
        int[] grantResults) {
         discover();

}


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        //return device.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID); // error !
         return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
}
