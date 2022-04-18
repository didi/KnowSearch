package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.util.Map;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送给夜莺的数据格式
 * Created by d06679 on 2017/6/23.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class N9eData {

    /**
     * 监控指标的名字
     */
    private String metric;

    /**
     * 时间戳 单位是秒
     */
    private Long time;

    /**
     * 指标的值
     */
    private String value;

    /**
     * 监控数据的维度信息，用tags来描述
     */
    private Map<String, String> tags = Maps.newHashMap();

    /**
     * 资源唯一标识，如果是机器的监控指标，这个字段大都填充为机器ip，如果是跟设备无关的指标，这个字段可以为空
     */
    private String ident = "";
    /**
     * 资源名称，如果是机器，这个字段很可能是填充为hostname，当然，如果是设备无关的指标，这个字段也是空，即使是设备的指标，这个字段也允许为空
     */
    private String alias = "";

    public void putTag(String key, String value) {
        tags.put(key, value);
    }

    @Override
    public String toString() {
        return "N9eData{" + "metric='" + metric + '\'' + ", time=" + time + ", value='" + value + '\''
                + ", tags=" + tags + '}';
    }

}
