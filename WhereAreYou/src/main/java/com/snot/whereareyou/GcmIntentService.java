/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.snot.whereareyou;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.snot.whereareyou.database.DatabaseHandler;
import com.snot.whereareyou.database.History;

//import android.database.sqlite.SQLiteDatabase;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
	String latitude = intent.getStringExtra("latitude");
	String longitude = intent.getStringExtra("longitude");
	String phoneNumber = intent.getStringExtra("phone_number");

	Log.i(TAG, "GCM from: " + phoneNumber);

// Make history... ;)
	History history = new History();
	history.latitude = latitude;
	history.longitude = longitude;
	history.phoneNumber = phoneNumber;
	long unixTime = System.currentTimeMillis() / 1000L;
	history.timestamp =String.valueOf(unixTime);
	// TODO: use provider
	DatabaseHandler.getInstance(this).putHistory(history);

	Uri uri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "&z=10");

	Context c = getApplicationContext();
	String message = c.getString(R.string.notification_message) + " " + MainActivity.getContactName(this, phoneNumber);
	sendNotification(message, uri);

//        Bundle extras = intent.getExtras();
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//        // The getMessageType() intent parameter must be the intent you received
//        // in your BroadcastReceiver.
//        String messageType = gcm.getMessageType(intent);
//
//        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
//            /*
//             * Filter messages based on message type. Since it is likely that GCM will be
//             * extended in the future with new message types, just ignore any message types you're
//             * not interested in, or that you don't recognize.
//             */
//            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
//                sendNotification("Deleted messages on server: " + extras.toString());
//            // If it's a regular GCM message, do some work.
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
//                // This loop represents the service doing some work.
////                for (int i = 0; i < 5; i++) {
////                    Log.i(TAG, "Working... " + (i + 1)
////                            + "/5 @ " + SystemClock.elapsedRealtime());
////                    try {
////                        Thread.sleep(5000);
////                    } catch (InterruptedException e) {
////                    }
////                }
////                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
//                // Post notification of received message.
//                sendNotification("Received: " + extras.toString());
//                Log.i(TAG, "Received: " + extras.toString());
//            }
//        }
//        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

	private void sendNotification(String title, Uri uri)
	{
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, i, 0);
// Build notification
                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle(title)
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentIntent(pIntent)
                        .build();
		notification.defaults |= Notification.DEFAULT_ALL;
// Hide the notification after its selected
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

// Add notification to NotificationManager
                NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, notification);
	}

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}

