package datatime;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author chinwe
 * 2021/9/20
 */
public class DSTMain {
    public static void main(String[] args) {

        long oneDay = 24 * 60 * 60 * 1000;
        //2019-10-26 04:00:00
        long startTime = 1572055200000L;
        //2019-10-26 06:00:00
        long endTime = 1572062400000L;
        SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Madrid");
        bjSdf.setTimeZone(timeZone);
        System.out.println(bjSdf.format(new Date(startTime)));
        //System.out.println(bjSdf.format(new Date(endTime)));
        System.out.println(bjSdf.format(new Date(startTime + oneDay)));
        //System.out.println(bjSdf.format(new Date(endTime + oneDay)));

        // 所有时区
        final Set<String> availableZoneIds = ZoneId.getAvailableZoneIds();
        // System.out.println(availableZoneIds);

        // ISO-8601
        LocalDateTime localDateTime = LocalDateTime.now();
        OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.of("+08:00"));
        System.out.println("Beijing Time:       " + offsetDateTime);

        // 巴黎时间
        final LocalDateTime parisNow = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Paris"));
        final OffsetDateTime parisISO = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Paris"));
        System.out.println("Paris Time:         " + parisISO);

        // America/Los_Angeles
        final OffsetDateTime isoLosAngeles = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("America/Los_Angeles"));
        System.out.println("Los Angeles Time:   " + isoLosAngeles);
        System.out.println(TimeZone.getTimeZone("America/Los_Angeles"));

        // Epoch Milli
        System.out.println(Instant.now().toEpochMilli());
        System.out.println(System.currentTimeMillis());

        // America/Sao_Paulo
        final OffsetDateTime isoBrasilia = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("America/Sao_Paulo"));
        System.out.println("Brasilia Time:   " + isoLosAngeles);
        System.out.println(TimeZone.getTimeZone("America/Sao_Paulo"));

        // tzdb
        System.out.println("tzdb version:   " + java.time.zone.ZoneRulesProvider.getVersions("UTC").lastEntry().getKey());
    }
}
