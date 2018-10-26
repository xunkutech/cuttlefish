package com.xunkutech.base.app.component.validate;

import com.xunkutech.base.app.util.AesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SimpleSmsValidator extends AbstractValidator {

    @Value("${sms.code.bound:10000}")
    int bound;

    @Value("${sms.expire.seconds:600}")
    long expireSeconds;

    @Override
    public String newCode() {
        return AesUtils.randomDigitalCode(bound);
    }

    @Override
    protected void checkExpire(Instant timestamp) throws ExpireException {
        if (null != timestamp && timestamp.plusSeconds(expireSeconds).isBefore(Instant.now())) {
            throw new ExpireException();
        }
    }
}
