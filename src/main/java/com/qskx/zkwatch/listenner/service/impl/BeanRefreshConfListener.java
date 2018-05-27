package com.qskx.zkwatch.listenner.service.impl;

import com.qskx.zkwatch.configuration.DefinedPropertyPlaceholderConfigurer;
import com.qskx.zkwatch.listenner.service.ConfListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author 111111
 * @date 2018-05-26 16:18
 */
public class BeanRefreshConfListener implements ConfListener{

    private static final Logger log = LoggerFactory.getLogger(BeanRefreshConfListener.class);

    public static class BeanField{
        private String beanName;
        private String property;

        public BeanField(){

        }

        public BeanField(String beanName, String property) {
            this.beanName = beanName;
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        @Override
        public boolean equals(Object o) {
            BeanField beanField = (BeanField) o;
            return Objects.equals(beanName, beanField.beanName) &&
                    Objects.equals(property, beanField.property);
        }

    }

    private static Map<String, List<BeanField>> bfMap = new ConcurrentHashMap<>();

    /**
     * @author 111111
     * @date 2018-05-26 16:43
     * @param key
     * @param beanField
     * @return void
     * @throws
     * @since
    */
    public static void addBeanField(String key, BeanField beanField){
        List<BeanField> beanFieldList = bfMap.get(key);
        if (null == beanFieldList){
            beanFieldList = new ArrayList<>();
            bfMap.put(key, beanFieldList);
        }

        beanFieldList.stream().forEach(item ->{
            if(item.equals(beanField)){
                log.info("addBeanField -> beanName {} already existed.", beanField.getBeanName());
                return;
            }
        });
        beanFieldList.add(beanField);
    }

    @Override
    public void onChange(String key, String value) throws Exception {
        List<BeanField> beanFieldList = bfMap.get(key);
        if (null != beanFieldList && beanFieldList.size() > 0){
            beanFieldList.stream().forEach(item -> {
                log.info("onChange -> beanName {} refresh {}", item.getBeanName(), value);
                DefinedPropertyPlaceholderConfigurer.refreshBeanField(item, value);
            });
        }

    }

//    @Test
//    public void testReturn(){
//        List<String> list = Arrays.asList("1", "2", "3", "4");
//        list.stream().forEach(item ->{
//            if ("4".equals(item)){
//                return;
//            }
//        });
//        String str = "program continue";
//        System.out.println(">>>>>>>>>>>>>>>>" + str);
//    }
}
