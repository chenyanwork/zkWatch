package com.qskx.zkwatch.listenner.factory;

import com.qskx.zkwatch.listenner.service.ConfListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerFactory {

    private static final Logger log = LoggerFactory.getLogger(ListenerFactory.class);

    public static boolean addListener(String key, ConfListener confListener){
        return true;
    }
}
