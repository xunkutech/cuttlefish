package com.xunkutech.base.model.enums;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.AttributeConverter;

/**
 * Created by jason on 9/9/15.
 */
public enum EntityStatus implements PersistableEnum<Integer> {
    ACTIVE(0),
    DEACTIVE(1),
    DELETED(2);

    @Getter
    @Setter
    private Integer enumKey;

    EntityStatus(Integer key) {
        this.enumKey = key;
    }

    @javax.persistence.Converter(autoApply = true)
    public static class Converter extends PersistableEnum.Converter<EntityStatus, Integer>
            implements AttributeConverter<EntityStatus, Integer> {
    }

}
