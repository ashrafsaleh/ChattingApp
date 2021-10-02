package com.example.whatsappv2.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappv2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    Button LoginButton,PhoneButton;
    EditText UserEmail,UserPassword;
    TextView NeedNewAccount,ForgetPassword;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        LoginButton = (Button)findViewById(R.id.login_button);
        PhoneButton = (Button)findViewById(R.id.phone_login);
        UserEmail = (EditText)findViewById(R.id.login_email);
        UserPassword = (EditText)findViewById(R.id.login_password);
        NeedNewAccount = (TextView)findViewById(R.id.need_account);
        ForgetPassword = (TextView)findViewById(R.id.forget_password);
        dialog = new ProgressDialog(this);
        NeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        PhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void AllowUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else
            {
                dialog.setTitle("Signing in");
                dialog.setMessage("Please wait...");
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
             firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if (task.isSuccessful()){
                         String currentUserId = firebaseAuth.getCurrentUser().getUid();
                         String deviceToken = FirebaseInstanceId.getInstance().getToken();
                         reference.child(currentUserId).child("device_token")
                                 .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isSuccessful()){

                                     sendUserToMainActivity();
                                     Toast.makeText(LoginActivity.this, "Logged in Successfuly...", Toast.LENGTH_SHORT).show();
                                     dialog.dismiss();
                                 }
                             }
                         });
                     }
                     else {
                         String message = task.getException().toString();
                         Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                         dialog.dismiss();
                     }
                 }
             });
            }
    }


    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}
