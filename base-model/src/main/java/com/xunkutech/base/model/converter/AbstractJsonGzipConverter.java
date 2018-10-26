package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.JsonUtils;

import javax.persistence.AttributeConverter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Jason on 5/17/2017.
 */
public abstract class AbstractJsonGzipConverter<T>
        implements AttributeConverter<T, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(T attribute) {
        return (null == attribute) ? null : JsonUtils.toGzip(attribute);
    }

    @Override
    public T convertToEntityAttribute(byte[] dbData) {
        if (null != dbData) {
            Type superclass = this.getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            return JsonUtils.fromGzip(dbData, parameterizedType.getActualTypeArguments()[0]);
        }
        return null;
    }
}
