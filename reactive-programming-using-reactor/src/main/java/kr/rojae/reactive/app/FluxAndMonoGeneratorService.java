package kr.rojae.reactive.app;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    // Flux는 다중 요소
    // publisher
    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .log();
    }

    // Mono는 하나의 요소
    // publisher
    public Mono<String> nameMono() {
        return Mono.just("alex")
                .log();
    }

    // Flux map operator
    public Flux<String> namesFlux_map() {
        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .map(String::toUpperCase)
//				.map(name -> name.toUpperCase())
                .log();
    }

    // Flux Immutability
    public Flux<String> namesFlux_immutability() {
        var namesFlux = Flux.fromIterable(List.of("rojae", "kim", "alex"));
        namesFlux.map(String::toUpperCase);    // 수정할 수 없다. 왜냐면 FLUX 객체가 현재 불변 상태이기 때문임.
        return namesFlux;
    }

    // Flex With Filter
    // Filter the string whose length is greater than 3
    public Flux<String> namesFlux_map(int stringLength) {
        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .map(s -> String.format("%s-%s", s.length(), s))
                .log();
    }

    // flatMap :: 스트림의 원소가 배열과 같은 경우, 단일 스트림으로 반환해줌
    public Flux<String> namesFlux_flatMap(int stringLength) {
        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString(s))
                .log();
    }

    // flatMap :: 스트림의 원소가 배열과 같은 경우, 단일 스트림으로 반환해줌
    // 속도 : concatMap < flatMap
    // 순서보장 : X
    // flatMap :: API들이 1초씩 걸린다면, 9초의 반 정도 걸림 (4-5초)
    // 중요 :: 이때 각각 원소들의 순서는 보장 받음 (rojae를 수신 받는 순서는 r > o > j > a > e 순서이다)
    public Flux<String> namesFlux_flatMap_async(int stringLength) {
        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString_withDelay(s))
                .log();
    }

    // concatMap : 비동기 파이프라인의 순서를 보장해줌 (파이프라인에서 처리 중인, 요소들의 순서를 유지시켜준다)
    // 속도 : concatMap < flatMap
    // 순서보장 : O
    // concatMap :: API들이 1초씩 걸리고, 9개를 보내면 9초의 시간이 걸릴 수 있음.. (순서 보장하기 때문)
    public Flux<String> namesFlux_concatMap_async(int stringLength) {
        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .concatMap(s -> splitString_withDelay(s))
                .log();
    }

    public Mono<String> namesMono_map_filter(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .log();
    }

    public Mono<List<String>> namesMono_map_flatMap(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono)
                .log();
    }

    // flatMap이지만 단일 원소이기 때문에, 순서는 상관이 없다
    // alex :: a > l > e > x 순서로 수신받음
    public Flux<String> namesMono_map_flatMapMany(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString_withDelay)
                .log();
    }

    // flatMap :: 스트림의 원소가 배열과 같은 경우, 단일 스트림으로 반환해줌
    public Flux<String> namesFlux_transform(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .transform(filterMap)
                .flatMap(s -> splitString_withDelay(s))
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFlux_transform_switchIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        var defaultFlux = Flux.just("default")
                .transform(filterMap);

        return Flux.fromIterable(List.of("rojae", "kim", "alex"))
                .transform(filterMap)
                .flatMap(s -> splitString_withDelay(s))
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> explore_concat() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux).log();
    }

    // Flux("A", "B", "C") + Flux("D", "E", "F")
    // -> Flux("A", "B", "C", "D", "E", "F")
    public Flux<String> explore_concatWith() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");

        return defFlux.concatWith(defFlux).log();
    }

    // Mono(A) + Mono(B) -> Flux("A", "B")
    public Flux<String> explore_concatWith_mono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");

        return aMono.concatWith(bMono).log();
    }

    public Flux<String> explore_merge() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100))
                .log(); // A -> B -> C

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125))
                .log(); // D -> E -> F

        return Flux.merge(abcFlux, defFlux).log();
    }

    public Flux<String> explore_mergeWith() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100))
                .log(); // A -> B -> C

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125))
                .log(); // D -> E -> F

        return defFlux.mergeWith(abcFlux).log();
    }

    // MergeSequential() :: 순서 보장
    public Flux<String> explore_mergeSequential() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100))
                .log(); // A -> B -> C

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125))
                .log(); // D -> E -> F

        return Flux.mergeSequential(abcFlux, defFlux).log();
    }

    public Flux<String> explore_zip() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second).log(); // AD, BE, CF
    }

    public Flux<String> explore_zipWith() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        return abcFlux.zipWith(defFlux, (first, second) -> first + second).log(); // AD, BE, CF
    }

    public Mono<String> explore_zipWith_mono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log(); // AB
    }

    public Flux<String> explore_zip_1() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        var _123Flux = Flux.just("1", "2", "3")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        var _456Flux = Flux.just("4", "5", "6")
                .delayElements(Duration.ofMillis(new Random().nextInt(1000)))
                .log();

        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log(); // AD14, BE25, CF36
    }


    //////////////////////////////////////////////////
    // Utility
    //////////////////////////////////////////////////

    // ROJAE -> FLUX(R,O,J,A,E)
    public Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    // ROJAE -> FLUX(R,O,J,A,E)
    // ADD Delay Option
    public Flux<String> splitString_withDelay(String name) {
        var charArray = name.split("");
        var delay = new Random().nextInt(1000);
//        var delay = 1000;
        return Flux.fromArray(charArray)
                .delayElements(Duration.ofMillis(delay));
    }

    private Mono<List<String>> splitStringMono(String s) {
        var charArrays = s.split("");
        System.out.println(charArrays);
        var charList = List.of(charArrays);     // [A,L,E,X]
        return Mono.just(charList);
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
