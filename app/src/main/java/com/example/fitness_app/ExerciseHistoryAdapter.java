package com.example.fitness_app;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.fitness_app.models.Exercise;
import com.example.fitness_app.models.ExerciseRecord;

import java.util.List;

public class ExerciseHistoryAdapter extends ArrayAdapter<ExerciseRecord> {
    private Context context;
    private List<ExerciseRecord> exercises;

    public ExerciseHistoryAdapter(Context context, List<ExerciseRecord> exercises) {
        super(context, R.layout.item_exercise_history, exercises);
        this.context = context;
        this.exercises = exercises;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Получаем данные для текущей позиции
        ExerciseRecord exercise = getItem(position);

        // 2. Проверяем, существует ли view, если нет - inflate
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_exercise_history, parent, false);

            // 3. Находим все View элементы
            viewHolder.exerciseName = convertView.findViewById(R.id.textViewExerciseName);
            viewHolder.exerciseDetails = convertView.findViewById(R.id.textViewExerciseDetails);
            viewHolder.arrowIcon = convertView.findViewById(R.id.imageViewArrow);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 4. Заполняем данные
        if (exercise != null) {
            // Устанавливаем название упражнения
            viewHolder.exerciseName.setText(exercise.getExerciseName());

            // Формируем строку с деталями: дата и пульс
            String details = String.format("%s • Пульс: %s уд/мин",
                    exercise.getDateTime(),
                    exercise.getHeartRate());

            viewHolder.exerciseDetails.setText(details);
        }

        return convertView;
    }

    // Класс для хранения View элементов
    private static class ViewHolder {
        TextView exerciseName;
        TextView exerciseDetails;
        ImageView arrowIcon;
    }
}
