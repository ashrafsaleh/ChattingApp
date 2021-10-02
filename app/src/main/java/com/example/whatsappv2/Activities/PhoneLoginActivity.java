package com.example.whatsappv2.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsappv2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationCode,verify;
    private EditText inputPhoneNumber,inputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendTokken;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        sendVerificationCode = (Button)findViewById(R.id.send_ver_code_button);
        verify = (Button)findViewById(R.id.verify_button);
        inputPhoneNumber = (EditText)findViewById(R.id.phone_number_input);
        inputVerificationCode = (EditText)findViewById(R.id.verification_code_input);
        dialog = new ProgressDialog(this);




        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = inputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    dialog.setTitle("Phone Verification");
                    dialog.setMessage("Please wait, while we are authenticating using your phone...");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, PhoneLoginActivity.this, mCallbacks);
                }
            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationCode.setVisibility(View.INVISIBLE);


                String verificationCode = inputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    dialog.setTitle("Verification Code");
                    dialog.setMessage("Please wait, while we are verifying verification code...");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationID, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                Toast.makeText(PhoneLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                dialog.dismiss();

                inputPhoneNumber.setVisibility(View.VISIBLE);
                sendVerificationCode.setVisibility(View.VISIBLE);

                inputVerificationCode.setVisibility(View.INVISIBLE);
                verify.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationID = verificationId;
                mResendTokken = token;


                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationCode.setVisibility(View.INVISIBLE);

                inputVerificationCode.setVisibility(View.VISIBLE);
                verify.setVisibility(View.VISIBLE);
            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            dialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in Successfully.", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
