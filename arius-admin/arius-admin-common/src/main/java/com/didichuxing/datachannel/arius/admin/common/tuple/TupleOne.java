package com.didichuxing.datachannel.arius.admin.common.tuple;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * tuple1
 *
 * @author shizeying
 * @date 2022/06/20
 */
@ToString
@AllArgsConstructor
public class TupleOne<T1> implements Tuples, Comparable<TupleOne<T1>>, Serializable {
    public final T1 V1;
    
    /**
     * 元组大小
     *
     * @return int
     */
    @Override
    public int tupleSize() {
        return 1;
    }
    
    public static <T1> Comparator<TupleOne<T1>> comparator(Comparator<? super T1> t1Comp) {
        return (Comparator<TupleOne<T1>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1.V1, t2.V1);
            if (check1 != 0) {
                return check1;
            }
            return 0;
        };
    }
    
    private static <U1 extends Comparable<? super U1>> int compareTo(TupleOne<?> o1, TupleOne<?> o2) {
        
        final TupleOne<U1> t1 = (TupleOne<U1>) o1;
        final TupleOne<U1> t2 = (TupleOne<U1>) o2;
        
        final int check1 = t1.V1.compareTo(t2.V1);
        if (check1 != 0) {
            return check1;
        }
        
        return 0;
    }
    
    @Override
    public int compareTo(TupleOne<T1> that) {
        return TupleOne.compareTo(this, that);
    }
    
    public T1 _1() {
        return V1;
    }
    
    public TupleOne<T1> update1(T1 value) {
        return new TupleOne<>(value);
    }
    
    public <U1> TupleOne<U1> map(Function<? super T1, ? extends U1> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Tuples.of(mapper.apply(V1));
    }
    
    public <U> U apply(Function<? super T1, ? extends U> f) {
        Objects.requireNonNull(f, "function is null");
        return f.apply(V1);
    }
    
    public <T2> TupleTwo<T1, T2> append(T2 t2) {
        return Tuples.of(V1, t2);
    }
    
    public <T2> TupleTwo<T1, T2> concat(TupleOne<T2> tuple) {
        Objects.requireNonNull(tuple, "tuple1 is null");
        return Tuples.of(V1, tuple.V1);
    }
    
    public <T2, T3> TupleThree<T1, T2, T3> concat(TupleTwo<T2, T3> tuple) {
        Objects.requireNonNull(tuple, "tuple2 is null");
        return Tuples.of(V1, tuple.v1, tuple.v2);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof TupleOne) {
            final TupleOne<?> that = (TupleOne<?>) o;
            return Objects.equals(this.V1, that.V1);
        }
        return false;
    }
    
    /**
     * @return
     */
    @Override
    public int hashCode() {
        return Tuples.hash(V1);
    }
    
    
}