package org.example.corejava.i18n;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author chinwe
 * 2021/9/21
 */
public class LocaleMain {
    public static void main(String[] args) {
     Arrays.stream(Locale.getAvailableLocales()).forEach(System.out::println);

    }
}
