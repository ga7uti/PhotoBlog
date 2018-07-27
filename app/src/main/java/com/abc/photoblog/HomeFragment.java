package com.abc.photoblog;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<BlogPost> blogPostList;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView blogListView;
    private BlogAdaptor blogAdaptor;
    private FirebaseAuth firebaseAuth;
    private DocumentSnapshot lastvisible;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth=FirebaseAuth.getInstance();
        blogPostList=new ArrayList<>();
        blogListView=view.findViewById(R.id.blog_list_recycler);
        blogListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogAdaptor=new BlogAdaptor(blogPostList);
        blogListView.setAdapter(blogAdaptor);


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore=FirebaseFirestore.getInstance();

            blogListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean scrolled=!recyclerView.canScrollVertically(1);

                    if (scrolled){

                        loadMorePost();
                    }
                }
            });

            Query firstquery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    lastvisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) ;
                        BlogPost blogdata = doc.getDocument().toObject(BlogPost.class);
                        blogPostList.add(blogdata);
                        blogAdaptor.notifyDataSetChanged();
                    }
                }
            });
        }

        return view;
    }

    private void loadMorePost(){
        if (firebaseAuth.getCurrentUser()!=null){

            Query nextquery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastvisible)
                    .limit(3);

            nextquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        lastvisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) ;
                            BlogPost blogdata = doc.getDocument().toObject(BlogPost.class);
                            blogPostList.add(blogdata);
                            blogAdaptor.notifyDataSetChanged();
                        }

                    }
                }
            });
        }
    }


}
