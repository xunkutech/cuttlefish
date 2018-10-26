package com.xunkutech.base.model;

import com.xunkutech.base.model.util.BeanUtils;

public interface IModelEntityExt<
        M extends AbstractModelBeanExt<P>,
        P extends M,
        EP extends IPayloadEntity
        > extends IModelEntity<M, P, EP> {

    default P detach() {
        M model = IModelEntity.super.detach();
        P payload = model.getPayload();
        BeanUtils.deepCopy(model, payload, resolvePayloadType());
        payload.setEntityCode(getPrimaryCode());
        payload.setPayload(payload);
        return payload;
    }
}
