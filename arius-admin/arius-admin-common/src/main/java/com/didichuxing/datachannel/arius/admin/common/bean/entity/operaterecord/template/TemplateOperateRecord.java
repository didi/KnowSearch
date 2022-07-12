package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil.DiffJson;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateOperateRecord {
    
    /**
     * 模板操作类型，如mapping, setting, etc
     */
    protected Integer    operateType;
    /**
     * 旧的schema 设置
     */
    protected   JSONObject source;
    
    /**
     * 新的schema 设置
     */
    protected JSONObject target;
    protected   List<DiffJson> diffResult = Lists.newArrayList();
    
    protected Boolean change;
    
}