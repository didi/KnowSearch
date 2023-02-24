package com.didi.cloud.fastdump.core.classload;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import com.didi.fastdump.adapter.BaseLuceneClient;
import com.didi.fastdump.adapter.lucene.LuceneClientAdapter;

/**
 * Created by linyunan on 2022/8/24
 *
 * 解决钻石依赖问题 提供Jar隔离的加载机制，破坏双亲委派
 * 类的加载是延迟, 定义后续类加载过程原则：
 *  1. 指定类用自定义的appClassLoader，其余都交给extClassLoader（不同 ClassLoader 间的同一个类不可）
 * */
public class CustomJarLoader extends URLClassLoader {
    private final ClassLoader appClassLoader;
    public CustomJarLoader(String[] paths) {
        this(paths, CustomJarLoader.class.getClassLoader());
    }

    @Override
    public void close() {

    }

    public CustomJarLoader(String path) {
        super(getURLs(path), /*extClassLoader*/CustomJarLoader.class.getClassLoader().getParent());
        this.appClassLoader = CustomJarLoader.class.getClassLoader();
    }

    public CustomJarLoader(String[] paths, ClassLoader appClassLoader) {
        super(getURLs(paths), appClassLoader.getParent());
        this.appClassLoader = appClassLoader;
    }

    private static URL[] getURLs(String path) {
        if (null == path || 0 == path.length()) {
            throw new RuntimeException("jar包路径不能为空.");
        }

        List<URL> urls = new ArrayList<>();

        ClassPathResource classPathResource = new ClassPathResource(path);
        File tempJarPath;
        try {
            InputStream inputStream = classPathResource.getInputStream();
            tempJarPath = File.createTempFile("temp-jar", ".jar");
            FileUtils.copyInputStreamToFile(inputStream, tempJarPath);
            URL url = tempJarPath.toURI().toURL();
            urls.add(url);
        } catch (IOException e) {
            throw new RuntimeException(String.format("jar包[%s]读取失败, detail:%s", path, e.getMessage()));
        }

        return urls.toArray(new URL[0]);
    }

    private static URL[] getURLs(String[] paths) {
        if (null == paths || 0 == paths.length) {
            throw new RuntimeException("jar包路径不能为空.");
        }

        List<String> dirs = new ArrayList<>();
        for (String path : paths) {
            dirs.add(path);
            CustomJarLoader.collectDirs(path, dirs);
        }

        List<URL> urls = new ArrayList<>();
        for (String path : dirs) {
            urls.addAll(doGetURLs(path));
        }
        return urls.toArray(new URL[0]);
    }

    private static void collectDirs(String path, List<String> collector) {
        if (null == path || "".equalsIgnoreCase(path)) {
            return;
        }

        File current = new File(path);
        if (!current.exists() || !current.isDirectory()) {
            return;
        }

        for (File child : Objects.requireNonNull(current.listFiles())) {
            if (!child.isDirectory()) { continue;}

            collector.add(child.getAbsolutePath());
            collectDirs(child.getAbsolutePath(), collector);
        }
    }

    private static List<URL> doGetURLs(final String path) {
        if (null == path || "".equalsIgnoreCase(path)) {
            throw new RuntimeException("jar包路径不能为空.");
        }

        File jarPath = new File(path);
        if (!jarPath.exists() || !jarPath.isDirectory()) {
            throw new RuntimeException("jar包路径必须存在且为目录.");
        }

        /* set filter */
        FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");
        /* iterate all jar */
        File[] allJars = new File(path).listFiles(jarFilter);
        assert allJars != null;

        List<URL> jarURLs = new ArrayList<>(allJars.length);
        for (File allJar : allJars) {
            try {
                jarURLs.add(allJar.toURI().toURL());
            } catch (Exception e) {
                throw new RuntimeException("系统加载jar包出错", e);
            }
        }
        return jarURLs;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // 注意这里, 需要使用ExtClassLoad加载的包
        return !name.startsWith("com.didi.fastdump.adapter")
                && !name.startsWith("com.didi.cloud.fastdump.common.bean.adapter")
                && !name.startsWith("com.alibaba.fastjson")
                ? /*extClassLoader*/super.loadClass(name, true)
                : this.appClassLoader.loadClass(name);
    }

    public static void main(String[] args) throws Exception {
        // jar 2
        // 注意这里是使 lucene 5.5.0 上下文类加载器为空，父加载器为空，通过子加载器实现类加载，否则会有类冲突
        Thread.currentThread().setContextClassLoader(null);
        /*// jar1
        String jar1 = "fastdump-persistence/packege/lucene/550";
        String indexPath = "/Users/didi/es-package/elasticsearch-2.3.3/data/test-2.3.3/nodes/0/indices/twitter/1/index";
        CustomJarLoader newJarLoader1 = new CustomJarLoader(new String[] { jar1 });
        Class<?> lucene550ClientClass = newJarLoader1
                .loadClass("com.didi.fastdump.persistence.lucene550.dao.Lucene550DAO");
        LuceneClientAdapter lucene550ClientInstance = (LuceneClientAdapter) lucene550ClientClass.newInstance();
        Integer maxDoc1 = lucene550ClientInstance.getMaxDoc(indexPath);
        System.out.println(maxDoc1);*/

        String jar2 = "fastdump-persistence/packege/lucene/760";
        String indexPath2 = "/Users/didi/es-package/elasticsearch-6.6.1/data/nodes/0/indices/PkYCfxjpScWXbX_s2GVVlA/2/index";
        CustomJarLoader newJarLoader = new CustomJarLoader(new String[] { jar2 });
        Class<?> lucene760ClientClass = newJarLoader
                .loadClass("com.didi.fastdump.persistence.lucene760.dao.Lucene760DAO");
        BaseLuceneClient lucene760ClientInstance = (BaseLuceneClient) lucene760ClientClass.newInstance();

        Integer maxDoc2 = lucene760ClientInstance.getMaxDoc(indexPath2);
        System.out.println(maxDoc2);

        // jar3
        String jar3 = "fastdump-persistence/packege/lucene/840";
        String indexPath3 = "/Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/u--GWFwzTsKJHFld6D8vng/0/index";
        CustomJarLoader newJarLoader2 = new CustomJarLoader(new String[] { jar3 });
        Class<?> lucene840ClientClass = newJarLoader2
                .loadClass("com.didi.fastdump.persistence.lucene840.dao.Lucene840DAO");
        LuceneClientAdapter lucene840ClientInstance = (LuceneClientAdapter) lucene840ClientClass.newInstance();
        Integer maxDoc3 = lucene840ClientInstance.getMaxDoc(indexPath3);
        System.out.println(maxDoc3);
    }
}
