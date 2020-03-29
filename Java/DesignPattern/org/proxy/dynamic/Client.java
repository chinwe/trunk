package org.proxy.dynamic;

/**
 * @author mozixun
 * @description
 * @date 2020/3/29 - 5:26 下午
 */
public class Client {
    public static void main(String[] args) {

        TeacherDao teacherDao = new TeacherDao();

        ProxyFactory proxyFactory = new ProxyFactory(teacherDao);

        ITeacherDao iTeacherDao = (ITeacherDao)proxyFactory.getProxyInstance();

        iTeacherDao.teach();
    }
}
