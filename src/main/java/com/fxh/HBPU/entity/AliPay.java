package com.fxh.HBPU.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author
 * @Date Created in  2023/5/5 15:26
 * @DESCRIPTION:
 * @Version V1.0
 */
@Data
public class AliPay {
    private String traceNo;       // 商户的订单号
    private BigDecimal totalAmount; // 总金额，这里使用BigDecimal来避免精度丢失
    private String subject;      // 订单标题
    private String productCode;  // 产品代码，这里针对即时到账的固定值

}