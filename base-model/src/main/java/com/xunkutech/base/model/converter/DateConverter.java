//package com.xunkutech.base.model.converter;
//
//import javax.persistence.AttributeConverter;
//import javax.persistence.Converter;
//import java.util.Date;
//
///**
// * Created by jason on 16-2-21.
// */
//@Converter(autoApply = true)
//public class DateConverter implements AttributeConverter<Date, Long> {
//
//    @Override
//    public Long convertToDatabaseColumn(Date attribute) {
//        return null == attribute ? null : attribute.getTime();
//    }
//
//    @Override
//    public Date convertToEntityAttribute(Long dbData) {
//        return null == dbData ? null : new Date(dbData);
//    }
//}