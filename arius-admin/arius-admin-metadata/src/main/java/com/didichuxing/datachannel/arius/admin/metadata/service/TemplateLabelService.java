package com.didichuxing.datachannel.arius.admin.metadata.service;


import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Label;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateLabel;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.UpdateIndexTemplateLabelParam;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateLabelESDAO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 索引模板标签服务
 * <p>
 * Created by d06679 on 2017/10/9.
 */
@Service
public class TemplateLabelService {

    private static final ILog LOGGER = LogFactory.getLog( TemplateLabelService.class);

    public static final String TEMPLATE_HAS_DELETED_DOC   = "21140";
    public static final String TEMPLATE_DSL_REVIEW_LABEL  = "11101";
    public static final String TEMPLATE_IMPORTANT_LABEL   = "11102";
    public static final String TEMPLATE_QUOTA_USAGE_LOW   = "32220";
    public static final String TEMPLATE_QUOTA_USAGE_HIGH  = "32321";
    public static final String TEMPLATE_FULL_LINK_SUPPORT = "12103";
    public static final String TEMPLATE_EXPIRE_TIME_LONG  = "32222";
    public static final String TEMPLATE_INVALID           = "22348";
    public static final String TEMPLATE_SUSPEND_GOVERN    = "12104";
    public static final String TEMPLATE_ERROR_QUERY       = "22243";
    public static final String TEMPLATE_SLOW_QUERY        = "22244";
    public static final String TEMPLATE_NO_DATA           = "22345";
    public static final String TEMPLATE_NO_QUERY          = "22347";
    public static final String TEMPLATE_HAVE_DCDR         = "12149";


    @Autowired
    private TemplateLabelESDAO indexTemplateLabelDAO;

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    private OperateRecordService operateRecordService;

    public Result<List<Label>> listAllLabel() throws AmsRemoteException {
        List<TemplateLabelPO> templateLabelPOS = indexTemplateLabelDAO.listAll();

        return Result.buildSucc(ConvertUtil.list2List(templateLabelPOS, Label.class));
    }

    /**
     * 模板是否有删除操作
     * @param logicId
     * @return
     */
    public boolean hasDeleteDoc(Integer logicId) {
        Map<String, TemplateLabelPO> labelMap = ConvertUtil.list2Map( listIndexTemplateLabelPO(logicId), TemplateLabelPO::getLabelId);
        return labelMap.containsKey(TEMPLATE_HAS_DELETED_DOC);
    }

    /**
     * 是否是重要索引
     * @param logicId
     * @return
     */
    public boolean isImportantIndex(Integer logicId) {
        Map<String, TemplateLabelPO> labelMap = ConvertUtil.list2Map( listIndexTemplateLabelPO(logicId), TemplateLabelPO::getLabelId);
        return labelMap.containsKey(TEMPLATE_IMPORTANT_LABEL);
    }

    /**
     *
     * @param logicId 逻辑模板id
     * @return map
     * @throws AmsRemoteException
     */
    public List<Label> listTemplateLabel(Integer logicId) throws AmsRemoteException {
        List<TemplateLabelPO> templateLabelPOS = listIndexTemplateLabelPO(logicId);

        return ConvertUtil.list2List(templateLabelPOS, Label.class);
    }

    /**
     * 根据表达式获取模板id
     * @param includeLabelIds
     * @return list
     */
    public Result<List<TemplateLabel>> listByLabelIds(String includeLabelIds, String excludeLabelIds) {
        if (includeLabelIds == null) {
            includeLabelIds = "";
        }
        if (excludeLabelIds == null) {
            excludeLabelIds = "";
        }

        Map<Integer/*indexTemplateId*/, Collection<TemplateLabelPO>> integerCollectionMap =
                listAllIndexTemplateLabelByLabelIds(includeLabelIds, excludeLabelIds);

        List<TemplateLabel> templateLabels = new ArrayList<>();
        for (Map.Entry<Integer, Collection<TemplateLabelPO>> entry : integerCollectionMap.entrySet()) {
            TemplateLabel temp = new TemplateLabel();
            temp.setIndexTemplateId(entry.getKey());
            temp.setLabels(ConvertUtil.list2List(new ArrayList<>(entry.getValue()), Label.class));

            templateLabels.add(temp);
        }

        return Result.build(true, templateLabels);
    }


