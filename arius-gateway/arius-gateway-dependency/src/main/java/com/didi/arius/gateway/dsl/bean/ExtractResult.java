package com.didi.arius.gateway.dsl.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.didi.arius.gateway.dsl.util.Utils;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/5/29 下午4:00
 * @Modified By
 *
 * 提取结果 含有查询，过滤，聚合，排序字段
 */
public class ExtractResult {

    /**
     * dsl 查询模板
     */
    private String dslTemplate;
    /**
     * 新的dsl 查询模板
     */
    private String newDslTemplate;
    /**
     * dsl 查询模板列表
     */
    private List<String> dslTemplateList;
    /**
     * 索引名称
     */
    private String indices;
    /**
     * 查询类型，dsl,sql
     */
    private String searchType;
    /**
     * dsl 查询模板 MD5值
     */
    private String dslTemplateMd5;
    /**
     * msearch 查询时查询模板MD5列表
     */
    private List<String> dslTemplateMd5List;
    /**
     * 格式化的dsl，去除空格
     */
    private String dsl;
    /**
     * msearch 查询时查询列表
     */
    private List<String> dslList;
    /**
     * dsl语句类型，aggs,scroll,msearch,get,normal,unknown
     */
    private String dslType;
    /**
     * 获取的字段
     */
    private String selectFields;
    /**
     * 过滤字段
     */
    private String whereFields;
    /**
     * 聚合字段
     */
    private String groupByFields;
    /**
     * 排序字段
     */
    private String sortByFields;
    /**
     * 查询语句标签
     */
    private Set<String> tags;

    public ExtractResult() {
    }

    public ExtractResult(String dslTemplate, String indices, String searchType, String dslTemplateMd5, String dsl, String dslType) {
        this.dslTemplate = dslTemplate;
        this.indices = indices;
        this.searchType = searchType;
        this.dslTemplateMd5 = dslTemplateMd5;
        this.dsl = dsl;
        this.dslType = dslType;
    }

    public ExtractResult(String dslTemplate, String indices, String searchType, String dslTemplateMd5, String dsl, String dslType,
                           String selectFields, String whereFields, String groupByFields, String sortByFields) {
        this.dslTemplate = dslTemplate;
        this.indices = indices;
        this.searchType = searchType;
        this.dslTemplateMd5 = dslTemplateMd5;
        this.dsl = dsl;
        this.dslType = dslType;
        this.selectFields = selectFields;
        this.whereFields = whereFields;
        this.groupByFields = groupByFields;
        this.sortByFields = sortByFields;
        this.tags = Sets.newHashSet();
    }

    /**
     * 追加文件内容
     *
     * @param fileChannel
     * @throws IOException
     */
    public void writeToFile(FileChannel fileChannel) throws IOException {
        byte[] enterBytes = "\r\n".getBytes("utf-8");
        byte[] bytes = null;

        appendFileContent(fileChannel, enterBytes);

        try {
            String dslFormat = new String(dsl);
            Map<String, Object> map = JSON.parseObject(dslFormat, SortedMap.class);
            dslFormat = JSON.toJSONString(map);
            bytes = dslFormat.getBytes("utf-8");
        } catch (JSONException e) {
            bytes = dsl.getBytes("utf-8");
            System.out.println(dsl);
            e.printStackTrace();
        }

        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);

