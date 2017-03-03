package org.sample;

import org.sample.service.SearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsApplication implements CommandLineRunner {

	private final SearchService searchService;

	public EsApplication(SearchService searchService) {
		this.searchService = searchService;
	}

	public static void main(String[] args) {
		SpringApplication.run(EsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (args.length > 0) {
			String index = args[0];
            System.out.println("Searching all items in index '" + index + "'");
            String result = searchService.searchIndex(index);
			System.out.println("Result: " + result);
		} else {
			throw new IllegalArgumentException("Please provide index name");
		}
	}
}
