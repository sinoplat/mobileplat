package com.sinosoft.mobileshop.bean;

import java.io.Serializable;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;


@Table("AppMessage")
public class AppMessage implements Serializable{

	/** 属性消息ID */
	@PrimaryKey(AssignType.BY_MYSELF)
	@Column("messageID")
	private String messageID;

	/** 属性消息标题 */
	@Column("messageTitle")
	private String messageTitle;

	/** 属性消息内容 */
	@Column("messageContent")
	private String messageContent;

	/** 属性机构 */
	@Column("comCode")
	private String comCode;

	/** 属性消息有效期 */
	@Column("validDate")
	private String validDate;

	/** 属性消息发布人 */
	@Column("operateCode")
	private String operateCode;

	/** 属性消息发布时间 */
	@Column("operateDate")
	private String operateDate;

	/** 属性消息状态 */
	@Column("messageStatus")
	private String messageStatus;

	/** 属性有效标志 */
	@Column("validStatus")
	private String validStatus;

	/** 属性备注 */
	@Column("remark")
	private String remark;
	
	/** 接受人标识 */
	@Column("receiveUserFlag")
	private String receiveUserFlag;
	
	/** 接受人 */
	@Column("userGroup")
	private String userGroup;
	
	@Column("readFlag")
	private String readFlag;

	/**
	 * 类prpMmessage的默认构造方法
	 */
	public AppMessage() {
	}

	public String getMessageID() {
		return this.messageID;
	}

	/**
	 * 属性消息ID的setter方法
	 */
	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	/**
	 * 属性消息标题的getter方法
	 */

	public String getMessageTitle() {
		return this.messageTitle;
	}

	/**
	 * 属性消息标题的setter方法
	 */
	public void setMessageTitle(String messageTitle) {
		this.messageTitle = messageTitle;
	}

	/**
	 * 属性消息内容的getter方法
	 */
	public String getMessageContent() {
		return this.messageContent;
	}

	/**
	 * 属性消息内容的setter方法
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	/**
	 * 属性机构的getter方法
	 */

	public String getComCode() {
		return this.comCode;
	}

	/**
	 * 属性机构的setter方法
	 */
	public void setComCode(String comCode) {
		this.comCode = comCode;
	}

	/**
	 * 属性消息有效期的getter方法
	 */
	public String getValidDate() {
		return this.validDate;
	}

	/**
	 * 属性消息有效期的setter方法
	 */
	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}

	/**
	 * 属性消息发布人的getter方法
	 */
	public String getOperateCode() {
		return this.operateCode;
	}

	/**
	 * 属性消息发布人的setter方法
	 */
	public void setOperateCode(String operateCode) {
		this.operateCode = operateCode;
	}

	/**
	 * 属性消息发布时间的getter方法
	 */
	public String getOperateDate() {
		return this.operateDate;
	}

	/**
	 * 属性消息发布时间的setter方法
	 */
	public void setOperateDate(String operateDate) {
		this.operateDate = operateDate;
	}

	/**
	 * 属性消息状态的getter方法
	 */
	public String getMessageStatus() {
		return this.messageStatus;
	}

	/**
	 * 属性消息状态的setter方法
	 */
	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}

	/**
	 * 属性有效标志的getter方法
	 */
	public String getValidStatus() {
		return this.validStatus;
	}

	/**
	 * 属性有效标志的setter方法
	 */
	public void setValidStatus(String validStatus) {
		this.validStatus = validStatus;
	}

	/**
	 * 属性备注的getter方法
	 */
	public String getRemark() {
		return this.remark;
	}

	/**
	 * 属性备注的setter方法
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getReceiveUserFlag() {
		return receiveUserFlag;
	}

	public void setReceiveUserFlag(String receiveUserFlag) {
		this.receiveUserFlag = receiveUserFlag;
	}

	public String getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}

	public String getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(String readFlag) {
		this.readFlag = readFlag;
	}
}
