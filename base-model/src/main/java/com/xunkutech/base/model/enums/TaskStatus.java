package com.xunkutech.base.model.enums;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.AttributeConverter;

/**
 * Created by jason on 7/16/17.
 */
public enum TaskStatus implements PersistableEnum<Integer> {
    CREATED(0),
    SUCCEEDED(1),
    FAILED(2),
    DISPATCHED(3);

    @Getter
    @Setter
    private Integer enumKey;

    TaskStatus(Integer key) {
        this.enumKey = key;
    }

    @javax.persistence.Converter(autoApply = true)
    public static class Converter extends PersistableEnum.Converter<TaskStatus, Integer>
            implements AttributeConverter<TaskStatus, Integer> {
    }
}
