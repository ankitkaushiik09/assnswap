package com.kaushik.assnswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaushik.assnswap.Model.BookPost;
import com.kaushik.assnswap.Model.User;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class userprofile extends AppCompatActivity {
    private CircleImageView image_profile;
    private TextView username;
    private DatabaseReference reference;
    private FirebaseUser fuser;
    Intent intent;
    String userID="";
    private List<BookPost> mBookPosts;
    private String mUsername="";
    private  String mProfileURL="";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        intent = getIntent ();
        userID = intent.getStringExtra ( "userID" );

       image_profile = findViewById ( R.id.profile_image );
        username= findViewById ( R.id.username);
        fuser = FirebaseAuth.getInstance ().getCurrentUser ();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        reference.keepSynced ( true );
        reference.addValueEventListener ( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue ( User.class );
                username.setText ( Objects.requireNonNull ( user ).getUsername () );

                if(user.getImageURL ().equals ( "default" ))
                {
                    image_profile.setImageResource ( R.drawable.userphoto );
                }
                else
                {
                    Glide.with ( getApplicationContext () ).load ( user.getImageURL () ).placeholder ( R.drawable.new_loader ).into ( image_profile);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        } );



    }
}