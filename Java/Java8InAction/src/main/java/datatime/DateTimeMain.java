package datatime;

import java.time.*;
import java.time.chrono.ThaiBuddhistDate;
import java.util.TimeZone;

public class DateTimeMain {
    public static void main(String[] args) {
        LocalDate localDate = LocalDate.of(2020, 7, 5);
        Month month = localDate.getMonth();
        System.out.println(month);

        LocalDate nowDate = LocalDate.now();
        DayOfWeek dayOfWeek = nowDate.getDayOfWeek();
        System.out.println(dayOfWeek);

        // 机器时间
        Instant instant = Instant.now();
        System.out.println(instant.getEpochSecond());

        TimeZone timeZone = TimeZone.getDefault();
        System.out.println(timeZone);

        // ISO-8601
        LocalDateTime localDateTime = LocalDateTime.now();
        OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.of("+08:00"));
        System.out.println(offsetDateTime);

        // 历法 泰国佛历
        ThaiBuddhistDate thaiBuddhistDate = ThaiBuddhistDate.from(localDate);
        System.out.println(thaiBuddhistDate);
    }
}
