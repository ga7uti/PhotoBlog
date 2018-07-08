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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;


public class AccountSetupActivity extends AppCompatActivity {

    private CircularImageView circularImageView;
    private Uri profileImage=null;
    private boolean isChanged=false;
    private FirebaseAuth firebaseAuth;
    private EditText getName;
    private Button saveProfile;
    private StorageReference mStorageRef;
    private ProgressBar setupProgress;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        final Toolbar setupActivityToolbar;
        setupActivityToolbar=findViewById(R.id.setup_activity_toolbar);
        setSupportActionBar(setupActivityToolbar);
        setupActivityToolbar.getTitle();
        getSupportActionBar().setTitle("Setup Account");

        getName=findViewById(R.id.setup_add_name);
        saveProfile=findViewById(R.id.save_profile_btn);

        firebaseAuth=FirebaseAuth.getInstance();
        user_id=firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        setupProgress=findViewById(R.id.setupProgress);

        setupProgress.setVisibility(View.VISIBLE);
        saveProfile.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    if(task.getResult().exists()){
                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");
                        getName.setText(name);
                        profileImage = Uri.parse(image);
                        RequestOptions placeholderequest=new RequestOptions();
                        placeholderequest.placeholder(R.drawable.default_image);
                        Glide.with(AccountSetupActivity.this).load(image).into(circularImageView);


                    }
                }else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetupActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
                saveProfile.setEnabled(true);
            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = getName.getText().toString();
                    if (!TextUtils.isEmpty(user_name) && profileImage != null) {
                        setupProgress.setVisibility(View.VISIBLE);
                        if(isChanged) {
                            final String user_id = firebaseAuth.getCurrentUser().getUid();
                            StorageReference imagePath = mStorageRef.child("profile_image").child(user_id + ".jpg");
                            imagePath.putFile(profileImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        setupFirestore(task, user_name);

                                    } else {
                                        Toast.makeText(AccountSetupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                    setupProgress.setVisibility(View.INVISIBLE);
                                }
                            });
                        }else {
                            setupFirestore(null,user_name);
                        }
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
                        cropImage();

                    }
                }else{
                    cropImage();
                }
            }
        });




    }

    private void setupFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name) {
        Uri download_uri;
        if(task!=null){
            download_uri = task.getResult().getDownloadUrl();

        }else {
            download_uri=profileImage;
        }

        Map<String,String> userName=new HashMap<>();
        userName.put("name",user_name);
        userName.put("image",download_uri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AccountSetupActivity.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(AccountSetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                }else{
                    Toast.makeText(AccountSetupActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    setupProgress.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AccountSetupActivity.this);
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
