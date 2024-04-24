package com.store.movie.controller;

import com.store.movie.controller.schema.MovieDetail;
import com.store.movie.service.DynamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Tag(name = "Movie Store API", description = "APIs to manage Movie Store")
@RestController
@RequestMapping("/store")
@Log4j2
public class MovieController {

    private final DynamoService dynamoService;

    public MovieController(final DynamoService dynamoService) {
        this.dynamoService = dynamoService;
    }

    @Operation(
            summary = "Retrieve Movie details",
            description = "Get Movie details by providing movie title, year, cast, or genre"
    )
    @Parameters({
            @Parameter(name = "title", description = "Search by Movie Title"),
            @Parameter(name = "year", description = "Search by Movie Year"),
            @Parameter(name = "cast", description = "Search by Movie Cast"),
            @Parameter(name = "genre", description = "Search by Movie Genre")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(
                            array = @ArraySchema(schema = @Schema(implementation = MovieDetail.class)),
                            mediaType = "application/json"
                    )
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Error")
    })
    @GetMapping(path = "/movie")
    public ResponseEntity<List<MovieDetail>> movie(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String cast,
            @RequestParam(required = false) String genre
    ) throws Exception {
        boolean isValid = validate(title, year, cast, genre);

        if(!isValid) {
            return ResponseEntity.badRequest().build();
        }

        if(StringUtils.hasLength(title)) {
            List<MovieDetail> movies = dynamoService.findByTitle(title);

            if(CollectionUtils.isEmpty(movies)) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(movies);
        }

        if(StringUtils.hasLength(year)) {
            List<MovieDetail> movies = dynamoService.findByYear(year);

            if(CollectionUtils.isEmpty(movies)) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(movies);
        }

        if(StringUtils.hasLength(cast)) {
            List<MovieDetail> movies = dynamoService.findByCast(cast);

            if(CollectionUtils.isEmpty(movies)) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(movies);
        }

        if(StringUtils.hasLength(genre)) {
            List<MovieDetail> movies = dynamoService.findByGenre(genre);

            if(CollectionUtils.isEmpty(movies)) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(movies);
        }

        return ResponseEntity.internalServerError().build();
    }

    private boolean validate(String title, String year, String cast, String genre) {
        return Stream
                .of(title, year, cast, genre)
                .filter(Objects::nonNull)
                .count() == 1;
    }

}
