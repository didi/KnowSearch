package com.didichuxing.datachannel.arius.admin.common.component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
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

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Maps;

/**
 * Created by linyunan on 2021-06-24
 *
 * 暂且不考虑动态钥匙, 仅存在一对
 */
@Component
public class RSATool {

    private static final ILog          LOGGER         = LogFactory.getLog(RSATool.class);

    private static Map<String, String> keyMap         = Maps.newHashMap();

    private static final int           KEY_MAX_LENGTH = 1024;

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

            keyMap.put("publicKey", publicKeyString);
            keyMap.put("privateKey", privateKeyString);
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

            //RAS加密
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
