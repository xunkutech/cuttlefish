package com.xunkutech.base.model;

import com.xunkutech.base.model.util.BeanUtils;

public interface IJoinEntityExt<
        M extends AbstractJoinBeanExt<P>,
        P extends M,
        EP extends IPayloadEntity,
        EA extends IModelEntity<?, ?, ?>,
        EB extends IModelEntity<?, ?, ?>
        > extends IJoinEntity<M, P, EP, EA, EB> {

    EA getEntityA();

    void setEntityA(EA entityA);

    EB getEntityB();

    void setEntityB(EB entityB);

    default P detach() {
        M model = IJoinEntity.super.detach();
        P payload = model.getPayload();
        BeanUtils.deepCopy(model, payload, resolvePayloadType());
        payload.setEntityCode(getPrimaryCode());
        payload.setPayload(payload);
        return payload;
    }
}
