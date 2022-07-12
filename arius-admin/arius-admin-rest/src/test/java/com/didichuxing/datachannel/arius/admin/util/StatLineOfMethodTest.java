package com.didichuxing.datachannel.arius.admin.util;

import com.didichuxing.datachannel.arius.admin.common.util.YamlUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wuxuan
 * @Date 2022/7/12
 */
public class StatLineOfMethodTest {
    /**
     * 将该类运行时抛出的异常记录到日志中
     */
    private static final Logger LOGGER               = LoggerFactory.getLogger(StatLineOfMethodTest.class);
    /**
     * 统计项目中有多少超过规定代码行数的方法
     */
    private int           exceedMethod         = 0;
    /**
     * 统计项目中有多少方法
     */
    private int           methodNumber         = 0;
    /**
     * 记录项目中的类
     */
    private List<Class> classes              = new ArrayList<>();
    /**
     * 记录项目中类的各个方法的信息
     */
    private List<InfoOfMethod> infoOfMethods = new ArrayList<>();
    /**
     * 规定的代码行数
     */
    private int standardLineOfMethod ;
    /**
     * 输出统计代码行的文件路径
     */
    public static final String  STORE_FILE           = "src/test/java/com/didichuxing/datachannel/arius/admin/statMethodLine.txt";
    /**
     * 项目的根路径
     */
    public static final String  FILE_OF_ADMIN        = getFileOfAdmin();
    /**
     * 包的前缀
     */
    public static final String  PREFIX_OF_PACKAGE    = "com/didichuxing";

    /**
     * 获得项目的根路径
     */
    public static String getFileOfAdmin() {
        String filepath = new File("").getAbsolutePath();
        File file = new File(filepath);
        filepath = file.getParent();
        return filepath;
    }

    /**
     * 初始化创建统计代码行数的文件
     */
    @BeforeEach
    private void setup() throws IOException {
        initProperties();
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(STORE_FILE);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("本文本用于记录项目中代码行数超过60行的方法(其中方法中的空行以及注释所在行的行数都记录在内)");
            bufferedWriter.newLine();
            bufferedWriter.write("项目中代码行数超过60行的方法记录如下：");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.write(String.format("%-40s", "类名"));
            bufferedWriter.write(String.format("%-45s", "方法名"));
            bufferedWriter.write(String.format("%-8s", "方法行数"));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (Exception exception) {
            LOGGER.error("class=StatLineOfMethodTest||method=setup||msg=open and write file fail");
        } finally {
            bufferedWriter.close();
            fileWriter.close();
        }
    }

    @AfterEach
    private void end() throws IOException {
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(STORE_FILE, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.newLine();
            bufferedWriter.write("据统计，项目中包含的类的个数为:");
            bufferedWriter.write(String.format("【" + classes.size() + "】"));
            bufferedWriter.newLine();
            bufferedWriter.write("其中，项目中包含的方法个数为:");
            bufferedWriter.write(String.format("【" + methodNumber + "】"));
            bufferedWriter.write("，代码行数超过60行的方法总数为:");
            bufferedWriter.write(String.format("【" + exceedMethod + "】"));
            bufferedWriter.newLine();
            bufferedWriter.write("项目中超过60行的方法占比为:");
            bufferedWriter.write(String.format("%.3f%%", (double) exceedMethod / methodNumber * 100));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception exception) {
            LOGGER.error("class=StatLineOfMethodTest||method=end||msg=open and write file fail");
        } finally {
            bufferedWriter.close();
            fileWriter.close();
        }
    }

    /**
     * 从配置文件中取出规定的不能超过的标准行数赋值给standardLineOfMethod变量
     */
    private void initProperties() throws IOException {
        String[] ymlPaths = {"arius-admin-rest", "target", "classes", "application.yml"};
        String path = System.getProperty("user.dir");
        path = path.substring(0, path.lastIndexOf(File.separator) + 1) + StringUtils.join(ymlPaths, File.separator);
        String standardLine = YamlUtil.getValue(path, "stat.method.line.standardLineOfMethod");
        if (StringUtils.isEmpty(standardLine)){
            throw new RuntimeException("统计代码行需要规定的代码行数，请配置文件application.yml中设置：stat.method.line.standardLineOfMethod");
        } else {
            standardLineOfMethod = Integer.parseInt(YamlUtil.getValue(path, "stat.method.line.standardLineOfMethod"));
        }
    }

