package sd2526.trab.impl.rest.servers;

import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import sd2526.trab.api.java.Messages;
import sd2526.trab.impl.java.servers.ReplicatedJavaMessages;
import sd2526.trab.impl.utils.IP;
import sd2526.trab.impl.utils.ServerSecret;
import sd2526.trab.impl.zookeeper.ZookeeperClient;

public class RestMessagesServer extends AbstractRestServer {
	public static final int PORT = 4567;

	private static Logger Log = Logger.getLogger(RestMessagesServer.class.getName());

	RestMessagesServer() {
		super(Log, Messages.SERVICE_NAME, PORT);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register(RestMessagesResource.class);
		config.register(RestReplicaMessagesResource.class);
	}

	public static void main(String[] args) throws Exception {
		String secret = null;
		String zookeeperHost = null;

		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("-secret"))
				secret = args[i + 1];
			if (args[i].equals("-zookeeper"))
				zookeeperHost = args[i + 1];
		}

		if (secret != null)
			ServerSecret.set(secret);

		var server = new RestMessagesServer();

		if (zookeeperHost != null) {
			RestMessagesResource.isReplicated = true;
			var zk = new ZookeeperClient(
					zookeeperHost,
					IP.domain(),
					server.serverURI,
					isPrimary -> Log.info("Role changed: " + (isPrimary ? "PRIMARY" : "SECONDARY"))
			);
			ReplicatedJavaMessages.getInstance(zk);
		}

		server.start();
	}
}