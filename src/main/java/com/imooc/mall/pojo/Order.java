package com.imooc.mall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer id;

    private Long orderNo;

    private Integer userId;

    private Integer shippingId;

    private BigDecimal payment;

    private Integer paymentType;

    private Integer postage;

    private Integer status;

    private Date paymentTime;

    private Date sendTime;

    private Date endTime;

    private Date closeTime;

    private Date createTime;

    private Date updateTime;

    public Order(Integer id, Long orderNo, Integer userId, Integer shippingId,
                 BigDecimal payment, Integer paymentType, Integer postage,
                 Integer status, Date paymentTime, Date sendTime, Date endTime, Date closeTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.userId = userId;
        this.shippingId = shippingId;
        this.payment = payment;
        this.paymentType = paymentType;
        this.postage = postage;
        this.status = status;
        this.paymentTime = paymentTime;
        this.sendTime = sendTime;
        this.endTime = endTime;
        this.closeTime = closeTime;
        this.createTime = new Date();
        this.updateTime = this.createTime;
    }
}