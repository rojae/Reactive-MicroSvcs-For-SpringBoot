package com.reactivespring;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sinkTest(){
        // given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        // when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux1 = replaySink.asFlux();
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscribe 1 : " + i);
        });

        Flux<Integer> integerFlux2 = replaySink.asFlux();
        integerFlux2.subscribe((i) -> {
            System.out.println("Subscribe 2 : " + i);
        });

        replaySink.tryEmitNext(3 );

        Flux<Integer> integerFlux3 = replaySink.asFlux();
        integerFlux3.subscribe((i) -> {
            System.out.println("Subscribe 3 : " + i);
        });
    }

    @Test
    void sinks_multicast(){
        // given
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        // when
        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux1 = multicast.asFlux();
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscribe 1 : " + i);
        });

        // 새로운 Subscriber 생성 이후에 발생된 이벤트만 수신
        Flux<Integer> integerFlux2 = multicast.asFlux();
        integerFlux2.subscribe((i) -> {
            System.out.println("Subscribe 2 : " + i);
        });

        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void sinks_unicast(){
        // given
        // UnicastProcessor는 하나의 Subscriber만 가진다
        Sinks.Many<Integer> unicast = Sinks.many().unicast().onBackpressureBuffer();

        // when
        unicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux1 = unicast.asFlux();
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscribe 1 : " + i);
        });

        // 새로운 subscriber는 생성 이후에 발생된 이벤트만 수신
        Flux<Integer> integerFlux2 = unicast.asFlux();
        integerFlux2.subscribe((i) -> {
            System.out.println("Subscribe 2 : " + i);
        });

        unicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }

}
