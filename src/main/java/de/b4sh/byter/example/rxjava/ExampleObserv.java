package de.b4sh.byter.example.rxjava;

import java.util.logging.Level;
import java.util.logging.Logger;

import rx.Observable;
import rx.Subscriber;

/**
 * Example Class for RxJava.
 */
public final class ExampleObserv {

    private static final Logger log = Logger.getLogger(ExampleObserv.class.getName());

    private ExampleObserv(){
        //nop
    }

    /**
     * main method to start.
     * @param args arguments
     */
    public static void main(final String[] args){
        createRxJavaComponentsAndRun();
    }

    /**
     * creating rx components.
     */
    private static void createRxJavaComponentsAndRun(){
        //create Observable
        final Observable<Integer> integerObservable = Observable.create(observableObj -> {
            for(int i = 0; i < 10; i++){
                observableObj.onNext(i);
            }
            observableObj.onCompleted();
        });
        //sleep for showoff purpose
        nap(1000);
        //Subscriber
        final Subscriber<Integer> integerSubscriber = new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                log.log(Level.INFO,"Observerable is complete.");
            }

            @Override
            public void onError(final Throwable throwable) {
                log.log(Level.WARNING, "Something bad happend.", throwable.getCause());
            }

            @Override
            public void onNext(final Integer integer) {
                log.log(Level.INFO, "Next Integer: " + integer);
            }
        };
        integerObservable.subscribe(integerSubscriber);
    }

    /**
     * sleep a bit to simulate some other process.
     * @param time
     */
    private static void nap(final long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.log(Level.WARNING,"Error during sleep.");
        }
    }

}
