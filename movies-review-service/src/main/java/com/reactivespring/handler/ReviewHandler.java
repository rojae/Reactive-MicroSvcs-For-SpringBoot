package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;
    private final Validator validator;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository, Validator validator) {
        this.reviewReactiveRepository = reviewReactiveRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constraintViolation = validator.validate(review);
        log.info("ConstraintViolation : {}", constraintViolation);

        if(constraintViolation.size() > 0){
            var errorMessage = constraintViolation
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));

            throw new ReviewDataException(errorMessage);
        }
    }


    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var movieInfoId = serverRequest.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            var reviewFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()))
                    .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review Not Found For Given movieInfoId Id " + movieInfoId.get())));
            return buildReviewsResponse(reviewFlux);
        } else {
            var reviewFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewFlux);
        }
    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewFlux) {
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review Not Found For Given Review Id " + reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
                //.switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review Not Found For Given Review Id " + reviewId)));

        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }
}
