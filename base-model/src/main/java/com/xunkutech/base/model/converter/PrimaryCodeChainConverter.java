package com.xunkutech.base.model.converter;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collector;

public class PrimaryCodeChainConverter implements AttributeConverter<String[], String> {

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        if (null == attribute) return null;
        if (attribute.length < 1) return "";
        return Arrays.stream(attribute)
                .collect(Collector.of(
                        () -> new StringJoiner(","),
                        (j, p) -> j.add(p),
                        (j1, j2) -> j1.merge(j2),
                        StringJoiner::toString
                ));
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (null == dbData) return null;
        if (dbData.isEmpty()) return new String[]{};
        return Arrays.stream(dbData.split(","))
                .toArray(String[]::new);
    }
}
