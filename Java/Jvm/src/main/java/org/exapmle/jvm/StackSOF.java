package org.exapmle.jvm;

/**
 * VM Args: -Xss128k
 *
 * @author mozixun
 * @description
 * @date 2022/5/3 - 14:23
 */
public class StackSOF {

    private int stackDepth = 1;

    public void stackLeak() {
        stackDepth++;
        stackLeak();
    }

    public static void main(String[] args) {
        StackSOF sof = new StackSOF();

        try {
            sof.stackLeak();
        } catch (Throwable t) {
            System.out.println("stack depth: " + sof.stackDepth);
            throw t;
        }
    }
}
