package com.example.fitness_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitness_app.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseListActivity extends AppCompatActivity {

    private ListView listViewExercises;
    private DatabaseHelper dbHelper;
    private List<Exercise> exerciseList;
    private ArrayAdapter<String> adapter;

    private static final String TAG = "ExerciseListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        listViewExercises = findViewById(R.id.listViewExercises);
        dbHelper = new DatabaseHelper(this);
        exerciseList = new ArrayList<>();

        // Получаем все упражнения из базы данных
        loadExercises();

        // Создаем адаптер для ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getExerciseNames());
        listViewExercises.setAdapter(adapter);

        // Обработчик нажатия на элемент списка
        listViewExercises.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранное упражнение
                Exercise selectedExercise = exerciseList.get(position);

                // Создаем Intent для перехода на DoingExerciseActivity
                Intent intent = new Intent(ExerciseListActivity.this, DoingExercise.class);
                intent.putExtra("exerciseName", selectedExercise.getName()); // Передаем название упражнения

                // Запускаем Activity
                startActivity(intent);
            }
        });
    }

    // Загрузка упражнений из базы данных
    private void loadExercises() {
        exerciseList = dbHelper.getAllExercises();
        Log.d(TAG, "Loaded " + exerciseList.size() + " exercises from database.");
    }

    // Создание списка названий упражнений для адаптера
    private List<String> getExerciseNames() {
        List<String> names = new ArrayList<>();
        for (Exercise exercise : exerciseList) {
            names.add(exercise.getName());
        }
        return names;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Перезагружаем список упражнений при возвращении в Activity,
        // чтобы отобразить новые упражнения, добавленные в AddExerciseActivity
        loadExercises();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getExerciseNames());
        listViewExercises.setAdapter(adapter);
    }
}