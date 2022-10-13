package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.0.0")
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @BeforeEach
    void setup() {
        var movieInfoList = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfoList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        // given

        // when
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        // then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        // given

        // when
        var movieInfoMono = movieInfoRepository.findById("abc").log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo(){
        // given
        var newMovieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        // when
        var moviesInfoMono = movieInfoRepository.save(newMovieInfo).log();

        // then
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(movieInfo.getName(), "Batman Begins");
                    assertNotNull(movieInfo.getMovieInfoId());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo(){
        // given
        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2022);

        // when
        var movieInfoMono = movieInfoRepository.save(movieInfo).log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertEquals(movieInfo1.getYear(), 2022);
                    assertEquals(movieInfo1.getMovieInfoId(), "abc");
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo(){
        // given

        // when
        movieInfoRepository.deleteById("abc").log().block();
        var moviesInfo = movieInfoRepository.findAll().log();

        // then
        StepVerifier.create(moviesInfo)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear(){
        var movieInfoFlux = movieInfoRepository.findByYear(2005).log();
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }


}