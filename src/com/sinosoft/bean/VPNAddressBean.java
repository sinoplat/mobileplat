package com.sinosoft.bean;
/**
 * VPN 地址Bean
 * 
 */
public class VPNAddressBean {
	/**
	 * VPN flag id;
	 */
	private Integer id;
	/**
	 * xmpp地址
	 */
	private String xmpp;
	/**
	 * ftp地址
	 */
	private String ftp;
	/**
	 * 网络地址
	 */
	private String network;
	
	public VPNAddressBean() {
		super();
	}
	
	public VPNAddressBean(Integer id,String xmpp, String ftp,String network) {
		super();
		this.id = id;
		this.xmpp = xmpp;
		this.ftp = ftp;
		this.network = network;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getXmpp() {
		return xmpp;
	}
	public void setXmpp(String xmpp) {
		this.xmpp = xmpp;
	}
	public String getFtp() {
		return ftp;
	}
	public void setFtp(String ftp) {
		this.ftp = ftp;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
	

}
