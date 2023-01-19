package com.didichuxing.datachannel.arius.admin.common.util;

import static java.util.regex.Pattern.compile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.op.manager.IpPort;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/18 上午11:48
 * @modified By D10865
 */
public class CommonUtils {

    private static final ILog   LOGGER = LogFactory.getLog(CommonUtils.class);

    private static final String REGEX  = ",";
    private static final String ELASTICSEARCH_YML = "elasticsearch.yml";
    private static final String APPLICATION_PROPERTIES="application.properties";

    private CommonUtils() {
    }

    /**
     * 获取MD5值
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取文件的md5
     * @param file
     * @return
     */
    public static String getMD5(MultipartFile file) {
        try {
            //获取文件的byte信息
            byte[] uploadBytes = file.getBytes();
            // 拿到一个MD5转换器
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(uploadBytes);
            //转换为16进制
            return new BigInteger(1, digest).toString(16);
        } catch (Exception e) {
            LOGGER.error("class=CommonUtils||method=getMD5||msg=获取文件的md5失败:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 保留指定的小数位 四舍五入
     * @param data    需要转换的数据
     * @param decimal 保留小数的位数 默认是2
     * @return
     */
    public static double formatDouble(double data, int decimal) {
        if (decimal < 0) {
            decimal = 2;
        }

        BigDecimal b = BigDecimal.valueOf(data);
        return b.setScale(decimal, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double divideIntAndFormatDouble(int v1, int v2, int scale, int multiply) {
        BigDecimal v1Decimal = new BigDecimal(v1);
        BigDecimal v2Decimal = new BigDecimal(v2);
        BigDecimal bigDecimal = new BigDecimal(multiply);

        BigDecimal greenDivide = v1Decimal.divide(v2Decimal, scale, 1);
        return greenDivide.multiply(bigDecimal).doubleValue();
    }

    public static double divideDoubleAndFormatDouble(double v1, double v2, int scale, int multiply) {
        BigDecimal v1Decimal = new BigDecimal(v1);
        BigDecimal v2Decimal = new BigDecimal(v2);
        BigDecimal bigDecimal = new BigDecimal(multiply);
  
        BigDecimal greenDivide = v2==0?new BigDecimal(0):v1Decimal.divide(v2Decimal, scale, 1);
        return greenDivide.multiply(bigDecimal).doubleValue();
    }

    public static String toHex(byte[] bytes) {

        final char[] hexDigits = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(hexDigits[(bytes[i] >> 4) & 0x0f]);
            ret.append(hexDigits[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    public static String strList2String(List<String> strList) {
        if (strList == null || strList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String elem : strList) {
            if (!StringUtils.hasText(elem)) {
                continue;
            }
            sb.append(elem).append(REGEX);
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
    }

    public static List<String> string2StrList(String str) {
        if (!StringUtils.hasText(str)) {
            return new ArrayList<>();
        }
        List<String> strList = new ArrayList<>();
        for (String elem : str.split(REGEX)) {
            if (!StringUtils.hasText(elem)) {
                continue;
            }
            strList.add(elem);
        }
        return strList;
    }

    /**
     * 将 .zip 压缩文件中的目标文件转化为输入流
     * @param in zip 压缩文件的输入流
     * @param targetFileName 目标文件名
     * @return 该目标文件的输入流
     */
    public static InputStream unZip(InputStream in, String targetFileName) {
        if (in == null || targetFileName == null) {
            return null;
        }
        byte[] bytes = new byte[1024];
        ZipArchiveEntry zipEntry;
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(in);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while ((zipEntry = zip.getNextZipEntry()) != null) {
                // 该 entry 在压缩包中的 完整路径 + 文件名
                String entryName = zipEntry.getName();
                // 该 entry 的文件名
                String fileName = entryName.substring(entryName.lastIndexOf("/") + 1);

                if (targetFileName.equals(fileName)) {
                    // 找到了目标文件
                    while (true) {
                        int len = zip.read(bytes);
                        if (len <= 0) {
                            break;
                        }
                        bos.write(bytes, 0, len);
                    }
                    return new ByteArrayInputStream(bos.toByteArray());
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=CommonUtils||method=unZip||msg=fail to unZip:", e);
        }
        return null;
    }

    /**
     * 将 .tar 压缩文件中的目标文件转化为输入流
     * @param in .tar 压缩文件的输入流
     * @param targetFileName 目标文件名
     * @return 该目标文件的输入流
     */
    public static InputStream unTar(InputStream in, String targetFileName) {
        if (in == null || targetFileName == null) {
            return null;
        }
        byte[] bytes = new byte[1024];
        TarArchiveEntry tarEntry;
        try (TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(in));
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while ((tarEntry = tar.getNextTarEntry()) != null) {
                // 该 entry 在压缩包中的 完整路径 + 文件名
                String entryName = tarEntry.getName();
                // 该 entry 的文件名
                String fileName = entryName.substring(entryName.lastIndexOf("/") + 1);

                if (targetFileName.equals(fileName)) {
                    // 找到了目标文件
                    while (true) {
                        int len = tar.read(bytes);
                        if (len <= 0) {
                            break;
                        }
                        bos.write(bytes, 0, len);
                    }
                    return new ByteArrayInputStream(bos.toByteArray());
                }
            }
        } catch (Exception e) {
            LOGGER.error("class=CommonUtils||method=unTar||msg=fail to unTar:", e);
        }
        return null;
    }

    public static Long monitorTimestamp2min(Long timestamp) {
        return timestamp - timestamp % 60000;
    }

    /**
     * 字符串追加
     *
     * @param items
     * @return
     */
    public static String strConcat(List<String> items) {
        StringBuilder stringBuilder = new StringBuilder(128);
        boolean isFirstItem = true;

        for (String item : items) {
            if (isFirstItem) {
                stringBuilder.append(String.format("\"%s\"", item));
                isFirstItem = false;
            } else {
                stringBuilder.append(",").append(String.format("\"%s\"", item));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 判断是否为合法IP
     * @return the ip
     */
    public static boolean checkIp(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        String rexp1 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])\\.";
        String rexp2 = "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])\\.";
        String rexp3 = "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";

        Pattern pat = Pattern.compile(rexp1 + rexp2 + rexp2 + rexp3);
        Matcher mat = pat.matcher(addr);

        return mat.find();

    }

    /**
     * 生成固定长度的随机字符串
     */
    public static String randomString(int length) {
        char[] value = new char[length];
        for (int i = 0; i < length; i++) {
            value[i] = randomWritableChar();
        }
        return new String(value);
    }

    /**
     * 随机生成单个随机字符
     */
    public static char randomWritableChar() {
        Random random = new Random();
        return (char) (33 + random.nextInt(94));
    }

    public static String getUniqueKey(String... arg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arg.length; i++) {
            if (i == (arg.length - 1)) {
                sb.append(arg[i]);
            } else {
                sb.append(arg[i]).append("@");
            }
        }
        return sb.toString();
    }
     public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = Maps.newConcurrentMap();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 避免模糊查询把查询条件中的"% _"当作通配符处理（造成结果是全量查询）
     */
    public static String sqlFuzzyQueryTransfer(String str){
        if(!StringUtils.isEmpty(str) && str.contains("%")){
            str = str.replaceAll("%", "\\\\%");
        }
        if(!StringUtils.isEmpty(str) && str.contains("_")){
            str = str.replaceAll("_","\\\\_");
        }
        return str;
    }

    public static List<IpPort> generalGroupConfig2ESIpPortList(GeneralGroupConfig config) {
        final String elasticsearchYml =
            JSON.parseObject(config.getFileConfig()).getString(ELASTICSEARCH_YML);
        //获取ip下最小的端口号
        final Integer port = getESPortByHttpPort(getESHttpPort(elasticsearchYml));
        //获取
        final JSONObject jsonObject = JSON.parseObject(config.getProcessNumConfig());
        final String     hosts      = config.getHosts();
        List<IpPort>     ipPorts    = Lists.newArrayList();
        for (String host : org.apache.commons.lang3.StringUtils.split(hosts, ",")) {
            final Integer processNum = jsonObject.getInteger(host);
            final IpPort ipPort = IpPort.builder().minPort(port).maxPort(port + processNum).ip(host)
                .build();
            ipPorts.add(ipPort);
        }
        return ipPorts;

    }

    /**
     * 一般分组config2网关知识产权港口列表
     *
     * @param config 配置
     * @return {@link List}<{@link IpPort}>
     */
    public static List<IpPort> generalGroupConfig2GatewayIpPortList(GeneralGroupConfig config) {
        final String applicationProperties =
            JSON.parseObject(config.getFileConfig()).getString(APPLICATION_PROPERTIES);
        //获取ip下最小的端口号
        final Integer port = Integer.parseInt(getGatewayHttpPort(applicationProperties));
        //获取
        final JSONObject jsonObject = JSON.parseObject(config.getProcessNumConfig());
        final String     hosts      = config.getHosts();
        List<IpPort>     ipPorts    = Lists.newArrayList();
        for (String host : org.apache.commons.lang3.StringUtils.split(hosts, ",")) {
            final Integer processNum = jsonObject.getInteger(host);
            final IpPort ipPort = IpPort.builder().minPort(port).maxPort(port + processNum).ip(host)
                .build();
            ipPorts.add(ipPort);
        }
        return ipPorts;
    }



    		/**
		 * 它接受一个字符串并返回一个字符串
		 *
		 * @param fileConfig 文件的内容。
		 * @return http 服务器的端口号。
		 */
        public static String getESHttpPort(String fileConfig) {
            Matcher matcher = compile("http.port:\\s+\\d+").matcher(fileConfig);
            if (matcher.find()) {
                return matcher.group();
            }
            Matcher matcher2 = compile("http:\\\\n\\s*port:\\s*\\d*").matcher(fileConfig);
            if (matcher2.find()) {
                return matcher2.group();
            }
            return null;
        }
    /**
     * > 它接受一个字符串，并返回它在该字符串中找到的第一个数字
     *
     * @param httpPort 应用程序运行的端口。
     * @return httpPort 字符串的端口号。
     */
    public static Integer getESPortByHttpPort(String httpPort) {
        Matcher matcher = compile("\\d+").matcher(httpPort);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return null;
    }

    /**
     * 获取gateway 端口号
     *
     * @param applicationProperties 应用程序属性
     * @return {@link String}
     */
    private static String getGatewayHttpPort(String applicationProperties) {
        final Matcher matcher = compile("gateway.httpTransport.port=\\d+").matcher(
            applicationProperties);
        if (matcher.find()) {
            final Matcher portMat = compile("\\d+").matcher(
                matcher.group());
            if (portMat.find()) {
                return portMat.group(0);
            }
        }
        return "0";
    }

}