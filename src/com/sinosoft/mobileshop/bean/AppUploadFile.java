package com.sinosoft.mobileshop.bean;

import java.io.Serializable;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;


@Table("AppUploadFile")
public class AppUploadFile implements Serializable{

	@PrimaryKey(AssignType.AUTO_INCREMENT)
    private long Id;

	@Column("SerialNo")
	private String SerialNo;
	@Column("ApplicationNo")
	private String ApplicationNo;
	@Column("ApplicationVersion")
	private String ApplicationVersion;
	@Column("ApplicationName")
	private String ApplicationName;
	@Column("FileName")
	private String FileName;
	@Column("FilePath")
	private String FilePath;
	@Column("FileType")
	private String FileType;

	public long getId() {
		return Id;
	}

	public void setId(long id) {
		Id = id;
	}

	public String getApplicationNo() {
		return ApplicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		ApplicationNo = applicationNo;
	}

	public String getApplicationVersion() {
		return ApplicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		ApplicationVersion = applicationVersion;
	}

	public String getSerialNo() {
		return SerialNo;
	}

	public void setSerialNo(String serialNo) {
		SerialNo = serialNo;
	}

	public String getApplicationName() {
		return ApplicationName;
	}

	public void setApplicationName(String applicationName) {
		ApplicationName = applicationName;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getFilePath() {
		return FilePath;
	}

	public void setFilePath(String filePath) {
		FilePath = filePath;
	}

	public String getFileType() {
		return FileType;
	}

	public void setFileType(String fileType) {
		FileType = fileType;
	}

}
