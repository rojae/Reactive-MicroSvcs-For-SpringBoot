package com.reactivespring.client;

import com.reactivespring.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
public class ReviewsRestClient {
    private final WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewInfoUrl;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId){
        var uri = UriComponentsBuilder.fromHttpUrl(reviewInfoUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand().toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Review.class)
                .log();
    }

}
