package com.sinosoft.mobileshop.bean;

import java.io.Serializable;

public class BaseParameter implements Serializable{
	private String ResultCode;
	private String Desc;

	public String getResultCode() {
		return ResultCode;
	}

	public void setResultCode(String resultCode) {
		ResultCode = resultCode;
	}

	public String getDesc() {
		return Desc;
	}

	public void setDesc(String desc) {
		Desc = desc;
	}

}
