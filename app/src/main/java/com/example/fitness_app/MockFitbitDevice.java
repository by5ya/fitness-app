package com.example.fitness_app;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MockFitbitDevice {

    private final Logger LOGGER = Logger.getLogger(MockFitbitDevice.class.getName());

    public interface FitbitDataListener {
        void onDataReceived(int steps, int heartRate, int calories);
    }

    private final FitbitDataListener listener;
    private boolean isRunning = false;

    public MockFitbitDevice(FitbitDataListener listener) {
        this.listener = listener;
    }

    public void start() {
        isRunning = true;
        new Thread(() -> {
            while (isRunning) {
                try {
                    // Генерация случайных данных
                    LOGGER.log(Level.INFO, "Данные генерируются");
                    int steps = (int) (Math.random() * 1000);
                    int heartRate = (int) (Math.random() * 21) + 130;
                    int calories = (int) (Math.random() * 500);

                    // Передача данных через callback
                    if (listener != null) {
                        listener.onDataReceived(steps, heartRate, calories);
                    }

                    // Задержка для имитации реального времени
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LOGGER.log(Level.INFO, "Данные не сгенерированы");
                }
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
    }
}