package com.abc.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserID;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        floatingActionButton=findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AddNewPost.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser =firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            switchActivity();
        }else{
            currentUserID=firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent intent=new Intent(MainActivity.this,AccountSetupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Toast.makeText(MainActivity.this,"Error : "+task.getException().getMessage(), Toast.LENGTH_LONG).show();;
                    }
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_logout_btn:
                logout();
                return true;

            case R.id.menu_account_setting:
                Intent intent=new Intent(MainActivity.this,AccountSetupActivity.class);
                startActivity(intent);
                return true;


                default:
                return false;
        }
    }

    private void logout() {
        firebaseAuth.signOut();
        switchActivity();
    }

    private void switchActivity() {
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
