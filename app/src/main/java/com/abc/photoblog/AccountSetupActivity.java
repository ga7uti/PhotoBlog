package com.abc.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;


public class AccountSetupActivity extends AppCompatActivity {

    private CircularImageView circularImageView;
    private Uri profileImage=null;
    private FirebaseAuth firebaseAuth;
    private EditText getName;
    private Button saveProfile;
    private StorageReference mStorageRef;
    private ProgressBar setupProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        Toolbar setupActivityToolbar;
        setupActivityToolbar=findViewById(R.id.setup_activity_toolbar);
        setSupportActionBar(setupActivityToolbar);
        setupActivityToolbar.getTitle();
        getSupportActionBar().setTitle("Setup Account");

        getName=findViewById(R.id.setup_add_name);
        saveProfile=findViewById(R.id.save_profile_btn);

        firebaseAuth=FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        setupProgress=findViewById(R.id.setupProgress);

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_name=getName.getText().toString();
                if(!TextUtils.isEmpty(user_name) && profileImage!=null){
                    setupProgress.setVisibility(View.VISIBLE);
                    String user_id=firebaseAuth.getCurrentUser().getUid();
                    StorageReference imagePath=mStorageRef.child("profile_image").child(user_id+".jpg");
                    imagePath.putFile(profileImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Uri download_uri=task.getResult().getDownloadUrl();
                                Toast.makeText(AccountSetupActivity.this,"Image Uploaded",Toast.LENGTH_LONG).show();

                            }else {
                                Toast.makeText(AccountSetupActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            setupProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        circularImageView=findViewById(R.id.circle_image);
        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(AccountSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(AccountSetupActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(AccountSetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(AccountSetupActivity.this);

                    }
                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileImage = result.getUri();
                circularImageView.setImageURI(profileImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AccountSetupActivity.this,"Error :"+error,Toast.LENGTH_LONG).show();
            }
        }
    }
}
