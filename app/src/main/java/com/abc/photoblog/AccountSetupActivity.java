package com.abc.photoblog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;


public class AccountSetupActivity extends AppCompatActivity {

    private CircularImageView circularImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        Toolbar setupActivityToolbar;
        setupActivityToolbar=findViewById(R.id.setup_activity_toolbar);
        setSupportActionBar(setupActivityToolbar);
        setupActivityToolbar.getTitle();
        getSupportActionBar().setTitle("Setup Account");

        circularImageView=findViewById(R.id.circle_image);
        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(AccountSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(AccountSetupActivity.this,"Permision Denied",Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(AccountSetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        Toast.makeText(AccountSetupActivity.this,"Permision Granted",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

}
