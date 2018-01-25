package com.sinosoft.phoneGapPlugins.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.vpn.VPNAddress;

/**
 * 维护天融信VPN数据
 */
public class VpnAddressIp {
	
	private Context context;
	public SharedPreferences sp;
	private int VPNflag;
	private String networkaddress;
	
	public VpnAddressIp(Context context){
		this.context =context;
	}
	
	public String VPNAddress() {
		sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		VPNflag = sp.getInt("VPNFlag", 1);
		VPNAddress vpnaddress = new VPNAddress(context);
		if (VPNflag == 1) {
			VPNAddressBean bean = vpnaddress.queryVPN(1);
			networkaddress = bean.getNetwork();
		} 
		if(VPNflag == 2){
			VPNAddressBean bean = vpnaddress.queryVPN(2);
			networkaddress = bean.getNetwork();
		}
		return networkaddress;
	}
	
	 
}
