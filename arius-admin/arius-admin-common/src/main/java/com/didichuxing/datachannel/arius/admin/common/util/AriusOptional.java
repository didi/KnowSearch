package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 模仿Java8 Optional, 兼容服务Result响应体
 * @author linyunan
 * @date 2021-05-21
 */
public class AriusOptional<T> {

    private static final AriusOptional<?> EMPTY = new AriusOptional<>();

    private final T                       value;

    private AriusOptional() {
        this.value = null;
    }

    public static <T> AriusOptional<T> empty() {
        @SuppressWarnings("unchecked")
        AriusOptional<T> t = (AriusOptional<T>) EMPTY;
        return t;
    }

    private AriusOptional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public static <T> AriusOptional<T> of(T value) {
        return new AriusOptional<>(value);
    }

	public static <T> AriusOptional<T> ofListNullable(T value) {
    	if (value instanceof Collection) {
    		return CollectionUtils.isEmpty((Collection<?>) value) ? empty() : of(value);
		}

		return ofObjNullable(value);
	}

    public static <T> AriusOptional<T> ofObjNullable(T value) {
        return value == null ? empty() : of(value);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);

        }
    }

    public AriusOptional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;

        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    public <U> AriusOptional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();

        } else {
            return AriusOptional.ofObjNullable(mapper.apply(value));
        }
    }

    public <U> AriusOptional<U> flatMap(Function<? super T, AriusOptional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();

        } else {
            return Objects.requireNonNull(mapper.apply(value));
        }
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public Result<T> orGetResult(Supplier<? extends Result<T>> other) {
        return value != null ? Result.buildSucc(value) : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AriusOptional)) {
            return false;
        }

        AriusOptional<?> other = (AriusOptional<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null ? String.format("AriusOptional[%s]", value) : "AriusOptional.empty";
    }


}
