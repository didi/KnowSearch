package com.didiglobal.logi.op.manager.infrastructure.common;

/**
 * @author didi
 * @date 2022-08-10 9:36 下午
 */
public class Tuple<V1, V2> {
    private final V1 v1;
    private final V2 v2;

    public static <V1, V2> Tuple<V1, V2> tuple(V1 v1, V2 v2) {
        return new Tuple(v1, v2);
    }

    public Tuple(V1 v1, V2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public V1 v1() {
        return this.v1;
    }

    public V2 v2() {
        return this.v2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Tuple tuple = (Tuple) o;
            if (this.v1 != null) {
                if (!this.v1.equals(tuple.v1)) {
                    return false;
                }
            } else if (tuple.v1 != null) {
                return false;
            }

            if (this.v2 != null) {
                if (this.v2.equals(tuple.v2)) {
                    return true;
                }
            } else if (tuple.v2 == null) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.v1 != null ? this.v1.hashCode() : 0;
        result = 31 * result + (this.v2 != null ? this.v2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple [v1=" + this.v1 + ", v2=" + this.v2 + "]";
    }
}
