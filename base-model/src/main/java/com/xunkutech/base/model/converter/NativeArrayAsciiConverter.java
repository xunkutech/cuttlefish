package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.CodecUtils;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collector;

/**
 * Using stream & lambda express to optimize the code
 * <p>
 * Created by Jason on 5/17/2017.
 */
public class NativeArrayAsciiConverter implements AsciiAttributeConverter<String[]> {

    @Override
    public String convertToDatabaseColumn(String[] attribute) {

        if (null == attribute) return null;

        if (attribute.length < 1) return "";

        return Arrays.stream(attribute)
                .collect(Collector.of(
                        () -> new StringJoiner(","),
                        (j, p) -> j.add(CodecUtils.toAscii(p)),
                        (j1, j2) -> j1.merge(j2),
                        StringJoiner::toString
                ));

    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {

        if (null == dbData) return null;

        if (dbData.isEmpty()) return new String[]{};

        return Arrays.stream(dbData.split(","))
                .map(CodecUtils::fromAscii)
                .toArray(String[]::new);
    }

    public static void main(String[] args) {
        NativeArrayAsciiConverter c = new NativeArrayAsciiConverter();
        System.out.println(c.convertToDatabaseColumn(new String[]{"entityA", "entityB", "c"}));

        Arrays.stream(c.convertToEntityAttribute("0061,0062,0063")).forEach(System.out::println);
    }
}
