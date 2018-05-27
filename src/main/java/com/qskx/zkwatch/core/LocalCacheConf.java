package com.qskx.zkwatch.core;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LocalCacheConf {

    private static final Logger log = LoggerFactory.getLogger(LocalCacheConf.class);

    private static CacheManager cacheManager;
    private static Cache<String, CacheNode> localCache;
    private static Thread refreshThread;
    private static boolean refreshThreadStop;

    private static void init(){
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);//true不用每次初始化
        //cacheManager.init();
        localCache = cacheManager.createCache("localCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CacheNode.class, ResourcePoolsBuilder.heap(1000)));

        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!refreshThreadStop){
                    try {
                        TimeUnit.SECONDS.sleep(60);
                        //TODO
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    static {
        init();
    }

    public static void destory(){
        if (null != cacheManager){
            cacheManager.close();
        }
        if (null != refreshThread){
            refreshThread.interrupt();
        }
    }

    public static void reloadAll(){
        Set<String> keySet = new HashSet<>();
        Iterator<Cache.Entry<String, CacheNode>> iterator = localCache.iterator();
        while (iterator.hasNext()){
            Cache.Entry<String, CacheNode> entry = iterator.next();
            keySet.add(entry.getKey());
        }
        if (keySet.size() > 0){
            for (String key : keySet){
                String zData = ZKConf.get(key);

                CacheNode node = localCache.get(key);
                if (null != node && null != node.getValue() && node.getValue().equals(zData)){
                    log.info("key:[{}] unchanged .", key);
                } else {
                    set(key, zData, "reload");
                    log.info("key:[{}] reload success .", key);
                }
            }
        }
    }

    public static void set(String key, String value, String optType){
        if (null != localCache){
            localCache.put(key, new CacheNode(value));
            log.info("set -> [key:{}, value:{}, optType:{}]", key, value, optType);
            //TODO listener DB
        }
    }

    public static void update(String key, String value){
        if (null != localCache && localCache.containsKey(key)){
            set(key, value, "UPDATE");
            log.info("key:[{}] update success .", key);
        }
    }

    public static CacheNode get(String key){
        if (null != localCache && localCache.containsKey(key)){
            CacheNode cacheNode = localCache.get(key);
            return cacheNode;
        }
        return null;
    }

    public static void remove(String key){
        if (null != localCache && localCache.containsKey(key)){
            localCache.remove(key);
        }
    }
    public static class CacheNode implements Serializable{

        private static final long serialVersionUID = -3916344892557905369L;

        private String value;
        public CacheNode(){

        }

        public CacheNode(String value){
            this.value = value;
        }
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
