package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.JsonUtils;

import javax.persistence.AttributeConverter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jason on 5/17/2017.
 */
public abstract class AbstractJsonTextConverter<T> implements AttributeConverter<T, String> {

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return (null == attribute) ? null : JsonUtils.toJson(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (null != dbData) {
            Type superclass = this.getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            return JsonUtils.fromJson(dbData, parameterizedType.getActualTypeArguments()[0]);
        }
        return null;
    }

    public static class Demo extends AbstractJsonTextConverter<Map<String, List<String>>> {

    }

    public static void main(String[] args) {
        Demo demo = new Demo();
        List<String> aa = new ArrayList<>();
        aa.add("haha");
        Map<String, List<String>> bb = new HashMap<>();
        bb.put("xixi", aa);
        System.out.println(demo.convertToDatabaseColumn(bb));

        String r = "{\"xixi\":[\"haha\"]}";
        Map<String, List<String>> t = demo.convertToEntityAttribute(r);
        System.out.println(JsonUtils.printJson(t));

    }
}
