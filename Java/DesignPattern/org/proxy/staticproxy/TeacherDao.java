package org.proxy.staticproxy;

/**
 * @author mozixun
 * @description
 * @date 2020/3/29 - 4:57 下午
 */
public class TeacherDao implements ITeacherDao {

    @Override
    public void teach() {
        System.out.println("TeacherDao teach.");
    }
}
