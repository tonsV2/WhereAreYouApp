package com.snot.whereareyou;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.content.Intent;
import android.view.View;
import android.net.Uri;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.app.PendingIntent;


import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.ArrayList;


/**
 * GCM - http://developer.android.com/google/gcm/client.html
 *       https://code.google.com/p/gcm/
 *
 * TODO
 *  - Remove hardcoded strings
 *  - move api key in to main activity as static property
 */


public class MainActivity extends Activity {

	public static final String PROPERTY_REG_ID = "registration_id";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final int PICK_CONTACT_REQUEST = 1;  // The request code
	private static final String PROPERTY_APP_VERSION = "appVersion";

	String SENDER_ID = "372247536430";
	String regid;

	GoogleCloudMessaging gcm;
	Context context;
	// Log tag
	static final String TAG = "GCM Demo";

	TextView mDisplay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDisplay = (TextView) findViewById(R.id.display);

		context = getApplicationContext();
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			if (regid.isEmpty()) {
				registerInBackground();
			}
			else
			{
				pickContact();
			}
		}
		else
		{
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}

        /** Shows contact picker dialog
         */
        public void pickContact() {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            intent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
            startActivityForResult(intent, PICK_CONTACT_REQUEST);
        }

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
		super.onActivityResult( requestCode, resultCode, intent );
		//if(resultCode != RESULT_CANCELED && resultCode == RESULT_OK) {
		if(resultCode == RESULT_OK) {
			if(requestCode == PICK_CONTACT_REQUEST) {
				handleContact(intent);
			}
		}
	}

	private String getPhoneNumber(Intent intent)
	{
		Uri contact = intent.getData();
		String[] projection = {Phone.NUMBER};
		Cursor cursor = getContentResolver().query(contact, projection, null, null, null);
		cursor.moveToFirst();
		// Retrieve the phone number from the NUMBER column
		int column = cursor.getColumnIndex(Phone.NUMBER);
		String phoneNumber = null;
		if(column != -1)
		{
			phoneNumber = cursor.getString(column);
		}
		cursor.close();
		return phoneNumber;
	}

	private void handleContact(Intent intent)
	{
		final String phoneNumber = getPhoneNumber(intent);

		String url = "";
                try {
			String regId = getRegistrationId(this);
			String query = "?regId=" + URLEncoder.encode(regId, "UTF-8") + "&phoneNumber=" + URLEncoder.encode(phoneNumber, "UTF-8");
                        url = "https://whereareyoudroid.appspot.com/location" + query;
                } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

		ShortenUrlTask task = new ShortenUrlTask() {
			@Override
			protected void onPostExecute(String result) {
				String message = "Where are you? Please click the following url to let me know.\n" + result;
				sendSMS(phoneNumber, message);
				Toast.makeText(this, "Location request sent", Toast.LENGTH_SHORT).show();
				exitApp();
			}
		};
		task.execute(url);
//		Toast.makeText(this, "SMS send to " + phoneNumber + ".\nAwaiting response...", Toast.LENGTH_SHORT).show();
        }

	private void exitApp()
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);	
	}


        /** SMS sender with support for big messages
         * @param phoneNumber
         * @param message
         */
	private void sendSMS(String phoneNumber, String message)
	{
		Log.v(TAG, String.format("sendSMS(%s, %s)", phoneNumber, message));
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		// TODO: this used to work with null arguments as below... not anymore!!!
		//sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
		ArrayList<PendingIntent> sentList = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveredList = new ArrayList<PendingIntent>();
		sms.sendMultipartTextMessage(phoneNumber, null, parts, sentList, deliveredList);
	}

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    //sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
		pickContact();
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}

