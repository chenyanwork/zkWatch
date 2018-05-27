package com.qskx.zkwatch.configuration;

import com.qskx.zkwatch.annotation.ConfMamage;
import com.qskx.zkwatch.core.LocalCacheConf;
import com.qskx.zkwatch.core.ZKConf;
import com.qskx.zkwatch.listenner.factory.ListenerFactory;
import com.qskx.zkwatch.listenner.service.impl.BeanRefreshConfListener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

public class DefinedPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DefinedPropertyPlaceholderConfigurer.class);

    public void init(){
        ListenerFactory.addListener(null, new BeanRefreshConfListener());
    }

    public void destroy(){
        ZKConf.destroy();
        LocalCacheConf.destory();
    }

    private static final String placeholderprefix="${";
    private static final String placeholderSuffix="}";

    private static boolean xmlKeyValid(String originKey){
        boolean start = originKey.startsWith(placeholderprefix);
        boolean end = originKey.endsWith(placeholderSuffix);
        if (start && end){
            return true;
        }
        return false;
    }

    private static String xmlKeyParse(String originKey){
        if (xmlKeyValid(originKey)){
            String parseKey = originKey.substring(originKey.indexOf(placeholderprefix) + 2, originKey.indexOf(placeholderSuffix));
//            String parseKey = originKey.substring(placeholderprefix.length(), originKey.length() - placeholderSuffix.length());
            return parseKey;
        }
        return null;
    }

    /**
     * @author 111111
     * @date 2018-05-26 21:53
     * @param beanField
     * @param value
     * @return void
     * @throws
     * @since
     */
    public static void refreshBeanField(BeanRefreshConfListener.BeanField beanField, String value){
        Object bean =beanFactory.getBean(beanField.getBeanName());
        if (bean != null){
            BeanWrapper beanWrapper = new BeanWrapperImpl(bean);

            PropertyDescriptor propertyDescriptor = null;
            PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
            if (propertyDescriptors != null &&propertyDescriptors.length > 0){
                for (PropertyDescriptor item : propertyDescriptors) {
                    log.info("refreshBeanField -> PropertyDescriptor.getName() {}", item.getName());
                    if (beanField.getProperty().equals(item.getName())) {
                        propertyDescriptor = item;
                    }
                }
            }

            if (propertyDescriptor != null && propertyDescriptor.getWriteMethod() != null){
                beanWrapper.setPropertyValue(beanField.getProperty(), value);
                log.info("refreshBeanField -> refresh[beanWrapper] success beanName {}, property {}, value {}", beanField.getBeanName(), beanField.getProperty(), value);
            } else{
                Field[] fields = bean.getClass().getDeclaredFields();
                if (fields != null && fields.length > 0){
                    Arrays.stream(fields).forEach(field -> {
                        if (beanField.getProperty().equals(field.getName())){
                            field.setAccessible(true);
                            try {
                                log.info("refreshBeanField -> refresh[field] success beanName {}, property {}, value {}", beanField.getBeanName(), beanField.getProperty(), value);
                                field.set(bean, value);
                            } catch (IllegalAccessException e) {
                                log.error("refreshBeanField -> refresh[field] failed beanName {}, property {}, value {}", beanField.getBeanName(), beanField.getProperty(), value);
                            }
                        }
                    });
                }
            }

        }
    }

    @Override
   /**
    * @author 111111
    * @date 2018-05-26 22:25
    * @param beanFactoryToProcess
    * @param propertyResolver
    * @return void
    * @throws BeansException
    * @since 
   */
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, final ConfigurablePropertyResolver propertyResolver) throws BeansException {
        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        if (beanNames != null && beanNames.length > 0){
            for (String s : beanNames) {
                if (!(s.equals(s) && beanFactoryToProcess.equals(beanFactory))) {

                    //XML('${...}')
                    BeanDefinition beanDefinition = beanFactoryToProcess.getBeanDefinition(s);
                    MutablePropertyValues mpvs = beanDefinition.getPropertyValues();
                    PropertyValue[] propertyValues = mpvs.getPropertyValues();
                    if (propertyValues != null && propertyValues.length > 0) {
                        Arrays.stream(propertyValues).forEach(propertyValue -> {
                            if (propertyValue.getValue() instanceof TypedStringValue) {
                                String propertyName = propertyValue.getName();
                                String typeStringVal = ((TypedStringValue) propertyValue.getValue()).getValue();
                                if (xmlKeyValid(typeStringVal)) {
                                    String confKey = xmlKeyParse(typeStringVal);
                                    //TODO confCentreclient
                                    String confValue = "";

                                    mpvs.add(propertyValue.getName(), confValue);

                                    //
                                    BeanRefreshConfListener.BeanField beanField = new BeanRefreshConfListener.BeanField(s, propertyName);
                                    BeanRefreshConfListener.addBeanField(confKey, beanField);
                                }
                            }
                        });
                    }

                    // 2、Annotation('@XxlConf')
                    if (beanDefinition.getBeanClassName() == null) {
                        continue;
                    }
                    Class beanClazz = null;

                    try {
                        beanClazz = Class.forName(beanDefinition.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        log.error("");
                    }
                    if (beanClazz == null){
                        continue;
                    }
                    ReflectionUtils.doWithFields(beanClazz, field -> {
                        if (field.isAnnotationPresent(ConfMamage.class)){
                            String propertyName = field.getName();
                            ConfMamage confMamage = field.getAnnotation(ConfMamage.class);

                            String confKey = confMamage.value();
                            //TODO 配置中心客户端
                            String confValue = "";

                            BeanRefreshConfListener.BeanField beanField = new BeanRefreshConfListener.BeanField(beanName, propertyName);
                            refreshBeanField(beanField, confValue);

                            //watch
                            if (confMamage.callback()){
                                BeanRefreshConfListener.addBeanField(confKey, beanField);
                            }
                        }

                    });
                }
            }
        }
    }

    private static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory){
        this.beanFactory = beanFactory;
    }

    private String beanName;

    @Override
    public void setBeanName(String beanName){
        this.beanName = beanName;
    }

    @Override
    public int getOrder(){
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders){
        super.setIgnoreUnresolvablePlaceholders(true);
    }


//
//    @Test
//    public void test01(){
//        String key = "${123456678}";
//        System.out.println(">>>>>>>>>>>>>>>>>>" + xmlKeyParse(key));
//    }
}
