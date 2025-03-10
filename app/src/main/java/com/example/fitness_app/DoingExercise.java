package com.example.fitness_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fitness_app.models.Exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoingExercise extends AppCompatActivity {

    private final Logger LOGGER = Logger.getLogger(DoingExercise.class.getName());
    private MockFitbitDevice mockFitbitDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private BluetoothAdapter bluetoothAdapter;
    private TextView textViewSteps, textViewHeartRate, textViewCalories, textViewMinPulse, textViewExerciseName;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doing_exercise);

        textViewSteps = findViewById(R.id.textViewSteps);
        textViewHeartRate = findViewById(R.id.textViewHeartRate);
        textViewCalories = findViewById(R.id.textViewCalories);
        textViewMinPulse = findViewById(R.id.textViewMinPulse);
        textViewExerciseName = findViewById(R.id.textViewExerciseName);

        dbHelper = new DatabaseHelper(this);

        mockFitbitDevice = new MockFitbitDevice((steps, heartRate, calories) -> {
            updateSteps(steps);
            updateHeartRate(heartRate);
            updateCalories(calories);
        });
        LOGGER.log(Level.INFO, "Устройство браслета создано");
        checkAndRequestPermissions();
        LOGGER.log(Level.INFO, "Разрешения проверены");
        String exerciseName = getIntent().getStringExtra("exerciseName");
        LOGGER.log(Level.INFO, "Передано название упражнения");

        // Поиск упражнения в базе данных по имени
        Exercise exercise = findExerciseByName(exerciseName);

        if (exercise != null) {
            textViewExerciseName.setText(exercise.getName());
            LOGGER.log(Level.INFO, "установлено значение имени");
            textViewMinPulse.setText("Минимальный пульс: " + exercise.getMinPulse());
            LOGGER.log(Level.INFO, "установлено значение пульса");

        } else {
            textViewExerciseName.setText("Упражнение не найдено");
            LOGGER.log(Level.INFO, "Exercise with name '" + exerciseName + "' not found in database.");
        }
    }

    // Поиск упражнения в базе данных по имени
    private Exercise findExerciseByName(String name) {
        for (Exercise exercise : dbHelper.getAllExercises()) {
            if (exercise.getName().equals(name)) {
                LOGGER.log(Level.INFO, "найдено упражнение");
                return exercise;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Останавливаем имитацию устройства при закрытии приложения
        if (mockFitbitDevice != null) {
            mockFitbitDevice.stop();
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION // Добавьте это разрешение
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
                Toast.makeText(this, "Для работы с Bluetooth необходимо предоставить все разрешения.", Toast.LENGTH_LONG).show();
                // Предложите пользователю перейти в настройки
                openAppSettings();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            mockFitbitDevice.start();
            /*try {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName() != null && device.getName().equals("Fitbit")) {
                        Toast.makeText(this, "Найден Fitbit: " + device.getName(), Toast.LENGTH_SHORT).show();
                        mockFitbitDevice.start();
                    }
                }
            } catch (SecurityException e) {
                Toast.makeText(this, "Ошибка доступа к Bluetooth", Toast.LENGTH_LONG).show();
            }
            Раскомментрировать при реальном подключении к браслету*/
        } else {
            Toast.makeText(this, "Отсутствует разрешение на доступ к Bluetooth", Toast.LENGTH_LONG).show();
            checkAndRequestPermissions();
        }
    }


    private void updateSteps(int steps) {
        runOnUiThread(() -> textViewSteps.setText("Шаги: " + steps));
    }

    private void updateHeartRate(int heartRate) {
        runOnUiThread(() -> textViewHeartRate.setText("Пульс: " + heartRate + " уд/мин"));
    }

    private void updateCalories(int calories) {
        runOnUiThread(() -> textViewCalories.setText("Калории: " + calories + " ккал"));
    }
}