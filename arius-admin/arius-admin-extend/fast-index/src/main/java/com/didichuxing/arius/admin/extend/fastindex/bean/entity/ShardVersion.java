package com.didichuxing.arius.admin.extend.fastindex.bean.entity;

import lombok.Data;

import java.util.Objects;

@Data
public class ShardVersion {
    private Integer shard;
    private String version;

    public ShardVersion(Integer shard, String version) {
        this.shard = shard;
        this.version = version;
    }

    public ShardVersion smaller(ShardVersion shardVersion) {
        Integer v1 = Integer.parseInt(version.replaceAll("\\.", ""));
        Integer v2 = Integer.parseInt(shardVersion.version.replaceAll("\\.", ""));
        return v1 > v2 ? shardVersion : this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShardVersion that = (ShardVersion) o;
        return Objects.equals(shard, that.shard) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, version);
    }

    @Override
    public String toString() {
        return "[" + shard + ":" + version + "]";
    }
}