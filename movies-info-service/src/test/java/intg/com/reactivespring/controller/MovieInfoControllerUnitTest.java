package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.isA;
import static reactor.core.publisher.Mono.when;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMoviesInfo() {
        var movieInfoList = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        Mockito.when(moviesInfoServiceMock.getAllMovieInfos())
                .thenReturn(Flux.fromIterable(movieInfoList));

        webTestClient.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        var newMovieInfo = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        Mockito.when(moviesInfoServiceMock.getMovieInfo(newMovieInfo.getMovieInfoId()))
                .thenReturn(Mono.just(newMovieInfo));

        webTestClient.get()
                .uri(MOVIES_INFO_URL + "/{id}", newMovieInfo.getMovieInfoId())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var bodyValue = movieInfoEntityExchangeResult.getResponseBody();
                    assert bodyValue.getMovieInfoId().equals(newMovieInfo.getMovieInfoId());
                    assert bodyValue.getName().equals(newMovieInfo.getName());
                });
    }

    @Test
    void addMovieInfo() {
        // given
        var newMovieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        Mockito.when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class)))
                .thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        // when
        webTestClient.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(newMovieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert Objects.equals(savedMovieInfo.getMovieInfoId(), "mockId");
                });

        // then
    }

    @Test
    void addMovieInfo_validation() {
        // given
        var newMovieInfo = new MovieInfo(null, "",
                -2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        Mockito.when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class)))
                .thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        // when
        webTestClient.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(newMovieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    var expectedErrorMessage = "movieInfo.name must be present,movieInfo.year must be positive value";
                    assert responseBody != null;
                    assert responseBody.equals(expectedErrorMessage);
                });

        // then
    }

    @Test
    void updateMovieInfo() {
        var id = "mockId";
        var newMovieInfo = new MovieInfo(null, "Dark Knight Rises 1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        Mockito.when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(new MovieInfo(id, "Dark Knight Rises 1",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        webTestClient.put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(newMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert Objects.equals(updatedMovieInfo.getName(), newMovieInfo.getName());
                    assert Objects.equals(updatedMovieInfo.getYear(), newMovieInfo.getYear());
                    assert updatedMovieInfo.getRelease_date().compareTo(newMovieInfo.getRelease_date()) == 0;
                    assert updatedMovieInfo.getCast().size() == newMovieInfo.getCast().size();
                    assert Objects.equals(updatedMovieInfo.getMovieInfoId(), "mockId");
                });
    }

    @Test
    void deleteMovieInfo() {
        var id = "abc";

        Mockito.when(moviesInfoServiceMock.deleteMovieInfo(isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }


}
