package com.led.led;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class PlayerSelection extends Activity {
    public static String EXTRA_ADDRESS = "device_address";
    //widgets
    Button btnPaired;
    ListView deviceList;
    //Bluetooth
    public static ArrayList<BTConnection> myBlueComms;
    private BluetoothAdapter myBluetooth = null;
    //Loading bar for connecting
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //Calling widgets
        btnPaired = (Button) findViewById(R.id.button);
        deviceList = (ListView) findViewById(R.id.listView);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
        pairedDevicesList();
    }

    public void launchRingDialog(View view, final BluetoothDevice bt) {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(PlayerSelection.this, "Connecting...", "Please wait!!!", true);
        ringProgressDialog.setCancelable(true);
        boolean ConnectSuccess = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    BTConnection conn = new BTConnection();
                    if (conn.connect(bt)) {
                        myBlueComms.add(conn);
                        //ConnectSuccess = true;
                    } else {
                        //ConnectSuccess = false;
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    msg(e.toString());
                    //ConnectSuccess = false;
                }
                ringProgressDialog.dismiss();
            }
        }).start();
        //return ConnectSuccess;
    }

    private class ConnectBT extends AsyncTask<BluetoothDevice, Void, Boolean> {  // UI thread
        boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(PlayerSelection.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Boolean doInBackground(BluetoothDevice... bt) {
            BTConnection conn = new BTConnection();
            if (conn.connect(bt[0])) {
                myBlueComms.add(conn);
                ConnectSuccess = true;
            } else {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
            } else {
                msg("Connected.");

            }
            progress.dismiss();
        }
    }

    private void pairedDevicesList() {
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList values = new ArrayList();
        ArrayList<ArrayList<Object>> list = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                values.add(bt.getName());
                values.add(bt);
                list.add(values);
            }
        } else {
            msg("No Paired Bluetooth Devices Found.");
        }

        ListViewAdapter adapter = new ListViewAdapter(this, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> rowList, View v, int position, long arg3) {
            //av.getItemAtPosition()
            // final String item = (String) parent.getItemAtPosition(position);
            ArrayList<Object> values = (ArrayList<Object>) rowList.getItemAtPosition(position);
            //String name = (String) values.get(0);
            BluetoothDevice bt = (BluetoothDevice) values.get(1);
            String address = bt.getAddress();

            try {
                BTConnection conn = new BTConnection();
                if (conn.connect(bt)) {
                    myBlueComms.add(conn);
                }
            } catch (Exception e) {
                msg(e.toString());
            }
            //new ConnectBT().execute(bt); //Call the class to connect

            //TODO Liste neu generieren und Einträge einfärben/disablen, dass man erkennen kann welche schon connected sind.

            // Get the device MAC address, the last 17 chars in the View
            //String info = ((TextView) v).getText().toString();
            //String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(PlayerSelection.this, BTController.class);

            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            if (PlayerSelection.myBlueComms != null){
                startActivity(i);
            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // fast way to call Toast
    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}
