package com.alibaba.mqtt.server.util;

import java.io.IOException;
import java.nio.charset.Charset;

public class Base64Utils {
    public static Charset UTF8 = Charset.forName("UTF-8");
    /**
     * Decode for Base64 string
     *
     *
     * @param str  String need to be decoded
     * @return String decode from the input string
     */
    public static String decode(String str) {
        try {
            return new String(net.iharder.Base64.decode(str), UTF8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Decoding input string exception", e);
        }
    }

    /**
     * Encode a string into Base64 String
     *
     * @param str  String need to be encoded
     * @return     An Base64 string
     */
    public static String encode(String str) {
        return net.iharder.Base64.encodeBytes(str.getBytes(UTF8));
    }

}