    private Result<List<TemplateLabel>> listByLabelId(String labelId){
        List<TemplateLabelPO> templateLabelPOS = listPOByLabelId(labelId);

        List<TemplateLabel> templateLabels = new ArrayList<>();
        for(TemplateLabelPO templateLabelPO : templateLabelPOS){
            TemplateLabel templateLabel = new TemplateLabel();
            templateLabel.setIndexTemplateId(templateLabelPO.getIndexTemplateId());
            templateLabels.add(templateLabel);
        }

        return Result.buildSucc(templateLabels);
    }

    /**
     * 根据表达式获取模板id
     *
     * @return list
     */
    public Result<List<TemplateLabel>> listDslReviewTemplates() {
        return listByLabelId(TEMPLATE_DSL_REVIEW_LABEL);
    }

    /**
     * 根据表达式获取模板id
     *
     * @return list
     */
    public Result<List<TemplateLabel>> listInvalidTemplates() {
        return listByLabelId(TEMPLATE_INVALID);
    }

    public Result<List<TemplateLabel>> listHaveDcdrTemplates() {
        return listByLabelId(TEMPLATE_HAVE_DCDR);
    }

    /**
     * 保存或者更新模板的标签
     *
     * @param logicId 逻辑模板id
     * @param labelIds  标签列表
     * @return true/false
     */
    public Result<Boolean> replaceTemplateLabel(Integer logicId, List<String> labelIds, String operator) {

        // 构造ams需要的数据结构
        List<Label> labels = Lists.newArrayList();
        for (String labelId : labelIds) {
            Label label = new Label();
            label.setLabelId(labelId);
            labels.add(label);
        }

        UpdateIndexTemplateLabelParam param = new UpdateIndexTemplateLabelParam();
        param.setTemplateId(logicId);
        param.setIndexTemplateLabels(labels);
        param.setOperator(operator);

        return Result.buildBoolen(updateIndexTemplateLabel(param));
    }

    /**
     * 更新没不安标签
     *
     * @param logicId    逻辑模板
     * @param shouldAdds 需要添加的
     * @param shouldDels 需要删除的
     * @param operator   operator
     * @return result
     */
    public Result<Boolean> updateTemplateLabel(Integer logicId, Set<String> shouldAdds, Set<String> shouldDels,
                                      String operator) {
        List<Label> allLabels = listTemplateLabel(logicId);

        Set<String> oldLabelIdSet = allLabels.stream().map(Label::getLabelId).collect(Collectors.toSet());

        Set<String> newLabelIdSet = Sets.newHashSet(oldLabelIdSet);
        if (CollectionUtils.isNotEmpty(shouldDels)) {
            newLabelIdSet.removeAll(shouldDels);
        }

        if (CollectionUtils.isNotEmpty(shouldAdds)) {
            newLabelIdSet.addAll(shouldAdds);
        }

        if (oldLabelIdSet.containsAll(newLabelIdSet) && newLabelIdSet.size() == oldLabelIdSet.size()) {
            return Result.buildSucc(true);
        }

        return replaceTemplateLabel(logicId, Lists.newArrayList(newLabelIdSet), operator);
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<TemplateLabelPO> list) {
        Date now = new Date();
        list.forEach(labelPO -> labelPO.setMarkTime(now));
        return indexTemplateLabelDAO.batchInsert(list);
    }

    /**
     * 批量删除
     *
     * @return
     */
    public boolean batchDelete(List<String> docIds) {
        return indexTemplateLabelDAO.batchDelete(docIds);
    }

