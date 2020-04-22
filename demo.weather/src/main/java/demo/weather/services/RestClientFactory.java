package demo.weather.services;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Never do this in production! :)
 */
public class RestClientFactory {
	private static final boolean IS_TRUSTSTORE_CONFIGURED = false;

	/**
	 * Create a trust manager that does not validate certificate chains.
	 * @return
	 */
	private static  TrustManager[] getInsecureTrustManager() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
		    public X509Certificate[] getAcceptedIssuers(){return null;}
		    public void checkClientTrusted(X509Certificate[] certs, String authType){}
		    public void checkServerTrusted(X509Certificate[] certs, String authType){}
		}};

		return trustAllCerts;
	}

	/**
	 * Install the all-trusting trust manager
	 * @return
	 */
	private static SSLContext getInsecureSSLContext () {
		SSLContext ctx = null;
		try {
			ctx  = SSLContext.getInstance("SSL");
			ctx.init(null, getInsecureTrustManager(), new SecureRandom());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, getInsecureTrustManager(), new SecureRandom());
		} catch (Exception e) {
			System.out.println("ERROR creating insecure SSL context: " + e.getMessage());
			e.printStackTrace();
		}
		return ctx;
	}

	private static ClientBuilder getBaseClient() {
		return ClientBuilder.newBuilder()
				.property("client.AutoRedirect", "true")
				.property("http.redirect.relative.uri", "true");
	}

	public static Client getRestClient() {
		if (IS_TRUSTSTORE_CONFIGURED) {
			return getBaseClient().build();
		} else {
			// Create a Rest Client with a trust manager that does not validate certificate chains
			// NEVER DO THIS IN PRODUCTION!
			return getBaseClient().sslContext(getInsecureSSLContext()).build();
		}
	}

}
