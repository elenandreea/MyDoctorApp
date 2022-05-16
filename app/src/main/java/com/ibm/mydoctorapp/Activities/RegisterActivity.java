package com.ibm.mydoctorapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ibm.mydoctorapp.Models.UserProfile;
import com.ibm.mydoctorapp.R;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText registerFullName, registerEmail, registerPassword, registerConfPassword, workplace;
    Button registerUserBtn, gotoLoginBtn;
    Spinner department;
    FirebaseAuth fAuth;
    String choice ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerFullName = findViewById(R.id.registerFullName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfPassword = findViewById(R.id.confirmPassword);

        workplace = findViewById(R.id.workplace);
        department = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> medicalDepAdapter = ArrayAdapter.createFromResource(this,
                R.array.medical_department_array, R.layout.support_simple_spinner_dropdown_item);
        medicalDepAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        department.setAdapter(medicalDepAdapter);
        department.setOnItemSelectedListener(this);

        registerUserBtn = findViewById(R.id.registerbtn);
        gotoLoginBtn = findViewById(R.id.gotoLoginBtn);

        fAuth = FirebaseAuth.getInstance();

        gotoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        registerUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = registerFullName.getText().toString();
                final String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confPassword = registerConfPassword.getText().toString();
                final String work = workplace.getText().toString();

                if(fullName.isEmpty()){
                    registerFullName.setError("Full Name is Required");
                    return;
                }

                if(email.isEmpty()){
                    registerEmail.setError("Email is Required");
                    return;
                }

                if(password.isEmpty()){
                    registerPassword.setError("Password is Required");
                    return;
                }

                if(confPassword.isEmpty()){
                    registerConfPassword.setError("Confirmation Password is Required");
                    return;
                }

                if(!confPassword.equals(password)){
                    registerConfPassword.setError("Password does not match");
                    return;
                }

                if(work.isEmpty()){
                    workplace.setError("Workplace is required!");
                    return;
                }

                if(choice.isEmpty()){
                    return;
                }

                Toast.makeText(RegisterActivity.this, "Data Validated", Toast.LENGTH_SHORT).show();

                fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName).build();
                        fAuth.getCurrentUser().updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //put workplace & department on intent before
                                    UserProfile userProfile = new UserProfile(fullName, email, choice, work, fAuth.getUid());
                                    addUserProfile(userProfile);
                                    subscribeUserProfile(choice);
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void subscribeUserProfile(String choice) {
        FirebaseMessaging.getInstance().subscribeToTopic(choice)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "You were subscribed to " + choice, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserProfile(UserProfile userProfile) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("User_Profiles").push();
        // add post data to firebase database
        myRef.setValue(userProfile).addOnSuccessListener(aVoid -> showMessage());
    }

    private void showMessage() {
        Toast.makeText(RegisterActivity.this, "User created successfully",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position > 0) {
            choice = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
