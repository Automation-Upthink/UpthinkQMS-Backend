package com.upthink.qms.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Converter(autoApply = false)
public class StringLongDateConverter implements AttributeConverter<Long, String> {

    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a"; // Adjust this to match your date format in the database

    private final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    @Override
    public String convertToDatabaseColumn(Long attribute) {
        if (attribute == null) {
            return null;
        }
        return formatter.format(new java.util.Date(attribute));
    }

    @Override
    public Long convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return formatter.parse(dbData).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format for due_date: " + dbData, e);
        }
    }
}
