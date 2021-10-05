package com.example.expensemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText mEmail, mPass;
    private Button btnLogin;
    private TextView mForgotPassword, mSignUpHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginDetails();
    }

    private void loginDetails() {
        mEmail = findViewById(R.id.Email_login);
        mPass = findViewById(R.id.Password_login);
        btnLogin = findViewById(R.id.btn_login);
        mForgotPassword = findViewById(R.id.forgot_password);
        mSignUpHere = findViewById(R.id.signup);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String pass = mPass.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email field is empty.");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    mPass.setError("Password field is empty.");
                    return;
                }

            }
        });
        //Registration
        mSignUpHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });
        //Password reset
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ResetActivity.class));
            }
        });
    }
}