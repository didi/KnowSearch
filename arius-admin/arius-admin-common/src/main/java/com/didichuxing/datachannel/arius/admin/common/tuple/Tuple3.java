package com.didichuxing.datachannel.arius.admin.common.tuple;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Tuple3<T1, T2, T3> implements TupleInterface, Comparable<Tuple3<T1, T2, T3>>, Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 第一个元素
     */
    public final T1 _1;
    
    /**
     * 第二个元素
     */
    public final T2 _2;
    
    /**
     * 第三个元素
     */
    public final T3 _3;
    
    /**
     * 比较器
     *
     * @param t1Comp t1 比较器
     * @param t2Comp t2 比较器
     * @param t3Comp t3 比较器
     * @return {@code Comparator<Tuple3<T1, T2, T3>>}
     */
    public static <T1, T2, T3> Comparator<Tuple3<T1, T2, T3>> comparator(Comparator<? super T1> t1Comp,
                                                                         Comparator<? super T2> t2Comp,
                                                                         Comparator<? super T3> t3Comp) {
        return (Comparator<Tuple3<T1, T2, T3>> & Serializable) (t1, t2) -> {
            final int check1 = t1Comp.compare(t1._1, t2._1);
            if (check1 != 0) {
                return check1;
            }
            
            final int check2 = t2Comp.compare(t1._2, t2._2);
            if (check2 != 0) {
                return check2;
            }
            
            final int check3 = t3Comp.compare(t1._3, t2._3);
            if (check3 != 0) {
                return check3;
            }
            
            return 0;
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
            Tuple3<?, ?, ?> o1, Tuple3<?, ?, ?> o2) {
        final Tuple3<U1, U2, U3> t1 = (Tuple3<U1, U2, U3>) o1;
        final Tuple3<U1, U2, U3> t2 = (Tuple3<U1, U2, U3>) o2;
        
        final int check1 = t1._1.compareTo(t2._1);
        if (check1 != 0) {
            return check1;
        }
        
        final int check2 = t1._2.compareTo(t2._2);
        if (check2 != 0) {
            return check2;
        }
        
        final int check3 = t1._3.compareTo(t2._3);
        if (check3 != 0) {
            return check3;
        }
        
        return 0;
    }
    
    @Override
    public int compareTo(Tuple3<T1, T2, T3> that) {
        return Tuple3.compareTo(this, that);
    }
    
    public T1 _1() {
        return _1;
    }
    
    public Tuple3<T1, T2, T3> update1(T1 value) {
        return new Tuple3<>(value, _2, _3);
    }
    
    public T2 _2() {
        return _2;
    }
    
    /**
     * 更新第二个元素
     *
     * @param value value
     * @return {@code Tuple3<T1, T2, T3>}
     */
    public Tuple3<T1, T2, T3> update2(T2 value) {
        return new Tuple3<>(_1, value, _3);
    }
    
    public T3 _3() {
        return _3;
    }
    
    /**
     * 更人第三个元素
     *
     * @param value value
     * @return {@code Tuple3<T1, T2, T3>}
     */
    public Tuple3<T1, T2, T3> update3(T3 value) {
        return new Tuple3<>(_1, _2, value);
    }
    
    /**
     * 地图
     *
     * @param f1 f1 _1 mapper 生成
     * @param f2 f2 _2 mapper 生成
     * @param f3 f3 _3 mapper 生成
     * @return {@code Tuple3<U1, U2, U3>}
     */
    public <U1, U2, U3> Tuple3<U1, U2, U3> map(Function<? super T1, ? extends U1> f1,
                                               Function<? super T2, ? extends U2> f2,
                                               Function<? super T3, ? extends U3> f3) {
        Objects.requireNonNull(f1, "f1 is null");
        Objects.requireNonNull(f2, "f2 is null");
        Objects.requireNonNull(f3, "f3 is null");
        return TupleInterface.of(f1.apply(_1), f2.apply(_2), f3.apply(_3));
    }
    
    public <U> Tuple3<U, T2, T3> map1(Function<? super T1, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final U u = mapper.apply(_1);
        return TupleInterface.of(u, _2, _3);
    }
    
    public <U> Tuple3<T1, U, T3> map2(Function<? super T2, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final U u = mapper.apply(_2);
        return TupleInterface.of(_1, u, _3);
    }
    
    public <U> Tuple3<T1, T2, U> map3(Function<? super T3, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final U u = mapper.apply(_3);
        return TupleInterface.of(_1, _2, u);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Tuple3) {
            final Tuple3<?, ?, ?> that = (Tuple3<?, ?, ?>) obj;
            return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2) && Objects.equals(this._3,
                    that._3);
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
        return TupleInterface.hash(_1, _2, _3);
    }
    
    
    
}