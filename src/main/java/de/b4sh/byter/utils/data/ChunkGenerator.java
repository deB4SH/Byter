package de.b4sh.byter.utils.data;

import java.util.Random;

/**
 * Class for a chunk generated that pre generates a byte array for the client.
 */
public final class ChunkGenerator {

    private ChunkGenerator(){
        //nop
    }

    /**
     * Chunk-Data Generator.
     * @param length length of the random byte array.
     * @return byte array with specific length
     */
    public static byte[] generateChunk(final int length){
        final byte[] tmp = new byte[length];
        new Random().nextBytes(tmp);
        return tmp;
    }

}
