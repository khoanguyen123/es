package org.sample.service;

import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * created 3/3/2017
 */
@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final ESPool esPool;

    @Autowired
    private ApplicationContext context;

    public SearchService(ESPool esPool) {
        this.esPool = esPool;
    }

    public String searchIndex(String index) throws Exception {
        SearchResult result = esPool.getClient().execute(sampleQuery(index));
        return result.getJsonString();
    }

    private <T extends JestResult> Search sampleQuery(String index) throws IOException {
        String searchString = loadFromFile("search.txt");
        logger.info("Search string: \n{}", searchString);
        Search search = new Search.Builder(searchString)
                .addIndex(index)
                .build();
        return search;
    }

    private String loadFromFile(String fileName) throws IOException {
        Resource resource = context.getResource(fileName);
        InputStream is = resource.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        }

    }
}
