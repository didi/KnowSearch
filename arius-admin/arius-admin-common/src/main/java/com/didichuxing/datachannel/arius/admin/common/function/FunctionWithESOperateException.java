package com.didichuxing.datachannel.arius.admin.common.function;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.Objects;

/**
 * 带有esoperate异常函数
 *
 * @author shizeying
 * @date 2022/09/08
 */
@FunctionalInterface
public interface FunctionWithESOperateException<T, R> {
	R apply(T t) throws ESOperateException;
	
	default <V> FunctionWithESOperateException<V, R> compose(
			FunctionWithESOperateException<? super V, ? extends T> before) throws ESOperateException {
		Objects.requireNonNull(before);
		return (V v) -> apply(before.apply(v));
	}
	
	default <V> FunctionWithESOperateException<T, V> andThen(
			FunctionWithESOperateException<? super R, ? extends V> after) throws ESOperateException {
		Objects.requireNonNull(after);
		return (T t) -> after.apply(apply(t));
	}
	
	static <T> FunctionWithESOperateException<T, T> identity() throws ESOperateException {
		return t -> t;
	}
}