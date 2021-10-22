package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.RandomFilledBean;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.component.RSATool;
import com.didichuxing.datachannel.arius.admin.constant.RequestPathThirdpart;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class AriusDataSource {
    public static String publicKey;

    static {
        try {
            publicKey = (String) new AriusClient().get(RequestPathThirdpart.PUBLIC_KEY).getData();
        } catch (IOException e) {
            throw new RuntimeException("get public key failed");
        }
    }

    public static AriusUserInfoDTO ariusUserInfo() throws Exception {
        AriusUserInfoDTO dto = RandomFilledBean.getRandomBeanOfType(AriusUserInfoDTO.class);
        dto.setStatus(1);
        dto.setRole(2);
        RSATool.init();
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String password = Base64.encodeBase64String(cipher.doFinal(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        dto.setPassword(password);
        return dto;
    }
}
