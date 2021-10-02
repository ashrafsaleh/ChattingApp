package com.example.whatsappv2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.whatsappv2.Model.Contacts;
import com.example.whatsappv2.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

        private Toolbar toolbar;
        private RecyclerView findFriendsRecycler;
        private Button back;
        private TextView title;
        private DatabaseReference userRef;
    FirebaseRecyclerOptions<Contacts> options;
    FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter;
        @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_find_friends);
            toolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setTitle("Find Friends");

            userRef = FirebaseDatabase.getInstance().getReference().child("Users");

            findFriendsRecycler = (RecyclerView) findViewById(R.id.find_friends_recycler);
            findFriendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        }

    @Override
    protected void onStart() {
        super.onStart();
        options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef,Contacts.class).build();
         adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                if(TextUtils.isEmpty(model.getImage())){
                    holder.profileImage.setImageResource(R.drawable.profile_image);
                }
                else {
                Picasso.get().load(model.getImage()).into(holder.profileImage);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id= getRef(position).getKey();
                        Intent intent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_ID",visit_user_id);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendViewHolder findFriendViewHolder = new FindFriendViewHolder(view);
                return findFriendViewHolder;
            }
        };
        findFriendsRecycler.setAdapter(adapter);
        adapter.startListening();
    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
            TextView userName,userStatus;
            CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}

