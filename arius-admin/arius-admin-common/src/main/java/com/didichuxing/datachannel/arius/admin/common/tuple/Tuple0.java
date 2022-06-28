package com.didichuxing.datachannel.arius.admin.common.tuple;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * tuple0 默认实现
 *
 * @author shizeying
 * @date 2022/06/20
 */
@ToString
@AllArgsConstructor
public final class Tuple0 implements TupleInterface, Comparable<Tuple0>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Tuple0 INSTANCE = new Tuple0 ();

   
    private static final Comparator<Tuple0> COMPARATOR = (Comparator<Tuple0> & Serializable) (t1, t2) -> 0;

   

   
    public static Tuple0 instance() {
        return INSTANCE;
    }

    public static  Comparator<Tuple0> comparator() {
        return COMPARATOR;
    }

   
    @Override
    public int compareTo(Tuple0 that) {
        return 0;
    }

    
    public <U> U apply(Supplier<? extends U> f) {
        Objects.requireNonNull(f, "supplier is null");
        return f.get();
    }

    public <T1> Tuple1<T1> append(T1 t1) {
        return TupleInterface.of(t1);
    }

   
    public <T1> Tuple1<T1> concat(Tuple1<T1> tuple) {
        Objects.requireNonNull(tuple, "tuple1 is null");
        return TupleInterface.of(tuple._1);
    }

    
    public <T1, T2> Tuple2<T1, T2> concat(Tuple2<T1, T2> tuple) {
        Objects.requireNonNull(tuple, "tuple2 is null");
        return TupleInterface.of(tuple._1, tuple._2);
    }

    
    public <T1, T2, T3> Tuple3<T1, T2, T3> concat(Tuple3<T1, T2, T3> tuple) {
        Objects.requireNonNull(tuple, "tuple3 is null");
        return TupleInterface.of(tuple._1, tuple._2, tuple._3);
    }

  
    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return 1;
    }
    
    
    private Object getInstance() {
        return INSTANCE;
    }
    
    /**
     * @return
     */
    @Override
    public int tupleSize() {
        return 0;
    }
}