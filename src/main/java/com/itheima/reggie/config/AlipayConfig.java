package com.itheima.reggie.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class AlipayConfig {

    @Value("${alipay.app_id}")
    private String appId;

    @Value("${alipay.rsa_private_key}")
    private String rsaPrivateKey;

    @Value("${alipay.url}")
    private String url;

    @Value("${alipay.charset}")
    private String charset;

    @Value("${alipay.format}")
    private String format;

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

    @Value("${alipay.sign_type}")
    private String signType;


    /**
     * 获得初始化的AlipayClient
     * @return
     */
    @Bean
    public AlipayClient alipayClient() {
        // 获得初始化的AlipayClient
        return new DefaultAlipayClient(url, appId, rsaPrivateKey, format, charset, alipayPublicKey, signType);
    }
}
