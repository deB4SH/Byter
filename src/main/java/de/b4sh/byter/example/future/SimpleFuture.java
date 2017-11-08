package de.b4sh.byter.example.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example class for Future usage.
 * Showcase for java.util.concurrent.Future and ExecutorService.
 */
public final class SimpleFuture {

    private static final Logger log = Logger.getLogger(SimpleFuture.class.getName());

    private SimpleFuture(){
        //nop
    }

    /**
     * main method to start.
     * @param args arguments
     */
    public static void main(final String[] args){
        try {
            doFuture();
        } catch (ExecutionException e) {
            log.log(Level.WARNING,"ExecutionException",e);
        } catch (InterruptedException e) {
            log.log(Level.WARNING,"InterruptedException",e);
        }
    }

    /**
     * Function for running futures.
     * @throws ExecutionException Exception thrown due to execution errors inside the future
     * @throws InterruptedException Exception thrown due to a exception on the interrupted state
     */
    private static void doFuture() throws ExecutionException, InterruptedException {
        final ExecutorService service = Executors.newFixedThreadPool(5);
        final Future<String> futureTwo = service.submit(() -> {
            doSomething(1,2);
            final String futureOne = service.submit(() -> {
                doSomething(2,3);
                return "hello";
            }).get();
            return futureOne + " master thesis";
        });
        log.log(Level.INFO,futureTwo.get());
        service.shutdown();
    }

    /**
     * simple function for miming some tasks.
     * @param a one parameter
     * @param b an another parameter
     */
    private static void doSomething(final int a, final int b){
        final int c =  a + b;
    }

}
