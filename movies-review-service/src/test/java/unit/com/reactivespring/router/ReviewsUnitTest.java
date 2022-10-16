package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    static String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addReview(){
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        // when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    System.out.println(savedReview);

                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assert savedReview.getComment().equals(review.getComment());
                });

        // then
    }

    @Test
    void addReview_validation(){
        // given
        var review = new Review(null, null, "Awesome Movie", -9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        // when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Review.movieInfoId : must not be null,Review.negative : please pass a non-negative value");

        // then
    }

    @Test
    void getReviews() {
        // given
        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        // when
        when(reviewReactiveRepository.findAll())
                .thenReturn(Flux.fromIterable(reviewList));

        webTestClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
        // then
    }

    @Test
    void getReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        // when
        when(reviewReactiveRepository.findReviewsByMovieInfoId(1L))
                .thenReturn(Flux.just(review));

        // then
        var uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class);
    }

    @Test
    void updateReview() {
        //given
        var id = "abc";
        var reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 8.0);

        //when
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 8.0)));
        when(reviewReactiveRepository.findById((String) any()))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", id)
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var updatedReview = reviewResponse.getResponseBody();
                    assert updatedReview != null;
                    System.out.println("updatedReview : " + updatedReview);
                    assertEquals(8.0, updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });
    }

    @Test
    void deleteReview() {
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 8.0)));
        when(reviewReactiveRepository.findById((String) any()))
                .thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 9.0)));
        when(reviewReactiveRepository.deleteById((String) any()))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(REVIEWS_URL + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();

    }
}
