package com.xunkutech.base.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.xunkutech.base.model.util.JsonUtils;
import lombok.EqualsAndHashCode;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Created by Jason Han on 5/20/2017.
 */
@EqualsAndHashCode(callSuper = true)
//@Entity
//@Table(name = "base_a_join_b",
//        indexes = {
//                @Index(columnList = "a_code"),
//                @Index(columnList = "b_code")
//        })
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@DiscriminatorColumn(name = "join_inherited_type",
//        discriminatorType = DiscriminatorType.STRING,
//        columnDefinition = "VARCHAR(64) COLLATE 'ascii_bin'")
//@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class AbstractJoinEntity<
        M extends AbstractJoinBean<P>,
        P,
        EP extends IPayloadEntity,
        EA extends IModelEntity<?, ?, ?>,
        EB extends IModelEntity<?, ?, ?>
        > extends AbstractModelEntity<M, P, EP> implements IJoinEntity<M, P, EP, EA, EB> {

    private static final long serialVersionUID = -6356985172384074936L;

    @Transient
    @SerializedName("entityACode")
    @JsonAdapter(JsonUtils.EntityAdapterFactory.class)
    protected EA entityA;

    @Transient
    @SerializedName("entityBCode")
    @JsonAdapter(JsonUtils.EntityAdapterFactory.class)
    protected EB entityB;

}
