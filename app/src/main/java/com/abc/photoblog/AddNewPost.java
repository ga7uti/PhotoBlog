package com.abc.photoblog;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import io.grpc.internal.zzeo;

public class AddNewPost extends AppCompatActivity {

    private ProgressBar newPostProgress;
    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private Uri postImageUri=null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUser;
    private Toolbar newPostToolbar;
    private Bitmap compressImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);


        newPostToolbar=findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("New Post");

        newPostProgress=findViewById(R.id.new_post_progress);
        newPostImage=findViewById(R.id.new_post_image);
        newPostDesc=findViewById(R.id.new_post_desc);
        newPostBtn=findViewById(R.id.post_btn);

        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=firebaseAuth.getCurrentUser().getUid();
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();


        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AddNewPost.this);
            }
        });


        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc=newPostDesc.getText().toString();
                if(!TextUtils.isEmpty(desc) && postImageUri!=null ){

                    newPostProgress.setVisibility(View.VISIBLE);
                    final UUID randomName=UUID.randomUUID();

                    File newImageFile= new File(postImageUri.getPath());
                    try{
                        compressImage=new Compressor(AddNewPost.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(100)
                                .compressToBitmap(newImageFile);

                    }catch (IOException e){

                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    compressImage.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] imageData=baos.toByteArray();

                    UploadTask filepath=storageReference.child("post_image").child(randomName+".jpg").putBytes(imageData);
                    filepath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                            File newThumbFile= new File(postImageUri.getPath());
                            try{
                                compressImage=new Compressor(AddNewPost.this)
                                        .setMaxHeight(200)
                                        .setMaxWidth(200)
                                        .setQuality(1)
                                        .compressToBitmap(newThumbFile);

                            }catch (IOException e){

                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos=new ByteArrayOutputStream();
                            compressImage.compress(Bitmap.CompressFormat.JPEG,100,baos);
                            byte[] thumbData=baos.toByteArray();

                            UploadTask thumbPath=storageReference.child("post_image/thumbs").child(randomName+".jpg").putBytes(thumbData);
                            thumbPath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    String download_thumb_uri =taskSnapshot.getDownloadUrl().toString();

                                    Map<String,Object> map=new HashMap<>();
                                    map.put("image_url",downloadUri);
                                    map.put("thumb_url",download_thumb_uri);
                                    map.put("desc",desc);
                                    map.put("timestamp", FieldValue.serverTimestamp());
                                    map.put("user_id",currentUser);

                                    firebaseFirestore.collection("Posts").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {

                                            if (task.isSuccessful()){
                                                Intent intent=new Intent(AddNewPost.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Toast.makeText(AddNewPost.this,"Error :  "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                            }
                                            newPostProgress.setVisibility(View.INVISIBLE);
                                        }
                                    });



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                        }
                    });



                }else{
                    Toast.makeText(AddNewPost.this,"Error : Empty Description or Image",Toast.LENGTH_SHORT).show();
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
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AddNewPost.this,"Error :"+error,Toast.LENGTH_LONG).show();
            }
        }
    }

}
