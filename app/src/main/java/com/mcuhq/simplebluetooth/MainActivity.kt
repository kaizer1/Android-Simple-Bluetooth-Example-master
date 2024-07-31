package com.mcuhq.simplebluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private var handlerFind: Handler? = null

    private var scanButton: Button? = null
    private var exitButton: Button? = null
    private var imageviewScan: ImageView? = null
    private lateinit var dataImage: ByteArray

    private lateinit var mBTAdapter: BluetoothAdapter
    private var mPairedDevices: Set<BluetoothDevice>? = null
    private var mBTArrayAdapter: ArrayAdapter<String>? = null
    private var mHandler: Handler? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var textFouyn: TextView
    private val connection = false
    private val byBuf: ByteBuffer? = null
    private var mediaPlayer: MediaPlayer? = null


    private var foundRaspberryOk = false
    private var okConnect = false


    @RequiresApi(api = Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


              hideSystemUI()

        // Запрос разрешений
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES }

        //}


        // BLUETOOTH_CONNECT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                3
            )
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ),
                    3
                )
            }
        }


        textFouyn =  findViewById<TextView>(R.id.text_fouynd)
        scanButton = findViewById<View>(R.id.scanid) as Button
        exitButton = findViewById<View>(R.id.exit_app) as Button
        imageviewScan = findViewById<View>(R.id.image_scan_res) as ImageView

        mediaPlayer = MediaPlayer.create(this, R.raw.alertone)
        println(" prepare do !")
       // mediaPlayer!!.prepare()
 println(" prepare do 22!")
        checkNotNull(textFouyn)
        textFouyn.setText(R.string.forun_ste)


        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
        mBTAdapter.setName("rasp_9c57b849-30c1-4282-80d1-0915b3109ccd")
        mBTArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        handlerFind = Handler(Looper.getMainLooper())
        okConnect = false

        exitButton!!.setOnClickListener {

            finish();
            exitProcess(0);

        }


            // Посылаем команду сканировать на аппарат
        scanButton!!.setOnClickListener {
            if (!scanning_current) {

                if(okConnect) {


                    textFouyn.setText(R.string.please_wait)

                    val df = JSONObject()
                    try {

                        df.put("header", "scan")
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }

                    mConnectedThread!!.write(df.toString())
                    imageviewScan!!.setImageDrawable(null)
                }
            }
        }


            // Работа с полученными данными от аппарата
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_READ) {
                    // {"header": "connected?", "status": "connected"}
                    // {"header": "scanning"}
                    // {"header": "loading", "chunks": 1076, "leftD": {"width": 155, "length": 155}, "rightD": {"width": 155, "length": 155}}

                    if (scanning_current) {
                        Log.d("df", " my static == " + iStatic)

                        if ((iStatic + 1) == numberChunks) {
                            Log.d("df", "End load images")
                            numberChunks = 0
                            iStatic = 0
                            iStaticByte = 0
                            //textFouyn.setText(R.string.ok_success_load_iad)


                            try {
                                // Bitmap df = BitmapFactory.decodeByteArray(byBuf.array(), 0, byBuf.limit());

                                val bmp =
                                    BitmapFactory.decodeByteArray(dataImage, 0, dataImage.size)
                                imageviewScan!!.setImageBitmap(bmp)
                            } catch (e: Exception) {
                                val bmp =
                                    BitmapFactory.decodeByteArray(dataImage, 0, dataImage.size)
                                imageviewScan!!.setImageBitmap(bmp)

                                Log.d("df", "errorLoad image: " + e.localizedMessage)
                            }

                            scanning_current = false


                            //    java.util.Arrays.fill(byBuf.array(), (byte)0);
                            //    byBuf.clear();
                        } else {
                            val readBuf = msg.obj as ByteArray

                             // 511 ??
                            for (i in 0..511) {
                                dataImage[iStaticByte] = readBuf[i]
                                iStaticByte++
                            }

                            //byBuf.put(readBuf);
                           // textFouyn.text =
                             //   "Получение фото:" + " " + iStatic + " получено  из " + numberChunks

                            // Log.d("df", " i = " + iStatic)
                            ++iStatic
                        }
                    } else {
                        val readMessage = String((msg.obj as ByteArray), StandardCharsets.UTF_8)
                        try {
                            val jObject = JSONObject(readMessage)

                            if (jObject.getString("header") == "loading") {
                                numberChunks = jObject.getInt("chunks")
                                Log.d("df", " my Chunck = " + numberChunks)
                                scanning_current = true

                                dataImage = ByteArray(numberChunks * 512 + 1)

                                //byBuf = ByteBuffer.allocate(numberChunks * 512 + 1);
                                textFouyn.setText(R.string.getting_iage)

                            }

                            if(jObject.getString("header") == "status"){
                                if (jObject.getString("status") == "photos_done"){

                                     try{
                                   mediaPlayer!!.start()
                                       // mediaPlayer!!.prepare()
                                }catch (e : Exception){
                                    println(" no playing sounD ! ${e.localizedMessage}")
                                }
                                 SeeToast()

                                }
                            }

                            if (jObject.getString("header") == "connected?") {
                                if (jObject.getString("status") == "connected") {

                                    textFouyn.setText(R.string.ok_ok)
                                    okConnect = true;
                                    callOk()
                                }
                            }
                        } catch (e: JSONException) {
                            throw RuntimeException(e)
                        }
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    Log.d("d", "Connection status LOS ")
                }
            }
        }


        // Проверяем, что есть девайс с нужным именем в pairing device
        mPairedDevices = mBTAdapter.getBondedDevices()
        if (mBTAdapter.isEnabled()) {
            for (device in mPairedDevices!!) {
                mBTArrayAdapter!!.add(device.name + "\n" + device.address)

                if (device.name == "raspberrypi") {
                    foundRaspberryOk = true

                    TryConnection(device)
                }
            }
        }


        if (!foundRaspberryOk) {
            discover()
        }
    }


   override fun onResume() {
        super.onResume()
        hideSystemUI()
    }


    // Первое подключение к аппарату, после того, как нашли устройство
    private fun TryConnection(device: BluetoothDevice?) {
        object : Thread() {
            override fun run() {
                var fail = false

                val deviceLocal = mBTAdapter!!.getRemoteDevice(device!!.address)

                try {
                    mBTSocket = createBluetoothSocket(deviceLocal)
                    Log.d("df", " ok create Socket blue")
                } catch (e: IOException) {
                    fail = true
                    // Toast.makeText(getBaseContext(), getString(R.string.ErrSockCrea), Toast.LENGTH_SHORT).show();
                }

                Log.d("df", " connection blue 222 ")
                try {
                    mBTSocket!!.connect()

                    //textFouyn.setText(R.string.ok_ok);
                    Log.d("df", " connect soccker 22 ! ")
                } catch (e: IOException) {
                    Log.d(" df", " microCather1 " + e.localizedMessage)
                    try {
                        fail = true
                        mBTSocket!!.close()
                        Log.d("df", " Ok CLose 1! ")
                        mHandler!!.obtainMessage(CONNECTING_STATUS, -1, -1)
                            .sendToTarget()

                        sleep(1000)
                        TryConnection(device)
                    } catch (e2: IOException) {
                        // TryConnection
                    } catch (e2: InterruptedException) {
                    }
                }
                if (!fail) {
                    mConnectedThread = ConnectedThread(mBTSocket!!, mHandler!!)

                    Log.d("df", " connection thread sdf23")

                    val df = JSONObject()
                    try {
                        df.put("header", "connected?") // was connected?
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }

                    mConnectedThread!!.write(df.toString())
                    mConnectedThread!!.start()

                    Log.d(" df", " start in Connectrion  ")
                    mHandler!!.obtainMessage(CONNECTING_STATUS, 1, -1, device.name)
                        .sendToTarget()
                }
            }
        }.start()
    }


    // Result после событий в activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, Data: Intent?) {
        super.onActivityResult(requestCode, resultCode, Data)
        if (requestCode == REQUEST_ENABLE_BT) {
        }

        discover()
    }


    // Начала поиска девайса
    private fun discover() {
        Log.d("df", " start discover ")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

             if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

                 println("no blueSCANEGRANTED ! ")
                 ActivityCompat.requestPermissions(
                     this,
                     arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 10
                 )

                 return
             }
        }

        if (mBTAdapter!!.isDiscovering) {
            Log.d("df", " in a stop discoverrr ")
        } else {
            if (mBTAdapter!!.isEnabled) {
                Log.d("df", " discoverrr ! ")
                mBTArrayAdapter!!.clear()
                mBTAdapter!!.startDiscovery()
                registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

                handlerFind!!.postDelayed(object : Runnable {
                    override fun run() {
                        // displayData();
                        checkOutFindDevices()
                        if (!foundRaspberryOk) handlerFind!!.postDelayed(this, 5000)
                    }
                }, 5000)


                //                int requestCode = 1;
//Intent discoverableIntent =
//       new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//startActivityForResult(discoverableIntent, requestCode);
            }
        }
    }



    // Проверка что девайс найден и можно его больше не искать
    private fun checkOutFindDevices() {
        Log.d("df", " check our Discover ")
        if (mBTAdapter!!.isDiscovering) {
            Log.d("df", " discovering finding ! ")
            // mBTAdapter.cancelDiscovery();
        } else {
            Log.d("df", " new Start ! ")
            mBTAdapter!!.startDiscovery()
        }
    }


    // Broadcast receiver на поиск девайса и попытки к нему подключиться
    val blReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d("df", " in action found ! ")
            if (BluetoothDevice.ACTION_FOUND == action) {
                val deviceOld =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                deviceOld!!.describeContents()

                //Log.d("df", " my device name = " + deviceOld.name)

                if (deviceOld.name == "raspberrypi") {
                    //Log.d("df", " ok found raspberry ! ")
                    mBTAdapter!!.cancelDiscovery()
                    foundRaspberryOk = true
                    TryConnection(deviceOld)
                }
            }
        }
    }



     // небольшие подсказки по связи приложения с аппаратом
    private fun callOk() {
        Toast.makeText(this, R.string.ok_device_conn, Toast.LENGTH_SHORT).show()
    }



    // Корректно закрываем программу
    public override fun onDestroy() {
        mConnectedThread!!.cancel()
        mediaPlayer!!.release()
        unregisterReceiver(blReceiver)
        super.onDestroy()
    }


    // Куда приходим после действий на наши permission
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

          println(" request result permi... ")
            // Manifest.permission.BLUETOOTH_CONNECT
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            println(" sdlkfjskdjf nNOOO ")
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 10)

        }else {
             println(" ok Connect BLuetooth sdf ")
        }

        discover()
    }



    // Скрываем все бары на  экране, чтобы не было ничего лишнего
       private fun hideSystemUI() {
         WindowCompat.setDecorFitsSystemWindows(window, false)
         WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    // Выводим большое сообщение что с устройства можно сойти
    private fun SeeToast(){
        //Toast.makeText(this, R.string.scan_final_go_home, Toast.LENGTH_LONG).show()

         val dialog = CustomDialogGOSCAN()
        dialog.show(getSupportFragmentManager(), "goscan")

    }



    // Создаём соединение с нашим аппаратом ( пробуем его создать )
    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        //return device.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID); // error !

        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
    }


     // переменные
    companion object {
        private val BT_MODULE_UUID: UUID = UUID.fromString("9c57b849-30c1-4282-80d1-0915b3109ccd")
        private const val REQUEST_ENABLE_BT = 1
        const val MESSAGE_READ: Int = 2
        private const val CONNECTING_STATUS = 3
        private var scanning_current = false
        private var numberChunks = 0
        private var iStatic = 0
        private var iStaticByte = 0
    }
}
