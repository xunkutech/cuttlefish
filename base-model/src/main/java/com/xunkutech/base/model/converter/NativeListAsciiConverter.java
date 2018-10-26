package com.xunkutech.base.model.converter;

import com.xunkutech.base.model.util.CodecUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collector.of;
import static java.util.stream.Collectors.toList;

/**
 * Created by jason on 7/9/17.
 */
public class NativeListAsciiConverter implements AsciiAttributeConverter<List<String>> {


    @Override
    public String convertToDatabaseColumn(List<String> attribute) {

        if (null == attribute) return null;

        if (attribute.size() < 1) return "";

        return attribute.stream()
                .collect(of(
                        () -> new StringJoiner(","),
                        (j, p) -> j.add(CodecUtils.toAscii(p)),
                        (j1, j2) -> j1.merge(j2),
                        StringJoiner::toString
                ));
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (null == dbData) return null;

        if (dbData.isEmpty()) return new ArrayList<>();

        return Arrays.stream(dbData.split(","))
                .map(CodecUtils::fromAscii)
                .collect(toList());

    }

    public static void main(String[] args) {
        NativeListAsciiConverter c = new NativeListAsciiConverter();

        List<String> list = new ArrayList<>();

        list.add("entityA");
        list.add("entityB");
        list.add("c");

        System.out.println(c.convertToDatabaseColumn(list));

        c.convertToEntityAttribute("0061,0062,0063").forEach(System.out::println);
    }
}
