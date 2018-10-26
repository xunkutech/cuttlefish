package com.xunkutech.base.model.component.configure;

import com.xunkutech.base.model.AbstractModelEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by jason on 7/18/17.
 */

@Entity
@Table(name = "base_configure",
        indexes = {@Index(columnList = "configure_type, configure_name", unique = true)})
@Access(AccessType.FIELD)
public class ConfigureEntity<P>
        extends AbstractModelEntity<Configure<P>, P, ConfigurePayloadEntity> {

    private static final long serialVersionUID = 2800325576611049392L;

    @Getter
    @Setter
    @Embedded
    private Configure model;

    /**
     * 元信息，保存到外部表
     */
    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            targetEntity = ConfigurePayloadEntity.class
    )
    @JoinColumn(
            name = "payloadcode",
            referencedColumnName = "primary_code",
            nullable = true,
            foreignKey = @ForeignKey(name = "none",
                    value = ConstraintMode.NO_CONSTRAINT),
            columnDefinition = "CHAR(64) COLLATE 'ascii_bin'"
    )
    @Override
    public ConfigurePayloadEntity getPayloadEntity() {
        return payloadEntity;
    }

    @Override
    public void setPayloadEntity(ConfigurePayloadEntity payloadEntity) {
        this.payloadEntity = payloadEntity;
    }
}
