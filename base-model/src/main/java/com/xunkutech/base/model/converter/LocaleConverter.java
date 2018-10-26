package com.xunkutech.base.model.converter;

import com.google.gson.reflect.TypeToken;
import com.xunkutech.base.model.util.JsonUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jason on 7/9/17.
 */
@Converter(autoApply = true)
public class LocaleConverter implements AttributeConverter<Locale, String> {

    @Override
    public String convertToDatabaseColumn(Locale attribute) {
        if (null == attribute) {
            return null;
        }

        return attribute.toLanguageTag();
    }

    @Override
    public Locale convertToEntityAttribute(String dbData) {
        if (null == dbData) {
            return null;
        }
        return Locale.forLanguageTag(dbData);
    }

    public static void main(String[] args) {
        LocaleConverter c = new LocaleConverter();
        System.out.println(c.convertToDatabaseColumn(Locale.CANADA_FRENCH));
        System.out.println(c.convertToEntityAttribute("fr_CA").toString());

        Map<Locale, String> map = new HashMap<>();

        map.put(Locale.SIMPLIFIED_CHINESE, "haha");

        System.out.println(JsonUtils.PRETTY_PRINT_GSON.toJson(map));

        map = JsonUtils.PRETTY_PRINT_GSON.fromJson("{\"en_US\":\"xixi\"}", new TypeToken<Map<Locale, String>>() {
        }.getType());

        System.out.println(JsonUtils.PRETTY_PRINT_GSON.toJson(map));
        map.keySet().stream().map(l -> l.getLanguage()).forEach(System.out::println);
    }
}
