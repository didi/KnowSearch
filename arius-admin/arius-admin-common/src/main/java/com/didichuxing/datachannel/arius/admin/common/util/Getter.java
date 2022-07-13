package com.didichuxing.datachannel.arius.admin.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 *
 * @author d06679
 * @date 2019/3/21
 */
public class Getter {

    private Getter() {
    }

    private static final ILog LOGGER = LogFactory.getLog(Getter.class);

    public static <T> T withDefault(T value, T defaultValue) {
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public static String strWithDefault(Object value, String defaultValue) {
        if (value != null) {
            return String.valueOf(value);
        }
        return defaultValue;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String getHtmlFileInJarFile(String fileName) {
        InputStream inputStream = Getter.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder("");
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                LOGGER.error("class=Getter||method=readMailHtmlFileInJarFile||errMsg=read file {} error. ", fileName,
                    e);
                return "";
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("class=Getter||method=readMailHtmlFileInJarFile||errMsg=fail to close file {} error. ",
                        fileName, e);
                }
            }
        } else {
            LOGGER.error(
                "class=Getter||class=MailTool||method=readMailHtmlFileInJarFile||errMsg=fail to read file {} content",
                fileName);
            return "";
        }
    }
}
