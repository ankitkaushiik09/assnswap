package com.kaushik.assnswap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaushik.assnswap.Fragments.ChatsFragment;
import com.kaushik.assnswap.Fragments.HomeFragment;
import com.kaushik.assnswap.Fragments.ProfileFragment;
import com.kaushik.assnswap.Model.Chat;
import com.kaushik.assnswap.Model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
public class MainActivity extends AppCompatActivity {
    CircleImageView profile_image;
    TextView username ;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    FloatingActionButton addPost;

    private GpsTracker gpsTracker;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private String address="";
    private String city="";

    public  String Uname="";
    public  String ProfileUrl="";


    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TapTargetSequence tapTargetSequence = new TapTargetSequence(this);
        tapTargetSequence.targets(
                TapTarget.forView(findViewById(R.id.addPost), "Become a seller", "Add Post")
                        .outerCircleColor(R.color.teal_200)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(20)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(15)
                        .descriptionTextColor(R.color.black)
                        .textColor(R.color.black)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.black)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .targetRadius(60));
        tapTargetSequence.listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {

            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {

            }
        });
        tapTargetSequence.start();


        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        addPost = findViewById(R.id.addPost);

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddBookActivity.class);
                intent.putExtra("address", address);
                intent.putExtra("city", city);
                intent.putExtra("latitude", String.valueOf(latitude));
                intent.putExtra("longitude", String.valueOf(longitude));
                startActivity(intent);
            }
        });

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        updateLocationData(address, city, String.valueOf(latitude), String.valueOf(longitude));

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                // username.setText ( Objects.requireNonNull ( user ).getUsername ()+" / "+address+", "+city);

                Uname = user.getUsername();
                ProfileUrl = user.getImageURL();


                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.userphoto);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).placeholder(R.drawable.new_loader).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       final TabLayout tabLayout = findViewById(R.id.tab_layout);
       final ViewPager viewPager = findViewById(R.id.viewpager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                viewPagerAdapter.addFragment(new HomeFragment(), "Home");

                //viewPagerAdapter.addFragment ( new UsersFragment (),"Users" );
                viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");



                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unread++;
                    }
                }
                if (unread == 0) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "(" + unread + ") Chats");
                }
                viewPager.setAdapter(viewPagerAdapter);

                tabLayout.setupWithViewPager(viewPager);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ().inflate ( R.menu.menu ,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId ())
        {
            case R.id.logout :
                FirebaseAuth.getInstance ().signOut ();
                //add this flag to save app from crash
                is_online ( "offline" );
                startActivity ( new Intent ( getApplicationContext (),StartActivity.class ).setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP ) );
                return true;

            case R.id.refresh :
                //finish();
                startActivity(getIntent());
                return true;
            case R.id.deleteuser:
                deletecurruser();
                return true;
        }
        return  false;
    }

    private void deletecurruser() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseAuth.getInstance().getCurrentUser().delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
                                            startActivity(intent);
                                        }
                                        else {

                                        }

                                    }
                                });

                    }
                });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super ( fm );
            this.fragments = new ArrayList<> (  );
            this.titles = new ArrayList<> (  );

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get ( position );
        }

        @Override
        public int getCount() {
            return fragments.size ();
        }

        public void  addFragment(Fragment fragment,String title)
        {
            fragments.add ( fragment );
            titles.add ( title );
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get ( position );
        }
    }

    private void is_online(String is_online)
    {
        reference  = FirebaseDatabase.getInstance ().getReference ("Users").child ( firebaseUser.getUid () );

        HashMap<String,Object> hashMap = new HashMap<> (  );
        hashMap.put ( "is_online",is_online );
        reference.updateChildren ( hashMap );
    }

    private void updateLocationData(String address,String city,String latitude,String longitude)
    {

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        reference  = FirebaseDatabase.getInstance ().getReference ("Users").child ( firebaseUser.getUid () );

        HashMap<String,Object> hashMap = new HashMap<> (  );
        hashMap.put ( "address",address );
        hashMap.put ( "city",city );
        hashMap.put ( "latitude",String.valueOf (latitude) );
        hashMap.put ( "longitude",String.valueOf (longitude));
        reference.updateChildren ( hashMap );
        UserData.USERNAME = Uname;
        UserData.PROFILEURL = ProfileUrl;
        UserData.LATITUDE = String.valueOf ( latitude );
        UserData.LONGITUDE = String.valueOf ( longitude );
        UserData.ADDRESS =  address;
        UserData.CITY = city;
    }

    @Override
    protected void onResume() {
        super.onResume ();
        getLatitudeLongitude ();
        try {
            getLocation ( latitude,longitude );
        } catch (IOException e) {
            e.printStackTrace ();
        }
        is_online ( "online" );
        updateLocationData ( address,city,String.valueOf (latitude),String.valueOf (longitude));
        UserData.USERNAME = Uname;
        UserData.PROFILEURL = ProfileUrl;
        UserData.LATITUDE = String.valueOf ( latitude );
        UserData.LONGITUDE = String.valueOf ( longitude );
        UserData.ADDRESS =  address;
        UserData.CITY = city;

    }

    @Override
    protected void onPause() {
        super.onPause ();
        is_online ( "offline" );
    }

    @Override
    protected void onStart() {
        super.onStart ();
        getLatitudeLongitude ();
        try {
            getLocation ( latitude,longitude );
        } catch (IOException e) {
            e.printStackTrace ();
        }
        updateLocationData ( address,city,String.valueOf (latitude),String.valueOf (longitude));
        UserData.USERNAME = Uname;
        UserData.PROFILEURL = ProfileUrl;
        UserData.LATITUDE = String.valueOf ( latitude );
        UserData.LONGITUDE = String.valueOf ( longitude );
        UserData.ADDRESS =  address;
        UserData.CITY = city;
    }

    public void getLatitudeLongitude(){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            //Toast.makeText ( getApplicationContext (), "LAT : "+latitude +"\n "+"LNG : "+longitude, Toast.LENGTH_SHORT ).show ();
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    private void getLocation(double latitude,double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);

        if(!addresses.isEmpty ())
        {
            String addr = addresses.get(0).getSubLocality ();
            String ct = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            address = addr;
            city = ct;
        }


        //Toast.makeText ( this, "Location : "+address+" "+city, Toast.LENGTH_SHORT ).show ();


    }

    @Override
    public void onBackPressed() {
        customDialoge();
    }

    private void customDialoge() {
        final Dialog dialog=new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.exit_alert);
        TextView Yes=(TextView)dialog.findViewById(R.id.yes);
        TextView No=(TextView)dialog.findViewById(R.id.no);
        No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();

    }

    public static class UserData
    {
        public static String USERNAME="";

        public static String PROFILEURL="";

        public static String LATITUDE="";

        public static String LONGITUDE="";

        public static String ADDRESS="";

        public static String CITY="";
    }
}