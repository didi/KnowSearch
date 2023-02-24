package com.didi.fastdump.persistence.lucene840.dao;

import java.util.Arrays;
import java.util.Base64;

import org.apache.lucene.util.BytesRef;

/**
 * Created by linyunan on 2022/9/15
 */
public class Lucene840UidUtil {
    public static String decodeId(byte[] idBytes) {
        return decodeId(idBytes, 0, idBytes.length);
    }

    public static String decodeId(byte[] idBytes, int offset, int length) {
        if (length == 0) {
            throw new IllegalArgumentException("Ids can't be empty");
        } else {
            int magicChar = Byte.toUnsignedInt(idBytes[offset]);
            switch (magicChar) {
                case 254:
                    return decodeNumericId(idBytes, offset, length);
                case 255:
                    return decodeUtf8Id(idBytes, offset, length);
                default:
                    return decodeBase64Id(idBytes, offset, length);
            }
        }
    }

    private static String decodeNumericId(byte[] idBytes, int offset, int len) {
        assert Byte.toUnsignedInt(idBytes[offset]) == 254;

        int length = (len - 1) * 2;
        char[] chars = new char[length];

        for(int i = 1; i < len; ++i) {
            int b = Byte.toUnsignedInt(idBytes[offset + i]);
            int b1 = b >>> 4;
            int b2 = b & 15;
            chars[(i - 1) * 2] = (char)(b1 + 48);
            if (i == len - 1 && b2 == 15) {
                --length;
                break;
            }

            chars[(i - 1) * 2 + 1] = (char)(b2 + 48);
        }

        return new String(chars, 0, length);
    }

    private static String decodeUtf8Id(byte[] idBytes, int offset, int length) {
        assert Byte.toUnsignedInt(idBytes[offset]) == 255;

        return (new BytesRef(idBytes, offset + 1, length - 1)).utf8ToString();
    }

    private static String decodeBase64Id(byte[] idBytes, int offset, int length) {
        assert Byte.toUnsignedInt(idBytes[offset]) <= 253;

        if (Byte.toUnsignedInt(idBytes[offset]) == 253) {
            idBytes = Arrays.copyOfRange(idBytes, offset + 1, offset + length);
        } else if (idBytes.length != length || offset != 0) {
            idBytes = Arrays.copyOfRange(idBytes, offset, offset + length);
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(idBytes);
    }
}
