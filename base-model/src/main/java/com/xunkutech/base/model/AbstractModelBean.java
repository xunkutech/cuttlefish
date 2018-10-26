package com.xunkutech.base.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * MetaBean this the super class for all embeddable object which stores specific business fields regarding
 * to entity relation fields.
 * <p>
 * For the embeddable class can not adopt the inheritance. So all fields definition should be applied to
 * the sub class.
 * <p>
 * Created by Jason Han on 5/18/2017.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, exclude = "payload")
public abstract class AbstractModelBean<P> implements JsonSerializable {

    private static final long serialVersionUID = 6150262414591839484L;

    protected transient P payload;

    protected String entityCode;

}
