package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TemplateOperateRecordEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil.DiffJson;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;

import lombok.Data;

/**
 * @author liuchengxiang
 * @date 2022/2/17
 */
@Data
public class TemplateMappingOperateRecord extends TemplateOperateRecord {

    public TemplateMappingOperateRecord(IndexMappingVO oldIndexMappingVO, IndexMappingVO newIndexMappingVO) {
        final String oldMappings = oldIndexMappingVO.getMappings();
        final String newMappings = newIndexMappingVO.getMappings();
        source = JSON.parseObject(oldMappings);
        target = JSON.parseObject(newMappings);
        final List<DiffJson> diffs = DiffUtil.diffJsonByString(oldMappings, newMappings);
        diffResult.addAll(diffs);
        operateType = TemplateOperateRecordEnum.MAPPING.getCode();
        change = CollectionUtils.isNotEmpty(diffs);
        this.behavior = "编辑MAPPING";
    }

    public TemplateMappingOperateRecord(MappingConfig before, MappingConfig after) {
        source = before.toJson();
        target = after.toJson();
        final List<DiffJson> diffs = DiffUtil.diffJsonByString(before.toJson().toJSONString(),
            after.toJson().toJSONString());

        diffResult.addAll(diffs);
        operateType = TemplateOperateRecordEnum.MAPPING.getCode();
        change = CollectionUtils.isNotEmpty(diffs);
        this.behavior = "编辑MAPPING";
    }
    public TemplateMappingOperateRecord(JSONObject before, MappingConfig after) {
        source = before;
        target = after.toJson();
        final List<DiffJson> diffs = DiffUtil.diffJsonByString(before.toJSONString(),
            after.toJson().toJSONString());

        diffResult.addAll(diffs);
        operateType = TemplateOperateRecordEnum.MAPPING.getCode();
        change = CollectionUtils.isNotEmpty(diffs);
        this.behavior = "编辑MAPPING";
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}