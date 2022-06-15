package com.ibm.mydoctorapp.NotificationModule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ibm.mydoctorapp.Activities.MainActivity;
import com.ibm.mydoctorapp.Interfaces.LocationCallable;
import com.ibm.mydoctorapp.Models.Notification;
import com.ibm.mydoctorapp.Models.UserLocation;
import com.ibm.mydoctorapp.R;

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "NoticeService";

    FirebaseDatabase firebaseDatabase;
    FirebaseUser currentUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        if(message.getData().size()> 0) {
            String patient = message.getData().get("patient");
            String postID = message.getData().get("postID");
            String userID = message.getData().get("userID");
            String category = message.getData().get("category");

            firebaseDatabase = FirebaseDatabase.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            getUserLocationCoordinates(userID, new LocationCallable() {
                @Override
                public void getUserLocation(UserLocation location1) {
                    getUserLocationCoordinates(currentUser.getUid(), location2 -> {
                        float[] distance = new float[1];
                        Location.distanceBetween(location1.getLatitude(), location1.getLongitude(),
                                location2.getLatitude(), location2.getLongitude(), distance);
                        if (distance[0] < 30.0){
                            addNotificationToDoctorDB(postID, userID, patient, category);
                            notifyTopicDoctor(patient, category);
                        }
                    });
                }
            });
        }
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotificationToDoctorDB(String postID, String userID, String patient, String category) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String currentUID = firebaseUser.getUid();

        Notification notification = new Notification(userID, patient, category, postID);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Notifications").child(currentUID).push();
        databaseReference.setValue(notification);
    }

    private void notifyTopicDoctor(String patient, String category) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "notification_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String body = patient + " posted a question to " + category;

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.notification_logo)
                        .setContentTitle("You have a new notification!")
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setColor(Color.BLUE)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For android Oreo and above notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Fcm notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 , notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
//        Log.d(TAG, "Refreshed token: " + token);
        
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//        String currentUID = firebaseUser.getUid();
//
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("Tokens").child(currentUID);
//        databaseReference.setValue(token);
    }
}
