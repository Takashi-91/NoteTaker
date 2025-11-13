package com.example.notetaker;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText emailField;
    private Button sendOtpBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        emailField = findViewById(R.id.emailField);
        sendOtpBtn = findViewById(R.id.sendOtpBtn);

        sendOtpBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            String otp = OtpGenerator.generateOtp();
            long expiryTime = System.currentTimeMillis() + (2 * 60 * 1000); // 2 mins

            // Send OTP via email in background
            new Thread(() -> EmailSender.sendEmail(email, otp)).start();

            // Save OTP to Firestore
            Map<String, Object> otpData = new HashMap<>();
            otpData.put("otp", otp);
            otpData.put("expiry", expiryTime);
            otpData.put("used", false);

            db.collection("otps").document(email).set(otpData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to store OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}