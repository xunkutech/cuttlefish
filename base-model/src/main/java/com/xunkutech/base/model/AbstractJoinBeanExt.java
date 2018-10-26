package com.xunkutech.base.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractJoinBeanExt<P extends AbstractJoinBeanExt<P>>
        extends AbstractJoinBean<P> {

    private static final long serialVersionUID = -8728751220953991509L;
}
