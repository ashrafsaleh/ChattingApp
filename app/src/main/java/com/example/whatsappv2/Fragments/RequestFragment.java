package com.example.whatsappv2.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappv2.Model.Contacts;
import com.example.whatsappv2.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestFragment extends Fragment {
    private View requestsFragmentView;
    private RecyclerView requestChatList;
    private DatabaseReference chatReqRef,userRef,contactsRef;
    private FirebaseAuth auth;
    private String currentUserID;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        requestsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);
        requestChatList = requestsFragmentView.findViewById(R.id.chat_request_list);
        requestChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatReqRef.child(currentUserID),Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, requestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, requestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final requestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.decline_btn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                String type = dataSnapshot.getValue().toString();
                                if(type.equals("recieved")){
                                    userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild("image")){
                                                final String requestname = dataSnapshot.child("name").getValue().toString();
                                                final String requestStatus = dataSnapshot.child("status").getValue().toString();
                                                final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                holder.userName.setText(requestname);
                                                holder.userStatus.setText(requestStatus);
                                                Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                            }
                                                final String requestname = dataSnapshot.child("name").getValue().toString();
                                                final String requestStatus = dataSnapshot.child("status").getValue().toString();
                                                holder.userName.setText(requestname);
                                                holder.userStatus.setText("Wants to connect with you.");

                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CharSequence options [] = new CharSequence[]
                                                            {
                                                                    "Accept",
                                                                    "Cancel"
                                                            };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(requestname + " Chat Request");
                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            if(which ==0){
                                                                contactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            contactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        chatReqRef.child(currentUserID).child(list_user_id)
                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    chatReqRef.child(list_user_id).child(currentUserID)
                                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                Toast.makeText(getContext(), "New contact saved", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                            if(which ==1){
                                                                chatReqRef.child(currentUserID).child(list_user_id)
                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            chatReqRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                                else if (type.equals("sent")){
                                    Button request_sent_btn = holder.itemView.findViewById(R.id.accept_btn);
                                    request_sent_btn.setText("Request Sent");
                                    holder.itemView.findViewById(R.id.decline_btn).setVisibility(View.INVISIBLE);
                                    userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild("image")){
                                                final String requestname = dataSnapshot.child("name").getValue().toString();
                                                final String requestStatus = dataSnapshot.child("status").getValue().toString();
                                                final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                holder.userName.setText(requestname);
                                                holder.userStatus.setText(requestStatus);
                                                Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                            }
                                            final String requestname = dataSnapshot.child("name").getValue().toString();
                                            final String requestStatus = dataSnapshot.child("status").getValue().toString();
                                            holder.userName.setText(requestname);
                                            holder.userStatus.setText("You have sent a request to " + requestname );

                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CharSequence options [] = new CharSequence[]
                                                            {
                                                                    "Cancel Chat Request"
                                                            };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("Already sent Request");
                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            if(which ==0){
                                                                chatReqRef.child(currentUserID).child(list_user_id)
                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            chatReqRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Chat request canceled", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        requestsViewHolder requestsViewHolder = new requestsViewHolder(view);
                        return requestsViewHolder;
                    }
                };
        requestChatList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class requestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button accept,cancel;

        public requestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            accept = itemView.findViewById(R.id.accept_btn);
            cancel = itemView.findViewById(R.id.decline_btn);
        }
    }
}
