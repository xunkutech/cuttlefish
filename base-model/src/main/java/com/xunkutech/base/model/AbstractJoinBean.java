package com.xunkutech.base.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by jason on 7/16/17.
 */
@Getter
@Setter
public abstract class AbstractJoinBean<P> extends AbstractModelBean<P> {

    private static final long serialVersionUID = -8728751220953991609L;

    protected transient String jsonA;

    protected transient String jsonB;

    protected String codeA;

    protected String codeB;

}
