package org.proxy.staticproxy;

/**
 * @author mozixun
 * @description
 * @date 2020/3/29 - 4:57 下午
 */
public class Client {
    public static void main(String[] args) {
        TeacherDao teacherDao = new TeacherDao();
        TeacherDaoProxy teacherDaoProxy = new TeacherDaoProxy(teacherDao);
        teacherDaoProxy.teach();
    }
}
