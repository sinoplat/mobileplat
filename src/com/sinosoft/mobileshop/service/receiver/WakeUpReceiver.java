package com.sinosoft.mobileshop.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sinosoft.mobileshop.service.VpnWorkService;

public class WakeUpReceiver extends BroadcastReceiver {

    /**
     * 监听 3 种系统广播 : BOOT_COMPLETED, CONNECTIVITY_CHANGE, USER_PRESENT
     * 在系统启动完成、网络连接改变、用户屏幕解锁时拉起 Service
     * Service 内部做了判断，若 Service 已在运行，不会重复启动
     * 运行在:watch子进程中
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, VpnWorkService.class));
    }

    public static class WakeUpAutoStartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, VpnWorkService.class));
        }
    }
}