    /**
     * 统计项目中各个类各个方法的代码行数，并对超过标准行数的方法进行记录
     */
    @Test
    public void statNumForMethodTest() {
        try {
            File file = new File(FILE_OF_ADMIN);
            // 调用函数遍历目录下的所有java文件
            ergodicDir(file);
            for (Class c : classes) {
                // 不统计测试类的方法
                if (c.getName().contains("test") || c.getName().contains("Test")) {
                    continue;
                }
                ClassPool pool = ClassPool.getDefault();
                // 获取一个ctClass对象
                CtClass ctClass = pool.get(c.getName());
                Method[] methods = c.getDeclaredMethods();
                for (Method method : methods) {
                    // 为了防止流水线进行jacoco命令测试出现异常，这里进行try..catch进行捕获
                    try {
                        methodNumber = methodNumber + 1;
                        CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
                        int beginLineOfMethod = ctMethod.getMethodInfo().getLineNumber(0);
                        int endLineOfMethod = ctMethod.getMethodInfo().getLineNumber(10000);
                        int linenumber = endLineOfMethod - beginLineOfMethod;
                        if (linenumber >= standardLineOfMethod ) {
                            exceedMethod = exceedMethod+1 ;
                            InfoOfMethod infoOfMethod = new InfoOfMethod(linenumber,method.getName(),c.getSimpleName());
                            infoOfMethods.add(infoOfMethod);
                        }
                    } catch (Exception exception) {
                        LOGGER.warn(
                                "class=StatLineOfMethodTest||method=statNumForMethodTest||msg=current capture method exception");
                    }
                }
            }
            recordExceedMethodToFile();
        } catch (Exception ex) {
            LOGGER.error("class=StatLineOfMethodTest||method=statNumForMethodTest||msg=open file fail");
        }
    }

    /**
     * 将超过标准行数的方法按照行数从高到低排序并记录到文件中
     */
    public void recordExceedMethodToFile() throws IOException {
        Collections.sort(infoOfMethods);
        for (InfoOfMethod infoOfMethod : infoOfMethods){
            BufferedWriter bufferedWriter = null;
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(STORE_FILE, true);
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(String.format("%-40s", infoOfMethod.className));
                bufferedWriter.write(String.format("%-45s", infoOfMethod.methodName));
                bufferedWriter.write(String.format("%8d", infoOfMethod.methodLine));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }catch (Exception exception) {
                LOGGER.error("class=StatLineOfMethodTest||method=recordExceedMethodToFile||msg=open and write file fail");
            } finally {
                fileWriter.close();
                bufferedWriter.close();
            }
        }
    }

    /**
     * 遍历目录下的所有java文件
     *
     * @param dir
     */
    private void ergodicDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    ergodicDir(file);
                }

                if (file.isFile() && file.getName().endsWith("java") && file.length() > 0) {
                    // 截取出className 将路径分割符替换为.（windows是\ linux、mac是/）
                    String filePath = file.getPath();
                    // 判断是否是我们所需要统计的包名
                    if (filePath.contains(PREFIX_OF_PACKAGE)) {
                        filePath = filePath.substring(filePath.indexOf(PREFIX_OF_PACKAGE)).replace("/", ".");
                        filePath = filePath.replaceAll("\\.java", "");
                        try {
                            classes.add(Class.forName(filePath));
                        } catch (Exception e) {
                            LOGGER.warn("class=StatLineOfMethodTest||method=ergodicDir||msg=Class didn't found");
                        }
                    }
                }

            }
        }
    }

    /**
     * 创建记录方法相关信息的类
     */
    public class InfoOfMethod implements Comparable<InfoOfMethod> {
        int methodLine;
        String methodName;
        String className;
        public InfoOfMethod(int lineOfMethod,String nameOfMethod,String nameOfClass){
            this.methodLine = lineOfMethod;
            this.className = nameOfClass;
            this.methodName = nameOfMethod;
        }
        @Override
        public int compareTo(InfoOfMethod infoOfMethod) {  //重写compareTo方法
            return infoOfMethod.methodLine-this.methodLine;  //  根据methodLine进行降序排序
        }
    }
}
