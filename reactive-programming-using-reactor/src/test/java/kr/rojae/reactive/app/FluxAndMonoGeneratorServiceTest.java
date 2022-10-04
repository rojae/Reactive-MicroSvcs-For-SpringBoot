package kr.rojae.reactive.app;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class FluxAndMonoGeneratorServiceTest {

	FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

	@Test
	void nameFlux(){
		// given

		// when
		var namesFlux = fluxAndMonoGeneratorService.namesFlux();

		// then
		// StepVerifier.create :: publisher와 subscriber사이의 event를 trigger 시킨다.
		StepVerifier.create(namesFlux)
				//.expectNext("rojae", "kim", "alex")
				.expectNextCount(3)
				.verifyComplete();
	}

	@Test
	void namesFlux_map() {
		// given

		// when
		var namesFluxMap= fluxAndMonoGeneratorService.namesFlux_map();

		// then
		StepVerifier.create(namesFluxMap)
				.expectNext("ROJAE", "KIM", "ALEX")
				.verifyComplete();
	}
}
