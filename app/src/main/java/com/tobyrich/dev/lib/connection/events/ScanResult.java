package com.tobyrich.dev.lib.connection.events;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by geno on 10/23/15.
 */
public class ScanResult {
    private ArrayList<BluetoothDevice> result;

    public ScanResult(ArrayList<BluetoothDevice> result) {
        this.result = result;
    }

    public ArrayList<BluetoothDevice> getResult(){
        return result;
    }
}
