package com.o7services.phonecalling.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class Auto extends BroadcastReceiver {

    final int SDK_INT = Build.VERSION.SDK_INT;

    @Override
    public void onReceive(Context ctx, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            Intent intent1 = new Intent(ctx, TestForecastService.class);
            PendingIntent alarmIntent = PendingIntent.getService(ctx, 0, intent1, 0);

            if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+120000, alarmIntent);
                Log.e("lowerFB","hahah");
            }
            else if (Build.VERSION_CODES.KITKAT <= SDK_INT  && SDK_INT < Build.VERSION_CODES.M) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+120000,alarmIntent);
                Log.e("kitkatFB","hahah");
            }
            else if (SDK_INT >= Build.VERSION_CODES.M) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+120000,alarmIntent);
                Log.e("marshmallowFB","hahah");
            }
        }
    }
}
