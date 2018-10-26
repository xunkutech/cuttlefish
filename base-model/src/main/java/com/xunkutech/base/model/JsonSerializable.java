package com.xunkutech.base.model;

import com.xunkutech.base.model.util.JsonUtils;

import java.io.Serializable;

/**
 * InfoBean stores non relation fields which will be packaged in entityA json object
 * <p>
 * Created by Jason on 5/19/2017.
 */
public interface JsonSerializable extends Serializable {

    default String printJson() {
        return JsonUtils.printJson(this);
    }

    default String toJson() {
        return JsonUtils.toJson(this);
    }

    default byte[] toJsonBin() {
        return JsonUtils.toBin(this);
    }

    default String toJsonBase64() {
        return JsonUtils.toBase64(this);
    }
}
