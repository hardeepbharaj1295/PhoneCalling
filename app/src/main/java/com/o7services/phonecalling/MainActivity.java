package com.o7services.phonecalling;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.o7services.phonecalling.background.TestForecastService;
import com.o7services.phonecalling.connection.Connection;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    Calendar calendar;
    Intent alarm;
    final int SDK_INT = Build.VERSION.SDK_INT;

    TextView textView;
    EditText editText;
    ArrayList<String> mPeopleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);

        textView.setText(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 2);
                return;
            }
            PopulatePeopleList();
        } else {
            PopulatePeopleList();     // Function Calling}
        }

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm = new Intent(this, TestForecastService.class);
        pendingIntent = PendingIntent.getService(this, 0, alarm, 0);

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60000, pendingIntent);
            Log.e("lowerMF","hahah");
        }
        else if (Build.VERSION_CODES.KITKAT <= SDK_INT  && SDK_INT < Build.VERSION_CODES.M) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60000,pendingIntent);
            Log.e("kitkatMF","hahah");
        }
        else if (SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60000,pendingIntent);
            Log.e("marshmallowMF","hahah");
        }
    }

    public void calling(View v) {
        if (TextUtils.isEmpty(editText.getText().toString()))
        {
            Toast.makeText(this, "Please Enter the number", Toast.LENGTH_SHORT).show();
        }
        else {
            phoneCall();
        }
    }

    public void phoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + editText.getText().toString()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                return;
            }
        }
        startActivity(intent);
    }

    int i=0;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PopulatePeopleList();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void PopulatePeopleList() {
        try {
            mPeopleList.clear();
            Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            while (people.moveToNext()) {
                String contactName = people.getString(people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
                String hasPhone = people.getString(people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if ((Integer.parseInt(hasPhone) > 0)) {
                    // You know have the number so now query it like this
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {

                        //store numbers and display a dialog letting the user select which.
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.e("Name : ",contactName);
                        Log.e("Number : ", phoneNumber);
                        Connection.name.add(contactName);
                        Connection.number.add(phoneNumber);
                    }
                    phones.close();
                }
                i++;
                if (i == 10) {
                    break;
                }
            }
            people.close();
        }
        catch (Exception e){
            e.getMessage();
            e.printStackTrace();
        }
    }


}
