package com.qskx.zkwatch.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author 111111
 * @date 2018-05-29 23:58
 */
@Configuration
@ImportResource(locations={"classpath:applicationcontext.xml"})
public class ImportSpringxml {
    private String paramByXml;

    public String getParamByXml() {
        return paramByXml;
    }

    public void setParamByXml(String paramByXml) {
        this.paramByXml = paramByXml;
    }
}
