package com.store.movie.service;

import com.store.movie.controller.schema.MovieDetail;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class AthenaService {

    String database = "default";
    String table = "movie";
    String outputLocation = "s3://athena-output-demo/";

    public List<MovieDetail> findByTitle(String search) throws InterruptedException {
        String findByName = "SELECT * FROM movie WHERE title LIKE '%" + search + "%'";
        return query(findByName);
    }

    public List<MovieDetail> findByYear(Integer search) throws InterruptedException {
        String findByYear = "SELECT * FROM movie WHERE year = " + search;
        return query(findByYear);
    }

    public List<MovieDetail> findByCast(String search) throws InterruptedException {
        String columnName = "\"cast\"";

        String findByCast = "SELECT * FROM " + database + "." + table +
                " CROSS JOIN UNNEST(" + columnName + ") AS t(cast_item)" +
                " WHERE cast_item LIKE '%" + search + "%'";

        return query(findByCast);
    }

    public List<MovieDetail> findByGenre(String search) throws InterruptedException {
        String columnName = "genres";

        String findByGenre = "SELECT * FROM " + database + "." + table +
                " CROSS JOIN UNNEST(" + columnName + ") AS t(cast_item)" +
                " WHERE cast_item LIKE '%" + search + "%'";

        return query(findByGenre);
    }

    private List<MovieDetail> query(String query) throws InterruptedException {
        AthenaClient client = AthenaClient.builder()
                .region(Region.US_EAST_1)
                .build();

        StopWatch stopWatch = new StopWatch("Athena");

        stopWatch.start("submitAthenaQuery");
        String queryExecutionId = submitAthenaQuery(client, query);
        stopWatch.stop();

        stopWatch.start("waitForQueryToComplete");
        waitForQueryToComplete(client, queryExecutionId);
        stopWatch.stop();

        stopWatch.start("processResultRows");
        List<MovieDetail> movieDetails = processResultRows(client, queryExecutionId);
        stopWatch.stop();

        log.info("Operation finished. Found: {} - StopWatch: {}", movieDetails.size(), stopWatch);

        client.close();

        return movieDetails;
    }

    // // Submits a sample query to Amazon Athena and returns the execution ID of the query.
    private String submitAthenaQuery(AthenaClient client, String query) {
        try {
            // The QueryExecutionContext allows us to set the database.
            QueryExecutionContext context = QueryExecutionContext.builder()
                    .database(database)
                    .build();

            // The result configuration specifies where the results of the query should go.
            ResultConfiguration result = ResultConfiguration.builder()
                    .outputLocation(outputLocation)
                    .build();

            StartQueryExecutionRequest request = StartQueryExecutionRequest.builder()
                    .queryString(query)
                    .queryExecutionContext(context)
                    .resultConfiguration(result)
                    .build();

            StartQueryExecutionResponse response = client.startQueryExecution(request);

            return response.queryExecutionId();
        } catch (AthenaException ex) {
            log.error("!!! Exception !!!", ex);
        }

        return "";
    }

    // Wait for an Amazon Athena query to complete, fail or to be cancelled.
    private void waitForQueryToComplete(AthenaClient client, String queryExecutionId) throws InterruptedException {
        GetQueryExecutionRequest request = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();

        GetQueryExecutionResponse response;

        boolean isQueryStillRunning = true;

        while (isQueryStillRunning) {
            response = client.getQueryExecution(request);

            String queryState = response.queryExecution().status().state().toString();

            if(queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("The Amazon Athena query failed to run with error message: " + response.queryExecution().status().stateChangeReason());
            } else if(queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("The Amazon Athena query was cancelled.");
            } else if(queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            }

            log.info("The current status is: " + queryState);
        }
    }

    // This code retrieves the results of a query.
    private List<MovieDetail> processResultRows(AthenaClient client, String queryExecutionId) {
        try {
            // Max Results can be set but if its not set, it will choose the maximum page size.
            GetQueryResultsRequest request = GetQueryResultsRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();

            GetQueryResultsIterable iterableResults = client.getQueryResultsPaginator(request);

            List<MovieDetail> movieDetails = new ArrayList<>();

            for(GetQueryResultsResponse result: iterableResults) {
                List<Row> results = result.resultSet().rows();
                processRow(movieDetails, results);
            }

            return movieDetails;
        } catch (AthenaException ex) {
            log.error("!!! Exception !!!", ex);
            return null;
        }
    }

    private void processRow(List<MovieDetail> movieDetails, List<Row> results) {
        results.stream().skip(1).forEach(row -> {
            MovieDetail movieDetail = new MovieDetail();
            List<Datum> datum = row.data();
            movieDetail.setTitle(datum.get(0).varCharValue());
            movieDetail.setYear(datum.get(1).varCharValue());
            movieDetail.setCast(datum.get(2).varCharValue());
            movieDetail.setGenre(datum.get(3).varCharValue());
            movieDetails.add(movieDetail);
        });
    }
}
