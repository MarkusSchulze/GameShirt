package com.led.led;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Maggi on 01.06.2016.
 * gets the BlueTooth device from the device list and connects to it. Can handle all data transfer via BT
 */
public class BTController extends ActionBarActivity {
    private ProgressDialog progress;
    BluetoothSocket mmSocket= null;
    BluetoothAdapter myBluetooth;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    String address = null;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    ArrayList<Object> values;
    Button btnOn, btnOff, btnDis;
    SeekBar brightness;
    TextView lumn, inputText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(PlayerSelection.EXTRA_ADDRESS); //receive the address of the bluetooth device
        //Versuche die richtige BT-Verbindung in der Arraylist von Playerselection zu finden
        for (int i=0;i<PlayerSelection.myBlueComms.size();i++){
            if (address.equalsIgnoreCase(PlayerSelection.myBlueComms.get(i).getAddress())){
                PlayerSelection.myBlueComms.get(i).beginListenForData();
                mmSocket = PlayerSelection.myBlueComms.get(i).getMmSocket();
                try {
                    mmOutputStream = mmSocket.getOutputStream();
                } catch (IOException e) {
                    msg(e.toString());
                    finish();
                }

            }
        }

        if (mmSocket==null){
            msg("Something went wrong");
            finish();
        }

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes
        btnOn = (Button) findViewById(R.id.button2);
        btnOff = (Button) findViewById(R.id.button3);
        btnDis = (Button) findViewById(R.id.button4);
        inputText = (TextView) findViewById(R.id.textView4);
        brightness = (SeekBar) findViewById(R.id.seekBar);
        lumn = (TextView) findViewById(R.id.lumn);


        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    mmSocket.close(); //close connection
                } catch (IOException e) {
                    msg("Error");
                }
                finish();
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lumn.setText(String.valueOf(progress));
                    try {
                        mmOutputStream.write(String.valueOf(progress).getBytes());
                    } catch (IOException e) {
                        msg(e.toString());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void turnOffLed() {
        try {
            mmOutputStream.write("TF".getBytes());
        } catch (IOException e) {
            msg(e.toString());
        }
    }

    private void turnOnLed() {
        try {
            mmOutputStream.write("TO".getBytes());
        } catch (IOException e) {
            msg(e.toString());
        }
    }


}
