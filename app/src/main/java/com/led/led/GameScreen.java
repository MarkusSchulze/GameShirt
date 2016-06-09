package com.led.led;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Maggi on 01.06.2016.
 * gets the BlueTooth connection from the PlayerSelection. Testclass to get the data transfer going.
 */
public class GameScreen extends ActionBarActivity {
    private boolean[] zones = {false, false, false, false};
    private OutputStream mmOutputStream;
    private BTConnection btConn;
    private TextView lumn, inputText1, inputText2;
    private final long startTime = System.currentTimeMillis();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        String address = newint.getStringExtra(PlayerSelection.EXTRA_ADDRESS);
        //Versuche die richtige BT-Verbindung in der Arraylist von Playerselection zu finden
        //DEBUG
        for (int i = 0; i < PlayerSelection.myBlueComms.size(); i++) {
            if (address.equalsIgnoreCase(PlayerSelection.myBlueComms.get(i).getAddress())) {
                btConn = PlayerSelection.myBlueComms.get(i);
                btConn.beginListenForData();
                btConn.getInputText();
                try {
                    mmOutputStream = btConn.getMmSocket().getOutputStream();
                } catch (IOException e) {
                    msg(e.toString());
                    finish();
                }

            }
        }

        if (btConn == null) {
            msg("Something went wrong");
            finish();
        }

        //view of the ledControl
        setContentView(R.layout.activity_game_screen);

        //call the widgtes
        Button btnOn = (Button) findViewById(R.id.button2);
        Button btnOff = (Button) findViewById(R.id.button3);
        Button btnDis = (Button) findViewById(R.id.button4);
        inputText1 = (TextView) findViewById(R.id.textView4);
        inputText2 = (TextView) findViewById(R.id.textView5);
        SeekBar brightness = (SeekBar) findViewById(R.id.seekBar);
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
//                try {
//                    btConn.closeBT(); //close connection
//                } catch (IOException e) {
//                    msg("Error");
//                }
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
            inputText2.setText(btConn.getInputText());
            mmOutputStream.write("zone1off".getBytes());
        } catch (IOException e) {
            msg(e.toString());
        }
    }

    private void turnOnLed() {
        try {
            mmOutputStream.write("zone1on".getBytes());
        } catch (IOException e) {
            msg(e.toString());
        }
        startTimer();
//        try {
//            mmOutputStream.write("TO".getBytes());
//            inputText2.setText(btConn.getInputText());
//        } catch (IOException e) {
//            msg(e.toString());
//        }
    }

    private void startTimer() {
        final Handler timerHandler = new Handler();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                seconds = seconds % 60;

                //Log.d("Timer", String.valueOf(seconds));

                if (millis % 3 == 0) {
                    selectZoneToHit();
                }
                detectHit();

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void selectZoneToHit() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(3) + 1;
        zones[randomInt] = true;
        String msg = "zone" + String.valueOf(randomInt) + "on";
        for (BTConnection bt : PlayerSelection.myBlueComms) {
            try {
                Log.d("send", msg);
                bt.getMmSocket().getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                msg(e.toString());
            }
        }
    }

    private void zoneOff(int zone) {
        String msg = "zone" + String.valueOf(zone) + "off";
        zones[zone] = false;
        for (BTConnection bt : PlayerSelection.myBlueComms) {
            try {
                Log.d("send", msg);
                bt.getMmSocket().getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                msg(e.toString());
            }
        }
        selectZoneToHit();
    }

    private void detectHit() {
        for (BTConnection bt : PlayerSelection.myBlueComms) {
            //TODO Highscore erhöhen, wenn man einen Treffer gelandet hat
            Log.d("HitDetection", bt.getInputText());
            switch (bt.getInputText()) {
                case "hit1":
                    if (zones[0]) {
                        Log.d("Treffer1", "true");
                        zoneOff(0);
                    }
                    break;
                case "hit2":
                    if (zones[1]) {
                        Log.d("Treffer2", "true");
                        zoneOff(0);
                    }
                    break;
                case "hit3":
                    if (zones[2]) {
                        Log.d("Treffer3", "true");
                        zoneOff(2);
                    }
                    break;
                case "hit4":
                    if (zones[3]) {
                        Log.d("Treffer4", "true");
                        zoneOff(3);
                    }
                    break;
            }
        }
    }
}
