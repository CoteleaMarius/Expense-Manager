package com.example.expensemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {
    private EditText mEmail, mPass;
    private Button btnReg;
    private TextView mLogin;
    private ProgressDialog mDialog;
    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);
        registration();
    }

    private void registration() {
        mEmail = findViewById(R.id.Email_signup);
        mPass = findViewById(R.id.Password_signup);
        btnReg = findViewById(R.id.btn_signup);
        mLogin = findViewById(R.id.login_here);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String pass = mPass.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("E-mail field is empty");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    mPass.setError("Password field is empty");
                }
                mDialog.setMessage("Processing");
                mDialog.show();
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Successful registration", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}