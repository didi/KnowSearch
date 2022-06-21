package com.didichuxing.datachannel.arius.admin.common.tuple;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * tuple2 实现
 *
 * @author shizeying
 * @date 2022/06/20
 */
@AllArgsConstructor
@ToString
public class Tuple2<T1, T2> implements Tuple, Comparable<Tuple2<T1, T2>>, Serializable {
    private static final long serialVersionUID = 1L;
    public final         T1   _1;
    public final         T2   _2;
    
    public static <T1, T2> Comparator<Tuple2<T1, T2>> comparator(Comparator<? super T1> t1Comp,
                                                                 Comparator<? super T2> t2Comp) {
        return (Comparator<Tuple2<T1, T2>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1._1, t2._1);
            if (check1 != 0) {
                return check1;
            }
            
            final int check2 = t2Comp.compare(t1._2, t2._2);
            if (check2 != 0) {
                return check2;
            }
            
            return 0;
        };
    }
    
    private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>> int compareTo(Tuple2<?, ?> o1,
                                                                                                        Tuple2<?, ?> o2) {
        final Tuple2<U1, U2> t1 = (Tuple2<U1, U2>) o1;
        final Tuple2<U1, U2> t2 = (Tuple2<U1, U2>) o2;
        
        final int check1 = t1._1.compareTo(t2._1);
        if (check1 != 0) {
            return check1;
        }
        
        final int check2 = t1._2.compareTo(t2._2);
        if (check2 != 0) {
            return check2;
        }
        
        return 0;
    }
    
    public T1 _1() {
        return _1;
    }
    
    public T2 _2() {
        return _2;
    }
    
    public Tuple2<T1, T2> update2(T2 value) {
        return new Tuple2<>(_1, value);
    }
    
    public Tuple2<T2, T1> swap() {
        return Tuple.of(_2, _1);
    }
    
    public Map.Entry<T1, T2> toEntry() {
        return new AbstractMap.SimpleEntry<>(_1, _2);
    }
    
    /**
     * 传入bi func 转换为tuple
     * <blockquote><pre>
     *   final Tuple2<Integer, Integer> tuple2 = Tuple.of(1, 2);
     *         BiFunction<Integer,Integer,Tuple2<Integer,Integer>> biFunction=(a,b)->Tuple.of(a+1,b+1 );
     *         result : tuple_2=3
     * </pre></blockquote>
     *
     * @param mapper 映射器
     * @return {@code Tuple2<U1, U2>}
     */
    public <U1, U2> Tuple2<U1, U2> map(BiFunction<? super T1, ? super T2, Tuple2<U1, U2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return mapper.apply(_1, _2);
    }
    
    /**
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Tuple2) {
            final Tuple2<?, ?> that = (Tuple2<?, ?>) obj;
            return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Tuple.hash(_1, _2);
    }
    
    /**
     * @return
     */
    @Override
    public int tupleSize() {
        return 2;
    }
    
    /**
     * @param that the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Tuple2<T1, T2> that) {
        Objects.requireNonNull(that, "that is not  null");
        return Tuple2.compareTo(this, that);
    }
    
}