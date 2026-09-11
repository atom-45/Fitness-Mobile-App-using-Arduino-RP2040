package com.atom.bluetoothfitnessapplication.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atom.bluetoothfitnessapplication.utilities.Constants;
import com.atom.bluetoothfitnessapplication.utilities.WidgetHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BluetoothLeService extends Service {

    public static final String TAG = "BluetoothLeService";
    public final static String ACTION_GATT_CONNECTED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.atom.bluetoothfitnessapplication.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_SENSOR_DATA = "com.atom.bluetoothfitnessapplication.bluetooth.le.EXTRA_DATA";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;

    public boolean isConnected() {
        return connectionState == STATE_CONNECTED;
    }

    private final Binder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {

            if(newState == BluetoothGatt.STATE_CONNECTED)
            {
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                WidgetHelper.INSTANCE.saveBluetoothStatus(getApplicationContext(), true);
                bluetoothGatt.discoverServices();

            } else if(newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                WidgetHelper.INSTANCE.saveBluetoothStatus(getApplicationContext(), false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                BluetoothGattService service = bluetoothGatt
                        .getService(UUID.fromString(Constants.RP2040_SERVICE_UUID));

                BluetoothGattCharacteristic characteristic = service
                        .getCharacteristic(UUID
                        .fromString(Constants.RP2040_CHARACTERISTICS_UUID));


                readCharacteristic(characteristic);
                //writeCharacteristic(characteristic);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status)
        {
            setCharacteristicNotification(characteristic, true);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead: Able to read the data from the sensor");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            } else {
                Log.d(TAG, "onCharacteristicRead: Unable to read the data from the sensor");
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "onCharacteristicWrite: Data written to a bluetooth peripheral device");
            } else {
                Log.d(TAG, "onCharacteristicWrite: Data not written to the bluetooth device");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {

            enableConfiguration(gatt, descriptor.getCharacteristic());

        }
    };

    public boolean initialize()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter." );
            return false;
        }
        return true;
    }

    public boolean connect(final String address)
    {
        if(bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Device not found with provided address.  Unable to connect.", e);
            return false;
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return null;
        }
        return bluetoothGatt.getServices();
    }

    public void writeCharacteristicToBLEDevice(String data)
    {
        if(bluetoothGatt == null){
            Log.w(TAG, "BluetoothGatt not initialized");
        }

        BluetoothGattService service = bluetoothGatt
                .getService(UUID
                .fromString(Constants.RP2040_SERVICE_UUID));

        BluetoothGattCharacteristic characteristic = service
                .getCharacteristic(UUID
                .fromString(Constants.RP2040_CHARACTERISTICS_UUID));

        characteristic.setValue(data.getBytes());
        bluetoothGatt.writeCharacteristic(characteristic);

    }
    
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(bluetoothGatt == null)
        {
            Log.w(TAG, "readCharacteristic: Bluetooth not initialized.");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(bluetoothGatt == null) {
            Log.w(TAG, "writeCharacteristic: Bluetooth not initialized.");
            return;
        }
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);
        intent.setPackage(getPackageName());

        //Use an if statement to check the characteristic and get data bytes from it!

        if(UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID)
                .equals(characteristic.getUuid()))
        {
            byte[] value = characteristic.getValue();

            float[] arraySensorData = getArraySensorData(value);

            intent.putExtra(EXTRA_SENSOR_DATA, arraySensorData);

        }

        sendBroadcast(intent);
    }

    @NonNull
    private float[] getArraySensorData(byte[] bytes)
    {
        if(bytes == null || bytes.length < 12) {

            Log.e(TAG, "getArraySensorData: Received invalid data length: " +
                    (bytes != null ? bytes.length : 0));

            return new float[]{0f, 0f, 0f, 0f, 0f, 0f};
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        float ax = buffer.getShort() / 100.0f;
        float ay = buffer.getShort() / 100.0f;
        float az = buffer.getShort() / 100.0f;

        float gx = buffer.getShort() / 100.0f;
        float gy = buffer.getShort() / 100.0f;
        float gz = buffer.getShort() / 100.0f;

        Log.d(TAG, "Decoded: Ax: " + ax + " | Ay: " + ay + " | Az: " +
                az + " | Gx: " + gx + " | Gy: " + gy + " | Gz: " + gz);

        return new float[] {ax, ay, az, gx, gy, gz};

    }



    private void enableConfiguration(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic)
    {

    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled)
    {
        if(bluetoothGatt == null){
            Log.w(TAG, "setCharacteristicNotification: Bluetooth not initialized.");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if(UUID.fromString(Constants.RP2040_CHARACTERISTICS_UUID)
                .equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                    .fromString(Constants.DESCRIPTOR_GEN_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);

        }
    }


    private void close()
    {
        if(bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        close();
        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        Toast.makeText(this,
                "Battery Low, Recharge and feel fresh again",
                Toast.LENGTH_SHORT).show();
    }

    public class LocalBinder extends Binder
    {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
