package com.didi.cloud.fastdump.core.factory;

import com.didi.cloud.fastdump.common.exception.NotSupportESVersionException;
import com.didi.cloud.fastdump.core.classload.CustomJarLoader;
import com.didi.fastdump.adapter.lucene.LuceneClientAdapter;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.didi.cloud.fastdump.core.factory.LuceneClientFactory.ClientVersionEnum.*;

/**
 * Created by linyunan on 2022/8/10
 *
 * LuceneClient工厂，负责加载不同版本的LuceneClient
 */
@Component
public class LuceneClientFactory extends BaseClientFactory<LuceneClientAdapter> {
    private static final Map<String/*luceneVersion*/, LuceneClientAdapter> VERSION_2_LUCENE_CLIENT_MAP = Maps.newConcurrentMap();

    @Override
    public LuceneClientAdapter getClientByType(String esVersion) throws NotSupportESVersionException {
        switch (ClientVersionEnum.valueOfESVersion(esVersion)) {
            case ES_230_LUCENE_550_CLIENT:
                return VERSION_2_LUCENE_CLIENT_MAP.computeIfAbsent(ES_230_LUCENE_550_CLIENT.getLuceneVersion(),
                        a -> buildLucene550Client());
            case ES_660_LUCENE_760_CLIENT:
            case ES_670_LUCENE_760_CLIENT:
                return VERSION_2_LUCENE_CLIENT_MAP.computeIfAbsent(ES_660_LUCENE_760_CLIENT.getLuceneVersion(),
                        a -> buildLucene760Client());
            case ES_760_LUCENE_840_CLIENT:
                return VERSION_2_LUCENE_CLIENT_MAP.computeIfAbsent(ES_760_LUCENE_840_CLIENT.getLuceneVersion(),
                        a -> buildLucene840Client());
            default:
                throw new NotSupportESVersionException(esVersion);
        }
    }

    private LuceneClientAdapter buildLucene840Client() {
        String relativePathOf840    = "lucene/840/lucene840-1.0-SNAPSHOT-allinone.jar";
        String daoClassPathOf840    = "com.didi.fastdump.persistence.lucene840.dao.Lucene840DAO";
        return commonGetLuceneClient(relativePathOf840, daoClassPathOf840);
    }

    private LuceneClientAdapter buildLucene760Client() {
        String relativePathOf760    = "lucene/760/lucene760-1.0-SNAPSHOT-allinone.jar";
        String daoClassPathOf760    = "com.didi.fastdump.persistence.lucene760.dao.Lucene760DAO";
        return commonGetLuceneClient(relativePathOf760, daoClassPathOf760);
    }

    private LuceneClientAdapter buildLucene550Client() {
        String relativePathOf550    = "lucene/550/lucene550-1.0-SNAPSHOT-allinone.jar";
        String daoClassPathOf550    = "com.didi.fastdump.persistence.lucene550.dao.Lucene550DAO";
        return commonGetLuceneClient(relativePathOf550, daoClassPathOf550);
    }

    private LuceneClientAdapter commonGetLuceneClient(String relativePathOfDirectory, String clientClassPath) {
        // 注意这里是使 lucene 5.5.0 上下文类加载器为空，父加载器为空，通过子加载器实现类加载，否则会有类冲突
        Thread.currentThread().setContextClassLoader(null);
        try (CustomJarLoader newJarLoader = new CustomJarLoader(relativePathOfDirectory)){
            Class<?> luceneClass = newJarLoader.loadClass(clientClassPath);
            return (LuceneClientAdapter) luceneClass.newInstance();
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public enum ClientVersionEnum {
        UNKNOWN("", ""),
        ES_230_LUCENE_550_CLIENT("2.3.3", "5.5.0"),
        ES_660_LUCENE_760_CLIENT("6.6.1", "7.6.0"),
        ES_670_LUCENE_760_CLIENT("6.7.0", "7.6.0"),
        ES_760_LUCENE_840_CLIENT("7.6.0","8.4.0");

        private final String esVersion;
        private final String luceneVersion;
        public String getESVersion() { return esVersion;}
        public String getLuceneVersion() { return luceneVersion;}
        ClientVersionEnum(String esVersion, String luceneVersion) {
            this.esVersion     = esVersion;
            this.luceneVersion = luceneVersion;
        }

        public static ClientVersionEnum valueOfESVersion(String esVersion) {
            if (null == esVersion) {
                return ClientVersionEnum.UNKNOWN;
            }
            for (ClientVersionEnum typeEnum : ClientVersionEnum.values()) {
                if (esVersion.equals(typeEnum.getESVersion())) {
                    return typeEnum;
                }
            }
            return ClientVersionEnum.UNKNOWN;
        }
    }
}
