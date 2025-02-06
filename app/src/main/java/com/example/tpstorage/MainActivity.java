package com.example.tpstorage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText inputEditText;
    private TextView outputTextView;
    private SharedPreferences sharedPreferences;
    private static final String PREF_KEY = "my_text";
    private AppDatabase db;
    private Handler mainThreadHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputEditText = findViewById(R.id.inputEditText);
        outputTextView = findViewById(R.id.outputTextView);
        Button saveButton = findViewById(R.id.saveButton);
        Button loadButton = findViewById(R.id.loadButton);
        Button addUserButton = findViewById(R.id.addUserButton);
        Button getUsersButton = findViewById(R.id.getUsersButton);
        Button getUserByIdButton = findViewById(R.id.getUserByIdButton);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        db = AppDatabase.getDatabase(this);
        mainThreadHandler = new Handler(Looper.getMainLooper());


        saveButton.setOnClickListener(view -> saveData());
        loadButton.setOnClickListener(view -> loadData());


        addUserButton.setOnClickListener(view -> new Thread(this::addUser).start());
        getUsersButton.setOnClickListener(view -> new Thread(this::getAllUsers).start());
        getUserByIdButton.setOnClickListener(view -> new Thread(this::getUserById).start());
    }

    private void saveData() {
        String text = inputEditText.getText().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY, text);
        editor.apply();
    }

    private void loadData() {
        String text = sharedPreferences.getString(PREF_KEY, "");
        outputTextView.setText(text);
    }

    private void addUser() {
        String userName = inputEditText.getText().toString();
        User user = new User(userName, "email@email.com");
        db.userDao().insert(user); // Database operation


        mainThreadHandler.post(() -> {
            Toast.makeText(MainActivity.this, "Utilisateur ajouté (avec Thread)", Toast.LENGTH_SHORT).show();
        });
    }

    private void getAllUsers() {
        new Thread(() -> {
            List<User> users = db.userDao().getAllUsers();


            mainThreadHandler.post(() -> {
                UserAdapter adapter = new UserAdapter(users);
                RecyclerView recyclerView = findViewById(R.id.usersRecyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void getUserById() {

        new Thread(() -> {
            User user = db.userDao().getUserByIdSync(1);


            mainThreadHandler.post(() -> {
                if (user != null) {
                    outputTextView.setText("id: " + user.getId() + " name: " + user.getName());
                } else {
                    outputTextView.setText("Utilisateur non trouvé!");
                }
            });
        }).start();
    }
}
