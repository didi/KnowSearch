package com.didichuxing.datachannel.arius.admin.common.component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * Created by linyunan on 2021-06-24
 *
 * 暂且不考虑动态钥匙, 仅存在一对
 */
@Component
public class RSATool {

    private static final ILog LOGGER         = LogFactory.getLog(RSATool.class);

    private static Map<String, String> keyMap         = Maps.newHashMap();

    private static final int           KEY_MAX_LENGTH = 1024;

    /**
     * 为了解决部署多个admin情况下，产生多对公密钥问题
     */
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDetlo800LsRDlotqocHticzdCpcMawEFlk0g9tNFNAEsHVFixD5RY378S4cGlDuoFBQU4/B0HG/Z8Vhu0hC7pO56wAYu0zcDWuU6a/N5b1aG8p3AtwbjOQot4qvtCa5VjW/xRvEXxt7dDNpCOs3OWOjtN3usZQYfiDHrTMpKGMwIDAQAB";
    private static final String PRIVATE_KEY = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMN62WjzTQuxEOWi2qhwe2JzN0KlwxrAQWWTSD200U0ASwdUWLEPlFjfvxLhwaUO6gUFBTj8HQcb9nxWG7SELuk7nrABi7TNwNa5Tpr83lvVobyncC3BuM5Ci3iq+0JrlWNb/FG8RfG3t0M2kI6zc5Y6O03e6xlBh+IMetMykoYzAgMBAAECgYAYtYKdvYNTowIbxMYW3ID8JMwrZICQ9RRPHprXEfAS56KUPpknaGy7/yxL2rV7g7MZB1VtqpCQwXwao1fFLaAwKoMt/exdeMAee18tBIyLKlKvHyOcoU49s0R0qq4coNxjn20EyhEM8Jsz3yoY50qsBNCVDLp0+6mikvZSlsonYQJBAOUh8o3Z3PMeAvd4R2oqXH5vFSaeTTf+G/BusqLVx59dQ0zym1yuZHedktgmcN0QJTrejJyC8GIqyuWX+fm7EI0CQQDaZri4eT9n/67vxQI+uPNiovkH19FOLuwf3ADGLOryA1P7PX/D6GywdYSJfoKpaCjynJrGkCgwXz8PX0cUGyG/AkEAvij8WklzG7nOkH8cFbnuZWffT9uVDmEv4sycKJPvRg1qq1O1KKf67WKy2fydMnoRy9ejLslkorQNC5qjdWSQiQJBAJ++KHgNfxoTQqHvSDAWWTaZG2roKRh1a5H1+b751bLESmXmWpAxbWY33oQuu2UrYV/qua5ofGy9DcOxnayQF8MCQQCcEeTqKC4CaBTv9hc6HreWVH1bruXDd2o+cudHl98BTsJjbXU6lzW0YreUot+jQ0VufU4UvGZ/8ZUn5cgTBN+/";

    @PostConstruct
    public static void init() {
        //基于RSA算法, 初始化密钥对生成器, 密钥大小为96-1024位
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(KEY_MAX_LENGTH, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();

            PrivateKey privateKey   = keyPair.getPrivate();
            PublicKey  publicKey    = keyPair.getPublic();
            String publicKeyString  = new String(Base64.encodeBase64(publicKey.getEncoded()));
            String privateKeyString = new String(Base64.encodeBase64(privateKey.getEncoded()));
            // 暂不使用每次重启admin后，重新生成的公密钥
            keyMap.put("publicKey", PUBLIC_KEY);
            keyMap.put("privateKey", PRIVATE_KEY);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("class=RSATool||method=init||msg={}", e.getMessage());
        }
    }

    public String getPublicKey() {
        return keyMap.get("publicKey");
    }

    public String getPrivateKey() {
        return keyMap.get("privateKey");
    }

    /**
     * RSA公钥加密
     *
     * @param str        加密字符串
     * @return           密文
     */
    public String encrypt(String str) {
        String outStr = null;

        try {
            //base64编码的公钥
            byte[] decoded = Base64.decodeBase64(getPublicKey());
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));

            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        } catch (Exception e) {
            LOGGER.error("class=RSATool||method=encrypt||msg={}", e.getMessage());
        }

        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param str          加密字符串
     * @return             明文
     */
    public Result<String> decrypt(String str) {
        if (AriusObjUtils.isNull(str)) {
            return Result.buildFail("加密字符串为空");
        }

        String outStr;
        try {
            //Base64解码加密后的字符串
            byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));

            //Base64编码的私钥
            byte[] decoded = Base64.decodeBase64(getPrivateKey());
            PrivateKey priKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));

            //RSA解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            outStr = new String(cipher.doFinal(inputByte));
        } catch (Exception e) {
            LOGGER.error("class=RSATool||method=encrypt||msg={}", e.getMessage());
            return Result.buildFail("解密失败, 检查是否为加密字符串");
        }

        return Result.build(Boolean.TRUE, outStr);
    }
}
