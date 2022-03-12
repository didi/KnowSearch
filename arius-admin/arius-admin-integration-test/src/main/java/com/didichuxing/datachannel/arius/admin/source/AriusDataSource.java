package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.RandomFilledBean;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.config.AriusConfigStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.component.RSATool;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;
import com.didichuxing.datachannel.arius.admin.constant.RequestPathThirdpart;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class AriusDataSource {
    private static String publicKey;

    private AriusDataSource() {}

    static {
        try {
            publicKey = (String) new AriusClient().get(RequestPathThirdpart.PUBLIC_KEY).getData();
        } catch (IOException e) {
            throw new AriusRunTimeException("get public key failed", ResultType.FAIL);
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

    public static AriusConfigInfoDTO ariusConfigInfoDTOFactory() {
        AriusConfigInfoDTO ariusConfigInfoDTO = RandomFilledBean.getRandomBeanOfType(AriusConfigInfoDTO.class);
        ariusConfigInfoDTO.setStatus(AriusConfigStatusEnum.NORMAL.getCode());
        ariusConfigInfoDTO.setDimension(1);
        return ariusConfigInfoDTO;
    }
}
