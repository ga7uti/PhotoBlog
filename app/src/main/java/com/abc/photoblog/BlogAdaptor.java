package com.abc.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.Date;
import java.util.List;

public class BlogAdaptor  extends RecyclerView.Adapter<BlogAdaptor.ViewHolder>{
    private List<BlogPost> blogPostList;
    private FirebaseFirestore firebaseFirestore;
    private Context context;

    public BlogAdaptor(List<BlogPost> blogPostList) {
        this.blogPostList = blogPostList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_view,parent,false);
        firebaseFirestore=FirebaseFirestore.getInstance();
        context=parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String descdata=blogPostList.get(position).getDesc();
        holder.setblogdesc(descdata);

        String uid=blogPostList.get(position).getUser_id();

        String current_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get name and user profile image
        firebaseFirestore.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    String name=task.getResult().getString("name");
                    String image=task.getResult().getString("image");
                    holder.setUserData(name,image);

                }
            }
        });

        //get blog image

        String blogimage=blogPostList.get(position).getImage_url();
        String thumbimage=blogPostList.get(position).getThumb_url();
        holder.setBlogImage(blogimage,thumbimage);

        //get time
        try {
            long millisecond = blogPostList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy",new Date(millisecond)).toString();
            holder.setDate(dateString);
        }catch (Exception e){
            Toast.makeText(context,"Error :"+e.getMessage(),Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView circleImageView;
        private ImageView blogImage;
        private TextView userName,dateTime,description;
        private View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
        }

        public void setblogdesc(String blogdesc){
            description=view.findViewById(R.id.blog_desc);
            description.setText(blogdesc);
        }
        public void setUserData(String name,String image){
            userName=view.findViewById(R.id.blog_user_name);
            circleImageView=view.findViewById(R.id.circle_image);

            userName.setText(name);

            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(image).into(circleImageView);


        }

        public void setBlogImage(String image,String thumb){

            blogImage =view.findViewById(R.id.blog_image_view);

            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(image).thumbnail(Glide.with(context).load(thumb)).into(blogImage);
        }

        public  void  setDate(String date){
            dateTime=view.findViewById(R.id.blog_date_time);
            dateTime.setText(date);
        }

    }




}
