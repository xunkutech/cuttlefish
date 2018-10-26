package com.xunkutech.base.model.converter;

import com.google.gson.reflect.TypeToken;
import com.xunkutech.base.model.util.JsonUtils;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;

public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return (null == attribute) ? null : JsonUtils.toJson(attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (null == dbData) return null;
        if (dbData.isEmpty()) return new ArrayList<>();

        return JsonUtils.fromJson(dbData, new TypeToken<List<String>>() {
        }.getType());
    }
}
