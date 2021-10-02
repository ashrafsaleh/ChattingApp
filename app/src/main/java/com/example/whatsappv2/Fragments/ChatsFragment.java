package com.example.whatsappv2.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.whatsappv2.Activities.ChatActivity;
import com.example.whatsappv2.Model.Contacts;
import com.example.whatsappv2.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View chatView;
    private RecyclerView chatList;
    private DatabaseReference chatRef,userRef;
    private FirebaseAuth auth;
    private String currentUserID;
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatList = chatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef,Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,chatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull Contacts model) {
                final String userIds = getRef(position).getKey();
                final String[] proImage = {"default_image"};
                userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            if(dataSnapshot.hasChild("image")){
                                 proImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(proImage[0]).into(holder.profileImage);
                            }
                            final String username = dataSnapshot.child("name").getValue().toString();
                            final String userstatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(username);
                            holder.userStatus.setText("Last seen: "+ "\n" + "Date " + " Time");

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("visit_user_id",userIds);
                                    intent.putExtra("visit_user_name",username);
                                    intent.putExtra("user_profile_image", proImage[0]);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                chatsViewHolder chatsViewHolder = new chatsViewHolder(view);
                return chatsViewHolder;
            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class chatsViewHolder extends RecyclerView.ViewHolder {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
