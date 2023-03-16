package com.alibaba.mqtt.server.util;


import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class UserUtils {
    public static int ACCESS_FROM_USER = 0;

    /***
     * @param ak  aliyun
     * @param instanceId
     * @return
     */
    public static String getUserName(String ak, String instanceId) {
        StringBuffer buf = new StringBuffer();
        return Base64Utils.encode(buf.append(ACCESS_FROM_USER).append(":").append(instanceId).append(":")
                .append(ak).toString());
    }

    /***
     * @param ak  aliyun
     * @param instanceId
     * @param stsToken
     * @return
     */
    public static String getUserName(String ak, String instanceId, String stsToken) {
        StringBuffer buf = new StringBuffer();
        return Base64Utils.encode(buf.append(ACCESS_FROM_USER).
                append(":").append(instanceId)
                .append(":").append(ak).append(":").
                        append(stsToken).toString());
    }

    /***
     *
     * @param sk aliyun secrectKey
     * @return  password
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public static String getPassord(String sk) throws InvalidKeyException, NoSuchAlgorithmException {
        long timestamp = System.currentTimeMillis();
        StringBuffer buf = new StringBuffer();
        String signature = HmacSHA1Utils.hamcsha1(sk.getBytes(StandardCharsets.UTF_8), String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8));
        return Base64Utils.encode(buf.append(signature).append(":").append(timestamp).toString());
    }
}
