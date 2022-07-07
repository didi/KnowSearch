package com.didichuxing.datachannel.arius.admin.common.tuple;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class TupleThree<T1, T2, T3> implements Tuples, Comparable<TupleThree<T1, T2, T3>>, Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 第一个元素
     */
    public final T1 v1;
    
    /**
     * 第二个元素
     */
    public final T2 v2;
    
    /**
     * 第三个元素
     */
    public final T3 v3;
    
    /**
     * 比较器
     *
     * @param t1Comp t1 比较器
     * @param t2Comp t2 比较器
     * @param t3Comp t3 比较器
     * @return {@code Comparator<Tuple3<T1, T2, T3>>}
     */
    public static <T1, T2, T3> Comparator<TupleThree<T1, T2, T3>> comparator(Comparator<? super T1> t1Comp,
                                                                             Comparator<? super T2> t2Comp,
                                                                             Comparator<? super T3> t3Comp) {
        return (Comparator<TupleThree<T1, T2, T3>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1.v1, t2.v1);
            if (check1 != 0) {
                return check1;
            }
            
            final int check2 = t2Comp.compare(t1.v2, t2.v2);
            if (check2 != 0) {
                return check2;
            }
            
            final int check3 = t3Comp.compare(t1.v3, t2.v3);
            return check3;
        };
    }
    
    /**
     * 比较具体实现
     *
     * @param o1 tuple 3
     * @param o2 o2
     * @return int
     */
    private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>, U3 extends Comparable<? super U3>> int compareTo(
            TupleThree<?, ?, ?> o1, TupleThree<?, ?, ?> o2) {
        final TupleThree<U1, U2, U3> t1 = (TupleThree<U1, U2, U3>) o1;
        final TupleThree<U1, U2, U3> t2 = (TupleThree<U1, U2, U3>) o2;
        
        final int check1 = t1.v1.compareTo(t2.v1);
        if (check1 != 0) {
            return check1;
        }
        
        final int check2 = t1.v2.compareTo(t2.v2);
        if (check2 != 0) {
            return check2;
        }
        
        final int check3 = t1.v3.compareTo(t2.v3);
        return check3;
    }
    
    @Override
    public int compareTo(TupleThree<T1, T2, T3> that) {
        return TupleThree.compareTo(this, that);
    }
    
    public T1 _1() {
        return v1;
    }
    
    public TupleThree<T1, T2, T3> update1(T1 value) {
        return new TupleThree<>(value, v2, v3);
    }
    
    public T2 _2() {
        return v2;
    }
    
    /**
     * 更新第二个元素
     *
     * @param value value
     * @return {@code Tuple3<T1, T2, T3>}
     */
    public TupleThree<T1, T2, T3> update2(T2 value) {
        return new TupleThree<>(v1, value, v3);
    }
    
    public T3 _3() {
        return v3;
    }
    
    /**
     * 更人第三个元素
     *
     * @param value value
     * @return {@code Tuple3<T1, T2, T3>}
     */
    public TupleThree<T1, T2, T3> update3(T3 value) {
        return new TupleThree<>(v1, v2, value);
    }
    
    /**
     * 地图
     *
     * @param f1 f1 _1 mapper 生成
     * @param f2 f2 _2 mapper 生成
     * @param f3 f3 _3 mapper 生成
     * @return {@code Tuple3<U1, U2, U3>}
     */
    public <U1, U2, U3> TupleThree<U1, U2, U3> map(Function<? super T1, ? extends U1> f1,
                                                   Function<? super T2, ? extends U2> f2,
                                                   Function<? super T3, ? extends U3> f3) {
        Objects.requireNonNull(f1, "f1 is null");
        Objects.requireNonNull(f2, "f2 is null");
        Objects.requireNonNull(f3, "f3 is null");
        return Tuples.of(f1.apply(v1), f2.apply(v2), f3.apply(v3));
    }
    
    public <U> TupleThree<U, T2, T3> map1(Function<? super T1, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final U u = mapper.apply(v1);
        return Tuples.of(u, v2, v3);
    }
    
    public <U> TupleThree<T1, U, T3> map2(Function<? super T2, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final U u = mapper.apply(v2);
        return Tuples.of(v1, u, v3);
    }
    
    public <U> TupleThree<T1, T2, U> map3(Function<? super T3, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final U u = mapper.apply(v3);
        return Tuples.of(v1, v2, u);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TupleThree) {
            final TupleThree<?, ?, ?> that = (TupleThree<?, ?, ?>) obj;
            return Objects.equals(this.v1, that.v1) && Objects.equals(this.v2, that.v2) && Objects.equals(this.v3,
                    that.v3);
        }
        return false;
    }
    
    /**
     * @return
     */
    @Override
    public int tupleSize() {
        return 3;
    }
    
    @Override
    public int hashCode() {
        return Tuples.hash(v1, v2, v3);
    }
    
    
    
}