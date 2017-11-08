package de.b4sh.byter.support;

import java.util.Random;
import java.util.logging.Logger;

public final class ThreadStarter {

    public static void main(String... args){
        Thread t1 = new Thread(new MyRunner());
        t1.run();
    }

    private static class MyRunner implements Runnable{
        private final Logger log = Logger.getLogger(getClazzName());

        @Override
        public void run() {
            log.info("Random Number: " +  new Random().nextInt(1000));
        }

        private String getClazzName(){
            return this.getClass().getName();
        }
    }
}
