package org.proxy.staticproxy;

/**
 * @author mozixun
 * @description
 * @date 2020/3/29 - 4:56 下午
 */
public class TeacherDaoProxy implements ITeacherDao {

    private  ITeacherDao target;

    public TeacherDaoProxy(ITeacherDao target) {
        this.target = target;
    }

    @Override
    public void teach() {
        System.out.println("Proxy start.");
        target.teach();
        System.out.println("Proxy end.");
    }
}
