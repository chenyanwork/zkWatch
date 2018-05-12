package com.qskx.zkwatch.env;

import com.qskx.zkwatch.util.PropUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Environment {

    private static final Logger log = LoggerFactory.getLogger(Environment.class);

    public static final String ENV_PROP = "qskx-conf.properties";

    public static String LOCAL_PROP;
    public static String ZK_ADDRESS;
    public static String ZK_PATH;

    public static void init(){
        Properties envProp = PropUtil.loadProp(ENV_PROP);
        String newEnvProp = envProp.getProperty("qskx.conf.envprop.location");
        if (StringUtils.isNotEmpty(newEnvProp)){
            envProp = PropUtil.loadProp(newEnvProp);
        }

        LOCAL_PROP = envProp.getProperty("qskx.conf.localprop.location", "qskx-conf-local.properties");
        ZK_ADDRESS = envProp.getProperty("qskx.conf.zkaddress", "192.168.99.100:2181,192.168.99.100:2182,192.168.99.100:2183");
        ZK_PATH = envProp.getProperty("qskx.conf.zkpath", "/qskx-conf");

        log.info("init -> Environment init success.[LOCAL_PROP: {}, ZK_ADDRESS: {}, ZK_PATH:{}]", LOCAL_PROP, ZK_ADDRESS, ZK_PATH);
    }

    static {
        init();
    }
}
