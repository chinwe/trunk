package org.example.corejava.datetime;

import java.time.*;
import java.time.temporal.TemporalAdjuster;

/**
 * @author chinwe
 * 2021/9/21
 */
public class DateTimeMain {

    public static void main(String[] args) {
        // Time Ticks
        // duration();

        // Local Time
        // localTime();

        // Time Zone
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("America/New_York"));
        System.out.println(zonedDateTime);
    }

    private static void localTime() {
        final LocalDate today = LocalDate.now();
        System.out.println(today);

        final LocalDate alonzosBirthday = LocalDate.of(1903, Month.JUNE, 14);
        System.out.println(alonzosBirthday);

        TemporalAdjuster NEXT_WORD_DAY = w -> {
            LocalDate result = (LocalDate) w;
            do {
                result = result.plusDays(1);
            } while (result.getDayOfWeek().getValue() >= DayOfWeek.SATURDAY.getValue());
            return result;
        };
        LocalDate backToWord = today.with(NEXT_WORD_DAY);
        System.out.println(backToWord);
    }

    private static void duration() {
        Instant begin = Instant.now();
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long l = Duration.between(begin, Instant.now()).toMillis();
        System.out.println(("duration: " + l + "ms"));
    }
}
