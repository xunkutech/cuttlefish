package com.xunkutech.base.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.xunkutech.base.model.util.JsonUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.lang.reflect.Type;

/**
 * Created by jason on 16-2-4.
 */
@EqualsAndHashCode(callSuper = true,
        exclude = {"payloadEntity"})
@MappedSuperclass
public abstract class AbstractModelEntity<M extends AbstractModelBean<P>, P, EP extends IPayloadEntity>
        extends AbstractBaseEntity implements IModelEntity<M, P, EP> {

    private static final long serialVersionUID = 3491414427592599729L;

    @Getter
    @Setter
    @Transient
    private transient Class<EP> payloadEntityClass;

    @Getter
    @Setter
    @Transient
    private transient Type modelType;

    @Getter
    @Setter
    @Transient
    private transient Type payloadType;

    @Getter
    @Setter
    @Column(name = "payload_type_holder",
            length = 1022,
            columnDefinition = "VARCHAR(1022) COLLATE 'ascii_bin'")
    public String payloadTypeHolder;

    /**
     * 非结构数据
     */
/*
    //Sample
    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "payload_code",
            referencedColumnName = "primary_code",
            nullable = true,
            foreignKey = @ForeignKey(name = "none",
                    value = ConstraintMode.NO_CONSTRAINT),
            columnDefinition = "CHAR(64) COLLATE 'ascii_bin'"
    )
*/
    @Transient
    @JsonAdapter(JsonUtils.EntityAdapterFactory.class)
    @SerializedName("payloadEntityCode")
    protected EP payloadEntity;

}
