package com.crimestoper.police.ui.activities;

import static com.crimestoper.police.utils.Utils.isOnline;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDex;

import com.crimestoper.police.R;
import com.crimestoper.police.utils.NetworkUtil;
import com.crimestoper.police.utils.UserDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Dashboard extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:

                    return true;
                case R.id.navigation_investigating:

                    return true;
                case R.id.navigation_received:

                    return true;
                case R.id.navigation_closed:

                    return true;
            }
            return false;
        }
    };
    private BottomNavigationView navigation;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;

    private void initializeActivity() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    public BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.i("Network reciever", "OnReceive: "+status);
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {

                    performUploadTask();
                    Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show();

                }
            }
        }

    };

    private void performUploadTask() {

        mFirestore.collection("Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        UserDatabase userDatabase=new UserDatabase(Dashboard.this);

                        Cursor rs = userDatabase.getData(1);
                        rs.moveToFirst();

                        @SuppressLint("Range") final String nam = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_NAME));
                        @SuppressLint("Range") String phon = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_PHONE));
                        @SuppressLint("Range") String pos = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_POST));
                        @SuppressLint("Range") String age = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_AGE));
                        @SuppressLint("Range") String city = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_CITY));
                        @SuppressLint("Range") String state = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_STATE));

                        if (!rs.isClosed()) {
                            rs.close();
                        }

                        if(TextUtils.isEmpty(nam) && !documentSnapshot.exists()){

                            Toast.makeText(Dashboard.this, "It seems you have not setup your profile, taking you back...", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Dashboard.this, ProfileSetup.class));
                            finish();

                        }else if(!documentSnapshot.exists()){

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("posting", pos);
                            userMap.put("phone", phon);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });


                        }else if(!documentSnapshot.getString("name").equals(nam)){
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("phone", phon);
                            userMap.put("posting", pos);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .update(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });
                        }else if(!documentSnapshot.getString("phone").equals(phon)){
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("phone", phon);
                            userMap.put("posting", pos);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .update(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });
                        }else if(!documentSnapshot.getString("posting").equals(pos)){
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("phone", phon);
                            userMap.put("posting", pos);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .update(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });
                        }else{
                            Log.i("Update","...");
                        }

                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        initializeActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOnline(this)){
            performUploadTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(NetworkChangeReceiver);

    }

    public void loadfragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
    }
}
