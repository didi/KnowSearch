package com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/28 上午10:08
 * @modified By D10865
 *
 * 加载dsl查询语句工具类
 *
 */
@Component
public class DslLoaderUtil {
    private static final ILog                                     LOGGER  = LogFactory.getLog(DslLoaderUtil.class);
    /**
     * 查询语句容器
     */
    private Map<String/*fileRelativePath*/, String/*dslContent*/> dslsMap = Maps.newHashMap();

    @PostConstruct
    public void init() {
        LOGGER.info("class=DslLoaderUtil||method=init||DslLoaderUtil init start.");
        List<String> dslFileNames = Lists.newLinkedList();

        // 反射获取接口中定义的变量中的值
        Field[] fields = DslsConstant.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            fields[i].setAccessible(true);
            try {
                dslFileNames.add(fields[i].get(null).toString());
            } catch (IllegalAccessException e) {
                LOGGER.error("class=DslLoaderUtil||method=init||errMsg=fail to read {} error. ", fields[i].getName(),
                    e);
            }
        }

        // 加载dsl文件及内容
        for (String fileName : dslFileNames) {
            dslsMap.put(fileName, readDslFileInJarFile(fileName));
        }

        // 输出加载的查询语句
        LOGGER.info("class=DslLoaderUtil||method=init||msg=dsl files count {}", dslsMap.size());
        for (Map.Entry<String/*fileRelativePath*/, String/*dslContent*/> entry : dslsMap.entrySet()) {
            LOGGER.info("class=DslLoaderUtil||method=init||msg=file name {}, dsl content {}", entry.getKey(),
                entry.getValue());
        }

        LOGGER.info("class=DslLoaderUtil||method=init||DslLoaderUtil init finished.");
    }

    /**
     * 获取查询语句
     *
     * @param fileName
     * @return
     */
    public String getDslByFileName(String fileName) {
        return dslsMap.get(fileName);
    }

    /**
     * 获取格式化的查询语句
     *
     * @param fileName
     * @param args
     * @return
     */
    public String getFormatDslByFileName(String fileName, Object... args) {
        String loadDslContent = getDslByFileName(fileName);

        if (StringUtils.isBlank(loadDslContent)) {
            LOGGER.error("class=DslLoaderUtil||method=getFormatDslByFileName||errMsg=dsl file {} content is empty",
                fileName);
            return "";
        }

        // 格式化查询语句
        String dsl = trimJsonBank(String.format(loadDslContent, args));
        // 如果不是线上环境，则输出dsl语句
        LOGGER.debug("class=DslLoaderUtil||method=getFormatDslByFileName||dsl={}", dsl);


        return dsl;
    }

    /**************************************** private method ****************************************************/
    /**
     * 去除json中的空格
     *
     * @param sourceDsl
     * @return
     */
    private String trimJsonBank(String sourceDsl) {
        List<String> dslList = Lists.newArrayList();

        DefaultJSONParser parser = null;
        Object obj = null;
        String dsl = sourceDsl;

        // 解析多个json，直到pos为0
        for (;;) {
            try {
                // 这里需要Feature.OrderedField.getMask()保持有序
                parser = new DefaultJSONParser(dsl, ParserConfig.getGlobalInstance(),
                    JSON.DEFAULT_PARSER_FEATURE | Feature.OrderedField.getMask());
                obj = parser.parse();
            } catch (Exception t) {
                LOGGER.error("class=DslLoaderUtil||method=trimJsonBank||errMsg=parse json {} error. ", dsl, t);
            }
            if (obj == null) {
                break;
            }

            if (obj instanceof JSONObject) {
                dslList.add(JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue));
                int pos = parser.getLexer().pos();
                if (pos <= 0) {
                    break;
                }
                dsl = dsl.substring(pos);
                parser.getLexer().close();
            } else {
                parser.getLexer().close();
                break;
            }
        }

        // 格式化异常或者有多个查询语句，返回原来的查询语句
        if (dslList.isEmpty() || dslList.size() > 1) {
            return sourceDsl;
        }

        return dslList.get(0);
    }

    /**
     * 从jar包中读取dsl语句文件
     *
     * @param fileName
     * @return
     */
    private String readDslFileInJarFile(String fileName) {
        InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream(String.format("dsl/%s", fileName));
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            List<String> lines = Lists.newLinkedList();
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
                return StringUtils.join(lines, "");

            } catch (IOException e) {
                LOGGER.error("class=DslLoaderUtil||method=readDslFileInJarFile||errMsg=read file {} error. ", fileName,
                    e);

                return "";
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(
                        "class=DslLoaderUtil||method=readDslFileInJarFile||errMsg=fail to close file {} error. ",
                        fileName, e);
                }
            }
        } else {
            LOGGER.error("class=DslLoaderUtil||method=readDslFileInJarFile||errMsg=fail to read file {} content",
                fileName);
            return "";
        }
    }
}
