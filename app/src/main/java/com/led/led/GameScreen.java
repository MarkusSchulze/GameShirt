package com.led.led;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Maggi on 01.06.2016.
 * gets the BlueTooth connection from the PlayerSelection. Testclass to get the data transfer going.
 */
public class GameScreen extends ActionBarActivity {
    private OutputStream mmOutputStream;
    private BTConnection btConn;
    private TextView lumn, inputText1, inputText2;
    private final long startTime = System.currentTimeMillis();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        String address = newint.getStringExtra(PlayerSelection.EXTRA_ADDRESS);
        //Versuche die richtige BT-Verbindung in der Arraylist von Playerselection zu finden
        for (int i=0;i<PlayerSelection.myBlueComms.size();i++){
            if (address.equalsIgnoreCase(PlayerSelection.myBlueComms.get(i).getAddress())){
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

        if (btConn==null){
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

        startTimer();
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void turnOffLed() {
        try {
            mmOutputStream.write("TF".getBytes());
            for (int i = 0;i < PlayerSelection.myBlueComms.size(); i++){
                if (i==0){
                    inputText1.setText(PlayerSelection.myBlueComms.get(i).getInputText());
                }else{
                    inputText2.setText(PlayerSelection.myBlueComms.get(i).getInputText());
                }
            }

        } catch (IOException e) {
            msg(e.toString());
        }
    }

    private void turnOnLed() {
        try {
            mmOutputStream.write("TO".getBytes());
            inputText2.setText(btConn.getInputText());
        } catch (IOException e) {
            msg(e.toString());
        }
    }

    private void startTimer(){
        final Handler timerHandler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                //timerTextView.setText(String.format("%d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        };
    }

    private void selectFieldToHit(){        //TODO

    }

    private int detectHit(){            //TODO
        return 0;
    }
}
