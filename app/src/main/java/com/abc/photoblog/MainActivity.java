package com.abc.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        floatingActionButton=findViewById(R.id.floatingActionButton);

        homeFragment=new HomeFragment();
        accountFragment=new AccountFragment();
        notificationFragment=new NotificationFragment();
        initializeFragment();


        bottomNavigationView=findViewById(R.id.bottomNavigationView2);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment=getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                switch (item.getItemId()){
                    case R.id.home_bottom_menu:
                        setFragment(homeFragment,fragment);
                        return true;
                    case R.id.notification_bottom_menu:
                        setFragment(notificationFragment,fragment);
                        return true;
                    case R.id.account_bottom:
                        setFragment(accountFragment,fragment);
                        return true;


                }
                return false;
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AddNewPost.class);
                startActivity(intent);
            }
        });
    }

    private void setFragment(Fragment fragment,Fragment current) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){

            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if(fragment == accountFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if(fragment == notificationFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);

        }
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
;
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
    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, homeFragment);
        fragmentTransaction.add(R.id.fragment_container, notificationFragment);
        fragmentTransaction.add(R.id.fragment_container, accountFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

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
