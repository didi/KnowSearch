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
public class Tuple1<T1> implements TupleInterface, Comparable<Tuple1<T1>>, Serializable {
    public final T1 _1;
    
    /**
     * 元组大小
     *
     * @return int
     */
    @Override
    public int tupleSize() {
        return 1;
    }
    
    public static <T1> Comparator<Tuple1<T1>> comparator(Comparator<? super T1> t1Comp) {
        return (Comparator<Tuple1<T1>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1._1, t2._1);
            if (check1 != 0) {
                return check1;
            }
            return 0;
        };
    }
    
    private static <U1 extends Comparable<? super U1>> int compareTo(Tuple1<?> o1, Tuple1<?> o2) {
        
        final Tuple1<U1> t1 = (Tuple1<U1>) o1;
        final Tuple1<U1> t2 = (Tuple1<U1>) o2;
        
        final int check1 = t1._1.compareTo(t2._1);
        if (check1 != 0) {
            return check1;
        }
        
        return 0;
    }
    
    @Override
    public int compareTo(Tuple1<T1> that) {
        return Tuple1.compareTo(this, that);
    }
    
    public T1 _1() {
        return _1;
    }
    
    public Tuple1<T1> update1(T1 value) {
        return new Tuple1<>(value);
    }
    
    public <U1> Tuple1<U1> map(Function<? super T1, ? extends U1> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TupleInterface.of(mapper.apply(_1));
    }
    
    public <U> U apply(Function<? super T1, ? extends U> f) {
        Objects.requireNonNull(f, "function is null");
        return f.apply(_1);
    }
    
    public <T2> Tuple2<T1, T2> append(T2 t2) {
        return TupleInterface.of(_1, t2);
    }
    
    public <T2> Tuple2<T1, T2> concat(Tuple1<T2> tuple) {
        Objects.requireNonNull(tuple, "tuple1 is null");
        return TupleInterface.of(_1, tuple._1);
    }
    
    public <T2, T3> Tuple3<T1, T2, T3> concat(Tuple2<T2, T3> tuple) {
        Objects.requireNonNull(tuple, "tuple2 is null");
        return TupleInterface.of(_1, tuple._1, tuple._2);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Tuple1) {
            final Tuple1<?> that = (Tuple1<?>) o;
            return Objects.equals(this._1, that._1);
        }
        return false;
    }
    
    /**
     * @return
     */
    @Override
    public int hashCode() {
        return TupleInterface.hash(_1);
    }
    
    
}