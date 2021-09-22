package com.crimestoper.police.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.crimestoper.police.R;
import com.crimestoper.police.utils.UserDatabase;
import com.crimestoper.police.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseFirestore mFirestore;
    private static String TAG= SplashScreen.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth= FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();
        mFirestore= FirebaseFirestore.getInstance();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                if(mCurrentUser==null){



                }else{

                    if(Utils.isOnline(SplashScreen.this)) {

                        mFirestore.collection("Cops")
                                .document(mCurrentUser.getUid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SplashScreen.this, "There was a problem in contacting the server: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, e.getMessage());
                                        finish();
                                    }
                                });

                    }else{


                        UserDatabase userDatabase=new UserDatabase(SplashScreen.this);

                        Cursor rs = userDatabase.getData(1);
                        rs.moveToFirst();

                        @SuppressLint("Range") String nam = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_NAME));

                        if (!rs.isClosed()) {
                            rs.close();
                        }

                        if(TextUtils.isEmpty(nam)){



                        }else{



                        }

                    }
                }

            }
        }, 1000);


    }
}
