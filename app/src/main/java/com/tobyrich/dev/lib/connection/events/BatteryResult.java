package com.tobyrich.dev.lib.connection.events;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by geno on 10/23/15.
 */
public class BatteryResult {
    private int result;

    public BatteryResult(int result) {
        this.result = result;
    }

    public int getResult(){
        return result;
    }
}
