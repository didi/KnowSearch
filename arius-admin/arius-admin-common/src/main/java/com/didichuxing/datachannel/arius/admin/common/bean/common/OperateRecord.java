package com.didichuxing.datachannel.arius.admin.common.bean.common;

import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import java.util.Calendar;
import java.util.Date;
import lombok.Data;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/17
 */
@Data
public class OperateRecord {
	/**
	 * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.NewModuleEnum
	 */
	private Integer moduleId;
	
	/**
	 * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationTypeEnum
	 */
	private Integer operateId;
	
	/**
	 * 操作描述
	 */
	private String content;
	
	/**
	 * 操作人
	 */
	private String userOperation;
	
	/**
	 * 操作时间
	 */
	private Date    operateTime;
	/**
	 * 触发方式
	 *
	 * @see com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum
	 */
	private Integer triggerWayId;
	/**
	 * 应用id
	 */
	private String  projectName;
	/**
	 * 业务id
	 */
	private Integer bizId;
	
	public OperateRecord(String projectName, OperationTypeEnum operationTypeEnum, TriggerWayEnum triggerWayEnum,
	                     String content, String userOperation, Integer bizId) {
		this.moduleId = operationTypeEnum.getModule().getCode();
		this.operateId = operationTypeEnum.getCode();
		this.content = content;
		this.userOperation = userOperation;
		this.operateTime = Calendar.getInstance().getTime();
		this.triggerWayId = triggerWayEnum.getCode();
		this.projectName = projectName;
		this.bizId = bizId;
	}
	
	public OperateRecord(String projectName, OperationTypeEnum operationTypeEnum, TriggerWayEnum triggerWayEnum,
	                     String content, String userOperation) {
		this.moduleId = operationTypeEnum.getModule().getCode();
		this.operateId = operationTypeEnum.getCode();
		this.content = content;
		this.userOperation = userOperation;
		this.operateTime = Calendar.getInstance().getTime();
		this.triggerWayId = triggerWayEnum.getCode();
		this.projectName = projectName;
	}
	
	public OperateRecord(OperationTypeEnum operationTypeEnum, TriggerWayEnum triggerWayEnum, String content,
	                     String userOperation, Integer bizId) {
		this.moduleId = operationTypeEnum.getModule().getCode();
		this.operateId = operationTypeEnum.getCode();
		this.content = content;
		this.userOperation = userOperation;
		this.operateTime = Calendar.getInstance().getTime();
		this.triggerWayId = triggerWayEnum.getCode();
		this.bizId = bizId;
	}
	
	public OperateRecord(OperationTypeEnum operationTypeEnum, TriggerWayEnum triggerWayEnum, String content,
	                     String userOperation) {
		this.moduleId = operationTypeEnum.getModule().getCode();
		this.operateId = operationTypeEnum.getCode();
		this.content = content;
		this.userOperation = userOperation;
		this.operateTime = Calendar.getInstance().getTime();
		this.triggerWayId = triggerWayEnum.getCode();
	}
	
	public OperateRecord(OperationTypeEnum operationTypeEnum, String content, String userOperation) {
		this.moduleId = operationTypeEnum.getModule().getCode();
		this.operateId = operationTypeEnum.getCode();
		this.content = content;
		this.userOperation = userOperation;
		this.operateTime = Calendar.getInstance().getTime();
	}
	
	public OperateRecord(OperationTypeEnum operationTypeEnum, String content, String userOperation, Integer bizId) {
		this.moduleId = operationTypeEnum.getModule().getCode();
		this.operateId = operationTypeEnum.getCode();
		this.content = content;
		this.userOperation = userOperation;
		this.operateTime = Calendar.getInstance().getTime();
		this.bizId = bizId;
	}
	
}