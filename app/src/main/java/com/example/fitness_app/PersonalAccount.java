package com.example.fitness_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitness_app.models.ExerciseRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PersonalAccount extends AppCompatActivity {

    private TextView userEmailTextView;
    private FirebaseAuth mAuth;
    private ListView listView;
    private ExerciseHistoryAdapter adapter;
    private List<ExerciseRecord> exerciseRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_account);

        listView = findViewById(R.id.listViewExercises);

        // Инициализация адаптера
        adapter = new ExerciseHistoryAdapter(this, exerciseRecords);
        listView.setAdapter(adapter);
        loadExerciseHistory();

        mAuth = FirebaseAuth.getInstance();
        userEmailTextView = findViewById(R.id.user_name); // Убедитесь, что в вашем layout есть TextView с этим ID

        // Получаем текущего пользователя
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Пользователь вошел в систему
            String userEmail = currentUser.getEmail();

            if (userEmail != null && !userEmail.isEmpty()) {
                // Устанавливаем email в TextView
                userEmailTextView.setText(userEmail);
            } else {
                userEmailTextView.setText("Email не указан");
            }
        } else {
            // Пользователь не вошел в систему
            userEmailTextView.setText("Не авторизован");
        }
    }

    private void loadExerciseHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("exerciseHistory");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                exerciseRecords.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ExerciseRecord record = dataSnapshot.getValue(ExerciseRecord.class);
                    exerciseRecords.add(record);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Ошибка загрузки", error.toException());
            }
        });
    }
}
