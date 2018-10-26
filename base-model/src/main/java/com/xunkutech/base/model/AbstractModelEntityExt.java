package com.xunkutech.base.model;

import lombok.EqualsAndHashCode;

import javax.persistence.MappedSuperclass;

@EqualsAndHashCode(callSuper = true,
        exclude = {"payloadEntity"})
@MappedSuperclass
public abstract class AbstractModelEntityExt<
        M extends AbstractModelBeanExt<P>,
        P extends M,
        EP extends IPayloadEntity
        > extends AbstractModelEntity<M, P, EP> implements IModelEntityExt<M, P, EP> {
}
