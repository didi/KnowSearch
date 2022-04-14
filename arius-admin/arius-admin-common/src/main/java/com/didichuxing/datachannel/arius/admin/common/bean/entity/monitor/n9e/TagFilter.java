package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagFilter {
    private String key;
    private String func = "InList";
    private List<String> params = Lists.newArrayList();
}
