package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;

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
     * @see ModuleEnum
     */
    private Integer moduleId;

    /**
     * @see OperateTypeEnum
     */
    private Integer operateId;

    /**
     * 操作描述
     */
    private String  content;

    /**
     * 操作人
     */
    private String  userOperation;

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
     * 项目id
     */
    private Integer  projectId;
    /**
     * 业务id
     */
    private String  bizId;
    /**
     * 用户操作的应用，即资源所属应用
     */
    private String  operateProjectName;

    public OperateRecord(String projectName, OperateTypeEnum operateTypeEnum, TriggerWayEnum triggerWayEnum,
                         String content, String userOperation, Object bizId, String operateProjectName) {
        this.moduleId = operateTypeEnum.getModule().getCode();
        this.operateId = operateTypeEnum.getCode();
        this.content = content;
        this.userOperation = userOperation;
        this.operateTime = Calendar.getInstance().getTime();
        this.triggerWayId = triggerWayEnum.getCode();
        this.projectName = projectName;
        this.bizId = Optional.ofNullable(bizId).map(String::valueOf).orElse(null);
        this.operateProjectName = operateProjectName;
    }

    public OperateRecord(String projectName, OperateTypeEnum operateTypeEnum, TriggerWayEnum triggerWayEnum,
                         String content, String userOperation, String operateProjectName) {
        this.moduleId = operateTypeEnum.getModule().getCode();
        this.operateId = operateTypeEnum.getCode();
        this.content = content;
        this.userOperation = userOperation;
        this.operateTime = Calendar.getInstance().getTime();
        this.triggerWayId = triggerWayEnum.getCode();
        this.projectName = projectName;
        this.operateProjectName = operateProjectName;
    }

    public OperateRecord(OperateTypeEnum operateTypeEnum, TriggerWayEnum triggerWayEnum, String content,
                         String userOperation, Object bizId) {
        this.moduleId = operateTypeEnum.getModule().getCode();
        this.operateId = operateTypeEnum.getCode();
        this.projectName="-";
        this.content = content;
        this.userOperation = userOperation;
        this.operateTime = Calendar.getInstance().getTime();
        this.triggerWayId = triggerWayEnum.getCode();
        this.bizId = Optional.ofNullable(bizId).map(String::valueOf).orElse(null);
    }

    public OperateRecord(OperateTypeEnum operateTypeEnum, TriggerWayEnum triggerWayEnum, String content,
                         String userOperation) {
        this.moduleId = operateTypeEnum.getModule().getCode();
        this.operateId = operateTypeEnum.getCode();
        this.content = content;
        this.userOperation = userOperation;
        this.operateTime = Calendar.getInstance().getTime();
        this.triggerWayId = triggerWayEnum.getCode();
    }

    public OperateRecord(OperateTypeEnum operateTypeEnum, String content, String userOperation) {
        this.moduleId = operateTypeEnum.getModule().getCode();
        this.operateId = operateTypeEnum.getCode();
        this.content = content;
        this.userOperation = userOperation;
        this.operateTime = Calendar.getInstance().getTime();
    }

    public OperateRecord(OperateTypeEnum operateTypeEnum, String content, String userOperation, Object bizId) {
        this.moduleId = operateTypeEnum.getModule().getCode();
        this.operateId = operateTypeEnum.getCode();
        this.content = content;
        this.userOperation = userOperation;
        this.operateTime = Calendar.getInstance().getTime();
        this.bizId = Optional.ofNullable(bizId).map(String::valueOf).orElse(null);
    }

    private OperateRecord(Builder builder) {
        Optional.ofNullable(builder.operateTypeEnum).map(OperateTypeEnum::getModule).map(ModuleEnum::getCode)
            .ifPresent(this::setModuleId);
        Optional.ofNullable(builder.operateTypeEnum).map(OperateTypeEnum::getCode).ifPresent(this::setOperateId);
        setContent(builder.content);
        setUserOperation(builder.userOperation);
        setOperateTime(Calendar.getInstance().getTime());
        Optional.ofNullable(builder.triggerWayEnum).map(TriggerWayEnum::getCode).ifPresent(this::setTriggerWayId);
        setProjectName(builder.projectName);
        setProjectId(builder.projectId);
        setBizId(builder.bizId);
    }

    public static final class Builder {
        private OperateTypeEnum operateTypeEnum;
        private TriggerWayEnum  triggerWayEnum;
        private String          content;
        private String          userOperation;

        private String          projectName;
        private String          bizId;
        private Integer         projectId;
        private String  operateProjectName;

        public Builder operationTypeEnum(OperateTypeEnum operationType) {
            this.operateTypeEnum = operationType;
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

        public Builder operateProject(ProjectBriefVO project) {
            Optional.ofNullable(project).ifPresent(projectBriefVO -> {
                this.operateProjectName = projectBriefVO.getProjectName();
            });
            return this;
        }

        public Builder operateProjectName(String operateProjectName) {
            this.operateProjectName = operateProjectName;
            return this;
        }

        public Builder project(ProjectBriefVO project) {
            Optional.ofNullable(project).ifPresent(projectBriefVO -> {
                this.projectName = projectBriefVO.getProjectName();
                this.projectId   = projectBriefVO.getId();
            });
            return this;
        }

        public Builder projectName(String project) {
            this.projectName = project;
            return this;
        }

        public Builder bizId(Object bizId) {
            this.bizId = Optional.ofNullable(bizId).map(String::valueOf).orElse(null);
            return this;
        }

        public OperateRecord build() {
            return new OperateRecord(this);
        }

        public OperateRecord buildDefaultManualTrigger() {
            this.triggerWayEnum = TriggerWayEnum.MANUAL_TRIGGER;
            return new OperateRecord(this);
        }

        public OperateRecord buildDefaultSystemTrigger() {
            this.triggerWayEnum = TriggerWayEnum.SYSTEM_TRIGGER;
            return new OperateRecord(this);
        }

        public OperateRecord buildDefaultSchedulingTasks() {
            this.triggerWayEnum = TriggerWayEnum.SCHEDULING_TASKS;
            return new OperateRecord(this);
        }

    }

}