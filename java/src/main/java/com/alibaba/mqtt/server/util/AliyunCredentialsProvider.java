package com.alibaba.mqtt.server.util;

import com.rabbitmq.client.impl.CredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AliyunCredentialsProvider implements CredentialsProvider {
    private static Logger logger = LoggerFactory.getLogger(AliyunCredentialsProvider.class);

    /**
     * Access Key ID.
     */
    private final String accessKeyId;
    /**
     * Access Key Secret.
     */
    private final String accessKeySecret;
    /**
     * security temp token. (optional)
     */
    private final String securityToken;

    /**
     * instanceId
     */
    private final String instanceId;
    private final long ts;

    public AliyunCredentialsProvider(final String accessKeyId, final String accessKeySecret,
                                     final String instanceId, final long ts) {
        this(accessKeyId, accessKeySecret, null, instanceId, ts);
    }
    public AliyunCredentialsProvider(final String accessKeyId, final String accessKeySecret,
                                     final String securityToken, final String instanceId, final long ts) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.securityToken = securityToken;
        this.instanceId = instanceId;
        this.ts = ts;
    }
    @Override
    public String getUsername() {
        if(StringUtils.isNotEmpty(securityToken)) {
            return UserUtils.getUserName(accessKeyId, instanceId, securityToken);
        } else {
            return UserUtils.getUserName(accessKeyId, instanceId);
        }
    }

    @Override
    public String getPassword() {
        try {
            return Tools.macSignature(String.valueOf(ts), accessKeySecret);
        } catch (InvalidKeyException e) {
            logger.error("get password error! check your params, NoSuchAlgorithmException:" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            logger.error("get password error! check your params, invalidKeyException:" + e.getMessage());
        }
        return null;
    }
}
