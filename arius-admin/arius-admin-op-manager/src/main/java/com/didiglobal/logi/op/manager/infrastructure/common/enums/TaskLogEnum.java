package com.didiglobal.logi.op.manager.infrastructure.common.enums;


public enum TaskLogEnum {
   STDOUT(0, "标准输出"),

   STDERR(1, "错误输出"),

   UN_KNOW(-1, "未知");

   private int type;
   private String describe;

   TaskLogEnum(int type, String describe) {
      this.type = type;
      this.describe = describe;
   }

   public int getType() {
      return type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getDescribe() {
      return describe;
   }

   public void setDescribe(String describe) {
      this.describe = describe;
   }

   public static TaskLogEnum valueOfType(int type) {
      for (TaskLogEnum taskLogEnum : TaskLogEnum.values()) {
         if (type == taskLogEnum.getType()) {
            return taskLogEnum;
         }
      }

      return UN_KNOW;
   }

}
