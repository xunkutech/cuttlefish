package com.xunkutech.base.app.component.pay;

public enum Channel {
    alipay, //支付宝 APP 支付
    alipay_wap,     //支付宝手机网页支付
    alipay_pc_direct,       //支付宝电脑网站支付
    alipay_qr,      //支付宝当面付，即支付宝扫码支付
    bfb,    //百度钱包移动快捷支付，即百度钱包 APP 支付
    bfb_wap,        //百度钱包手机网页支付
    cp_b2b, //银联企业网银支付，即 B2B 银联 PC 网页支付
    upacp,  //银联支付，即银联 APP 支付（2015 年 1 月 1 日后的银联新商户使用。若有疑问，请与 Ping++ 或者相关的收单行联系）
    upacp_wap,      //银联手机网页支付（2015 年 1 月 1 日后的银联新商户使用。若有疑>问，请与 Ping++ 或者相关的收单行联系）
    upacp_pc,       //银联网关支付，即银联 PC 网页支付
    wx,     //微信 APP 支付
    wx_pub, //微信公众号支付
    wx_pub_qr,      //微信公众号扫码支付
    wx_wap, //微信 WAP 支付（此渠道仅针对特定客户开放）
    wx_lite,        //微信小程序支付
    yeepay_wap,     //易宝手机网页支付
    jdpay_wap,      //京东手机网页支付
    fqlpay_wap,     //分期乐支付
    qgbc_wap,       //量化派支付
    cmb_wallet,     //招行一网通
    applepay_upacp, //Apple Pay
    mmdpay_wap,     //么么贷
    qpay,   //QQ 钱包支付

}
