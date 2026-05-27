package sd2526.trab.impl.java.servers;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.impl.api.java.AdminMessages;
import sd2526.trab.impl.java.clients.Clients;
import sd2526.trab.impl.zookeeper.ZookeeperClient;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ReplicatedJavaMessages implements Messages, AdminMessages {

    private static final Logger Log = Logger.getLogger(ReplicatedJavaMessages.class.getName());

    public static final String VERSION_HEADER = "X-MESSAGES-VERSION";

    private final ZookeeperClient zk;
    private final JavaMessages local;
    private final ConcurrentHashMap<String, ExecutorService> executors = new ConcurrentHashMap<>();

    private volatile long version = 0;

    private ReplicatedJavaMessages(ZookeeperClient zk) {
        this.zk = zk;
        this.local = JavaMessages.getInstance();
    }

    public long getVersion() {
        return version;
    }

    public void applyVersion(long newVersion) {
        this.version = newVersion;
    }

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        if (!zk.isPrimary())
            return Clients.MessagesClient.get().postMessage(pwd, msg);
        var result = local.postMessage(pwd, msg);
        if (result.isOK()) propagate(client -> client.replicaPostMessage(msg, incrementVersion()));
        return result;
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        if (!zk.isPrimary())
            return Clients.MessagesClient.get().deleteMessage(name, mid, pwd);
        var result = local.deleteMessage(name, mid, pwd);
        if (result.isOK()) propagate(client -> client.replicaDeleteMessage(mid, incrementVersion()));
        return result;
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        if (!zk.isPrimary())
            return Clients.MessagesClient.get().removeInboxMessage(name, mid, pwd);
        var result = local.removeInboxMessage(name, mid, pwd);
        if (result.isOK()) propagate(client -> client.replicaRemoveInboxMessage(name, mid, incrementVersion()));
        return result;
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        return local.getInboxMessage(name, mid, pwd);
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        return local.getAllInboxMessages(name, pwd);
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        return local.searchInbox(name, pwd, query);
    }

    @Override
    public Result<Void> remotePostMessage(Message msg) {
        if (!zk.isPrimary())
            return Clients.AdminMessagesClient.get().remotePostMessage(msg);
        var result = local.remotePostMessage(msg);
        if (result.isOK()) propagate(client -> client.replicaRemotePostMessage(msg, incrementVersion()));
        return result;
    }

    @Override
    public Result<Void> remoteDeleteMessage(String mid) {
        if (!zk.isPrimary())
            return Clients.AdminMessagesClient.get().remoteDeleteMessage(mid);
        var result = local.remoteDeleteMessage(mid);
        if (result.isOK()) propagate(client -> client.replicaRemoteDeleteMessage(mid, incrementVersion()));
        return result;
    }

    @Override
    public Result<Void> remoteDeleteUserInbox(String name) {
        if (!zk.isPrimary())
            return Clients.AdminMessagesClient.get().remoteDeleteUserInbox(name);
        var result = local.remoteDeleteUserInbox(name);
        if (result.isOK()) propagate(client -> client.replicaRemoteDeleteUserInbox(name, incrementVersion()));
        return result;
    }

    private synchronized long incrementVersion() {
        return ++version;
    }

    private void propagate(java.util.function.Consumer<ReplicaMessagesClient> op) {
        for (String uri : zk.getSecondaryURIs()) {
            executors.computeIfAbsent(uri, u -> Executors.newSingleThreadExecutor())
                    .submit(() -> {
                        try { op.accept(new ReplicaMessagesClient(uri)); }
                        catch (Exception e) { Log.warning("Propagate failed to " + uri + ": " + e.getMessage()); }
                    });
        }
    }

    private static ReplicatedJavaMessages instance;

    public static synchronized ReplicatedJavaMessages getInstance(ZookeeperClient zk) {
        if (instance == null)
            instance = new ReplicatedJavaMessages(zk);
        return instance;
    }

    public static synchronized ReplicatedJavaMessages getInstance() {
        return instance;
    }
}