package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.CodecUtils;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by jason on 7/9/17.
 */
public class NativeMapAsciiConverter implements AsciiAttributeConverter<Map<String, String>> {
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (null == attribute) return null;

        if (attribute.size() < 1) return "";

        return attribute.entrySet()
                .stream()
                .map(e -> new StringJoiner(":")
                        .add(CodecUtils.toAscii(e.getKey()))
                        .add(CodecUtils.toAscii(e.getValue())))
                .collect(Collector.of(
                        () -> new StringJoiner(","),
                        (j, p) -> j.add(p.toString()),
                        (j1, j2) -> j1.merge(j2),
                        StringJoiner::toString
                ));
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (null == dbData) return null;

        if (dbData.isEmpty()) return new LinkedHashMap<>();

        return Arrays.stream(dbData.split(","))
                .map(e -> e.split(":"))
                .collect(Collectors.toMap(
                        a -> CodecUtils.fromAscii(a[0]),
                        a -> CodecUtils.fromAscii(a[1])
                ));
    }

    public static void main(String[] args) {
        NativeMapAsciiConverter c = new NativeMapAsciiConverter();

        Map<String, String> map = new HashMap<>();

        map.put("entityA", "entityB");

        map.put("c", "d");

        System.out.println(c.convertToDatabaseColumn(map));

        map = c.convertToEntityAttribute("0061:0062,0063:0064");

        map.forEach((k, v) -> System.out.println(k + ":" + v));
    }
}
