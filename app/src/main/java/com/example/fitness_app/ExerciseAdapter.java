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
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_exercise, parent, false);
            holder = new ViewHolder();
            holder.imageViewExercise = convertView.findViewById(R.id.imageViewExercise);
            holder.textViewExerciseName = convertView.findViewById(R.id.textViewExerciseName);
            holder.textViewExerciseDetails = convertView.findViewById(R.id.textViewExerciseDetails);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Получаем текущее упражнение
        Exercise exercise = exercises.get(position);

        // Устанавливаем текстовые значения
        holder.textViewExerciseName.setText(exercise.getName());
        holder.textViewExerciseDetails.setText(String.valueOf(exercise.getMinPulse()));

        // Устанавливаем фон в зависимости от типа упражнения
        switch (exercise.getType()) {
            case "Руки":
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_blue);
                break;
            case "Спина":
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_green);
                break;
            case "Грудь":
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_red);
                break;
            case "Ноги":
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_red);
                break;
            case "Ягодицы":
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_yellow);
                break;
            case "Плечи":
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_purple);
                break;
            default:
                holder.imageViewExercise.setBackgroundResource(R.drawable.circle_background_blue);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageViewExercise;
        TextView textViewExerciseName;
        TextView textViewExerciseDetails;
    }
}