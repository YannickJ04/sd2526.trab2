package sd2526.trab.impl.rest.servers;

import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import sd2526.trab.api.Message;
import sd2526.trab.impl.java.servers.JavaMessages;
import sd2526.trab.impl.java.servers.ReplicatedJavaMessages;
import sd2526.trab.impl.utils.ServerSecret;

@Singleton
@Path("/rest/messages/replica")
public class RestReplicaMessagesResource extends RestResource {

    @Context
    HttpHeaders headers;

    private void checkSecret() {
        String incoming = headers.getHeaderString(ServerSecret.SECRET_HEADER);
        if (!ServerSecret.isValid(incoming))
            throw new WebApplicationException(Status.FORBIDDEN);
    }

    private JavaMessages local() {
        return JavaMessages.getInstance();
    }

    private ReplicatedJavaMessages replicated() {
        return ReplicatedJavaMessages.getInstance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void replicaPostMessage(@QueryParam("version") long version, Message msg) {
        checkSecret();
        super.resultOrThrow(local().remotePostMessage(msg));
        replicated().applyVersion(version);
    }

    @DELETE
    @Path("/{mid}")
    public void replicaDeleteMessage(@PathParam("mid") String mid, @QueryParam("version") long version) {
        checkSecret();
        super.resultOrThrow(local().remoteDeleteMessage(mid));
        replicated().applyVersion(version);
    }

    @DELETE
    @Path("/inbox/{name}/{mid}")
    public void replicaRemoveInboxMessage(@PathParam("name") String name, @PathParam("mid") String mid,
                                          @QueryParam("version") long version) {
        checkSecret();
        super.resultOrThrow(local().removeInboxMessage(name, mid, ""));
        replicated().applyVersion(version);
    }

    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    public void replicaRemotePostMessage(@QueryParam("version") long version, Message msg) {
        checkSecret();
        super.resultOrThrow(local().remotePostMessage(msg));
        replicated().applyVersion(version);
    }

    @DELETE
    @Path("/admin/{mid}")
    public void replicaRemoteDeleteMessage(@PathParam("mid") String mid, @QueryParam("version") long version) {
        checkSecret();
        super.resultOrThrow(local().remoteDeleteMessage(mid));
        replicated().applyVersion(version);
    }

    @DELETE
    @Path("/admin/inbox/{name}")
    public void replicaRemoteDeleteUserInbox(@PathParam("name") String name, @QueryParam("version") long version) {
        checkSecret();
        super.resultOrThrow(local().remoteDeleteUserInbox(name));
        replicated().applyVersion(version);
    }
}