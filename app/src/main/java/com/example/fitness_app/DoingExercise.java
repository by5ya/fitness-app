package com.example.fitness_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

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
    private ImageView imageViewDoExercise;
    private TextView textViewSteps, textViewHeartRate, textViewCalories, textViewMinPulse, textViewExerciseName;
    private DatabaseHelper dbHelper;
    private Button buttonStartStop;
    private boolean isExercising = false; // Флаг для отслеживания состояния упражнения
    private CountDownTimer countDownTimer; // Таймер
    private long timeLeftInMillis = 600000; // Время в миллисекундах (например, 10 минут)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doing_exercise);

        textViewSteps = findViewById(R.id.textViewSteps);
        textViewHeartRate = findViewById(R.id.textViewHeartRate);
        textViewCalories = findViewById(R.id.textViewCalories);
        textViewMinPulse = findViewById(R.id.textViewMinPulse);
        textViewExerciseName = findViewById(R.id.textViewExerciseName);
        imageViewDoExercise = findViewById(R.id.imageViewDoExercise);
        buttonStartStop = findViewById(R.id.buttonStartStop);


        dbHelper = new DatabaseHelper(this);



        LOGGER.log(Level.INFO, "Устройство браслета создано");
        checkAndRequestPermissions();
        LOGGER.log(Level.INFO, "Разрешения проверены");
        String exerciseName = getIntent().getStringExtra("exerciseName");
        LOGGER.log(Level.INFO, "Передано название упражнения");

        // Поиск упражнения в базе данных по имени
        Exercise exercise = findExerciseByName(exerciseName);
        mockFitbitDevice = new MockFitbitDevice((steps, heartRate, calories) -> {
            updateSteps(steps);
            updateHeartRate(heartRate, exercise);
            updateCalories(calories);
        });
        buttonStartStop.setOnClickListener(v -> {
            if (!isExercising) {
                startExercise(exercise);
            } else {
                stopExercise();
            }
        });

        if (exercise != null) {
            textViewExerciseName.setText(exercise.getName());
            LOGGER.log(Level.INFO, "установлено значение имени");
            textViewMinPulse.setText("Минимальный пульс: " + exercise.getMinPulse());
            LOGGER.log(Level.INFO, "установлено значение пульса");
            String filePath = exercise.getGifPath();
            MediaScannerConnection.scanFile(this, new String[]{filePath}, null, null);
            Glide.with(this)
                    .asGif() // Указываем, что это GIF
                    .load(filePath) // Путь к GIF
                    .into(imageViewDoExercise);
            LOGGER.log(Level.INFO, "установлено gif");

        } else {
            textViewExerciseName.setText("Упражнение не найдено");
            LOGGER.log(Level.INFO, "Exercise with name '" + exerciseName + "' not found in database.");
        }

    }
    private void startExercise(Exercise exercise) {
        isExercising = true;
        buttonStartStop.setText("Завершить");
        startTimer(exercise);
        mockFitbitDevice.start(); // Начинаем получать данные от устройства
    }

    private void stopExercise() {
        isExercising = false;
        buttonStartStop.setText("Начать");
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Останавливаем таймер
        }
        mockFitbitDevice.stop(); // Останавливаем получение данных от устройства
    }
    private void startTimer(Exercise exercise) {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                // Обновите UI, если нужно
                // Например, обновите текстовое поле с оставшимся временем
            }

            @Override
            public void onFinish() {
                stopExercise(); // Останавливаем упражнение, когда таймер истекает
                Toast.makeText(DoingExercise.this, "Время вышло!", Toast.LENGTH_SHORT).show();
            }
        }.start();

        // Проверка пульса
        mockFitbitDevice.setHeartRateListener((heartRate) -> {
            LOGGER.log(Level.INFO, "Текущий пульс: " + heartRate);
            if (heartRate < exercise.getMinPulse()) {
                runOnUiThread(() -> {
                    Toast.makeText(DoingExercise.this, "Пульс ниже минимального! Вы не делаете упражнение.", Toast.LENGTH_SHORT).show();
                });
            }
        });
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

    private void updateHeartRate(int heartRate, Exercise exercise) {
        runOnUiThread(() -> {
            textViewHeartRate.setText("Пульс: " + heartRate + " уд/мин");
            if (heartRate < exercise.getMinPulse()) {
                Toast.makeText(DoingExercise.this, "Пульс ниже минимального! Вы не делаете упражнение.", Toast.LENGTH_SHORT).show();
            }
        });
    };


    private void updateCalories(int calories) {
        runOnUiThread(() -> textViewCalories.setText("Калории: " + calories + " ккал"));
    }
}