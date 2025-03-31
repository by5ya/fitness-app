package com.example.fitness_app.models;

public class ExerciseRecord {
    private String exerciseName;
    private String heartRate;
    private String dateTime;

    // Конструктор, геттеры и сеттеры
    public ExerciseRecord() {} // Пустой конструктор нужен для Firebase

    public ExerciseRecord(String exerciseName, String heartRate, String dateTime){
        this.exerciseName = exerciseName;
        this.heartRate = heartRate;
        this.dateTime = dateTime;
    }

    public String getExerciseName() { return exerciseName; }
    public String getHeartRate() { return heartRate; }
    public String getDateTime() { return dateTime; }

}
