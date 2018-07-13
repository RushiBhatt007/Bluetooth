package com.example.rushi.leds;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class ReceiveData extends AppCompatActivity {

    private Button btnOn, btnDis, btnClr;
    private TextView textView,displayView;
    private EditText sendData;
    private ProgressDialog progress;
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String MACaddress = null;
    private String deviceName = null;

    private InputStream inputStream;
    private byte buffer[];
    private boolean stopThread;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        MACaddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);//receive the MAC address of the bluetooth device
        deviceName = intent.getStringExtra(MainActivity.EXTRA_NAME);//receive the name of the bluetooth device

        setContentView(R.layout.activity_receive_data);

        btnOn = (Button)findViewById(R.id.button2);
        btnDis = (Button)findViewById(R.id.button4);
        btnClr = (Button)findViewById(R.id.clearData);
        textView = (TextView)findViewById(R.id.textView2);
        displayView = (TextView)findViewById(R.id.displayView);
        sendData = (EditText)findViewById(R.id.dataSend);

        new ConnectBT().execute();//Call the class to connect

        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendData();
            }
        });

        btnClr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                displayView.setText("");
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect();//close connection
            }
        });

        while(btSocket != null)
            receiveData();
    }

    private void Disconnect()
    {
        if (btSocket!=null)//If the btSocket is busy
        {
            try
            {
                btSocket.close();//close connection
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
        finish();//return to the first layout

    }

    private void receiveData()
    {
        if (btSocket!=null)
        {
                try
                {
                    sendData();
                    while(inputStream == null) {
                        inputStream = btSocket.getInputStream();
                    }
                    beginListenForData();
                }
                catch (IOException e)
                {
                    msg("error in showing");
                }
        }
    }

    void beginListenForData()
    {
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                byte[] rawBytes = new byte[1024];
                int byteCount;
                while (!stopThread)
                {
                    try
                    {
                        SystemClock.sleep(200);
                        byteCount = inputStream.read(rawBytes);
                        final String string = new String(rawBytes,0,byteCount);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                displayView.append(string);
                            }
                        });

                        stopThread = true;
                        inputStream = null;
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });
        thread.start();
    }

    private void sendData()
    {
        String send = sendData.toString();
        if (btSocket!=null)
        {
            try
            {
                if(send=="")
                    msg("No data found");
                else
                    for(int i = 0 ; i < send.length() ; i++ )
                        btSocket.getOutputStream().write(send.charAt(i));
                
                displayView.append(send.trim() + "\n" );
            }
            catch (IOException e)
            {
                msg("Error in receiving data");
            }
        }
    }

    // efficient way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>// UI thread
    {
        private boolean ConnectSuccess = true;//if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ReceiveData.this, "Connecting with "+deviceName, "Please wait!!");//show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices)//while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                 BluetoothDevice bluetoothDevice = myBluetooth.getRemoteDevice(MACaddress);//connects to the device's address and checks if it's available
                 btSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)//after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is "+deviceName +" a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                textView.setText("Connected with "+deviceName);
                msg("Successfully Connected with "+deviceName);
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
