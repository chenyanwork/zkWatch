package com.qskx.zkwatch.listenner.service;

import org.junit.Test;

public interface ConfListener {

    /**
     * 动态监听刷新配置
     * @author gangcheng
     * @date 2018-05-26 17:46
     * @param key
     * @param value
     * @return void
     * @throws
     * @since
    */
    void onChange(String key, String value) throws Exception;
}
