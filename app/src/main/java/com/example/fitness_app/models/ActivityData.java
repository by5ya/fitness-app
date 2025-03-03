package com.example.fitness_app.models;

public class ActivityData {
        private int steps;
        private int heartRate;
        private int calories;

        public int getSteps() {
                return steps;
        }

        public void setSteps(int steps) {
                this.steps = steps;
        }

        public int getHeartRate() {
                return heartRate;
        }

        public void setHeartRate(int heartRate) {
                this.heartRate = heartRate;
        }

        public int getCalories() {
                return calories;
        }

        public void setCalories(int calories) {
                this.calories = calories;
        }
}