package com.abc.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText regEmail,regPass,regConfirmPass;
    private Button createAccountBtn,previousUserBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressBar regProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regEmail=findViewById(R.id.reg_email_text);
        regPass=findViewById(R.id.reg_pass_text);
        regConfirmPass=findViewById(R.id.reg_confrim_pass);
        regProgressBar=findViewById(R.id.reg_progress);
        createAccountBtn=findViewById(R.id.login_btn);
        previousUserBtn=findViewById(R.id.reg_login_btn);
        firebaseAuth=FirebaseAuth.getInstance();

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=regEmail.getText().toString();
                String pass=regPass.getText().toString();
                String confpass=regConfirmPass.getText().toString();
                regProgressBar.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confpass)){
                    if(pass.equals(confpass)){
                        firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    switchToMain();
                                }else {
                                    Toast.makeText(RegisterActivity.this,"Error :"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }else {
                        Toast.makeText(RegisterActivity.this,"Password doesnot match",Toast.LENGTH_SHORT).show();
                    }
                    regProgressBar.setVisibility(View.INVISIBLE);
                }

            }
        });


    }

    @Override
    protected void onStart() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null) {
            switchToMain();
        }
        super.onStart();
    }

    private void switchToMain() {
        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
