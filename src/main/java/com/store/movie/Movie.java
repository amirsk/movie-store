package com.store.movie;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Movie {

    private String title;
    private int year;
    private String cast;
    private String genres;

    @DynamoDbPartitionKey
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return this.year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public String getCast() {
        return this.cast;
    }
    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getGenres() {
        return this.genres;
    }
    public void setGenres(String genres) {
        this.genres = genres;
    }

}
