package com.example.fitness_app.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.Set;

public class BluetoothUtils {
    @SuppressLint("MissingPermission")
    public static Set<BluetoothDevice> getPairedDevices(BluetoothAdapter bluetoothAdapter) {
        return bluetoothAdapter.getBondedDevices();
    }
}