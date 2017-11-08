package de.b4sh.byter.utils.io;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java PortScanner to find next open port.
 * uses Java8 Futures and ExecutorService to scan.
 */
public final class PortScanner {

    private static final Logger log = Logger.getLogger(PortScanner.class.getName());

    private PortScanner(){
        //nop
    }

    /**
     * Looks up for open Ports on localhost.
     * @param startPort start port you want to look at
     * @param range how many ports should be tested?
     * @return next open and free port
     */
    public static int getNextPort(final int startPort, final int range){
        final ExecutorService service = Executors.newFixedThreadPool(decidePoolSize(range));
        final int timeout = 200;
        final List<Future<Boolean>> futures = new ArrayList<>();
        for(int port = startPort; port < startPort + range; port++){
            futures.add(checkPort(service,port,timeout));
        }
        service.shutdown();
        nap(1000); //give executorservice a bit time to run
        int actualPort = startPort;
        for(Future<Boolean> f: futures){
            try {
                if(!f.get()){//false means there is no service alive on this port - or something listening
                    return actualPort;
                }else{
                    actualPort++;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.log(Level.WARNING, "Exception during testing ports.",e);
            }
        }
        return -1;
    }

    private static Future<Boolean> checkPort(final ExecutorService es, final int port, final int to){
        return es.submit(() -> {
            try{
                final Socket socket = new Socket();
                socket.connect(new InetSocketAddress("127.0.0.1",port),to);
                socket.close();
                return true;
            }catch (SocketTimeoutException | ConnectException e){
                return false;
            }
        });
    }

    private static int decidePoolSize(final int range){
        if(range < 10){
            return 2;
        }else if(range > 10 && range < 50){
            return 5;
        }else if(range > 50){
            return 10;
        }else{
            return 2;
        }
    }

    private static void nap(final long sleepTime){
        try{
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.log(Level.WARNING,"Could not interrupt and sleep!");
        }
    }
}
