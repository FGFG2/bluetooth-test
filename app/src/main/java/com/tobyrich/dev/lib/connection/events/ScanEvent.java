package com.tobyrich.dev.lib.connection.events;

/**
 * Created by geno on 10/23/15.
 */
public class ScanEvent {
    private boolean state;

    public ScanEvent(boolean state) {
        this.state = state;
    }

    public boolean getState(){
        return state;
    }
}
