package com.example.whatsappv2.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class ContactFragment extends Fragment {
    private View contatcsView;
    private RecyclerView contactsList;
    private DatabaseReference contactsRef,userRef;
    private FirebaseAuth auth;
    private String currentUserID;
    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contatcsView = inflater.inflate(R.layout.fragment_contact, container, false);
        contactsList = contatcsView.findViewById(R.id.contact_list);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return contatcsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,contactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsViewHolder holder, int position, @NonNull Contacts model) {
                String userIDs = getRef(position).getKey();
                userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image")){
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(userName);
                            holder.userStatus.setText(userStatus);
                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }
                        else {
                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(userName);
                            holder.userStatus.setText(userStatus);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                contactsViewHolder contactsViewHolder = new contactsViewHolder(view);
                return contactsViewHolder;
            }
        };
        contactsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class contactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
