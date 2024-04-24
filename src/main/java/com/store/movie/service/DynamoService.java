package com.store.movie.service;

import com.store.movie.controller.schema.MovieDetail;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class DynamoService {

    private final String tableName = "movie";

    public List<MovieDetail> findByTitle(String searchQuery) {
        List<MovieDetail> movies = new ArrayList<>();

        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = DynamoDbClient.create();

        ScanResponse response = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#title, :query)")
                        .expressionAttributeNames(Map.of("#title", "title"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        stopWatch.stop();

        List<Map<String, AttributeValue>> items = response.items();

        convert(movies, items);

        log.info("Found {}. StopWatch: {}", movies.size(), stopWatch);

        return movies;
    }

    public List<MovieDetail> findByYear(String searchQuery) {
        List<MovieDetail> movies = new ArrayList<>();

        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = DynamoDbClient.create();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#year, :query)")
                        .expressionAttributeNames(Map.of("#year", "year"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        stopWatch.stop();

        List<Map<String, AttributeValue>> items = scanResponse.items();

        convert(movies, items);

        log.info("Found {}. StopWatch: {}", movies.size(), stopWatch);

        return movies;
    }

    public List<MovieDetail> findByCast(String searchQuery) {
        List<MovieDetail> movies = new ArrayList<>();

        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = DynamoDbClient.create();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#cast, :query)")
                        .expressionAttributeNames(Map.of("#cast", "cast"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        stopWatch.stop();

        List<Map<String, AttributeValue>> items = scanResponse.items();

        convert(movies, items);

        log.info("Found {}. StopWatch: {}", movies.size(), stopWatch);

        return movies;
    }

    public List<MovieDetail> findByGenre(String searchQuery) {
        List<MovieDetail> movies = new ArrayList<>();

        StopWatch stopWatch = new StopWatch("DynamoDB");

        stopWatch.start("Scan Cast");

        DynamoDbClient client = DynamoDbClient.create();

        ScanResponse scanResponse = client.scan(
                ScanRequest.builder()
                        .tableName(tableName)
                        .filterExpression("contains(#genres, :query)")
                        .expressionAttributeNames(Map.of("#genres", "genres"))
                        .expressionAttributeValues(Map.of(":query", AttributeValue.builder().s(searchQuery).build()))
                        .build()
        );

        stopWatch.stop();

        List<Map<String, AttributeValue>> items = scanResponse.items();

        convert(movies, items);

        log.info("Found {}. StopWatch: {}", movies.size(), stopWatch);

        return movies;
    }

    private static void convert(List<MovieDetail> movies, List<Map<String, AttributeValue>> items) {
        for (Map<String, AttributeValue> item : items) {
            movies.add(
                    MovieDetail.builder()
                            .title(item.get("title") == null ? "" : item.get("title").s())
                            .year(item.get("year") == null ? "" : item.get("year").s())
                            .cast(item.get("cast") == null ? "" : item.get("cast").s())
                            .genre(item.get("genres") == null ? "" : item.get("genres").s())
                            .build());
        }
    }

}
