package com.tobyrich.dev.lib.connection.events;

import android.bluetooth.BluetoothDevice;

/**
 * Created by geno on 10/23/15.
 */
public class ConnectEvent {
    private BluetoothDevice device;

    public ConnectEvent(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice(){
        return device;
    }
}
