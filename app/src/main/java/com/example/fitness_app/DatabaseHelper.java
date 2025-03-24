package com.example.fitness_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.fitness_app.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "exercises.db";
    private static final int DATABASE_VERSION = 2;

    // Таблица упражнений
    private static final String TABLE_EXERCISES = "exercises";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_GIF_PATH = "gif_path";
    private static final String COLUMN_MIN_PULSE = "min_pulse";

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL-запрос для создания таблицы
        String CREATE_EXERCISES_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TYPE + " TEXT," // Исправлено: добавлен пробел перед TEXT
                + COLUMN_GIF_PATH + " TEXT,"
                + COLUMN_MIN_PULSE + " INTEGER" + ")";
        db.execSQL(CREATE_EXERCISES_TABLE);

        Log.d(TAG, "Table created: " + CREATE_EXERCISES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем старую таблицу и создаем новую, если версия базы данных изменилась
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        onCreate(db);
    }

    // Добавление нового упражнения
    public long addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, exercise.getName());
        values.put(COLUMN_TYPE, exercise.getType());
        values.put(COLUMN_GIF_PATH, exercise.getGifPath());
        values.put(COLUMN_MIN_PULSE, exercise.getMinPulse());

        // Вставляем строку
        long id = db.insert(TABLE_EXERCISES, null, values);
        db.close(); // Закрываем соединение с базой данных

        Log.d(TAG, "Added exercise with ID: " + id);
        return id;
    }

    // Получение одного упражнения по ID
    public Exercise getExercise(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES, new String[] { COLUMN_ID,
                        COLUMN_NAME, COLUMN_TYPE, COLUMN_GIF_PATH, COLUMN_MIN_PULSE }, COLUMN_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        else {
            db.close();
            return null;
        }

        Exercise exercise = new Exercise(cursor.getString(1), cursor.getString(2),cursor.getString(3), cursor.getInt(4));
        exercise.setId(Integer.parseInt(cursor.getString(0)));

        db.close();
        cursor.close();

        return exercise;
    }

    // Получение всех упражнений
    public List<Exercise> getAllExercises() {
        List<Exercise> exerciseList = new ArrayList<Exercise>();

        // SQL-запрос для получения всех строк из таблицы
        String selectQuery = "SELECT  * FROM " + TABLE_EXERCISES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Перебираем все строки и добавляем их в список
        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise();
                exercise.setId(Integer.parseInt(cursor.getString(0)));
                exercise.setName(cursor.getString(1));
                exercise.setType(cursor.getString(2));
                exercise.setGifPath(cursor.getString(3));
                exercise.setMinPulse(Integer.parseInt(cursor.getString(4)));
                // Добавляем упражнение в список
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        // возвращаем список упражнений
        return exerciseList;
    }

    // Обновление упражнения
    public int updateExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, exercise.getName());
        values.put(COLUMN_TYPE, exercise.getType());
        values.put(COLUMN_GIF_PATH, exercise.getGifPath());
        values.put(COLUMN_MIN_PULSE, exercise.getMinPulse());

        // Обновляем строку
        int rowsAffected = db.update(TABLE_EXERCISES, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(exercise.getId()) });

        db.close();
        return rowsAffected;
    }

    // Удаление упражнения
    public void deleteExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISES, COLUMN_ID + " = ?",
                new String[] { String.valueOf(exercise.getId()) });
        db.close();
    }

    // Получение количества упражнений
    public int getExercisesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_EXERCISES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        // возвращаем количество
        return count;
    }
}