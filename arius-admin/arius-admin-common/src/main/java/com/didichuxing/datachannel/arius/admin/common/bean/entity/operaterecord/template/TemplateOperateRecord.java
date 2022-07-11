package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil.Diff;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateOperateRecord {
    
    /**
     * 模板操作类型，如mapping, setting, etc
     */
    protected Integer    operateType;
    protected   List<Diff> diffResult = Lists.newArrayList();
    
}