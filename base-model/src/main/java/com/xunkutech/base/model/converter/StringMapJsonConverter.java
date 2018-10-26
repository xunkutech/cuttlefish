package com.xunkutech.base.model.converter;

import com.google.gson.reflect.TypeToken;
import com.xunkutech.base.model.util.JsonUtils;

import javax.persistence.AttributeConverter;
import java.util.LinkedHashMap;
import java.util.Map;

public class StringMapJsonConverter implements AttributeConverter<Map<String, String>, String> {
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        return (null == attribute) ? null : JsonUtils.toJson(attribute);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (null == dbData) return null;
        if (dbData.isEmpty()) return new LinkedHashMap<>();

        return JsonUtils.fromJson(dbData, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
