package com.store.movie.controller.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Movie Detail Information")
@Data
@Builder
public class MovieDetail {

    @Schema(description = "Movie title")
    private String title;

    @Schema(description = "Movie year")
    private String year;

    @Schema(description = "Movie cast")
    private String cast;

    @Schema(description = "Movie genre")
    private String genre;

}
