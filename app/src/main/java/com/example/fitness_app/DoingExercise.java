package com.example.fitness_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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
import com.example.fitness_app.models.ExerciseRecord;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoingExercise extends AppCompatActivity {

    private final Logger LOGGER = Logger.getLogger(DoingExercise.class.getName());
    private MockFitbitDevice mockFitbitDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private BluetoothAdapter bluetoothAdapter;
    private ImageView imageViewDoExercise;
    private TextView textViewSteps, textViewHeartRate, textViewCalories, textViewMinPulse, textViewExerciseName, textViewTimer;
    private DatabaseHelper dbHelper;
    private Button buttonStartStop;
    private boolean isExercising = false; // Флаг для отслеживания состояния упражнения
    private CountDownTimer countDownTimer; // Таймер
    private long elapsedTimeInMillis = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doing_exercise);
        FirebaseApp.initializeApp(this);

        textViewHeartRate = findViewById(R.id.textViewHeartRate);
        textViewMinPulse = findViewById(R.id.textViewMinPulse);
        textViewExerciseName = findViewById(R.id.textViewExerciseName);
        imageViewDoExercise = findViewById(R.id.imageViewDoExercise);
        buttonStartStop = findViewById(R.id.buttonStartStop);
        textViewTimer = findViewById(R.id.timer);


        dbHelper = new DatabaseHelper(this);



        LOGGER.log(Level.INFO, "Устройство браслета создано");
        checkAndRequestPermissions();
        LOGGER.log(Level.INFO, "Разрешения проверены");
        String exerciseName = getIntent().getStringExtra("exerciseName");
        LOGGER.log(Level.INFO, "Передано название упражнения");

        // Поиск упражнения в базе данных по имени
        Exercise exercise = findExerciseByName(exerciseName);
        mockFitbitDevice = new MockFitbitDevice((heartRate) -> {

            updateHeartRate(heartRate, exercise);
        });
        buttonStartStop.setOnClickListener(v -> {
            if (!isExercising) {
                startExercise(exercise);
            } else {
                stopExercise();
                Log.d("Остановка упражнения", "упражнение удачно приостановлено");
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
        startTimer();
        mockFitbitDevice.start(); // Начинаем получать данные от устройства
    }

    private void stopExercise() {
        isExercising = false;
        buttonStartStop.setText("Начать");
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Останавливаем таймер
        }
        mockFitbitDevice.stop();

        saveExerciseData();

    }
    private void startTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) { // Используем Long.MAX_VALUE для бесконечного таймера
            @Override
            public void onTick(long millisUntilFinished) {
                elapsedTimeInMillis += 1000; // Увеличиваем прошедшее время на 1 секунду
                updateTimer(); // Обновляем таймер
            }

            @Override
            public void onFinish() {
                // Этот метод не будет вызван, так как мы используем Long.MAX_VALUE
            }
        }.start();
    }

    private void updateTimer() {
        int minutes = (int) (elapsedTimeInMillis / 1000) / 60;
        int seconds = (int) (elapsedTimeInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeLeftFormatted); // Обновляем текстовое поле
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

    private void updateHeartRate(int heartRate, Exercise exercise) {
        runOnUiThread(() -> {
            textViewHeartRate.setText("Пульс: " + heartRate + " уд/мин");
            if (heartRate < exercise.getMinPulse()) {
                Toast.makeText(DoingExercise.this, "Пульс ниже минимального! Вы не делаете упражнение.", Toast.LENGTH_SHORT).show();
            }
        });
    };

    private void saveExerciseData() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null){ Log.d("Ошибка", "Пользователь не найден"); return;};

        // Получаем данные
        String exerciseName = textViewExerciseName.getText().toString();
        String heartRate = textViewHeartRate.getText().toString().replace("Пульс: ", "");
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Log.d("Информация", "Данные для записи получены");

        // Создаем объект для сохранения
        ExerciseRecord record = new ExerciseRecord(
                exerciseName,
                heartRate,
                dateTime
        );

        // Сохраняем в Firebase
        DatabaseReference database = FirebaseDatabase.getInstance("https://fitness-app-190c1-default-rtdb.firebaseio.com/").getReference();
        database.child("users")
                .child(currentUser.getUid())
                .child("exerciseHistory")
                .push() // Генерируем уникальный ID для записи
                .setValue(record)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Данные сохранены"))
                .addOnFailureListener(e -> Log.e("Firebase", "Ошибка сохранения"));

        FirebaseDatabase.getInstance().getReference(".info/connected")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean connected = snapshot.getValue(Boolean.class);
                        Log.d("Firebase", "Connected: " + connected);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Connection error", error.toException());
                    }
                });
    }

}