package cn.ggsn.openrxlight.utils;

import cn.ggsn.openrxlight.Constants;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_DATE_TIME,                           // RFC3339 / ISO8601
        Constants.DATE_TIME_FORMATTER,                             // yyyy-MM-dd HH:mm:ss
        Constants.DATE_HH_MM_FORMATTER,                            // yyyy-MM-dd HH:mm
        Constants.ISO_DATE_TIME_FORMATTER,                         // yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
        DateTimeFormatter.ofPattern(Constants.ISO_DATE_TIME_MILLIS_PATTERN), // yyyy-MM-dd'T'HH:mm:ss.SSSSSS
        Constants.QUERY_DATE_TIME_FORMATTER,                       // yyyyMMddHHmmss
        Constants.DATE_FORMATTER,                                  // yyyy-MM-dd (date only)
        Constants.QUERY_DATE_FORMATTER,                            // yyyyMMdd (date only)
        Constants.TIME_FORMATTER,                                  // HH:mm:ss (time only)
        Constants.TIME_WITH_ZONE_FORMATTER,                        // HH:mm Z (time only)
        Constants.HOUR_MINUTE_FORMATTER,                           // HH:mm (time only)
    };

    /**
     * Parse a datetime string trying multiple formats including RFC3339/ISO8601
     * and the formatters defined in Constants.
     *
     * @param data the datetime string to parse
     * @return the parsed LocalDateTime
     * @throws DateTimeParseException if no format matches
     */
    public static LocalDateTime parse(String data) {
        if (data == null || data.isEmpty()) {
            throw new DateTimeParseException("Input is null or empty", data, 0);
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(data, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        // For date-only formats, try parsing as LocalDate and convert to LocalDateTime at start of day
        try {
            LocalDate date = LocalDate.parse(data, Constants.DATE_FORMATTER);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            // Try next
        }

        try {
            LocalDate date = LocalDate.parse(data, Constants.QUERY_DATE_FORMATTER);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            // Try next
        }

        // For time-only formats, try parsing as LocalTime and combine with epoch date
        try {
            LocalTime time = LocalTime.parse(data, Constants.TIME_FORMATTER);
            return LocalDateTime.of(LocalDate.of(1970, 1, 1), time);
        } catch (DateTimeParseException e) {
            // Try next
        }

        try {
            LocalTime time = LocalTime.parse(data, Constants.HOUR_MINUTE_FORMATTER);
            return LocalDateTime.of(LocalDate.of(1970, 1, 1), time);
        } catch (DateTimeParseException e) {
            // Try next
        }

        try {
            LocalTime time = LocalTime.parse(data, Constants.TIME_WITH_ZONE_FORMATTER);
            return LocalDateTime.of(LocalDate.of(1970, 1, 1), time);
        } catch (DateTimeParseException e) {
            // Fall through
        }

        throw new DateTimeParseException("Unable to parse datetime with any known format", data, 0);
    }
}
