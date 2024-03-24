package com.itheima.reggie.config;/*
package com.itheima.reggie.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

*/

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author
 * @Date Created in  2023/5/5 15:06
 * @DESCRIPTION:
 * @Version V1.0
 *//*


@Data
@Component
//读取yml文件中alipay 开头的配置
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;


    @PostConstruct
    public void init() {
        // 设置参数（全局只需设置一次）
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipaydev.com";
        config.signType = "RSA2";
        config.appId = this.appId;
        config.merchantPrivateKey = this.appPrivateKey;
        config.alipayPublicKey = this.alipayPublicKey;
        config.notifyUrl = this.notifyUrl;
        Factory.setOptions(config);
        System.out.println("=======支付宝SDK初始化成功=======");
    }
}*/
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
