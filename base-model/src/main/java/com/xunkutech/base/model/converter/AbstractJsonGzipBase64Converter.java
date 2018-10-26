package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.JsonUtils;

import javax.persistence.AttributeConverter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Created by Jason on 5/17/2017.
 */
public abstract class AbstractJsonGzipBase64Converter<T>
        implements AttributeConverter<T, String> {

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return (null == attribute) ? null : JsonUtils.toGzipBase64(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (null != dbData) {
            Type superclass = this.getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            return JsonUtils.fromGzipBase64(dbData, parameterizedType.getActualTypeArguments()[0]);
        }
        return null;
    }
}