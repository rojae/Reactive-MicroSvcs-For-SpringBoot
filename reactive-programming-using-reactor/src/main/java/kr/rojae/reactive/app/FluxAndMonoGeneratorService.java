package kr.rojae.reactive.app;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FluxAndMonoGeneratorService {

	// Flux는 다중 요소
	// publisher
	public Flux<String> namesFlux(){
		return Flux.fromIterable(List.of("rojae", "kim", "alex"))
				.log();
	}

	// Mono는 하나의 요소
	// publisher
	public Mono<String> nameMono(){
		return Mono.just("alex")
				.log();
	}

	// Flux map operator
	public Flux<String> namesFlux_map(){
		return Flux.fromIterable(List.of("rojae", "kim", "alex"))
				.map(String::toUpperCase)
//				.map(name -> name.toUpperCase())
				.log();
	}
	public static void main(String[] args) {
		FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

		// Flux 객체를 접근하며, 구독함 (Flux : 0 ~ N)
		// 구독하지 않으면, 아무런 동작이 일어나지 않는다. (Subscribe)
		fluxAndMonoGeneratorService.namesFlux()
				.subscribe(name -> {
					System.out.println("Name is : " + name);
				});

		// Mono 객체 접근하여, 구독 (Mono : 0 ~ 1)
		// 구독하지 않으면, 아무런 동작이 일어나지 않는다. (Subscribe)
		fluxAndMonoGeneratorService.nameMono()
				.subscribe(name -> {
					System.out.println("Mono Name is : " + name);
				});
	}

}
