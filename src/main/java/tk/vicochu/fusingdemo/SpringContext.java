package tk.vicochu.fusingdemo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static Object [] getBeans(Class<? extends Object> type) throws BeansException {
        String[] beanNames = getApplicationContext().getBeanNamesForType(type);
        if(null == beanNames){
            return new Object [0];
        }
        Object [] beans = new Object[beanNames.length];
        for(int i=0 ; i<beanNames.length;i++){
            beans[i] = getBean(beanNames[i]);
        }
        return beans;
    }

}
