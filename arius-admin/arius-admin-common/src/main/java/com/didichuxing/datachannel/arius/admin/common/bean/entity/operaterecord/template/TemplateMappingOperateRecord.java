package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil.Diff;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMappingOperateRecord extends TemplateOperateRecord {
    
    /**
     * 旧的schema 设置
     */
    private JSONObject oldMapping;
    
    /**
     * 新的schema 设置
     */
    private JSONObject newMapping;
    
    public TemplateMappingOperateRecord(IndexMappingVO oldIndexMappingVO, IndexMappingVO newIndexMappingVO) {
        final String oldMappings = oldIndexMappingVO.getMappings();
        final String newMappings = newIndexMappingVO.getMappings();
        oldMapping = JSON.parseObject(oldMappings);
        newMapping = JSON.parseObject(newMappings);
        try {
            final List<Diff> diffs = DiffUtil.diffJson(oldMappings, newMappings);
            
            diffResult.addAll(diffs);
        } catch (Exception ignore) {
        }
        
        operateType = TemplateOperateRecordEnum.MAPPING.getCode();
    }
    
    
    
    public TemplateMappingOperateRecord(MappingConfig before, MappingConfig after) {
        oldMapping =before.toJson();
        newMapping = after.toJson();
        try {
            final List<Diff> diffs = DiffUtil.diffJson(before.toJson().toJSONString(), after.toJson().toJSONString());
            
            diffResult.addAll(diffs);
        } catch (Exception ignore) {
        }
        
        operateType = TemplateOperateRecordEnum.MAPPING.getCode();
    }
    
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}