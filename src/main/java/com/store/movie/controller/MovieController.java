package com.store.movie.controller;

import com.store.movie.service.AthenaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.stream.Stream;

@Tag(name = "Movie Store API", description = "APIs to manage Movie Store")
@RestController
@RequestMapping("/store")
@Log4j2
public class MovieController {

    private final AthenaService athenaService;

    public MovieController(final AthenaService athenaService) {
        this.athenaService = athenaService;
    }

    @Parameters({
            @Parameter(name = "year", description = "Search by Movie Year"),
            @Parameter(name = "name", description = "Search by Movie Name"),
            @Parameter(name = "cast", description = "Search by Movie Cast"),
            @Parameter(name = "genre", description = "Search by Movie Genre")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @GetMapping(path = "/movie")
    public ResponseEntity<String> movie(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cast,
            @RequestParam(required = false) String genre
    ) throws Exception {
        boolean isValid = validate(year, name, cast, genre);

        if(!isValid) {
            return ResponseEntity.badRequest().body("Input parameters are incorrect.");
        }

        if(StringUtils.hasLength(year)) {
            athenaService.findByYear(Integer.valueOf(year));
            return ResponseEntity.ok("findByYear");
        }

        if(StringUtils.hasLength(name)) {
            athenaService.findByName(name);
            return ResponseEntity.ok("findByName");
        }

        if(StringUtils.hasLength(cast)) {
            athenaService.findByCast(cast);
            return ResponseEntity.ok("findByCast");
        }

        if(StringUtils.hasLength(genre)) {
            athenaService.findByGenre(genre);
            return ResponseEntity.ok("findByGenre");
        }

        return ResponseEntity.internalServerError().body("Something went wrong");
    }

    private boolean validate(String year, String name, String cast, String genre) {
        return Stream
                .of(year, name, cast, genre)
                .filter(Objects::nonNull)
                .count() == 1;
    }

}
