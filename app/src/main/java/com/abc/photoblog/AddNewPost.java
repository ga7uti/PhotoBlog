package com.abc.photoblog;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AddNewPost extends AppCompatActivity {

    private Toolbar setupToolbar;
    private ProgressBar newPostProgress;
    private ImageView newPostImage;
    private Uri postImage=null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private EditText newPostText;
    private Button addPostButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);

        setupToolbar=findViewById(R.id.new_post_toolbar);
        setSupportActionBar(setupToolbar);
        setupToolbar.getTitle();
        getSupportActionBar().setTitle("New Post");

        newPostProgress=findViewById(R.id.new_post_progress);
        newPostText=findViewById(R.id.new_post_desc);
        addPostButton=findViewById(R.id.post_btn);

        newPostImage=findViewById(R.id.new_post_image);
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AddNewPost.this);
            }
        });


        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc=newPostText.getText().toString();
                if(!TextUtils.isEmpty(desc) && postImage!=null){
                    String random= UUID.randomUUID().toString();
                    StorageReference postImagePath=storageReference.child("post_image").child(random+".jpg");
                    postImagePath.putFile(postImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                String downloadUri = task.getResult().getDownloadUrl().toString();
                                String current_user_id= FirebaseAuth.getInstance().getUid().toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_url", downloadUri);
                                postMap.put("desc", desc);
                                postMap.put("user_id", current_user_id);
                                postMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Post").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(AddNewPost.this,"post added",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }else {

                            }
                        }
                    });
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
                postImage = result.getUri();
                newPostImage.setImageURI(postImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AddNewPost.this,"Error :"+error,Toast.LENGTH_LONG).show();
            }
        }
    }
}
