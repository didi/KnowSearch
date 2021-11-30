package com.didi.arius.gateway.common.metadata;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

/**
 *
 * @desc 可以跳过多type索引启用映射查询的appid列表
 */
@Data
@NoArgsConstructor
public class MappingIndexNameWhiteAppIds {

    /**
     * appid列表
     */
    private Set<Integer> appids = Sets.newHashSet();

    /**
     * 该appid是否为白名单
     *
     * @param appid
     * @return
     */
    public boolean isWhiteAppid(int appid){
        return this.appids.contains(appid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappingIndexNameWhiteAppIds that = (MappingIndexNameWhiteAppIds) o;
        return Objects.equals(appids, that.appids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appids);
    }

}
