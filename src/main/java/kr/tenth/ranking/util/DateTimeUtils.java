package kr.tenth.ranking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private DateTimeUtils() {
        // 유틸리티 클래스의 인스턴스 생성을 막기 위한 private 생성자
    }

    public static LocalDateTime formatWithoutMilliseconds(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedString = dateTime.format(formatter);
        return LocalDateTime.parse(formattedString, formatter);
    }
}
