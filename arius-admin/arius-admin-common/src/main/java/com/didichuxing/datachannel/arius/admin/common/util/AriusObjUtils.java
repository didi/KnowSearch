package com.didichuxing.datachannel.arius.admin.common.util;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

/**
 * Bean操作工具
 *
 * @author d06679
 * @date 2019/2/26
 */
public class AriusObjUtils {

    private AriusObjUtils() {
    }

    private static final ILog LOGGER = LogFactory.getLog(AriusObjUtils.class);

    /**
     * 比较两个对象是否相等
     * @param dest 目标对象  如果是null  则认为没有变
     * @param src 源对象
     * @return 变了 true  没变 false
     */
    public static boolean isChanged(Object dest, Object src) {
        return dest != null && !ObjectUtils.nullSafeEquals(src, dest);
    }

    /**
     * 找到两个对象不一样的地方
     * 这个方法要求参数bean中必须都是基本类型,且是封装类;如过不是封装类,例如boolean,getter方法不是get开头,而是is开头
     * @param src 源
     * @param dest 目标
     * @return 不一样的字段
     */
    public static String findChanged(Object src, Object dest) {
        if (src == null || dest == null) {
            return "";
        }
        StringBuilder content = new StringBuilder("");
        try {
            Map<String, Method> destMethodMap = Maps.newHashMap();
            for (Method destMethod : dest.getClass().getMethods()) {
                if (isGetter(destMethod)) {
                    destMethodMap.put(destMethod.getName(), destMethod);
                }
            }

            for (Method srcMethod : src.getClass().getMethods()) {
                if (isGetter(srcMethod)) {
                    Method destMethod = destMethodMap.get(srcMethod.getName());
                    if (destMethod != null) {
                        Object srcV = srcMethod.invoke(src);
                        Object destV = destMethod.invoke(dest);

                        if (isChanged(destV, srcV)) {
                            content.append(getPropertyName(srcMethod.getName())).append(":").append(srcV).append("->")
                                .append(destV).append("; ");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=AriusObjUtils||method=findChanged||errMsg={}", e.getMessage(), e);
        }

        return content.toString();
    }

    /**
     * 找到两个对象不一样的地方
     * 这个方法要求参数bean中必须都是基本类型,且是封装类;如过不是封装类,例如boolean,getter方法不是get开头,而是is开头
     * @param src 源
     * @param dest 目标
     * @return 不一样的字段，格式更加清晰
     */
    public static String findChangedWithClear(Object src, Object dest) {
        if (src == null || dest == null) {
            return "";
        }
        StringBuilder content = new StringBuilder("");
        try {
            Map<String, Method> destMethodMap = Maps.newHashMap();
            for (Method destMethod : dest.getClass().getMethods()) {
                if (isGetter(destMethod)) {
                    destMethodMap.put(destMethod.getName(), destMethod);
                }
            }

            for (Method srcMethod : src.getClass().getMethods()) {
                if (isGetter(srcMethod)) {
                    Method destMethod = destMethodMap.get(srcMethod.getName());
                    if (destMethod != null) {
                        Object srcV = srcMethod.invoke(src);
                        Object destV = destMethod.invoke(dest);

                        if (isChanged(destV, srcV)) {
                            content.append("字段").append(getPropertyName(srcMethod.getName())).append("的原值").append("【")
                                .append(srcV).append("】").append("修改为").append("【").append(destV).append("】")
                                .append("\r\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=AriusObjUtils||method=findChanged||errMsg={}", e.getMessage(), e);
        }

        return content.toString();
    }

    /**
     * 找出两个vo中不同的值
     *
     * @param srcVo                       srcVo
     * @param destVo                      destVo
     * @param apiModelPropertyValueModify apiModelPropertyValueModify
     * @return {@code String}
     */
    public static String findChangedWithClearByBeanVo(Object srcVo, Object destVo,
                                                      Map</*apiModelPropertyValue*/String, /*修改后的apiModelPropertyValue*/String> apiModelPropertyValueModify) {
        if (srcVo == null || destVo == null) {
            return null;
        }
        StringJoiner content = new StringJoiner("");
        try {
            Map<String, String> destMethodMap = Maps.newHashMap();
            for (Field destField : destVo.getClass().getDeclaredFields()) {
                if (destField.isAnnotationPresent(ApiModelProperty.class)) {
                    String apiModelPropertyValue = destField.getAnnotation(ApiModelProperty.class).value();
                    String getValue = getInvokeValue(destField.getName(), destVo);
                    if (StringUtils.isNotBlank(getValue)) {
                        destMethodMap.put(apiModelPropertyValue, getValue);
                    }
                }
            }

            for (Field srcField : srcVo.getClass().getDeclaredFields()) {
                if (srcField.isAnnotationPresent(ApiModelProperty.class)) {
                    String apiModelPropertyValue = srcField.getAnnotation(ApiModelProperty.class).value();
                    String destValue = destMethodMap.get(apiModelPropertyValue);
                    if (destValue != null) {
                        String srcGetValue = getInvokeValue(srcField.getName(), srcVo);
                        String finalApiModelPropertyValue = apiModelPropertyValue;
                        final Optional<String> apiModelPropertyValueModifyOptional = Optional
                            .ofNullable(apiModelPropertyValueModify).map(m -> m.get(finalApiModelPropertyValue));
                        if (apiModelPropertyValueModifyOptional.isPresent()) {
                            apiModelPropertyValue = apiModelPropertyValueModifyOptional.get();
                        }
                        if (isChanged(destValue, srcGetValue)) {
                            content.add("字段").add("【").add(apiModelPropertyValue).add("】").add("的原值").add("【")
                                .add(srcGetValue).add("】").add("修改为").add("【").add(destValue).add("】").add("\r\n");
                        }
                    }

                }

            }
        } catch (Exception e) {
            LOGGER.error("class=AriusObjUtils||method=findChangedWithClearByBeanVo||errMsg={}", e.getMessage(), e);
        }

        return content.toString();
    }

    /**
     * 是否是空
     * @param object
     * @return
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNull(String str) {
        return isBlack(str);
    }

    /**
     * 是否是空
     * @param str
     * @return
     */
    public static boolean isNullStr(String str) {
        return str == null;
    }

    /**
     * 是否是空字符串
     * @param str
     * @return
     */
    public static boolean isBlack(String str) {
        return StringUtils.isBlank(str);
    }

    private static String getInvokeValue(String propertyName, Object bean) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, bean.getClass());
            Object invoke = pd.getReadMethod().invoke(bean);
            return String.valueOf(invoke);
        } catch (Exception e) {
            return null;
        }

    }

    private static boolean isGetter(Method method) {
        String methodName = method.getName();
        return (methodName.startsWith("get")) && !"getClass".equals(methodName)
               && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0
               && isPrimitive(method.getReturnType());
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Character.class || type == Boolean.class
               || type == Byte.class || type == Short.class || type == Integer.class || type == Long.class
               || type == Float.class || type == Double.class || type == Object.class;
    }

    private static String getPropertyName(String name) {
        return name.length() > 3 ? name.substring(3, 4).toLowerCase() + name.substring(4) : "";
    }

    /**
     * 是空字符串或者空
     */
    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * 是空字符串或者空
     */
    public static boolean anyBlank(String... strings) {
        return Arrays.stream(strings).anyMatch(StringUtils::isBlank);
    }

    /**
     * 存在空
     */
    public static boolean isExistBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是空字符串
     */
    public static boolean equalList(List<Object> seq1, List<Object> seq2) {
        if (isNull(seq1) && isNull(seq2)) {
            return true;
        } else if (isNull(seq1) || isNull(seq2) || seq1.size() != seq2.size()) {
            return false;
        }
        for (Object elem : seq1) {
            if (!seq2.contains(elem)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmptyList(List<?> seq) {
        return isNull(seq) || seq.isEmpty();
    }

    public static boolean isEmptySet(Set<?> seq) {
        return isNull(seq) || seq.isEmpty();
    }

    public static boolean isNullOrLessThanZero(Long value) {
        return value == null || value < 0;
    }

    public static boolean isNullOrLessThanZero(Integer value) {
        return value == null || value < 0;
    }

    public static boolean isNullOrLessThanZero(Double value) {
        return value == null || value < 0;
    }
}