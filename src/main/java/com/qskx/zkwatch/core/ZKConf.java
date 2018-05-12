package com.qskx.zkwatch.core;

import com.qskx.zkwatch.env.Environment;
import com.qskx.zkwatch.util.ZKClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKConf {

    private static final Logger log = LoggerFactory.getLogger(ZKClient.class);

    public static ZKClient zkClient;
    public static void init(){
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                log.info("init -> watcher start listener.");

                if (watchedEvent.getState() == Event.KeeperState.Expired){
                    zkClient.destroy();
                    zkClient.getClient();
                    //TODO
                }

                String path = watchedEvent.getPath();
                String key = pathToKey(path);
                if (null != key){
                    try {
                        Stat stat = zkClient.getClient().exists(path, true);
                        if (null != stat){
                            if (watchedEvent.getType() == Event.EventType.NodeDeleted){
                                //TODO
                                log.info("localCacheConf remove key and data.");
                            } else if (watchedEvent.getType() == Event.EventType.NodeDataChanged){
                                //TODO
                                String data = get(key);
                                log.info("localCacheConf change data: {}", data );
                            }
                        }
                    } catch (KeeperException e) {
                        log.error("process -> " + e.getMessage());
                    } catch (InterruptedException e) {
                       log.info("process -> " + e.getMessage());
                    }
                }
            }
        };

        zkClient = new ZKClient(Environment.ZK_ADDRESS, watcher);
        log.info("init -> zkConf init success.");
    }

    static {
        init();
    }

    public static void destroy(){
        if (zkClient != null) {
            zkClient.destroy();
        }
    }

    public static void set(String key, String data) {
        String path = keyToPath(key);
        zkClient.setData(path, data);
    }

    public static void delete(String key){
        String path = keyToPath(key);
        zkClient.deletePath(path);
    }

    public static String get(String key){
        String path = keyToPath(key);
        return zkClient.getData(path);
    }

    public static String pathToKey(String nodePath){
        if (nodePath==null || nodePath.length() <= Environment.ZK_PATH.length() || !nodePath.startsWith(Environment.ZK_PATH)) {
            return null;
        }
        return nodePath.substring(Environment.ZK_PATH.length()+1, nodePath.length());
    }

    public static String keyToPath(String nodeKey){
        return Environment.ZK_PATH + "/" + nodeKey;
    }

    public static void main(String[] args) {
        String data1 = get("zk01");
        String data2 = get("zk02");
        System.out.println(data1 + ":" + data2);
        while (true);
    }

}
