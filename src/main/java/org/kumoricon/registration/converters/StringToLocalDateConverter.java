package org.kumoricon.registration.converters;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/**
 * Handles converting multiple date formats. For example, MM/DD/YYYY, and MM/DD/YY.
 * For MM/DD/YY
 */
public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    private static final DateTimeFormatter DEFAULT = DateTimeFormatter.ofPattern("MMddyyyy");
    private static final DateTimeFormatter DASHES = DateTimeFormatter.ofPattern("M-d-yyyy");
    private static final DateTimeFormatter TWO_DIGIT_YEAR =
            new DateTimeFormatterBuilder()
                    .appendPattern("MMdd")
                    .appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 4, LocalDate.now(ZoneId.of("America/Los_Angeles")).minusYears(99))
                    .toFormatter();
    private static final DateTimeFormatter TWO_DIGIT_YEAR_SLASHES =
            new DateTimeFormatterBuilder()
                    .appendPattern("M/d/")
                    .appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 4, LocalDate.now(ZoneId.of("America/Los_Angeles")).minusYears(99))
                    .toFormatter();
    private static final DateTimeFormatter[] FORMATTERS = {DEFAULT, TWO_DIGIT_YEAR_SLASHES, DASHES, TWO_DIGIT_YEAR};

    @Override
    public LocalDate convert(String from) {
        if (from == null) return null;
        LocalDate date = null;

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                date = LocalDate.parse(from, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        return date;
    }
}
