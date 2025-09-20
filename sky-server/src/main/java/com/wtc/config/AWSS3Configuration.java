package com.wtc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wtc.properties.AWSS3Properties;
import com.wtc.utils.AWSS3Util;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AWSS3Configuration {
    @Bean
    @ConditionalOnMissingBean
    public AWSS3Util awss3Util(AWSS3Properties AWSS3Properties) {
        return new AWSS3Util(AWSS3Properties.getEndpoint(),
                AWSS3Properties.getAccessKeyId(),
                AWSS3Properties.getAccessKeySecret(),
                AWSS3Properties.getBucketName());
    }
}
