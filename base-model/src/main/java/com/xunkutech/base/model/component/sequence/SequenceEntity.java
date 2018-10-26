package com.xunkutech.base.model.component.sequence;

import com.xunkutech.base.model.AbstractBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Created by jason on 7/16/17.
 */
@Getter
@Setter
@Entity
@Table(name = "base_sequence")
public class SequenceEntity extends AbstractBaseEntity {

    private static final long serialVersionUID = -2494977136195739515L;

    @Column(name = "name",
            unique = true,
            nullable = false,
            updatable = false,
            length = 500,
            columnDefinition = "VARCHAR(510) COLLATE 'ascii_bin'")
    private String name;

    @Column(name = "sequence")
    private Long sequence;

    @Version
    @Column(name = "version")
    private Long version;

}
