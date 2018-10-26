package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.CodecUtils;

/**
 * Created by Jason on 5/17/2017.
 */
public class NativeStringAsciiConverter implements AsciiAttributeConverter<String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return CodecUtils.toAscii(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return CodecUtils.fromAscii(dbData);
    }
}
