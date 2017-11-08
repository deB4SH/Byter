package de.b4sh.byter.support;

import java.io.File;

import de.b4sh.byter.utils.data.ChunkGenerator;
import de.b4sh.byter.utils.writer.WriterBuffered;
import de.b4sh.byter.utils.writer.WriterInterface;

public final class TestCaseHelper {

    /**
     * creates a x-times chunk size byte file at the given location.
     * @param times how often should the chunk be written to disc
     * @param chunkSize the desired chunk size
     * @param directory directory to write to
     * @param filename filename to use
     */
    public static void createTestCaseFile(final String directory, final String filename, final int chunkSize, final int times){
        //create test dummy file
        final File file = new File(directory, filename);
        final byte[] byteArray = ChunkGenerator.generateChunk(chunkSize);
        final WriterInterface wi = new WriterBuffered(8192,file);
        wi.setAutomaticFileRemoval(false);
        for(int i = 0; i < times; i++){
            wi.handleData(byteArray);
        }
        wi.finish();
    }

}
