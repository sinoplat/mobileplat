package com.sinosoft.traffic;

/**
 * 网络流量记录表
 * @author aaa
 *
 */
public class prpmFlowCount {
	/**
	 * 主键id
	 */
	private int id;
	/**
	 * 用户工号
	 */
	private String UserCode;
	
	/**
	 * 记录时间
	 */
	private String time;
	/**
	 * 网络速度(/s)
	 */
	private long rapidity;
	/**
	 * 应用使用的总流量
	 */
	private long totalNetWork;
	/**
	 * 经度
	 */
	private String Longitude;
	/**
	 * 纬度
	 */
	private String Latitude;
	/**
	 * 设备号
	 */
	private String imei;
	/**
	 * 平台
	 */
	private String os;
	/**
	 * 应用编号
	 */
	public String getLongitude() {
		return Longitude;
	}

	public void setLongitude(String longitude) {
		Longitude = longitude;
	}

	public String getLatitude() {
		return Latitude;
	}

	public void setLatitude(String latitude) {
		Latitude = latitude;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	
	private String applicationNo;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public long getRapidity() {
		return rapidity;
	}

	public void setRapidity(long rapidity) {
		this.rapidity = rapidity;
	}

	public long getTotalNetWork() {
		return totalNetWork;
	}

	public void setTotalNetWork(long totalNetWork) {
		this.totalNetWork = totalNetWork;
	}
	
	public String getUserCode() {
		return UserCode;
	}

	public void setUserCode(String userCode) {
		UserCode = userCode;
	}

}
