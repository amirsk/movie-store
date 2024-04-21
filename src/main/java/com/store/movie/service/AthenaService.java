package com.store.movie.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;

import java.util.List;

@Service
@Log4j2
public class AthenaService {

    String database = "default";
    String table = "movie";
    String outputLocation = "s3://athena-output-demo/";

    public void findByYear(Integer search) throws InterruptedException {
        String findByYear = "SELECT * FROM movie WHERE year = " + search;
         query(findByYear);
    }

    public void findByName(String search) throws InterruptedException {
        String findByName = "SELECT * FROM movie WHERE title LIKE '%" + search + "%'";
         query(findByName);
    }

    public void findByCast(String search) throws InterruptedException {
        String columnName = "\"cast\"";

        String findByCast = "SELECT * FROM " + database + "." + table +
                " CROSS JOIN UNNEST(" + columnName + ") AS t(cast_item)" +
                " WHERE cast_item LIKE '%" + search + "%'";

        query(findByCast);
    }

    public void findByGenre(String search) throws InterruptedException {
        String columnName = "genres";

        String findByGenre = "SELECT * FROM " + database + "." + table +
                " CROSS JOIN UNNEST(" + columnName + ") AS t(cast_item)" +
                " WHERE cast_item LIKE '%" + search + "%'";

        query(findByGenre);
    }

    private void query(String query) throws InterruptedException {
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
        processResultRows(client, queryExecutionId);
        stopWatch.stop();

        log.info("Operation finished. StopWatch: {}", stopWatch);

        client.close();
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
            } else {
                // Sleep an amount of time before retrying again.
                Thread.sleep(1000);
            }

            log.info("The current status is: " + queryState);
        }
    }

    // This code retrieves the results of a query.
    private void processResultRows(AthenaClient client, String queryExecutionId) {
        try {
            // Max Results can be set but if its not set, it will choose the maximum page size.
            GetQueryResultsRequest request = GetQueryResultsRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();

            GetQueryResultsIterable iterableResults = client.getQueryResultsPaginator(request);

            for(GetQueryResultsResponse result: iterableResults) {
                List<ColumnInfo> columnInfoList = result.resultSet().resultSetMetadata().columnInfo();
                List<Row> results = result.resultSet().rows();
                processRow(results, columnInfoList);
            }
        } catch (AthenaException ex) {
            log.error("!!! Exception !!!", ex);
        }
    }

    private void processRow(List<Row> results, List<ColumnInfo> columnInfoList) {
        for(Row row:results) {
            List<Datum> allData = row.data();
            for (Datum data : allData) {
                log.info("The value of the column is " + data.varCharValue());
            }
        }
    }
}
