package com.store.movie.service;

import com.store.movie.controller.schema.MovieDetail;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.dax.ClusterDaxClient;
import software.amazon.dax.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class DynamoService {

    private final String tableName = "movie";
    private final String daxUrl = "PLACEHOLDER";
    private final Region region = Region.US_EAST_1;

    private DynamoDbClient getClient() throws IOException {
        return ClusterDaxClient.builder()
                .overrideConfiguration(Configuration.builder()
                        .url(daxUrl)
                        .region(region)
                        .build())
                .build();
    }

    public List<MovieDetail> findByTitle(String searchQuery) throws IOException {
        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan");

        DynamoDbClient client = getClient();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#title, :query)")
                        .expressionAttributeNames(Map.of("#title", "title"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        List<Map<String, AttributeValue>> items = scanResponse.items();

        stopWatch.stop();

        stopWatch.start("Convert");

        List<MovieDetail> movies = convert(items);

        stopWatch.stop();

        log.info("Found: {} - Converted: {} - StopWatch: {}", items.size(), movies.size(), stopWatch);

        return movies;
    }

    public List<MovieDetail> findByYear(String searchQuery) throws IOException {
        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = getClient();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#year, :query)")
                        .expressionAttributeNames(Map.of("#year", "year"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        List<Map<String, AttributeValue>> items = scanResponse.items();

        stopWatch.stop();

        stopWatch.start("Convert");

        List<MovieDetail> movies = convert(items);

        stopWatch.stop();

        log.info("Found: {} - Converted: {} - StopWatch: {}", items.size(), movies.size(), stopWatch);

        return movies;
    }

    public List<MovieDetail> findByCast(String searchQuery) throws IOException {
        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = getClient();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#cast, :query)")
                        .expressionAttributeNames(Map.of("#cast", "cast"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        List<Map<String, AttributeValue>> items = scanResponse.items();

        stopWatch.stop();

        stopWatch.start("Convert");

        List<MovieDetail> movies = convert(items);

        stopWatch.stop();

        log.info("Found: {} - Converted: {} - StopWatch: {}", items.size(), movies.size(), stopWatch);

        return movies;
    }

    public List<MovieDetail> findByGenre(String searchQuery) throws IOException {
        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = getClient();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#genres, :query)")
                        .expressionAttributeNames(Map.of("#genres", "genres"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        List<Map<String, AttributeValue>> items = scanResponse.items();

        stopWatch.stop();

        stopWatch.start("Convert");

        List<MovieDetail> movies = convert(items);

        stopWatch.stop();

        log.info("Found: {} - Converted: {} - StopWatch: {}", items.size(), movies.size(), stopWatch);

        return movies;
    }

    private static List<MovieDetail> convert(List<Map<String, AttributeValue>> items) {
        List<MovieDetail> movieDetails = new ArrayList<>();

        for (Map<String, AttributeValue> item : items) {
            MovieDetail movieDetail = new MovieDetail();
            movieDetail.setTitle(item.get("title") == null ? "" : item.get("title").s());
            movieDetail.setYear(item.get("year") == null ? "" : item.get("year").s());
            movieDetail.setCast(item.get("cast") == null ? "" : item.get("cast").s());
            movieDetail.setGenre(item.get("genres") == null ? "" : item.get("genres").s());
            movieDetails.add(movieDetail);
        }

        return movieDetails;
    }

}
