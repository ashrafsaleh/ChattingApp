package com.example.whatsappv2.Activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappv2.Adapter.MessagesAdapter;
import com.example.whatsappv2.Model.Messages;
import com.example.whatsappv2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String recievedID,recievedName,recprofileImage,senderID;
    private TextView customUserName,userLastSeen;
    private CircleImageView userProImage;
    private Toolbar chat_toolbar;
    private ImageButton sendMessageBtn;
    private EditText inputMessage;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessagesAdapter messagesAdapter;
    private RecyclerView privatMessageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        senderID = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        recievedID = getIntent().getExtras().get("visit_user_id").toString();
        recievedName = getIntent().getExtras().get("visit_user_name").toString();
        recprofileImage = getIntent().getExtras().get("user_profile_image").toString();
        chat_toolbar= (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chat_toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionbarView);
        customUserName = (TextView) findViewById(R.id.custom_user_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_status);
        userProImage =(CircleImageView) findViewById(R.id.custom_profile_image);
        sendMessageBtn =(ImageButton) findViewById(R.id.chat_send_message_button);
        inputMessage = (EditText)findViewById(R.id.chat_input_message);
        privatMessageList = (RecyclerView)findViewById(R.id.user_list_of_messages);
        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayout = new LinearLayoutManager(this);
        privatMessageList.setLayoutManager(linearLayout);
        privatMessageList.setAdapter(messagesAdapter);
        customUserName.setText(recievedName);
        Picasso.get().load(recprofileImage).placeholder(R.drawable.profile_image).into(userProImage);

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reference.child("Messages").child(senderID).child(recievedID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messagesAdapter.notifyDataSetChanged();
                privatMessageList.smoothScrollToPosition(privatMessageList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please enter your message first...", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSednerRef = "Messages/" + senderID + "/" + recievedID;
            String messageReceiverRef = "Messages/" + recievedID + "/" + senderID;

            DatabaseReference userMessageKeyRef = reference.child("Messages")
                    .child(senderID).child(recievedID).push();
            String messagePushId = userMessageKeyRef.getKey();
            
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",senderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSednerRef + "/" + messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId,messageTextBody);

            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message sent successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            inputMessage.setText("");
        }
    }
}
