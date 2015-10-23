package com.tobyrich.dev.lib.connection.events;

/**
 * Created by geno on 10/23/15.
 */
public class MotorEvent {
    private int value;

    public MotorEvent(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
