package com.example.fitness_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fitness_app.models.Exercise;

import java.util.List;

public class ExerciseAdapter extends ArrayAdapter<Exercise> {

    private Context context;
    private List<Exercise> exercises;

    public ExerciseAdapter(Context context, List<Exercise> exercises) {
        super(context, R.layout.item_exercise, exercises);
        this.context = context;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_exercise, parent, false);

        // Получаем текущее упражнение
        Exercise exercise = exercises.get(position);

        // Находим элементы в макете
        ImageView imageViewExercise = rowView.findViewById(R.id.imageViewExercise);
        TextView textViewExerciseName = rowView.findViewById(R.id.textViewExerciseName);
        TextView textViewExerciseDetails = rowView.findViewById(R.id.textViewExerciseDetails);

        // Устанавливаем данные
        imageViewExercise.setImageResource(exercise.getImageResource()); // Установите изображение
        textViewExerciseName.setText(exercise.getName());
        textViewExerciseDetails.setText(exercise.getDetails());

        return rowView;
    }
}