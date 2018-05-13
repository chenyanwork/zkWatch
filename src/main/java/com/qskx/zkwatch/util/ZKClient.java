package com.qskx.zkwatch.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ZKClient {

    private static final Logger log = LoggerFactory.getLogger(ZKClient.class);

    private String zkServer;
    private Watcher watcher;
    public ZKClient(String zkServer, Watcher watcher){
        this.zkServer = zkServer;
        this.watcher = watcher;

        if (null == watcher){
            watcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.Expired){
                        destroy();
                        getClient();
                    }
                }
            };
        }
    }

    private ZooKeeper zooKeeper;
    private ReentrantLock instanceLock = new ReentrantLock(true);
    public ZooKeeper getClient(){
        if (null == zooKeeper){
            try {
                if (instanceLock.tryLock(2, TimeUnit.SECONDS)){
                    if (null == zooKeeper){
                        try {
                            zooKeeper = new ZooKeeper(zkServer, 10000, watcher);
                        } catch (IOException e) {
                            log.error("getClient -> 获取ZK实例失败:{}", e.getMessage(), e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("getClient -> 加锁失败: {}", e.getMessage(), e);
            } finally {
                instanceLock.unlock();
            }
        }

        if (null == zooKeeper){
            throw new RuntimeException("zooKeeper instance is null !");
        }
        return zooKeeper;
    }

    public Stat createPath(String path){
        if (StringUtils.isEmpty(path)){
            return null;
        }
        try {
            Stat stat = getClient().exists(path, true);
            if (null == stat){
                if (path.lastIndexOf("/") > 0){
                    String parentPath = path.substring(0, path.lastIndexOf("/"));
                    Stat parentStat = getClient().exists(parentPath, true);
                    if (null == parentPath){
                        createPath(parentPath);
                    }
                }
                getClient().create(path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            return getClient().exists(path, true);
        } catch (Exception e) {
            log.error("createPath -> 创建路径错误:" + e.getMessage());
            throw new RuntimeException("createPath -> 创建路径错误:" + e.getMessage());
        }
    }

    public Stat setData(String path, String data){
        try {
            Stat stat = getClient().exists(path, true);
            if (null == stat){
                createPath(path);
                stat = getClient().exists(path, true);
            }
            return getClient().setData(path, data.getBytes(), stat.getVersion());
        }  catch (Exception e) {
            log.error("setData -> 存储数据错误:" + e.getMessage());
            throw new RuntimeException("setData -> 存储数据错误:" + e.getMessage());
        }
    }

    public String getData(String path){
        String znodeData = null;
        try {
            Stat stat = getClient().exists(path,true);
            if (null != stat){
                byte [] resultData = getClient().getData(path, true, null);
                if (null != resultData){
                    znodeData = new String(resultData);
                }
            } else {
                log.info("getData ->zk path: {}不存在!", path);
            }
        } catch (Exception e) {
            log.error("getData -> 获取zk数据失败" + e.getMessage());
            throw new RuntimeException("getData -> 获取zk数据失败" + e.getMessage());
        }
        return znodeData;
    }

    public void deletePath(String path){
        try {
            Stat stat = getClient().exists(path, true);
            if (null != stat){
                getClient().delete(path, stat.getVersion());
            } else {
                log.info("deletePath ->zk path: {}不存在!", path);
            }
        } catch (Exception e) {
            log.error("deletePath ->zk删除路径错误: {}", e.getMessage(), e);
            throw new RuntimeException("deletePath ->zk删除路径错误" + e.getMessage());
        }
    }

    private Map<String, String> getAllChildrenData(String path){
        Map<String, String> allData = new HashMap<>();
        try {
            List<String> childrenPaths = getClient().getChildren(path, true);
            if (null != childrenPaths || childrenPaths.size() > 0){
                for (String childrenPath : childrenPaths){
                    String childrenData = getData(childrenPath);
                    allData.put(childrenPath, childrenData);
                }
            }
            return allData;
        } catch (Exception e) {
            log.info("getAllChildrenData -> zk 获取所有子路径数据错误" + e.getMessage());
           throw new RuntimeException("getAllChildrenData -> zk 获取所有子路径数据错误" + e.getMessage());
        }
    }
    public void destroy() {
        if (null != zooKeeper){
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                log.error("destroy -> zooKeeper destroy is error !");
            } finally {
                zooKeeper = null;
            }
        }
    }

}
