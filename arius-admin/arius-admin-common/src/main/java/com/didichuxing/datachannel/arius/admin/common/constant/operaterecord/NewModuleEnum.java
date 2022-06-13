package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;


/**
 * 操作记录模块枚举
 *
 * @author shizeying
 * @date 2022/06/13
 */
public enum NewModuleEnum {
    APPLICATION("应用","访问模式","新增访问模式:%s");
    
    /**
     * 模块
     */
    private String module;
    /**
     * 操作类型
     */
    private String operationType;
    /**
     * 操作内容
     */
    private String operatingContent;
    
    NewModuleEnum(String module, String operationType, String operatingContent) {
        this.module = module;
        this.operationType = operationType;
        this.operatingContent = operatingContent;
    }
    
    public void setOperatingContent(String operatingContent) {
        this.operatingContent = operatingContent;
    }
    
    public String getModule() {
        return module;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public String getOperatingContent() {
        return operatingContent;
    }
    
    /**
     * 获取操作内容
     *
     * @param moduleEnum 模块枚举
     * @param param      入参
     * @return {@code String}
     */
    public static String getOperatingContent(NewModuleEnum moduleEnum,String... param){
        final String content = moduleEnum.getOperatingContent();
        return  String.format(content, param);
    }
    
   
    
    
}