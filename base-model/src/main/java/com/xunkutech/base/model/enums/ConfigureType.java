package com.xunkutech.base.model.enums;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.AttributeConverter;

/**
 * Created by jason on 7/14/17.
 */
public enum ConfigureType implements PersistableEnum<Integer> {
    TASK(0),;

    @Getter
    @Setter
    private Integer enumKey;

    ConfigureType(Integer key) {
        this.enumKey = key;
    }

    @javax.persistence.Converter(autoApply = true)
    public static class Converter extends PersistableEnum.Converter<ConfigureType, Integer>
            implements AttributeConverter<ConfigureType, Integer> {
    }

}
