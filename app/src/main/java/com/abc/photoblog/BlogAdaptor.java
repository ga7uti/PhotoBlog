package com.abc.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class BlogAdaptor  extends RecyclerView.Adapter<BlogAdaptor.ViewHolder>{
    private List<BlogPost> blogPostList;

    public BlogAdaptor(List<BlogPost> blogPostList) {
        this.blogPostList = blogPostList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_view,parent,false);
        ViewHolder viewHolder=new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String descdata=blogPostList.get(position).getDesc();
        holder.setblogdesc(descdata);
    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private CircularImageView profileImage;
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
    }




}
