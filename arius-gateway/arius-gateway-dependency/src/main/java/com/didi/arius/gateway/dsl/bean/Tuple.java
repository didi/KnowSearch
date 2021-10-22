package com.didi.arius.gateway.dsl.bean;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/5/29 下午4:08
 * @Modified By
 */
public class Tuple<T, V> {

    private T v1;
    private V v2;

    public Tuple(T v1, V v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T getV1() {
        return v1;
    }

    public Tuple setV1(T v1) {
        this.v1 = v1;
        return this;
    }

    public V getV2() {
        return v2;
    }

    public Tuple setV2(V v2) {
        this.v2 = v2;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;

        if (v1 != null ? !v1.equals(tuple.v1) : tuple.v1 != null) return false;
        return v2 != null ? v2.equals(tuple.v2) : tuple.v2 == null;
    }

    @Override
    public int hashCode() {
        int result = v1 != null ? v1.hashCode() : 0;
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        return result;
    }

}
