package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Review {
    @Id
    private String reviewId;
    @NotNull(message = "Review.movieInfoId : must not be null")
    private Long movieInfoId;
    @NotBlank(message = "Review.comment : must not be null")
    private String comment;
    @Min(value = 0L, message = "Review.negative : please pass a non-negative value")
    private Double rating;
}
