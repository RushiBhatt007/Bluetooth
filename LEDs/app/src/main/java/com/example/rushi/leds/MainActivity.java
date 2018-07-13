package com.example.rushi.leds;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button btnPaired,btnBluetooth;
    private ListView deviceList;
    private TextView textView;

    private boolean bluetoothOnFlag = false;
    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_NAME = "device_name";
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        btnPaired = (Button)findViewById(R.id.show_paired_button);
        btnBluetooth = (Button)findViewById(R.id.bluetooth_on);
        deviceList = (ListView)findViewById(R.id.device_list_view);
        textView = (TextView)findViewById(R.id.textView);

        //if the device has bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnBluetooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bluetoothButtonClick();
            }
        });


        btnPaired.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(bluetoothOnFlag || bluetoothAdapter.isEnabled())
                    pairedDevicesList();
                else
                    msg("Please Turn Bluetooth On");
            }
        });

    }

    private void bluetoothButtonClick()
    {
        if(bluetoothAdapter == null)
        {
            //Show a message that the device has no bluetooth adapter
            msg("Bluetooth Device Not Available");
            //finish apk
            finish();
        }
        else if( !bluetoothAdapter.isEnabled() )
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
            bluetoothOnFlag = true;
        }
        else
        {
            msg("Bluetooth is already on!");
            bluetoothOnFlag = true;
        }
    }

    private void pairedDevicesList()
    {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList arrayList = new ArrayList();

        if (pairedDevices.size()>0)
        {
            textView.setText("Paired Devices:");
            for(BluetoothDevice bt : pairedDevices)
            {
                arrayList.add(bt.getName().trim() + "\n" + bt.getAddress().trim());//Get the device's name and the address
            }
        }
        else
        {
            msg("No Paired Bluetooth Devices Found.");
        }

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, arrayList);
        deviceList.setAdapter(arrayAdapter);
        deviceList.setOnItemClickListener(myListClickListener);//Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String name = info.substring(0,info.indexOf("\n"));
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent nextActivity = new Intent(MainActivity.this, ReceiveData.class);

            //Change the activity.
            nextActivity.putExtra(EXTRA_ADDRESS, address);
            nextActivity.putExtra(EXTRA_NAME, name);
            startActivity(nextActivity);
        }
    };

    // efficient way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

}


