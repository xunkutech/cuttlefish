package com.xunkutech.base.model;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractModelBeanExt<P extends AbstractModelBeanExt<P>>
        extends AbstractModelBean<P> {
}
