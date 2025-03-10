package com.example.fitness_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitness_app.models.Exercise;

public class AddExerciseActivity extends AppCompatActivity {

    private EditText editTextName, editTextGifPath, editTextMinPulse;
    private Button buttonAddExercise;
    private DatabaseHelper dbHelper;

    private static final String TAG = "AddExerciseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_exercise);

        editTextName = findViewById(R.id.editTextName);
        editTextGifPath = findViewById(R.id.editTextGifPath);
        editTextMinPulse = findViewById(R.id.editTextMinPulse);
        buttonAddExercise = findViewById(R.id.buttonAddExercise);

        dbHelper = new DatabaseHelper(this);

        buttonAddExercise.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            String gifPath = editTextGifPath.getText().toString();
            String minPulseStr = editTextMinPulse.getText().toString();

            if (name.isEmpty() || gifPath.isEmpty() || minPulseStr.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int minPulse = Integer.parseInt(minPulseStr);
                Exercise exercise = new Exercise(name, gifPath, minPulse);
                long id = dbHelper.addExercise(exercise);

                if (id > 0) {
                    Toast.makeText(this, "Упражнение добавлено!", Toast.LENGTH_SHORT).show();
                    editTextName.setText("");
                    editTextGifPath.setText("");
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
}