    /**
     * 获取指定索引模板的所有标签
     *
     * @param logicTemplateId
     * @return
     */
    public List<TemplateLabelPO> listIndexTemplateLabelPO(Integer logicTemplateId) {
        return indexTemplateLabelDAO.getLabelByLogicTemplateId(logicTemplateId);
    }

    /**
     * 根据标签ID获取标签
     *
     * @param labelId
     * @return
     */
    public List<TemplateLabelPO> listPOByLabelId(String labelId) {
        return indexTemplateLabelDAO.getLabelByLabelId(labelId);
    }

    /**
     * 根据标签ID获取标签
     *
     * @return
     */
    public List<TemplateLabelPO> listAllPO() {
        return indexTemplateLabelDAO.listAll();
    }

    /**
     * 根据条件查询
     *
     * @param includeLabelIds 包含的标签
     * @param excludeLabelIds 不能包含的标签
     * @return map
     */
    public Map<Integer/*indexTemplateId*/, Collection<TemplateLabelPO>> listAllIndexTemplateLabelByLabelIds(String includeLabelIds, String excludeLabelIds) {
        Map<Integer/*indexTemplateId*/, Collection<TemplateLabelPO>> retMap = new ConcurrentHashMap<>();

        List<IndexTemplateInfo> templateLogics = indexTemplateInfoService.getAllLogicTemplates();

        templateLogics.parallelStream().forEach(indexTemplate -> {
            Integer templateId = indexTemplate.getId();
            List<TemplateLabelPO> templateLabelPOS = listIndexTemplateLabelPO(templateId);

            LOGGER.info("class=IndexTemplateLabelService||method=listAllIndexTemplateLabelByLabelIds||indexTemplates={}||templateLabelPOS={}",
                    templateId, JSON.toJSONString( templateLabelPOS ));

            if (!CollectionUtils.isEmpty( templateLabelPOS )) {
                List<String> labelIds = templateLabelPOS.stream().map( s -> String.valueOf(s.getLabelId())).collect(Collectors.toList());

                if (isIndexTemplateLabelsMatch(labelIds, includeLabelIds, excludeLabelIds)) {
                    retMap.put(templateId, templateLabelPOS );
                }
            }
        });

        return retMap;
    }

    /**
     * 修改指定索引模板的标签
     * 全量覆盖的方式
     *
     * @param param
     * @return
     */
    public boolean updateIndexTemplateLabel(UpdateIndexTemplateLabelParam param) {
        int indexTemplateId = param.getTemplateId();
        String operator = param.getOperator();

        List<Label> labels = param.getIndexTemplateLabels();
        if (labels == null) {
            labels = Lists.newArrayList();
        }

        List<TemplateLabelPO> indexTemplateLabels = ConvertUtil.list2List(labels, TemplateLabelPO.class);

        if (!checkAndFillLabels(indexTemplateId, indexTemplateLabels)) {
            return false;
        }

        // 获取这个索引模板已有的标签
        List<TemplateLabelPO> deleted = listIndexTemplateLabelPO(indexTemplateId);
        Map<String, TemplateLabelPO> deleteMap = Maps.newHashMap();
        for (TemplateLabelPO po : deleted) {
            deleteMap.put(po.getLabelId(), po);
        }

        List<TemplateLabelPO> news = Lists.newArrayList();

        for (TemplateLabelPO po : indexTemplateLabels) {
            if (deleteMap.containsKey(po.getLabelId())) {
                LOGGER.info("class=IndexTemplateLabelService||method=updateIndexTemplateLabel||msg=label has EXIST. label: {}", po);
                deleteMap.remove(po.getLabelId());
            } else {
                LOGGER.info("class=IndexTemplateLabelService||method=updateIndexTemplateLabel||msg=label is NEW. label: {}", po);
                news.add(po);
            }
        }

        List<String> deleteIds = Lists.newArrayList();
        for (Map.Entry<String, TemplateLabelPO> entry : deleteMap.entrySet()) {
            deleteIds.add(entry.getValue().getId());
        }

        boolean change = false;
        if (CollectionUtils.isEmpty(deleteIds)) {
            LOGGER.info("class=IndexTemplateLabelService||method=updateIndexTemplateLabel||msg=no label need delete.");
        } else {
            change = true;
            boolean result = batchDelete(deleteIds);
            if (!result) {
                LOGGER.error("class=IndexTemplateLabelService||method=updateIndexTemplateLabel||errMsg=fail to delete label {}", deleteIds);
                return false;
            }
        }

        if (CollectionUtils.isEmpty(news)) {
            LOGGER.info("class=IndexTemplateLabelService||method=updateIndexTemplateLabel||msg=no label need insert.");
        } else {
            change = true;
            boolean result = batchInsert(news);
            if (!result) {
                LOGGER.error("class=IndexTemplateLabelService||method=updateIndexTemplateLabel||errMsg=fail to add label {}", news);
                return false;
            }
        }

        //记录操作记录
        if (change) {
            OperateRecordDTO operateRecord = buildLabelSettingOperatorRecord(
                    String.valueOf(indexTemplateId), OperationEnum.EDIT_LABELS.getCode(), operator,
                    getEditContent(deleteMap.values(), news)
            );
            operateRecordService.save(operateRecord);
        }

        return true;
    }

