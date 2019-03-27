package com.o7services.phonecalling.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.o7services.phonecalling.connection.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TestForecastService extends IntentService {

    int a;
    String android_id;
    private static final String TAG = "MoonWalker";
    NotificationCompat.Builder notificationBuilder;
    Context context;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    final int SDK_INT = Build.VERSION.SDK_INT;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    Intent alarm;

    public TestForecastService(){
        super("TestForecastService");
    }

    public TestForecastService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e("Handle","result");
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "o7services:com.o7services.backgroundprocess");
        wakeLock.acquire();
        sendDATA();
    }

    private void sendDATA() {
        Log.e("SendData","Sending");
        try {
            android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            if (Connection.name.size()>0) {
                InsertData insertData = new InsertData();
                insertData.execute();
            }

        } catch (Exception e) {
            Log.e("fserviceerror","erre");
        }
        reSETALARAM();
        wakeLock.release();
    }

    private void reSETALARAM() {
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm = new Intent(this,TestForecastService.class);
        pendingIntent = PendingIntent.getService(this, 0, alarm, 0);

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+60000, pendingIntent);
            Log.e("lowerFS","hahah");
        }
        else if (Build.VERSION_CODES.KITKAT <= SDK_INT  && SDK_INT < Build.VERSION_CODES.M) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+60000,pendingIntent);
            Log.e("kitkatFS","hahah");
        }
        else if (SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+60000,pendingIntent);
            Log.e("marshmallowFS","hahah");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class InsertData extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String link = Connection.API + "android_data.php";
                JSONArray array = new JSONArray();

                for (int i = 0; i < Connection.name.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("device_id", android_id);
                    object.put("name", Connection.name.get(i));
                    object.put("number", Connection.number.get(i));
                    array.put(object);
                }

                String msg = array.toString();
                Log.e("Message", msg);
                String data = URLEncoder.encode("json", "UTF-8") + "=" + URLEncoder.encode(msg, "UTF-8");

                System.out.println("Data : " + data);
                URL url = new URL(link);

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data);
                writer.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                return reader.readLine();
            } catch (Exception e) {
                return "Error! " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Result",s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                //Log.e("ssssss",s);
                if (jsonObject.getString("response").equals("success")) {
                    Connection.name.clear();
                    Connection.number.clear();
                    //   Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                } else {
                 //   sendNotification("Moon Walker","Data Not Synced");
                }
            }
            catch (JSONException e){
                e.getMessage();
            }
        }
    }
}
