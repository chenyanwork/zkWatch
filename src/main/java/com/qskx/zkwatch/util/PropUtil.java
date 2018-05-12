package com.qskx.zkwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropUtil {

    private static final Logger log = LoggerFactory.getLogger(PropUtil.class);

    public static Properties loadProp(String fileName){
        Properties prop = new Properties();
        InputStream in = null;
        URL url = null;
        try {
            if (fileName.startsWith("file:")){
                url = new File(fileName.substring("file:".length())).toURI().toURL();
            } else {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                url = loader.getResource(fileName);
            }

            if (null != url){
                in = new FileInputStream(url.getPath());
                if (null != in){
                    prop.load(in);
                }
            }
        } catch (Exception e){
            log.error("loadProp -> 读取properties文件错误: {}", e.getMessage(), e);
        } finally {
            if (null != in){
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                } finally {
                    in = null;
                }
            }
        }

        return prop;
    }

}
