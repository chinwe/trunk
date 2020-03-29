package org.flyweight;

/**
 * @author mozixun
 * @description
 * @date 2020/3/29 - 11:47 上午
 */
public class ConcreteWebSite extends BaseWebSite {

    /**
     * 网站的发布形式
     */
    private String type = "";

    /**
     * @param type 网站的发布形式
     */
    public ConcreteWebSite(String type) {
        this.type = type;
    }

    /**
     *
     * @param user
     */
    @Override
    public void use(User user) {
        System.out.println(user.getName() + " use " + type + " website.");
    }
}
