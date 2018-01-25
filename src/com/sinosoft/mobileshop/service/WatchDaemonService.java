package com.sinosoft.mobileshop.service;

import android.app.*;
import android.content.*;
import android.os.*;

public class WatchDaemonService extends Service {

    private static final int sHashCode = WatchDaemonService.class.getName().hashCode();

    private static boolean sAlive;

    /**
     * 守护服务，运行在:watch子进程中
     */
    private int onStart(Intent intent, int flags, int startId) {
        startForeground(sHashCode, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startService(new Intent(this, WatchDogNotificationService.class));
        }

        if (sAlive) return START_STICKY;

        sAlive = true;

        //每 15 分钟检查一次VpnWorkService是否在运行，如果不在运行就把它拉起来
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, VpnWorkService.class);
        PendingIntent pi = PendingIntent.getService(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
//        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//                pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 2*60*1000,
                2*60*1000,
                pi);
        

        return START_STICKY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    private void onEnd(Intent rootIntent) {
        startService(new Intent(this, VpnWorkService.class));
        startService(new Intent(this, WatchDaemonService.class));
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
        onEnd(null);
    }

    public static class WatchDogNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         * 运行在:watch子进程中
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WatchDaemonService.sHashCode, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
