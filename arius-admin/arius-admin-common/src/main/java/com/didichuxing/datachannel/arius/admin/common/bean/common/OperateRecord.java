package com.didichuxing.datachannel.arius.admin.common.bean.common;

import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.NewModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/17
 */
@Data
@NoArgsConstructor
public class OperateRecord {
	/**
	 * @see NewModuleEnum
	 */
	private Integer moduleId;
	
	/**
	 * @see OperationTypeEnum
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
	 * @see TriggerWayEnum
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
	
	private OperateRecord(Builder builder) {
		Optional.ofNullable(builder.operationTypeEnum).map(OperationTypeEnum::getModule).map(NewModuleEnum::getCode)
				.ifPresent(this::setModuleId);
		Optional.ofNullable(builder.operationTypeEnum).map(OperationTypeEnum::getCode).ifPresent(this::setOperateId);
		setContent(builder.content);
		setUserOperation(builder.userOperation);
		setOperateTime(Calendar.getInstance().getTime());
		Optional.ofNullable(builder.triggerWayEnum).map(TriggerWayEnum::getCode).ifPresent(this::setTriggerWayId);
		setProjectName(builder.projectName);
		setBizId(builder.bizId);
	}
	
	public static final class Builder {
		private OperationTypeEnum operationTypeEnum;
		private TriggerWayEnum    triggerWayEnum;
		private String            content;
		private String            userOperation;
		
		private String  projectName;
		private Integer bizId;
		
		public Builder operationTypeEnum(OperationTypeEnum operationType) {
			this.operationTypeEnum = operationType;
			return this;
		}
		
		public Builder triggerWayEnum(TriggerWayEnum triggerWay) {
			triggerWayEnum = triggerWay;
			return this;
		}
		
		public Builder content(String content) {
			this.content = content;
			return this;
		}
		
		public Builder userOperation(String operation) {
			userOperation = operation;
			return this;
		}
		
		public Builder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}
		
		public Builder bizId(Integer bizId) {
			this.bizId = bizId;
			return this;
		}
		
		public OperateRecord build() {
			return new OperateRecord(this);
		}
		
	}
	

	
}