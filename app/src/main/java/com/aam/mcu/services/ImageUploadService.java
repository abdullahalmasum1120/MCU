package com.aam.mcu.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aam.mcu.R;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageUploadService extends Service {

    public static final String TAG = "----upload service----";
    public static final String CHANNEL_ID = "foregroundServiceChannel";
    DatabaseReference databaseReference;
    StorageReference storageReference;
    NotificationCompat.Builder notification;

    @Override
    public void onCreate() {
        super.onCreate();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called");
        //initialize variables

        String url = intent.getStringExtra("url");
        DatabaseReference mDatabase = databaseReference.child(intent.getStringExtra("databaseRef"));
        StorageReference mStorage = storageReference.child(intent.getStringExtra("storageRef"));

        createNotificationChannel();

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Uploading...")
                .setContentText("Uploading your image\nYou will be notified if completed")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        startForeground(1, notification.build());

        if (url != null) {
            //upload image
            uploadImage(Uri.parse(url), mStorage, mDatabase);
        }
        return START_REDELIVER_INTENT;
    }

    private void uploadImage(final Uri uri, final StorageReference storageReference, final DatabaseReference databaseReference) {
        storageReference.putFile(uri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                //show or send progress
                final int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());

            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ImageUploadService.this, "Image upload completed", Toast.LENGTH_LONG).show();
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null) {
                                Log.d(TAG, "onSuccess: success");
                                databaseReference.setValue(uri.toString());
                                stopSelf();
                            } else {
                                Log.d(TAG, "onSuccess: failed");
                            }
                        }
                    });
                }
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(@NonNull UploadTask.TaskSnapshot snapshot) {
                Log.d(TAG, "onPaused: called");
                Toast.makeText(ImageUploadService.this, "Image upload paused", Toast.LENGTH_LONG).show();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG, "onCanceled: called");
                Toast.makeText(ImageUploadService.this, "Image upload canceled", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: called");
                Toast.makeText(ImageUploadService.this, "Image upload failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Uploading Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
