package com.example.fitness_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private BluetoothAdapter bluetoothAdapter;
    private TextView textViewSteps, textViewHeartRate, textViewCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewSteps = findViewById(R.id.textViewSteps);
        textViewHeartRate = findViewById(R.id.textViewHeartRate);
        textViewCalories = findViewById(R.id.textViewCalories);

        // Проверка и запрос разрешений
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS);
        } else {
            initBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                initBluetooth();
            } else {
                // Более информативное сообщение
                Toast.makeText(this, "Для работы с Bluetooth необходимо предоставить все разрешения.", Toast.LENGTH_LONG).show();
                //  Предложите пользователю перейти в настройки приложения
                //  Или отключите функциональность, требующую разрешений
            }
        }
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Устройство не поддерживает Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            searchForDevices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                searchForDevices();
            } else {
                Toast.makeText(this, "Bluetooth не включен", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchForDevices() {
        try {
            //  Для getBondedDevices() разрешение на местоположение не всегда нужно, но может потребоваться
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null && device.getName().equals("Fitbit")) {
                    // Подключение к устройству
                    Toast.makeText(this, "Найден Fitbit: " + device.getName(), Toast.LENGTH_SHORT).show();
                    // TODO:  Здесь ваш код для подключения к устройству Fitbit и получения данных
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Отсутствует разрешение на доступ к местоположению", Toast.LENGTH_LONG).show();
            // Предложите пользователю перейти в настройки
            // Или отключите функциональность Bluetooth
        }
    }
}