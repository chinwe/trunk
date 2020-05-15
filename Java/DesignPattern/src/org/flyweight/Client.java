package org.flyweight;

/**
 * @author mozixun
 */
public class Client {

    public static void main(String[] args) {
        WebSiteFactory webSiteFactory = new WebSiteFactory();

        User tom = new User("Tom");
        User jerry = new User("Jerry");

        BaseWebSite website1 = webSiteFactory.getWebSiteCategory("news");
        website1.use(tom);

        BaseWebSite website2 = webSiteFactory.getWebSiteCategory("blog");
        website2.use(tom);

        BaseWebSite website3 = webSiteFactory.getWebSiteCategory("blog");
        website3.use(jerry);
    }
}
