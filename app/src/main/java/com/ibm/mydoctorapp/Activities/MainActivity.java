package com.ibm.mydoctorapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibm.mydoctorapp.Adapters.NotificationAdapter;
import com.ibm.mydoctorapp.Interfaces.LocationCallable;
import com.ibm.mydoctorapp.Models.Notification;
import com.ibm.mydoctorapp.Models.UserLocation;
import com.ibm.mydoctorapp.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Toolbar toolbar;
    FirebaseAuth auth;
    AlertDialog.Builder resetAlert;
    LayoutInflater inflater;

    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<Notification> notificationList;
    NotificationAdapter notificationAdapter;
    RecyclerView notificationRecyclerView;

    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    Location firstLocation;

    @Override
    protected void onStart() {
        super.onStart();

        getCurrentLocation();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList = new ArrayList<>();

                for (DataSnapshot postsnap: snapshot.getChildren()) {
                    Notification post = postsnap.getValue(Notification.class);
                    notificationList.add(post) ;
                }

                System.out.println(notificationList.size());
                notificationAdapter = new NotificationAdapter(MainActivity.this, notificationList);
                notificationRecyclerView.setAdapter(notificationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getCurrentLocation() {
        if (checkForLocationPermission()) {
            if (isLocationEnabled()){
                //we can get the location
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(MainActivity.this, task -> {
                    Location location = task.getResult();
                    if (location == null){
                        Toast.makeText(this, "Problem", Toast.LENGTH_SHORT).show();
                    } else {
                        double currentLatitude = location.getLatitude();
                        double currentLongitude = location.getLongitude();

                        DatabaseReference ref = firebaseDatabase.getReference("Locations").child(currentUser.getUid());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()){
                                    ref.setValue(new UserLocation(currentLatitude, currentLongitude));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                });
            } else {
                // open settings here
                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermission();
        }
    }


    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,  new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_ACCESS_LOCATION);
    }

    private boolean checkForLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        setToolbar();

        resetAlert = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        currentUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Notifications").child(currentUser.getUid());

        notificationRecyclerView = findViewById(R.id.notification_recyclerview);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        notificationRecyclerView.setHasFixedSize(true);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    private void setToolbar() {
        toolbar = findViewById(R.id.styled_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.resetUserPassword){
            startActivity(new Intent(getApplicationContext(),ResetPasswordActivity.class));
        }

        if(item.getItemId() == R.id.updateEmailMenu){
            final View view = inflater.inflate(R.layout.reset_pop, null);

            resetAlert.setTitle("Update Email")
                    .setMessage("Enter new Email Address")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            EditText email = view.findViewById(R.id.resetEmailPop);
                            if(email.getText().toString().isEmpty()){
                                email.setError("Required Field");
                                return;
                            }

                            FirebaseUser user = auth.getCurrentUser();
                            user.updateEmail(email.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setView(view)
                    .create().show();
        }

        if(item.getItemId() == R.id.deleteAccountMenu){
            resetAlert.setTitle("Delete Account Permanently?")
                    .setMessage("Are you sure?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseUser user = auth.getCurrentUser();
                            user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create().show();
        }

        if(item.getItemId() == R.id.profileMenu){
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double newLatitude = location.getLatitude();
        double newLongitude = location.getLongitude();
        getUserLocationCoordinates(currentUser.getUid(), currentLocation -> {
            float[] distance = new float[1];
            Location.distanceBetween(newLatitude, newLongitude,
                    currentLocation.getLatitude(), currentLocation.getLongitude(), distance);
            if (distance[0] > 30.0) {
                DatabaseReference ref = firebaseDatabase.getReference("Locations").child(currentUser.getUid());
                ref.setValue(location).addOnCompleteListener(task -> System.out.println("Done."));
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    private void getUserLocationCoordinates(String userID, LocationCallable locationCallable){
        DatabaseReference reference = firebaseDatabase.getReference("Locations").child(userID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserLocation location = snapshot.getValue(UserLocation.class);
                    locationCallable.getUserLocation(location);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