    /**************************************** private methods ****************************************/
    /**
     * 获取修改的内容
     *
     * @param deletes
     * @param news
     * @return
     */
    private String getEditContent(Collection<TemplateLabelPO> deletes, List<TemplateLabelPO> news) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (CollectionUtils.isNotEmpty(deletes)) {
            stringBuilder.append("删除标签:");
            for (TemplateLabelPO po : deletes) {
                stringBuilder.append(po.getLabelName()).append(",");
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), ";");
        }

        if (CollectionUtils.isNotEmpty(news)) {
            stringBuilder.append("增加标签:");
            for (TemplateLabelPO po : news) {
                stringBuilder.append(po.getLabelName()).append(",");
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), ";");
        }

        return stringBuilder.toString();
    }

    private boolean isIndexTemplateLabelsMatch(List<String> labelIds, String includeLabelIds, String excludeLabelIds) {
        if (StringUtils.isNotBlank(includeLabelIds)
            && !labelIds.containsAll(Arrays.asList(includeLabelIds.split(",")))) {
                return false;
        }

        if (StringUtils.isNotBlank(excludeLabelIds)) {
            for (String excludeLabelId : excludeLabelIds.split(",")) {
                if (labelIds.contains(excludeLabelId)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkAndFillLabels(int indexTemplateId, List<TemplateLabelPO> templateLabelPOS) {
        for (TemplateLabelPO labelPO : templateLabelPOS) {
            TemplateLabelEnum labelEnum = TemplateLabelEnum.valueBy(labelPO.getLabelId());
            if (labelEnum == null) {
                return false;
            }

            labelPO.setIndexTemplateId(indexTemplateId);
            labelPO.setLabelName(labelEnum.getName());
        }

        return true;
    }

    /**
     * 构建索引标签配置操作记录
     *
     * @param bizId
     * @param operateId
     * @param operator
     * @param content
     * @return
     */
    private OperateRecordDTO buildLabelSettingOperatorRecord(String bizId, Integer operateId, String operator, String content) {
        OperateRecordDTO operateRecord = new OperateRecordDTO();
        operateRecord.setBizId(bizId);
        operateRecord.setModuleId(ModuleEnum.TEMPLATE.getCode());
        operateRecord.setOperateId(operateId);
        operateRecord.setOperator(operator);
        operateRecord.setContent(content);

        return operateRecord;
    }
}
