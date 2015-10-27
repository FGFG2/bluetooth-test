package com.tobyrich.dev.lib.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import android.widget.Toast;
import java.util.UUID;
import de.greenrobot.event.EventBus;
import com.tobyrich.dev.lib.connection.events.*;

/**
 * Created by geno on 10/20/15.
 */
public class BluetoothService extends Service implements BluetoothAdapter.LeScanCallback  {
    private static final String TAG = "tr.lib.BluetoothService";
    private EventBus bus = EventBus.getDefault();

    // MAC: 5C:31:3E:4D:33:49
    //private static final String PLAN_PART = "TobyRich SmartPlane";
    private static final String PLAN_PART = "TobyRich";
    private static final UUID HANGARSERVICE = UUID.fromString("75B64E51-6000-4ED1-921A-476090D80BA7");
    private static final UUID GET_X = UUID.fromString("75B64E51-0010-4ED1-921A-476090D80BA7");
    private static final UUID GET_Y = UUID.fromString("75B64E51-0020-4ED1-921A-476090D80BA7");
    private static final UUID GET_Z = UUID.fromString("75B64E51-0021-4ED1-921A-476090D80BA7");

    private static final UUID SMARTPLANESERVICE = UUID.fromString("75B64E51-F171-4ED1-921A-476090D80BA7");
    //private static final UUID SMARTPLANESERVICE = UUID.fromString("75B64E51-E171-4ED1-921A-476090D80BA7");
    private static final UUID SMARTPLANE_MOTOR = UUID.fromString("75B64E51-0010-4ED1-921A-476090D80BA7");
    private static final UUID SMARTPLANE_RUDER = UUID.fromString("75B64E51-0021-4ED1-921A-476090D80BA7");

    private static final UUID BATTERYSERVICE = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    private static final UUID BATTERY_characteristic = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    private static final UUID BATTERY_descriptor = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");



    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private BluetoothGattCharacteristic battery,ruder,motor;


    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new ArrayList<BluetoothDevice>();
        bus.register(this);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }else if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "Create");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);

    }

    /*
     * Events to Handle
     */
    public void onEvent(RuderEvent evt){
        int value = evt.getValue();
        Log.d(TAG, "event-Ruder: " + value);
        if(value > 126)
            value = 126;
        else if(value < -126)
            value = -126;
        ruder.setValue(value, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
        mConnectedGatt.writeCharacteristic(ruder);
    }
    public void onEvent(MotorEvent evt){
        int value = evt.getValue();
        Log.d(TAG, "event-Motor: " + value);
        if (value > 254)
            value = 254;
        else if (value < 0)
            value = 0;
        motor.setValue(value,BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        mConnectedGatt.writeCharacteristic(motor);
    }

    public void onEvent(ConnectEvent evt){
        Log.d(TAG, "event-connecting: " + evt.getDevice().getAddress());
        mConnectedGatt = evt.getDevice().connectGatt(this, false, mGattCallback);
    }
    public void onEvent(ScanEvent evt){
        Log.d(TAG, "event-Scan: " + evt.getState());
        if(evt.getState())
            startScan();
        else
            stopScan();
    }
    private Runnable mEndRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"scan-end: ScanResult");
            bus.post(new ScanResult(mDevices));
            stopScan();
        }
    };


    private Handler mHandler = new Handler() {

    };
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(device.getName() != null && device.getName().contains("TobyRich")) {
            mDevices.add(device);
            Log.d(TAG, "scan-found-toby: " + device.getName() + " - " + device.getAddress() + " @ " + rssi);
        }else
            Log.d(TAG, "scan-found: " + device.getName() + " - " + device.getAddress() + " @ " + rssi);
    }

    private void startScan(){
        mBluetoothAdapter.startLeScan(this);
        mHandler.postDelayed(mEndRunnable, 10000);
    }

    private void stopScan(){
        mBluetoothAdapter.stopLeScan(this);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.v(TAG, "Services Discovered: " + status + ":" + mConnectedGatt.getServices());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for(BluetoothGattService service : mConnectedGatt.getServices()){
                    Log.v(TAG, "Services: " + service.getUuid() + ":");
                    for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                        UUID c = characteristic.getUuid();
                        Log.v(TAG, "Characteristic: " + c + ":");
                        if(c.equals(BATTERY_characteristic)) {
                            battery = characteristic;
                            //NEED to get CharacteristicRead Result
                            mConnectedGatt.setCharacteristicNotification(characteristic, true);
                            //ENABLE Autonotify
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BATTERY_descriptor);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mConnectedGatt.writeDescriptor(descriptor);
                            //GET FIRST-Value
                            mConnectedGatt.readCharacteristic(characteristic);
                            Log.d(TAG, "Characteristic-found: Battery - " + characteristic.getUuid());
                        }else if(c.equals(SMARTPLANE_MOTOR)) {
                            motor = characteristic;
                            //mConnectedGatt.readCharacteristic(characteristic);
                            Log.d(TAG, "Characteristic-found: Motor - "+characteristic.getUuid());
                        }else if(c.equals(SMARTPLANE_RUDER)) {
                            ruder = characteristic;
                            //mConnectedGatt.readCharacteristic(characteristic);
                            Log.d(TAG, "Characteristic-found: Ruder - " + characteristic.getUuid());
                        }else{
                            Log.v(TAG, "onCharacteristicRead: Unknown - "+ characteristic.getUuid());
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            sendResultEvent("onCharacteristicRead",gatt,characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            sendResultEvent("onCharacteristicWrite",gatt,characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            sendResultEvent("onCharacteristicChange",gatt,characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.v(TAG, "Descriptor: ");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.v(TAG, "Remote RSSI: " + rssi);
        }

        public void sendResultEvent(String s,BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            UUID c = characteristic.getUuid();
            int value;
            if(c.equals(SMARTPLANE_RUDER)) {
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                bus.post(new RuderResult(value));
                Log.d(TAG, s + ": Ruder - "+value);
            }else if(c.equals(SMARTPLANE_MOTOR)) {
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                bus.post(new MotorResult(value));
                Log.d(TAG, s + ": Motor - "+value);
            }else if(c.equals(BATTERY_characteristic)) {
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                bus.post(new BatteryResult(value));
                Log.d(TAG, s + ": Battery - "+value);
            }else{
                Log.v(TAG, s + ": Unknown - " + characteristic.getUuid());
            }
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Bind");
        return null;
    }
}
