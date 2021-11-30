package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class AggsPath {

    private String aggsName;

    private String aggsTypedKey;

    private Map<String, AggsPath> items = new HashMap<>();

    public void addItems(String key, AggsPath item) {
        items.put(key, item);
    }

    public AggsPath getItem(String key) {
        return items.get(key);
    }


}
