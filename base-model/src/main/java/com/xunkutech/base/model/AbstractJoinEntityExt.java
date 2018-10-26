package com.xunkutech.base.model;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractJoinEntityExt<
        M extends AbstractJoinBeanExt<P>,
        P extends M,
        EP extends IPayloadEntity,
        EA extends IModelEntity<?, ?, ?>,
        EB extends IModelEntity<?, ?, ?>
        > extends AbstractJoinEntity<M, P, EP, EA, EB> implements IJoinEntityExt<M, P, EP, EA, EB> {

}
