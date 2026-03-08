package com.fussy.trueman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnParental = findViewById(R.id.btnParental);

        btnParental
                .setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ParentalControlActivity.class)));

    }
}
