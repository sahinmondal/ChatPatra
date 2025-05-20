package com.tum.chatpatra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText editPhone, editOtp;
    private Button btnContinue;
    private CountryCodePicker ccp;
    private FirebaseAuth auth;
    private String verificationId;
    private boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        editPhone = findViewById(R.id.editPhone);
        editOtp = findViewById(R.id.editOtp);
        btnContinue = findViewById(R.id.btnContinue);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editPhone);

        btnContinue.setOnClickListener(v -> {
            if (!isOtpSent) {
                String phone = ccp.getFullNumberWithPlus();
                sendOtp(phone);
            } else {
                String code = editOtp.getText().toString().trim();
                if (!code.isEmpty() && verificationId != null) {
                    verifyCode(code);
                }
            }
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Toast.makeText(this, "Sending OTP...", Toast.LENGTH_SHORT).show();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(LoginActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationId = s;
                    isOtpSent = true;
                    editOtp.setVisibility(View.VISIBLE);
                    btnContinue.setText("Verify OTP");
                    Toast.makeText(LoginActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}