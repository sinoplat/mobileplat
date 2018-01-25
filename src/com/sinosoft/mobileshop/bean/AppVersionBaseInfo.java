package com.sinosoft.mobileshop.bean;

public class AppVersionBaseInfo extends BaseParameter {
	private String checkFlag;
	private String newCurrentVersion;
	private String fileName;
	private String filePath;
	private String downLoadUrl;

	public String getCheckFlag() {
		return checkFlag;
	}

	public void setCheckFlag(String checkFlag) {
		this.checkFlag = checkFlag;
	}

	public String getNewCurrentVersion() {
		return newCurrentVersion;
	}

	public void setNewCurrentVersion(String newCurrentVersion) {
		this.newCurrentVersion = newCurrentVersion;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDownLoadUrl() {
		return downLoadUrl;
	}

	public void setDownLoadUrl(String downLoadUrl) {
		this.downLoadUrl = downLoadUrl;
	}

}
