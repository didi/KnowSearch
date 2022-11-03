package com.didichuxing.datachannel.arius.admin.common.constant.template;

import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @Authoer: zyl
 * @Date: 2022/10/28
 * @Version: 1.0
 */
public enum TemplateSettingEnum {

    INDEX_TRANSLOG_DURABILITY("index.translog.durability", "async,request", "translog刷新策略"),
    INDEX_PRIORITY("index.priority", "10,5,0", "恢复优先级");

    private String setting;
    private String values;
    private String desc;

    TemplateSettingEnum(String setting, String values, String desc){
        this.setting = setting;
        this.values = values;
        this.desc = desc;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getValues() {
        return values;
    }

    public List<String> getValueList() {
        return Arrays.asList(StringUtils.split(values, ","));
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static Stream<TemplateSettingEnum> stream() {
        return Stream.of(TemplateSettingEnum.values());
    }

    public static TemplateSettingEnum getBySetting(String setting){
        for (TemplateSettingEnum contain : values()) {
            if (contain.equals(setting)){
                return contain;
            }
        }
        return null;
    }


}
