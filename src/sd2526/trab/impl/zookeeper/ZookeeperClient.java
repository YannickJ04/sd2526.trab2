package sd2526.trab.impl.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ZookeeperClient {

    private static final Logger Log = Logger.getLogger(ZookeeperClient.class.getName());

    private static final String ROOT = "/messages";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String NODE_PREFIX = "p_";

    private final ZooKeeper zk;
    private final String domain;
    private final String myNodePath;   // full path of our ephemeral node
    private final String myServerURI;  // our URI to store in the node data

    private volatile boolean isPrimary = false;
    private final Consumer<Boolean> onRoleChange; // called when primary/secondary status changes

    public ZookeeperClient(String zookeeperHost, String domain, String serverURI,
                           Consumer<Boolean> onRoleChange) throws Exception {
        this.domain = domain;
        this.myServerURI = serverURI;
        this.onRoleChange = onRoleChange;

        this.zk = new ZooKeeper(zookeeperHost, SESSION_TIMEOUT, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                Log.info("Connected to ZooKeeper at " + zookeeperHost);
            }
        });

        createPermanentIfAbsent(ROOT);
        createPermanentIfAbsent(ROOT + "/" + domain);

        String created = zk.create(
                ROOT + "/" + domain + "/" + NODE_PREFIX,
                serverURI.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

        this.myNodePath = created;
        Log.info("Created znode: " + myNodePath);


        checkLeadership();
    }

    private void checkLeadership() {
        try {
            List<String> children = zk.getChildren(
                    ROOT + "/" + domain,
                    event -> {
                        Log.info("ZooKeeper directory changed, re-checking leadership...");
                        checkLeadership();
                    });

            Collections.sort(children);

            String myNodeName = myNodePath.substring(myNodePath.lastIndexOf('/') + 1);

            boolean wasPrimary = isPrimary;
            isPrimary = children.get(0).equals(myNodeName);

            if (isPrimary != wasPrimary) {
                Log.info(isPrimary ? "*** Became PRIMARY ***" : "*** Became SECONDARY ***");
                onRoleChange.accept(isPrimary);
            }

        } catch (Exception e) {
            Log.warning("Error checking leadership: " + e.getMessage());
        }
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public String getPrimaryURI() {
        try {
            List<String> children = zk.getChildren(ROOT + "/" + domain, false);
            if (children.isEmpty()) return null;
            Collections.sort(children);
            byte[] data = zk.getData(ROOT + "/" + domain + "/" + children.get(0), false, new Stat());
            return new String(data);
        } catch (Exception e) {
            Log.warning("Error getting primary URI: " + e.getMessage());
            return null;
        }
    }

    public List<String> getSecondaryURIs() {
        try {
            List<String> children = zk.getChildren(ROOT + "/" + domain, false);
            if (children.size() <= 1) return List.of();
            Collections.sort(children);
            return children.subList(1, children.size()).stream()
                    .map(name -> {
                        try {
                            byte[] data = zk.getData(ROOT + "/" + domain + "/" + name, false, new Stat());
                            return new String(data);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(uri -> uri != null)
                    .toList();
        } catch (Exception e) {
            Log.warning("Error getting secondary URIs: " + e.getMessage());
            return List.of();
        }
    }

    private void createPermanentIfAbsent(String path) throws Exception {
        try {
            zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException.NodeExistsException e) {
            // Already exists, that's fine
        }
    }
}