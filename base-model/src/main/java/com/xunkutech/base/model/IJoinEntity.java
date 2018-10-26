package com.xunkutech.base.model;

/**
 * Created by jason on 7/16/17.
 */
public interface IJoinEntity<
        M extends AbstractJoinBean<P>,
        P,
        EP extends IPayloadEntity,
        EA extends IModelEntity<?, ?, ?>,
        EB extends IModelEntity<?, ?, ?>
        > extends IModelEntity<M, P, EP> {

    EA getEntityA();

    void setEntityA(EA entityA);

    EB getEntityB();

    void setEntityB(EB entityB);

    default M detach() {
        M model = IModelEntity.super.detach();
        model.setJsonA(getEntityA().detach().toJson());
        model.setJsonB(getEntityB().detach().toJson());
        model.setCodeA(getEntityA().getPrimaryCode());
        model.setCodeB(getEntityB().getPrimaryCode());
        return model;
    }
}
