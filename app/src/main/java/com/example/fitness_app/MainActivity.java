package com.example.fitness_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;




public class MainActivity extends AppCompatActivity {
    private Button btnAddExercise, btnStartTrain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddExercise = findViewById(R.id.add_exercise);
        btnStartTrain = findViewById(R.id.start_train);
        btnAddExercise.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExerciseActivity.class);
            startActivity(intent);
        });

        btnStartTrain.setOnClickListener(v ->{
            Intent intent= new Intent(MainActivity.this, ExerciseListActivity.class);
            startActivity(intent);
        });
    }


}
