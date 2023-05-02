package kr.tenth.ranking.enu;

import java.time.LocalDate;

public enum DateRange {
    ONE_DAY,
    ONE_WEEK,
    ONE_MONTH;

    public static DateRange getDateRange(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isEqual(today)) {
            return ONE_DAY;
        } else if (date.isAfter(today.minusWeeks(1))) {
            return ONE_WEEK;
        } else {
            return ONE_MONTH;
        }
    }
}