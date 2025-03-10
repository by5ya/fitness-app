package com.example.fitness_app.models;

import androidx.annotation.NonNull;

public class Exercise {
    private int id; // Уникальный идентификатор упражнения
    private String name; // Название упражнения
    private String gifPath; // Путь к GIF-изображению (храним только путь, а не саму гифку)
    private int minPulse; // Минимальный пульс

    public Exercise() {
    }

    public Exercise(String name, String gifPath, int minPulse) {
        this.name = name;
        this.gifPath = gifPath;
        this.minPulse = minPulse;
    }

    // Геттеры и сеттеры для всех полей
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGifPath() {
        return gifPath;
    }

    public void setGifPath(String gifPath) {
        this.gifPath = gifPath;
    }

    public int getMinPulse() {
        return minPulse;
    }

    public void setMinPulse(int minPulse) {
        this.minPulse = minPulse;
    }

    @NonNull
    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gifPath='" + gifPath + '\'' +
                ", minPulse=" + minPulse +
                '}';
    }
}