package org.sample.service;

import com.google.common.collect.Iterators;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Maintain connection pool to ES
 *
 */
@Service
public class ESPool {
	@Value("${es.connection.timeout.ms:5000}")
	private int connectionTimeout;

	@Value("${es.read.timeout.ms:60000}")
	private int readTimeout;

	@Value("${es.connection.max:10}")
	private int maxConnections;

	@Value("${es.urls}")
	private String esUrls;

	@Value("${es.user}")
	private String esUser;

	@Value("${es.password}")
	private String esPassword;

	private static final int DEFAULT_MAX_CONNECTION = 10; // 10 connections for each es url

	private static final Logger logger = LoggerFactory.getLogger(ESPool.class);
	private static Iterator<JestClient> roundRobinClients = null;

	@PostConstruct
	public void init() {
		buildRoundRobinJetClients(esUrls);
	}

	/**
	 * Build Round Robin JestClients
	 * @param esUrls -- ES endpoints
	 */
	private void buildRoundRobinJetClients(String esUrls) {
		List<String> existedEndpoints = new ArrayList<>();
		List<JestClient> clients = new ArrayList<>();
		final List<String> urls = Arrays.asList(esUrls.split(","));

		urls.stream()
		    .filter(url -> !existedEndpoints.contains(url) && isValid(url))
		    .forEach(url -> { 
			    addClient(clients, url);
			    existedEndpoints.add(url);
		    });

		logger.info("Round Robin ES clients has {} clients", clients.size());
		if (clients.isEmpty()) {
			logger.error("No ES client available");
		} else {
			roundRobinClients = Iterators.cycle(clients);
		}
	}

	/**
	 * Build a new JestClient and add it to the clients list
	 * @param clients
	 * @param url
	 */
	private void addClient(List<JestClient> clients, String url) {
		logger.info("Adding ES URL {}", url);
		// create an ES client
		HttpClientConfig httpClientConfig = new HttpClientConfig.Builder(url)
				.connTimeout(connectionTimeout)
				.readTimeout(readTimeout)
				.multiThreaded(true)
				.maxTotalConnection(maxConnections)
				.defaultMaxTotalConnectionPerRoute(DEFAULT_MAX_CONNECTION)
				.defaultCredentials(esUser, esPassword)
				.build();
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(httpClientConfig);
		JestClient esClient = factory.getObject();
		clients.add(esClient);
	}

	private boolean isValid(String urlString) {
		logger.info("Validating URL: {}", urlString);
		try {
			new URL(urlString).toURI();
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			return false;
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
			return false;
		}

		return true;
	}
	
	/**
	 * @return the ES client if available
	 * @throws Exception
	 */
	public synchronized JestClient getClient() throws Exception {
		if (roundRobinClients != null) {
			return roundRobinClients.next();
		}
		throw new Exception("No ES client is available");
	}

}
