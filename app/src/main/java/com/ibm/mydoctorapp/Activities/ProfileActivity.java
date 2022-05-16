package com.ibm.mydoctorapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibm.mydoctorapp.Models.UserProfile;
import com.ibm.mydoctorapp.R;

public class ProfileActivity extends AppCompatActivity {

    Button verifyEmailBtn;
    TextView verifyMsg;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser user;
    UserProfile userProfile;

    Button logout;
    TextView userProfName, userProfSpeciality, userProfworkplace, userProfEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userProfName = findViewById(R.id.userProfileName);
        userProfSpeciality = findViewById(R.id.speciality_text);
        userProfworkplace = findViewById(R.id.workplace_text);
        userProfEmail = findViewById(R.id.email_text);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        String uid = user.getUid();
        DatabaseReference ref = firebaseDatabase.getReference("User_Profiles");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    UserProfile userP = snap.getValue(UserProfile.class);
                    String uid_user = userP.getUid();
                    if (uid_user.equals(uid)) {
                        userProfile = userP;
                        break;
                    }
                }

                userProfName.setText(userProfile.getName());
                userProfSpeciality.setText(userProfile.getDepartment());
                userProfworkplace.setText(userProfile.getWorkplace());
                userProfEmail.setText(user.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        verifyMsg = findViewById(R.id.verifyEmailMessage);
        verifyEmailBtn = findViewById(R.id.verifyEmailBtn);

        if(!auth.getCurrentUser().isEmailVerified()){
            verifyEmailBtn.setVisibility(View.VISIBLE);
            verifyMsg.setVisibility(View.VISIBLE);
        }

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                        verifyEmailBtn.setVisibility(View.GONE);
                        verifyMsg.setVisibility(View.GONE);
                    }
                });
            }
        });

        logout = findViewById(R.id.logoutBtn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }
}
