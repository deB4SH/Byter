/*
 * File: CompletableFutureExample
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-15
 * Type: Class
 */
package de.b4sh.byter.example.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example-Class for a completable future.
 */
public final class CompletableFutureExample {

    private final static Logger log = Logger.getLogger(CompletableFutureExample.class.getName());

    /**
     * Run Method for this example.
     * @param args arugments are silently ignored
     * @throws ExecutionException silently ignored
     * @throws InterruptedException silently ignored
     */
    public static void main(final String[] args) throws ExecutionException, InterruptedException {
        final CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " master thesis."));
        log.log(Level.INFO,completableFuture.get());
    }
}
