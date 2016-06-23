package com.led.led.archive;


import com.led.led.GameScreen;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Maggi on 23.06.2016.
 */
public class HitListener implements Observer {

    @Override
    public void update(Observable o, Object msg) {
        GameScreen.setScore( o,(String) msg);
    }

}
