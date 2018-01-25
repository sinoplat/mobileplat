package com.sinosoft.traffic;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.sinosoft.mobileshop.bean.VpnBean;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;
import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.format.Formatter;

public class TrafficService extends Service {
	private DecimalFormat format;
	private LocationManager locationManager;
	private long total;
	private Timer timer;
	private TimerTask timertask;
	private long lasttotal;
	private int i = 1;
	private String provider;
	public String latStr;
	public String lonStr;
	public String uuid;
	private long received;
	private long transmitted;
	private String apptotal;
//	private NetWorkSQL netsql;
	private String last;
	private JSONObject obj;
	private String user;
	private double lat;
	private double lon;
	private String location;
	private Double last1;
	private Double apptotal1;
	private String apptotal3;
	private String last2;
	public SharedPreferences sp;
	private String networkaddress;
	private int lastid;
	private long tolast;
	private long tot;
	private List<String> list1;
	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
//		netsql = new NetWorkSQL(context);
		networkaddress = Constant.NETURL;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		format = new DecimalFormat("#.000000");
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(false);
		provider = locationManager.getBestProvider(criteria, false);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
				0, new NetWorkLocationListener());
		initialComponment();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void initialComponment() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(false);
		provider = locationManager.getBestProvider(criteria, false);
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, new GPSLocationListener());
		}

		timer = new Timer();
		timertask = new TimerTask() {
			public void run() {
				if (lat != 0) {
					HttpLocation http = new HttpLocation();
					location = http.httpClient(lat, lon);
				}
//				 if(lastid==0){
//					 ++lastid;
//				 }
//				 ++lastid;
//				total = getTotalRxBytes() + getTotalTxBytes();
//				lasttotal = total;
//				if (count != 0) {
//					list1 = netsql.lastTotalNetWork();
//					long lasttotal = Long.parseLong(list1.get(0));
//					tot = total - lasttotal;
//				}
//				String velocity = TextFormater.dataSizeFormat(tot/15);
				String velocity = TextFormater.dataSizeFormat(tot/15);
//				System.out.println("速度："+velocity);
				String time = formatter.format(new Date());
				String nowtime = time;
//				System.out.println(time);
//				
//				if (count > 100) {
//					String times = netsql.getTime();
//					netsql.delete(times);
//				}
				try {
					// 拿到一个包管理器
					PackageManager packageManager = getPackageManager();
					String packageName = "com.sinosoft.gyicPlat";
					int uid = 0;
					// 得到应用的packageInfo对象
					PackageInfo packageInfo;
					packageInfo = packageManager.getPackageInfo(packageName, 0);
					// 得到这个应用对应的uid
					uid = packageInfo.applicationInfo.uid;
					received = TrafficStats.getUidRxBytes(uid);
					transmitted = TrafficStats.getUidTxBytes(uid);
					long total = received + transmitted;
					tolast = total;
					System.out.println("total流量:"+total);
					if (total < 0) {
						total = 0;
					}
					uuid = Settings.Secure.getString(getContentResolver(),
							android.provider.Settings.Secure.ANDROID_ID);
//					if (count != 0) {
//						long l = Long.parseLong(list1.get(1));
//						 long totalmix =  total-l;
//						 if(totalmix<0){
//							 totalmix=0;
//						 }
//						 apptotal = TextFormater.dataSizeFormat(totalmix);
//						System.out.println("last数据库中最后一条数据："+l+"上传使用流量："+apptotal);
////						String[] tras1 = last.split("[a-zA-Z]+");
////						if (tras1 != null && tras1.length > 0) {
////							last2 = tras1[0];
////							System.out.println("last2"+last2);
////						}
////						last1 = Double.parseDouble(last2);				 
//					}
//					apptotal = TextFormater.dataSizeFormat(total);
//					System.out.println("apptotal总流量使用统计："+apptotal);
//					if (last1 != null) {
//						String[] tras = apptotal.split("[a-zA-Z]+");
//						if (tras != null && tras.length > 0) {
//							apptotal = tras[0];
//							System.out.println("apptotal:"+apptotal);
//							apptotal1 = Double.parseDouble(apptotal);
//							Double apptotal2 = apptotal1 - last1;
//							System.out.println("apptotal1:"+apptotal1+"last1"+last1);
//							apptotal3 = apptotal2 + "0KB";
//							System.out.println("apptotal2:"+apptotal2);
//							System.out.println("apptotal3:"+apptotal3);
//						}
//					} else {
//						apptotal3 = "0KB";
//					}
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

//				user = getuser.getUsercode();
				// System.out.println(user);
				user = "0000000000";
//				if (i!= 1) {
//					prpmFlowCount flow = new prpmFlowCount();
//					flow.setId(lastid);
//					flow.setTime(nowtime);
//					flow.setRapidity(lasttotal);
//					flow.setTotalNetWork(tolast);
//					netsql.intert(flow);
//				}

				try {
					obj = new JSONObject();
					obj.put("UserCode", user);
					obj.put("ApplicationNo", "APP20140911110645");
					obj.put("IMEI", uuid);
					obj.put("OS", "1");
					obj.put("LocateTime", nowtime);
					obj.put("Longitude", lonStr);
					obj.put("Latitude", latStr);
					obj.put("Locate", location);
					obj.put("RunningSpeed", velocity);
					obj.put("TrafficStatistics", apptotal);

				} catch (JSONException e) {
					e.printStackTrace();
				}

				TrafficHttp ht = new TrafficHttp();
				String url = "http://" + networkaddress
						+ "/meap/service/savaRealtimeMonitor.do?";
				if (i != 1) {
					ht.httpClient(obj, url);
				}
				i++;
				
			}

		};
		timer.schedule(timertask, 0, 20000);
	}

	public long getTotalRxBytes() { // 获取总的接受字节数，包含Mobile和WiFi等
		return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getTotalRxBytes());
	}

	public long getTotalTxBytes() { // 总的发送字节数，包含Mobile和WiFi等
		return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getTotalTxBytes());
	}

	private class GPSLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			lat = location.getLatitude();
			lon = location.getLongitude();
			latStr = format.format(lat);
			lonStr = format.format(lon);
			System.out.println("经度：" + latStr + "纬度：" + lonStr);
			try {
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	private class NetWorkLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			lat = location.getLatitude();
			lon = location.getLongitude();
			latStr = format.format(lat);
			lonStr = format.format(lon);
			System.out.println("经度：" + latStr + "纬度：" + lonStr);
			try {
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void onProviderDisabled(String provider) {
		}
		@Override
		public void onProviderEnabled(String provider) {
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		timertask.cancel();
	}

}
