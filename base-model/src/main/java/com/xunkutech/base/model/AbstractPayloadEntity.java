package com.xunkutech.base.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;

/**
 * Created by jason on 7/9/17.
 */
@MappedSuperclass
public class AbstractPayloadEntity extends AbstractBaseEntity implements IPayloadEntity {

    private static final long serialVersionUID = -2012639139309337528L;

    @Getter
    @Setter
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "json_text",
            nullable = true,
            length = 4194303,
            columnDefinition = "MEDIUMTEXT COLLATE 'utf8mb4_bin'")
    private String jsonText;

}
