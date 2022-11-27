package org.exapmle.jvm;

/**
 * @author chinwe
 * 2022/10/23
 */
public class OperandStackTest {

    public void testAddOperation() {
        byte i = 15;
        int j = 8;
        int k = i + j;

        int m = 1000;
    }

    public void add() {
        // 0 iconst_1
        // 1 istore_1
        int i = 1;

        // 2 iload_1
        // 3 iinc 1 by 1
        // 6 istore_2
        int j = i++;

        // 7 iinc 1 by 1
        // 10 iload_1
        // 11 istore_3
        int k = ++i;
    }
}
