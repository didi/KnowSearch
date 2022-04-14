package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class IndexHitNode {
    private IndexTemplatePhyWithLogic template;

    public IndexHitNode(IndexTemplatePhyWithLogic template) {
        this.template = template;
    }


    public boolean isExpress() {
        return template.getExpression().endsWith("*");
    }

    public boolean isDataFormat() {
        return !StringUtils.isBlank(template.getLogicTemplate().getDateFormat());
    }

    private static final Long ONE_DAY = 24 * 60 * 60 * 1000L;

    /**
     * 获取索引物理名称集合，在索引过期范围内
     *
     * @param time
     * @return
     */
    public Set<String> getIndexDateNames(long time) {
        Set<String> ret = new HashSet<>();
        if (!isExpress() || !isDataFormat()) {
            return ret;
        }

        long expireTime = template.getLogicTemplate().getExpireTime();
        if (expireTime <= 0) {
            expireTime = 3000;
        }

        String dataFormat = template.getLogicTemplate().getDateFormat().replace("YYYY", "yyyy");
        String extraFormat = null;
        if (!dataFormat.contains("dd")) {
            extraFormat = "_yyyy-MM-dd";
        }

        String prefix = getNameFromExpress(template.getExpression());
        for (int i = 0; i <= expireTime; i++) {
            Long t = time - i * ONE_DAY;

            SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);
            String indexName = prefix + sdf.format(new Date(t));
            ret.add(indexName);

            if (extraFormat != null) {
                sdf = new SimpleDateFormat(extraFormat);
                ret.add(prefix + sdf.format(new Date(t)));
            }
        }

        return ret;
    }

    /**
     * 只匹配没有express或者format的
     *
     * @param index
     * @return
     */
    public boolean matchIndex(String index) {
        if (isExpress() && isDataFormat()) {
            return false;
        }

        if (isExpress()) {
            String prefix = getNameFromExpress(template.getExpression());
            if (index.startsWith(prefix)) {
                return true;
            }

        } else {
            if (index.equalsIgnoreCase(template.getExpression())) {
                return true;
            }
        }

        return false;
    }


    /**
     * 只匹配没有express或者format的
     *
     * @param indices
     * @return
     */
    public boolean matchIndices(String indices) {
        if (isExpress() && isDataFormat()) {
            return false;
        }

        String name = getNameFromExpress(template.getExpression());
        return IndexNameUtils.indexExpMatch(name, indices);
    }


    private String getNameFromExpress(String express) {
        String prefix = express;
        if (prefix == null) {
            prefix = "";
        }
        prefix = prefix.trim();
        if (prefix.endsWith("*")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        return prefix;
    }

    public String getName() {
        return template.getName();
    }

    public IndexTemplatePhyWithLogic getTemplate() {
        return template;
    }
}
