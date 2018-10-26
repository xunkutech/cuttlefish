package com.xunkutech.base.app.component.pay;

import com.pingplusplus.Pingpp;
import com.pingplusplus.exception.*;
import com.pingplusplus.model.Charge;
import com.pingplusplus.model.Refund;
import com.pingplusplus.net.APIResource;
import com.xunkutech.base.app.context.AppContextAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PingPlugPlugAdapter implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(PingPlugPlugAdapter.class);

    @Autowired
    AppContextAccessor accessor;

    @Value("${pingpp.api-key:xxx}")
    private String pingppApiKey;

    @Value("${pingpp.app-id:xxx}")
    private String pingppAppId;

    public Charge charge(PaymentObject payment) throws PaymentException {
        if (null == payment) {
            return null;
        }
        Charge charge = null;
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("order_no", payment.getOrderNo());
        chargeParams.put("amount", payment.getAmount());//订单总金额, 人民币单位：分（如订单总金额为 1 元，此处请填 100）
        Map<String, String> app = new HashMap<>();
        app.put("id", pingppAppId);
        chargeParams.put("app", app);
        chargeParams.put("channel", payment.getChannel().toString());
        chargeParams.put("currency", "cny");
        chargeParams.put("client_ip", accessor.getClientInfo().getDevice().getRemoteAddr());
        chargeParams.put("subject", payment.getSubject());
        chargeParams.put("body", payment.getBody());
        chargeParams.put("time_expire", payment.getTimeExpire());
        chargeParams.put("description", payment.getDescription());
        logger.debug(APIResource.getGson().toJson(chargeParams));
        try {
            charge = Charge.create(chargeParams);
            logger.info("Obtained Charge Object: \n{}", charge.toString());
        } catch (AuthenticationException
                | InvalidRequestException
                | APIConnectionException
                | APIException
                | ChannelException
                | RateLimitException
                e) {
            throw new PaymentException(e);
        }
        return charge;
    }

    public Refund refund(String chargeId, long amount, String reason) throws PaymentException {
        if (chargeId == null) {
            return null;
        }
        Refund refund = null;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("description", reason);
        params.put("amount", amount);// 退款的金额, 单位为对应币种的最小货币单位，例如：人民币为分（如退款金额为 1 元，此处请填 100）。必须小于等于可退款金额，默认为全额退款
        try {
            refund = Refund.create(chargeId, params);
            logger.info("Obtained Refund Object: \n{}", refund.toString());
        } catch (AuthenticationException
                | InvalidRequestException
                | APIConnectionException
                | APIException
                | ChannelException
                | RateLimitException
                e) {
            throw new PaymentException(e);
        }
        return refund;
    }

    public Charge retrieveCharge(String chargeId) throws PaymentException {
        Charge charge = null;
        try {
            charge = Charge.retrieve(chargeId);
        } catch (AuthenticationException
                | InvalidRequestException
                | APIConnectionException
                | APIException
                | ChannelException
                | RateLimitException
                e) {
            throw new PaymentException(e);
        }
        return charge;
    }

    public Refund retrieveRefund(String chargeId, String refundId) throws PaymentException {
        Refund refund = null;
        try {
            refund = Refund.retrieve(chargeId, refundId);
            logger.info("Obtained Refund Object: \n{}", refund.toString());
        } catch (AuthenticationException
                | InvalidRequestException
                | APIConnectionException
                | APIException
                | ChannelException
                | RateLimitException
                e) {
            throw new PaymentException(e);
        }
        return refund;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Pingpp.apiKey = pingppApiKey;
    }

    public static void main(String[] args) throws Exception {
        PaymentObject paymentObject = PaymentObject.builder().orderNo("111").subject("eee").body("aaa").channel(Channel.alipay).amount(1222).build();
        PingPlugPlugAdapter pingPlugPlugAdapter = new PingPlugPlugAdapter();
        pingPlugPlugAdapter.charge(paymentObject);
    }
}
