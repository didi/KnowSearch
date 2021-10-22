package com.didichuxing.datachannel.arius.admin.client.bean.common;

import java.util.Map;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送给Odin的数据格式
 * Created by d06679 on 2017/6/23.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OdinData {

    /**
     * 指标名称
     */
    private String              name;

    /**
     * 时间戳 单位是秒
     */
    private Long                timestamp;

    /**
     * 指标的值
     */
    private String              value;

    /**
     * 标签  主机名必须存在
     */
    private Map<String, String> tags = Maps.newHashMap();

    /**
     * 上报周期  单位秒
     */
    private int                 step;

    public void putTag(String key, String value) {
        tags.put(key, value);
    }

    @Override
    public String toString() {
        return "OdinDataFormat{" + "name='" + name + '\'' + ", timestamp=" + timestamp + ", value='" + value + '\''
               + ", tags=" + tags + ", step=" + step + '}';
    }

}
