package sd2526.trab.impl.java.servers;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import sd2526.trab.api.Message;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.impl.rest.clients.RestClient;
import sd2526.trab.impl.utils.ServerSecret;

public class ReplicaMessagesClient extends RestClient {

    static final String REPLICA_PATH = "/replica";
    static final String VERSION_PARAM = "version";

    public ReplicaMessagesClient(String serverURI) {
        super(serverURI, RestMessages.PATH);
    }

    public void replicaPostMessage(Message msg, long version) {
        super.reTry(() -> super.toJavaResult(
                target.path(REPLICA_PATH)
                        .queryParam(VERSION_PARAM, version)
                        .request()
                        .header(ServerSecret.SECRET_HEADER, ServerSecret.get())
                        .post(Entity.entity(msg, MediaType.APPLICATION_JSON))));
    }

    public void replicaDeleteMessage(String mid, long version) {
        super.reTry(() -> super.toJavaResult(
                target.path(REPLICA_PATH).path(mid)
                        .queryParam(VERSION_PARAM, version)
                        .request()
                        .header(ServerSecret.SECRET_HEADER, ServerSecret.get())
                        .delete()));
    }

    public void replicaRemoveInboxMessage(String name, String mid, long version) {
        super.reTry(() -> super.toJavaResult(
                target.path(REPLICA_PATH).path("inbox").path(name).path(mid)
                        .queryParam(VERSION_PARAM, version)
                        .request()
                        .header(ServerSecret.SECRET_HEADER, ServerSecret.get())
                        .delete()));
    }

    public void replicaRemotePostMessage(Message msg, long version) {
        super.reTry(() -> super.toJavaResult(
                target.path(REPLICA_PATH).path("admin")
                        .queryParam(VERSION_PARAM, version)
                        .request()
                        .header(ServerSecret.SECRET_HEADER, ServerSecret.get())
                        .post(Entity.entity(msg, MediaType.APPLICATION_JSON))));
    }

    public void replicaRemoteDeleteMessage(String mid, long version) {
        super.reTry(() -> super.toJavaResult(
                target.path(REPLICA_PATH).path("admin").path(mid)
                        .queryParam(VERSION_PARAM, version)
                        .request()
                        .header(ServerSecret.SECRET_HEADER, ServerSecret.get())
                        .delete()));
    }

    public void replicaRemoteDeleteUserInbox(String name, long version) {
        super.reTry(() -> super.toJavaResult(
                target.path(REPLICA_PATH).path("admin").path("inbox").path(name)
                        .queryParam(VERSION_PARAM, version)
                        .request()
                        .header(ServerSecret.SECRET_HEADER, ServerSecret.get())
                        .delete()));
    }
}