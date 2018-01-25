package com.sinosoft.mobileshop.bean;

import java.util.ArrayList;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Mapping;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;
import com.litesuits.orm.db.enums.Relation;

@Table("AppVersionInfo")
public class AppVersionInfo extends BaseParameter {
	
	@PrimaryKey(AssignType.BY_MYSELF)
	@Column("ApplicationNo")
	private String ApplicationNo;

	@Column("ApplicationName")
	private String ApplicationName;

	@Column("ApplicationType")
	private String ApplicationType;

	@Column("ApplicationNewVersion")
	private String ApplicationNewVersion;

	@Column("ApplicationTag")
	private String ApplicationTag;
	
	@Column("ApplicationVersionContent")
	private String ApplicationVersionContent;
	
	@Column("ApplicationRefer")
	private String ApplicationRefer;
	
	@Column("PackageName")
	private String PackageName;

	@Mapping(Relation.OneToMany)
	private ArrayList<AppUploadFile> ReFileList;
	
	@Column("NewViewUpdateTime")
	private String NewViewUpdateTime;
	
	@Column("ApplicationStatus")
	private String ApplicationStatus;
	
	@Column("ApplicationLaunch")
	private String ApplicationLaunch;

	public String getApplicationNo() {
		return ApplicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		ApplicationNo = applicationNo;
	}

	public String getApplicationName() {
		return ApplicationName;
	}

	public void setApplicationName(String applicationName) {
		ApplicationName = applicationName;
	}

	public String getApplicationType() {
		return ApplicationType;
	}

	public void setApplicationType(String applicationType) {
		ApplicationType = applicationType;
	}

	public String getApplicationNewVersion() {
		return ApplicationNewVersion;
	}

	public void setApplicationNewVersion(String applicationNewVersion) {
		ApplicationNewVersion = applicationNewVersion;
	}

	public String getApplicationTag() {
		return ApplicationTag;
	}

	public void setApplicationTag(String applicationTag) {
		ApplicationTag = applicationTag;
	}

	public String getApplicationVersionContent() {
		return ApplicationVersionContent;
	}

	public void setApplicationVersionContent(String applicationVersionContent) {
		ApplicationVersionContent = applicationVersionContent;
	}

	public String getApplicationRefer() {
		return ApplicationRefer;
	}

	public void setApplicationRefer(String applicationRefer) {
		ApplicationRefer = applicationRefer;
	}

	public String getPackageName() {
		return PackageName;
	}

	public void setPackageName(String packageName) {
		PackageName = packageName;
	}

	public ArrayList<AppUploadFile> getReFileList() {
		return ReFileList;
	}

	public void setReFileList(ArrayList<AppUploadFile> reFileList) {
		ReFileList = reFileList;
	}

	public String getNewViewUpdateTime() {
		return NewViewUpdateTime;
	}

	public void setNewViewUpdateTime(String newViewUpdateTime) {
		NewViewUpdateTime = newViewUpdateTime;
	}

	public String getApplicationStatus() {
		return ApplicationStatus;
	}

	public void setApplicationStatus(String applicationStatus) {
		ApplicationStatus = applicationStatus;
	}

	public String getApplicationLaunch() {
		return ApplicationLaunch;
	}

	public void setApplicationLaunch(String applicationLaunch) {
		ApplicationLaunch = applicationLaunch;
	}
}
