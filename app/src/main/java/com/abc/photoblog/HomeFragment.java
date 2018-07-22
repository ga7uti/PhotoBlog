package com.abc.photoblog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<BlogPost> blogPostList;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView blogListView;
    private BlogAdaptor blogAdaptor;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_home, container, false);
        blogPostList=new ArrayList<>();
        blogListView=view.findViewById(R.id.blog_list_recycler);
        blogListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogAdaptor=new BlogAdaptor(blogPostList);
        blogListView.setAdapter(blogAdaptor);
        firebaseFirestore=FirebaseFirestore.getInstance();


        firebaseFirestore.collection("Post").addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for (DocumentChange doc:documentSnapshots.getDocumentChanges()){
                    if (doc.getType()==DocumentChange.Type.ADDED){
                        BlogPost blogPost=doc.getDocument().toObject(BlogPost.class);
                        blogPostList.add(blogPost);
                        Log.d("Data","Data saving");
                        blogAdaptor.notifyDataSetChanged();
                    }
                }
            }
        });



        return view;
    }

}
