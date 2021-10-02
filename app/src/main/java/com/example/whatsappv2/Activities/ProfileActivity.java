package com.example.whatsappv2.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.whatsappv2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    CircleImageView userProfileImage;
    TextView userName,userStatus;
    Button sendMessage,declineMesageRequest;
    private String recUserId;
    private DatabaseReference reference,chatReqRef,contactRef,notifiRef;
    String current_state,sendertUserID;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notifiRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        sendertUserID = auth.getCurrentUser().getUid();
        recUserId = getIntent().getExtras().getString("visit_ID");
        userName = (TextView)findViewById(R.id.visit_user_name);
        userStatus = (TextView)findViewById(R.id.visit_profile_status);
        userProfileImage = (CircleImageView)findViewById(R.id.visit_profile_image);
        sendMessage = (Button)findViewById(R.id.send_message_request);
        declineMesageRequest = (Button)findViewById(R.id.decline_message_request);
        current_state = "new";
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        reference.child(recUserId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userNameS = dataSnapshot.child("name").getValue().toString();
                    String userStatusS = dataSnapshot.child("status").getValue().toString();
                    userName.setText(userNameS);
                    userStatus.setText(userStatusS);
                    Picasso.get().load(userImage).into(userProfileImage);
                    manageChatRequest();
                }
                else {
                    String userNameS = dataSnapshot.child("name").getValue().toString();
                    String userStatusS = dataSnapshot.child("status").getValue().toString();

                    userName.setText(userNameS);
                    userStatus.setText(userStatusS);
                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {
        chatReqRef.child(sendertUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(recUserId)){
                    String request_type = dataSnapshot.child(recUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        current_state = "request_sent";
                        sendMessage.setText("Cancel Chat Request");
                    }
                    else if(request_type.equals("recieved")){
                        current_state = "request_recieved";
                        sendMessage.setText("Accept Chat Request");
                        declineMesageRequest.setVisibility(View.VISIBLE);
                        declineMesageRequest.setEnabled(true);
                        declineMesageRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else {
                    contactRef.child(sendertUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(recUserId)){
                                current_state = "friends";
                                sendMessage.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(!sendertUserID.equals(recUserId)){
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage.setEnabled(false);
                    if(current_state.equals("new")){
                        sendChatRequest();
                    }
                    else if (current_state.equals("request_sent")){
                        cancelChatRequest();
                    }
                    else if (current_state.equals("request_recieved")){
                        acceptChatRequest();
                    }

                    else if (current_state.equals("friends")){
                        removeSpecificContact();
                    }
                }
            });
        }
        else {
            sendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactRef.child(sendertUserID).child(recUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactRef.child(recUserId).child(sendertUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessage.setEnabled(true);
                                current_state = "new";
                                sendMessage.setText("Send Message");
                                declineMesageRequest.setVisibility(View.INVISIBLE);
                                declineMesageRequest.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });
    }

    private void acceptChatRequest() {
        contactRef.child(sendertUserID).child(recUserId).child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactRef.child(recUserId).child(sendertUserID).child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatReqRef.child(sendertUserID).child(recUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    chatReqRef.child(recUserId).child(sendertUserID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        sendMessage.setEnabled(true);
                                                                                        current_state = "friends";
                                                                                        sendMessage.setText("Remove this contact");
                                                                                        declineMesageRequest.setVisibility(View.INVISIBLE);
                                                                                        declineMesageRequest.setEnabled(false);
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

    private void cancelChatRequest() {
        chatReqRef.child(sendertUserID).child(recUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatReqRef.child(recUserId).child(sendertUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessage.setEnabled(true);
                                current_state = "new";
                                sendMessage.setText("Send Message");
                                declineMesageRequest.setVisibility(View.INVISIBLE);
                                declineMesageRequest.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });
    }

    private void sendChatRequest() {
        chatReqRef.child(sendertUserID).child(recUserId).
                child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                chatReqRef.child(recUserId).child(sendertUserID).
                        child("request_type").setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            HashMap<String,String> map = new HashMap<>();
                            map.put("from",sendertUserID);
                            map.put("type","request");
                            notifiRef.child(recUserId).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                    sendMessage.setEnabled(true);
                                    current_state = "request_sent";
                                    sendMessage.setText("Cancel Chat Request");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
