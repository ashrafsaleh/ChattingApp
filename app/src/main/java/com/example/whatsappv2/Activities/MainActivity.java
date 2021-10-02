package com.example.whatsappv2.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsappv2.Adapter.TabsAccessorAdapter;
import com.example.whatsappv2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTablayout;
    private TabsAccessorAdapter tabsAccessorAdapter;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Whats App");

        myViewPager = findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(tabsAccessorAdapter);

        myTablayout = findViewById(R.id.main_tabs);
        myTablayout.setupWithViewPager(myViewPager);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseUser == null){
            sendUserToLoginActivity();
        }
        else {
            updateUserStatus("online");
            verfyUserExistance();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseUser != null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(firebaseUser != null){
            updateUserStatus("offline");
        }
    }

    private void verfyUserExistance() {
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @SuppressLint("ResourceAsColor")
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
       final EditText groupNameFiled = new EditText(MainActivity.this);
       groupNameFiled.setTextColor(R.color.colorPrimaryDark);
       groupNameFiled.setHint("Room one");
       builder.setView(groupNameFiled);
       builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               String groupName = groupNameFiled.getText().toString();
               if(TextUtils.isEmpty(groupName)){
                   Toast.makeText(MainActivity.this, "Please write group name", Toast.LENGTH_SHORT).show();
               }
               else{
                   createNewGroup(groupName);
               }
           }
       });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createNewGroup(final String groupName) {
        reference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName + "group is created successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(intent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent intent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option){
            firebaseAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_settings_option){
            sendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();
        }
        if(item.getItemId()==R.id.main_find_option){
            sendUserToFindFriendsActivity();
        }
        if(item.getItemId() == R.id.home){
            finish();
            return true;
        }
        return  super.onOptionsItemSelected(item);
    }
    private void updateUserStatus(String statu){
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM dd/ yyyy");
        saveCurrentDate = simpleDateFormat.format(calendar.getTime());
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = simpleDateFormat1.format(calendar.getTime());

        HashMap<String,Object> lastseen = new HashMap<>();
        lastseen.put("time",saveCurrentTime);
        lastseen.put("date",saveCurrentDate);
        lastseen.put("state",statu);
        if(firebaseAuth.getCurrentUser().getUid()!=null){
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).child("userState")
                .updateChildren(lastseen);
        }

    }
}