        bytes = dslTemplate.getBytes("utf-8");
        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);

        bytes = String.format("md5 [%s]", dslTemplateMd5).getBytes("utf-8");
        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);

        bytes = String.format("select [%s]", selectFields).getBytes("utf-8");
        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);

        bytes = String.format("where [%s]", whereFields).getBytes("utf-8");
        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);

        bytes = String.format("group by [%s]", groupByFields).getBytes("utf-8");
        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);

        bytes = String.format("sort by [%s]", sortByFields).getBytes("utf-8");
        appendFileContent(fileChannel, bytes);
        appendFileContent(fileChannel, enterBytes);
    }

    /**
     * 构建失败结果，填入默认值
     */
    public void buildFailedResult(String version) {
        this.setDslType("normal");
        this.setDslTemplate("FAILED");
        this.setDslTemplateMd5(Utils.getMD5WithVersion("FAILED", version));
        this.setIndices("");
        this.setSelectFields("*");
        this.setWhereFields("*");
        this.setGroupByFields("*");
        this.setSortByFields("*");
        this.setTags(Sets.newHashSet());
    }

    /**
     * 构建scroll查询
     */
    public void buildScrollResult(String dslContent, String version) {
        this.setSearchType("dsl");
        this.setDslType("normal");
        this.setDsl(dslContent);
        this.setDslTemplate("SCROLL ID");
        this.setDslTemplateMd5(Utils.getMD5WithVersion("SCROLL ID", version));
        this.setIndices("");
        this.setSelectFields("*");
        this.setWhereFields("*");
        this.setGroupByFields("*");
        this.setSortByFields("*");
        this.setTags(Sets.newHashSet());
    }

    public String getDslTemplate() {
        return dslTemplate;
    }

    public ExtractResult setDslTemplate(String dslTemplate) {
        this.dslTemplate = dslTemplate;
        return this;
    }

    public String getIndices() {
        return indices;
    }

    public ExtractResult setIndices(String indices) {
        this.indices = indices;
        return this;
    }

    public String getSearchType() {
        return searchType;
    }

    public ExtractResult setSearchType(String searchType) {
        this.searchType = searchType;
        return this;
    }

    public String getDslTemplateMd5() {
        return dslTemplateMd5;
    }

    public ExtractResult setDslTemplateMd5(String dslTemplateMd5) {
        this.dslTemplateMd5 = dslTemplateMd5;
        return this;
    }

    public String getDsl() {
        return dsl;
    }

    public ExtractResult setDsl(String dsl) {
        this.dsl = dsl;
        return this;
    }

    public String getDslType() {
        return dslType;
    }

    public ExtractResult setDslType(String dslType) {
        this.dslType = dslType;
        return this;
    }

    public String getSelectFields() {
        return selectFields;
    }

    public ExtractResult setSelectFields(String selectFields) {
        this.selectFields = selectFields;
        return this;
    }

    public String getWhereFields() {
        return whereFields;
    }

    public ExtractResult setWhereFields(String whereFields) {
        this.whereFields = whereFields;
        return this;
    }

    public String getGroupByFields() {
        return groupByFields;
    }

    public ExtractResult setGroupByFields(String groupByFields) {
        this.groupByFields = groupByFields;
        return this;
    }

    public String getSortByFields() {
        return sortByFields;
    }

    public ExtractResult setSortByFields(String sortByFields) {
        this.sortByFields = sortByFields;
        return this;
    }

    public List<String> getDslTemplateMd5List() {
        return dslTemplateMd5List;
    }

    public void setDslTemplateMd5List(List<String> dslTemplateMd5List) {
        this.dslTemplateMd5List = dslTemplateMd5List;
    }

    public List<String> getDslList() {
        return dslList;
    }

    public void setDslList(List<String> dslList) {
        this.dslList = dslList;
    }

    public List<String> getDslTemplateList() {
        return dslTemplateList;
    }

    public void setDslTemplateList(List<String> dslTemplateList) {
        this.dslTemplateList = dslTemplateList;
    }

    public String getNewDslTemplate() {
        return newDslTemplate;
    }

    public ExtractResult setNewDslTemplate(String newDslTemplate) {
        this.newDslTemplate = newDslTemplate;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public ExtractResult setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtractResult that = (ExtractResult) o;

        if (dslTemplate != null ? !dslTemplate.equals(that.dslTemplate) : that.dslTemplate != null) return false;
        if (newDslTemplate != null ? !newDslTemplate.equals(that.newDslTemplate) : that.newDslTemplate != null)
            return false;
        if (dslTemplateList != null ? !dslTemplateList.equals(that.dslTemplateList) : that.dslTemplateList != null)
            return false;
        if (indices != null ? !indices.equals(that.indices) : that.indices != null) return false;
        if (searchType != null ? !searchType.equals(that.searchType) : that.searchType != null) return false;
        if (dslTemplateMd5 != null ? !dslTemplateMd5.equals(that.dslTemplateMd5) : that.dslTemplateMd5 != null)
            return false;
        if (dslTemplateMd5List != null ? !dslTemplateMd5List.equals(that.dslTemplateMd5List) : that.dslTemplateMd5List != null)
            return false;
        if (dsl != null ? !dsl.equals(that.dsl) : that.dsl != null) return false;
        if (dslList != null ? !dslList.equals(that.dslList) : that.dslList != null) return false;
        if (dslType != null ? !dslType.equals(that.dslType) : that.dslType != null) return false;
        if (selectFields != null ? !selectFields.equals(that.selectFields) : that.selectFields != null) return false;
        if (whereFields != null ? !whereFields.equals(that.whereFields) : that.whereFields != null) return false;
        if (groupByFields != null ? !groupByFields.equals(that.groupByFields) : that.groupByFields != null)
            return false;
        if (sortByFields != null ? !sortByFields.equals(that.sortByFields) : that.sortByFields != null) return false;
        return tags != null ? tags.equals(that.tags) : that.tags == null;
    }

    @Override
    public int hashCode() {
        int result = dslTemplate != null ? dslTemplate.hashCode() : 0;
        result = 31 * result + (newDslTemplate != null ? newDslTemplate.hashCode() : 0);
        result = 31 * result + (dslTemplateList != null ? dslTemplateList.hashCode() : 0);
        result = 31 * result + (indices != null ? indices.hashCode() : 0);
        result = 31 * result + (searchType != null ? searchType.hashCode() : 0);
        result = 31 * result + (dslTemplateMd5 != null ? dslTemplateMd5.hashCode() : 0);
        result = 31 * result + (dslTemplateMd5List != null ? dslTemplateMd5List.hashCode() : 0);
        result = 31 * result + (dsl != null ? dsl.hashCode() : 0);
        result = 31 * result + (dslList != null ? dslList.hashCode() : 0);
        result = 31 * result + (dslType != null ? dslType.hashCode() : 0);
        result = 31 * result + (selectFields != null ? selectFields.hashCode() : 0);
        result = 31 * result + (whereFields != null ? whereFields.hashCode() : 0);
        result = 31 * result + (groupByFields != null ? groupByFields.hashCode() : 0);
        result = 31 * result + (sortByFields != null ? sortByFields.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * 追加文件内容
     *
     * @param fileChannel
     * @param bytes
     * @throws IOException
     */
    private void appendFileContent(FileChannel fileChannel, byte[] bytes) throws IOException {
        ByteBuffer bbf = ByteBuffer.wrap(bytes);
        bbf.put(bytes) ;
        bbf.flip();
        fileChannel.write(bbf);
    }


}
