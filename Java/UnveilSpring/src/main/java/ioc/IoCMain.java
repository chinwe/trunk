package ioc;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;


/**
 * @author chinwe
 * 2021/10/21
 */
public class IoCMain {
    public static void main(String[] args) {
        // BeanFactory
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.registerBeanDefinition(String.class.getName(), BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());

        final String bean = beanFactory.getBean(String.class);
        System.out.println(bean);

        // ApplicationContext

    }
}
