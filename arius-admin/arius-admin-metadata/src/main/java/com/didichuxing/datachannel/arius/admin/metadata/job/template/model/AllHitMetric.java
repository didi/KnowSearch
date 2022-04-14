package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;

import java.util.HashMap;
import java.util.Map;

public class AllHitMetric {
    private Map<String, MetricNode> hitMap = new HashMap<>();

    private Map<String, MetricNode> aggsMap = new HashMap<>();


    public void addAggs(Map<String, Long> m,  boolean isHit) {
        for (Map.Entry<String, Long> entry : m.entrySet()) {
            addCount(entry.getKey(), entry.getValue(), isHit);
        }
    }

    private void addCount(String key, Long count, boolean isHit) {
        if (key == null || key.trim().length() == 0) {
            return;
        }

        Map<String, MetricNode> map;
        if (isHit) {
            map = hitMap;
        } else {
            map = aggsMap;
        }

        map.computeIfAbsent(key, k -> new MetricNode());

        map.get(key).add(count);
    }

    public Map<String, MetricNode> getHitMap() {
        return hitMap;
    }

    public Map<String, MetricNode> getAggsMap() {
        return aggsMap;
    }

    public class MetricNode {
        private Long count = 0L;

        public void add(Long c) {
            this.count += c;
        }

        public Long getCount() {
            return count;
        }

        public void incCount() {
            this.count++;
        }
    }
}
