package com.example.fitness_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitness_app.models.Exercise;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddExerciseActivity extends AppCompatActivity {

    private EditText editTextName, editTextMinPulse;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button buttonAddExercise, buttonChooseGif;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;

    private static final String TAG = "AddExerciseActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_exercise);
        String[] data = {"Руки", "Спина", "Грудь", "Ноги", "Ягодицы", "Плечи"};

        Spinner type_spinner = findViewById(R.id.type_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_spinner.setAdapter(adapter);

        editTextName = findViewById(R.id.editTextName);
        buttonChooseGif = findViewById(R.id.buttonChooseGif);
        editTextMinPulse = findViewById(R.id.editTextMinPulse);
        buttonAddExercise = findViewById(R.id.buttonAddExercise);

        dbHelper = new DatabaseHelper(this);
        buttonChooseGif.setOnClickListener(v -> openGallery());

        buttonAddExercise.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            String minPulseStr = editTextMinPulse.getText().toString();
            String type = type_spinner.getSelectedItem().toString();

            // Проверяем, что выбранный GIF не равен null
            if (selectedImageUri == null) {
                Toast.makeText(this, "Пожалуйста, выберите GIF", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем GIF в внутреннее хранилище и получаем путь
            String gifPath = saveGifToInternalStorage(selectedImageUri);
            if (gifPath == null) {
                Toast.makeText(this, "Ошибка при сохранении GIF", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty() || minPulseStr.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int minPulse = Integer.parseInt(minPulseStr);
                Exercise exercise = new Exercise(name, type, gifPath, minPulse);
                long id = dbHelper.addExercise(exercise);

                if (id > 0) {
                    Toast.makeText(this, "Упражнение добавлено!", Toast.LENGTH_SHORT).show();
                    editTextName.setText("");
                    editTextMinPulse.setText("");
                    Log.d(TAG, "Exercise added successfully with ID: " + id);
                } else {
                    Toast.makeText(this, "Ошибка при добавлении упражнения", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding exercise");
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректный формат пульса", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "NumberFormatException: " + e.getMessage());
            }
        });
    }

    private void openGallery() {
        // Открываем галерею для выбора GIF-файлов
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/gif"); // Устанавливаем MIME-тип для выбора только GIF
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Сохраняем GIF в внутреннее хранилище
                String gifPath = saveGifToInternalStorage(selectedImageUri);
            } else {
                Log.e(TAG, "Selected GIF URI is null");
            }
        } else {
            Log.e(TAG, "Failed to pick GIF: data is null or result is not OK");
        }
    }

    private String saveGifToInternalStorage(Uri gifUri) {
        try {
            // Открываем поток для чтения GIF
            InputStream inputStream = getContentResolver().openInputStream(gifUri);

            // Создаем файл в внутреннем хранилище
            File directory = getDir("exercise_gifs", MODE_PRIVATE);
            File gifFile = new File(directory, "exercise_" + System.currentTimeMillis() + ".gif");

            // Копируем данные из InputStream в FileOutputStream
            FileOutputStream outputStream = new FileOutputStream(gifFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            return gifFile.getAbsolutePath(); // Возвращаем путь к файлу
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void updateMediaDatabase(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                // Здесь вы можете обработать результат сканирования, если это необходимо
                Log.i("MediaScanner", "Scanned " + path + ":");
                Log.i("MediaScanner", "-> uri=" + uri);
            }
        });
    }
}