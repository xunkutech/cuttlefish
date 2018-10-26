package com.xunkutech.base.model;

import com.xunkutech.base.model.enums.EntityStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

/**
 * Created by Jason Han on 5/20/2017.
 */
@EntityListeners(DefaultEntityListener.class)
@EqualsAndHashCode(callSuper = false,
        exclude = {"lastModifiedTime", "entityStatus", "createdDate"})
@MappedSuperclass
public abstract class AbstractBaseEntity implements IBaseEntity, JsonSerializable {

    private static final long serialVersionUID = -2050630137131009608L;

    /**
     * 数据库ID
     * 实体对象业务编码
     */
    @Getter
    @Setter
    @Id
    @Basic
    @Column(name = "primary_code",
            unique = true,
            nullable = false,
            updatable = false,
            length = 64,
            columnDefinition = "CHAR(64) COLLATE 'ascii_bin'")
    protected String primaryCode;

    /**
     * 对象最后修改时间
     * <p>
     * For mariadb, the clauses DEFAULT CURRENT_TIMESTAMP and ON UPDATE CURRENT_TIMESTAMP are by default applied to entity's
     * timestamp field, and enable the default behavior.
     */
    @Getter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_time",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
//    @Convert(disableConversion = true)
    protected Date lastModifiedTime;

    /**
     * 实体对象状态
     */
    @Getter
    @Setter
    @Column(name = "entity_status",
            nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0")
    protected EntityStatus entityStatus;


    /**
     * 实体创建时间
     */
    @Getter
    @Setter
    @Basic
    @Column(name = "created_date",
            nullable = false,
            updatable = false,
            columnDefinition = "BIGINT")
    protected Instant createdDate;

    /**
     * 是否有效
     */
    @Setter
    @Getter
    @Basic
    @Column(name = "enable",
            nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 1")
    protected Boolean enable;
}
