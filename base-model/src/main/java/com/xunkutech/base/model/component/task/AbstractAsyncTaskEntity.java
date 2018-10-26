package com.xunkutech.base.model.component.task;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.xunkutech.base.model.AbstractModelEntity;
import com.xunkutech.base.model.IPayloadEntity;
import com.xunkutech.base.model.component.configure.ConfigureEntity;
import com.xunkutech.base.model.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by jason on 7/15/17.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAsyncTaskEntity<P, EP extends IPayloadEntity>
        extends AbstractModelEntity<AsyncTask<P>, P, EP> {

    private static final long serialVersionUID = 6199074789981833704L;

    @ManyToOne(
            fetch = FetchType.LAZY,
            targetEntity = ConfigureEntity.class
    )
    @JoinColumn(
            name = "configure_code",
            referencedColumnName = "primary_code",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "none",
                    value = ConstraintMode.NO_CONSTRAINT)
    )
    @SerializedName("configureEntityCode")
    @JsonAdapter(JsonUtils.EntityAdapterFactory.class)
    private ConfigureEntity configureEntity;
}
