package org.example.corejava.inheritance;

public class EnumTest {
    public static void main(String[] args) {
        Size size = Enum.valueOf(Size.class, "LARGE");
        System.out.println(size.getAbbreviation());
    }
}
