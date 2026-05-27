package sd2526.trab.impl.rest.servers;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2526.trab.impl.discovery.Discovery;
import sd2526.trab.impl.java.servers.AbstractServer;
import sd2526.trab.impl.utils.IP;

public abstract class AbstractRestServer extends AbstractServer {

	private static final String SERVER_BASE_URI = "https://%s:%s%s";
	private static final String REST_CTX = "/rest";

	protected AbstractRestServer(Logger log, String service, int port) {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostname(), port, REST_CTX));
	}

	protected void start() {
		sd2526.trab.impl.db.Hibernate.getInstance();

		ResourceConfig config = new ResourceConfig();
		registerResources(config);

		try {
			String keyStorePath = System.getProperty("javax.net.ssl.keyStore");
			String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword", "changeit");

			KeyStore ks = KeyStore.getInstance("pkcs12");
			try (var fis = new FileInputStream(keyStorePath)) {
				ks.load(fis, keyStorePassword.toCharArray());
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyStorePassword.toCharArray());

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, null);

			JdkHttpServerFactory.createHttpServer(
					URI.create(serverURI.replace(IP.hostname(), INETADDR_ANY)),
					config,
					sslContext);

		} catch (Exception e) {
			Log.severe("Failed to start HTTPS server: " + e.getMessage());
			throw new RuntimeException(e);
		}

		if (service != null)
			Discovery.getInstance().announce(serviceName(), super.serverURI);

		Log.info(String.format("%s Server ready @ %s\n", service, serverURI));
	}

	abstract void registerResources(ResourceConfig config);
}