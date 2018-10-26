package com.xunkutech.base.app.component.validate;


import com.xunkutech.base.app.util.AesUtils;
import com.xunkutech.base.app.util.InvalidException;
import com.xunkutech.base.model.JsonSerializable;
import com.xunkutech.base.model.util.CodecException;
import com.xunkutech.base.model.util.CodecUtils;
import com.xunkutech.base.model.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

public abstract class AbstractValidator {

    @Value("${validate.seed:#{T(com.xunkutech.base.app.util.AesUtils).RANDOM_SEED}}")
    byte[] seed;

    public <P> String createHash(P payload, byte[]... codes) {
        byte[] encryptValue = AesUtils.encrypt(
                JsonUtils.toBin(payload),
                AesUtils.concatByteArray(AesUtils.concatByteArray(codes), seed));
        return CodecUtils.toBase64(encryptValue);
    }

    public abstract String newCode();

    public <P> P validateHash(Class<P> payloadClass, String hash, byte[]... codes) throws InvalidException, ExpireException {
        if (null == hash || null == codes || codes.length == 0) throw new InvalidException();
        byte[] originValue;
        try {
            byte[] encryptValue = CodecUtils.fromBase64(hash);
            originValue = AesUtils.decrypt(
                    encryptValue,
                    AesUtils.concatByteArray(AesUtils.concatByteArray(codes), seed));
        } catch (CodecException e) {
            throw new InvalidException(e);
        }

        if (null != payloadClass) {
            P payload = JsonUtils.fromBin(originValue, payloadClass);
            if (null == payload) throw new InvalidException();
            if (payload instanceof SimplePayload) {
                checkExpire(((SimplePayload) payload).getTimestamp());
            }
            return payload;
        }

        return null;
    }

    protected abstract void checkExpire(Instant timestamp) throws ExpireException;

    @Getter
    @Setter
    public static class SimplePayload implements JsonSerializable {
        private Instant timestamp = Instant.now();
    }

}
