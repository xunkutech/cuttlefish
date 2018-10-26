package com.xunkutech.base.model.component.configure;

import com.xunkutech.base.model.AbstractModelBean;
import com.xunkutech.base.model.converter.NativeStringAsciiConverter;
import com.xunkutech.base.model.enums.ConfigureType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

/**
 * Created by jason on 7/18/17.
 */
@Getter
@Setter
@Embeddable
public class Configure<P> extends AbstractModelBean<P> {

    @Column(name = "configure_type",
            nullable = false,
            updatable = false,
            columnDefinition = "TINYINT(2)")
    private ConfigureType configureType;

    @Column(name = "configure_name",
            nullable = false,
            updatable = false,
            length = 2044,
            columnDefinition = "VARCHAR(2044) COLLATE 'ascii_bin'")
    @Convert(converter = NativeStringAsciiConverter.class)
    private String configureName;

}
