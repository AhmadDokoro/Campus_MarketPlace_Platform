package com.ahsmart.campusmarket.service.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public record AdminReportPeriod(
        int year,
        Integer month,
        LocalDateTime start,
        LocalDateTime end,
        String label,
        String fileLabel
) {

    public static AdminReportPeriod of(Integer year, Integer month) {
        if (year == null) {
            throw new IllegalArgumentException("Please select a report year.");
        }
        int currentYear = LocalDate.now().getYear();
        if (year < 2020 || year > currentYear) {
            throw new IllegalArgumentException("Please select a valid report year.");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Please select a valid report month.");
        }

        if (month == null) {
            LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
            return new AdminReportPeriod(
                    year,
                    null,
                    start,
                    start.plusYears(1),
                    "Year " + year,
                    String.valueOf(year)
            );
        }

        Month monthValue = Month.of(month);
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        return new AdminReportPeriod(
                year,
                month,
                start,
                start.plusMonths(1),
                monthValue.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year,
                year + "-" + String.format("%02d", month)
        );
    }

    public boolean isMonthly() {
        return month != null;
    }
}
