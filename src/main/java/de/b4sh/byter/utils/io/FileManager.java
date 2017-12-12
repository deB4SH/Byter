package de.b4sh.byter.utils.io;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.data.DateGenerator;
import de.b4sh.byter.utils.data.OsUtils;

/**
 * FileManager class for FileIO Tasks.
 */
@SuppressWarnings({"WeakerAccess"})
public final class FileManager {

    private static final Logger log = Logger.getLogger(FileManager.class.getName());
    //this is just the intital value, rebuildEvaluationFolder gets called in every running test
    public static String evaluationFolder = System.getProperty("user.dir") + File.separator
            + "evaluation" + File.separator + DateGenerator.generateTodayString();

    private FileManager(){
        //nop
    }

    private static String evaluationSub(){
        return System.getProperty("user.dir") + File.separator
                + "evaluation" + File.separator + DateGenerator.generateTodayString()
                + File.separator + DateGenerator.generateTimeStringForFile() + "_";
    }

    /**
     * Get a new evaluationFolder for saving eval data.
     * @param testName testname to use
     * @return String with the full eval folder.
     */
    public static String getNewEvaluationFolder(final String testName){
        return evaluationSub() + testName;
    }

    /**
     * Rebuilds the evaluationFolder for the test.
     * /day/timestamp_testname
     * @param testName testname to concat
     */
    public static void rebuildEvaluationFolder(final String testName){
        evaluationFolder = System.getProperty("user.dir") + File.separator
                + "evaluation" + File.separator + DateGenerator.generateTodayString()
                + File.separator + DateGenerator.generateTimeStringForFile() + "_" + testName;
    }

    /**
     * Path correction based on the operating system they are aquired on.
     * @param path path to correct.
     * @return corrected path.
     */
    public static String operationSystemBasedPathCorrection(final String path){
        String ret = "";
        if(OsUtils.isUnix()){
            if(path.contains("\\")){
                //rewrite them to /
                ret = path.replaceAll("\\\\","/");
            }else{
                return path;
            }
        }else if(OsUtils.isWindows()){
            if(path.contains("/")){
                //rewrite them to \
                ret = path.replace("/","\\\\");
            }else{
                return path;
            }
        }
        return ret;
    }

    /**
     * Checks if the given path is write-able.
     * CHECKSTYLE COMPLAIN  SimplifyBooleanReturnCheck - this is a useful complex boolean return
     * @param path path to check
     * @return true | false
     */
    public static boolean isPathWritalbe(final String path){
        final File testFile = new File(path,"writeable.test.io");
        if(!testFile.exists()){
            return testFileCreation(testFile);
        }else{
            if(removeTestFile(testFile)){
                //if io successfully removed, try to create it again.
                return testFileCreation(testFile);
            }
        }
        return false;
    }

    /**
     * Checks if the path exists.
     * @param path path to check for
     * @return true | false
     */
    public static boolean isFolderExisting(final String path){
        final File file = new File(path);
        return isFolderExisting(file);
    }

    /**
     * Checks if the path exists.
     * @param path path to check for
     * @return true | false
     */
    public static boolean isFolderExisting(final File path){
        return path.exists() && path.isDirectory();
    }

    /**
     * Creates folders on given FilePath.
     * The filePath should start from root. eg. /tmp/test/byter
     * @param filePath new file path to create
     * @return flag if folder is created or not
     */
    public static boolean createFolder(final String filePath){
        return createFolder(new File(filePath));
    }

    /**
     * Creates folders on given FilePath.
     * @param file filepath to file
     * @return true (created a new folder) | false (nothing created)
     */
    public static boolean createFolder(final File file){
        synchronized (FileManager.class){
            if(!file.exists()){
                if(file.mkdirs()){
                    log.log(Level.INFO,"Created requested folder: " + file.toString());
                    return true;
                }else{
                    log.log(Level.WARNING, "Could not create requested Folder. Either it exists or the runtime has no rights todo.");
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * Creates a simple file with no content.
     * @param file file to create
     * @return true | false (on fail)
     */
    public static boolean createFile(final File file){
        try {
            return file.createNewFile();
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not create File on given path.");
        }
        return false;
    }

    /**
     * Removes a file from disc.
     * @param file file to remove.
     * @return true | false (on fail)
     */
    public static boolean removeFile(final File file){
        if(!file.delete()){
            log.log(Level.WARNING,"Could not remove file on given path. Please consider yourself to delete it."
                    + "Path: " + file.getAbsolutePath());
            return false;
        }
        return true;
    }

    private static boolean removeTestFile(final File file){
        if(!file.delete()){
            log.log(Level.WARNING,"Could not remove test-io if the path is writable. Please consider yourself to delete the io."
                    + "Path: " + file.getAbsolutePath());
            return false;
        }
        return true;
    }

    private static boolean testFileCreation(final File file){
        try{
            if(file.getParentFile().exists()){
                return fileCreator(file);
            }else{
                log.log(Level.INFO, "Parent of requested is not existing. Creating Parent!");
                if(file.getParentFile().mkdir()){
                    return fileCreator(file);
                }else{
                    log.log(Level.WARNING, "cannot create new folder - does this user have write permissions? requested folder: " + file.getParentFile().toString());
                }
            }
            return false;
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during test if the given path is writeable",e);
            return false;
        }
    }

    private static boolean fileCreator(final File file) throws IOException {
        final boolean created =  file.createNewFile();
        if(created){
            file.delete();
            log.log(Level.INFO, "testfile successfully created and removed , write permissions are on this folder!");
            return created;
        }else{
            return false;
        }
    }

    /**
     * Create a random filename.
     * @param length length you desire
     * @param fileExtension file extension you desire
     * @return String
     */
    public static String randomFileName(final int length, final String fileExtension){
        final Random rand = new Random();
        return new BigInteger(130,rand).toString(length) + "." + fileExtension;
    }

    /**
     * Get all full file links to a given path.
     * @param filepath filepath to scan!
     * @return List with strings of files
     */
    public static List<String> getFilesLinks(final File filepath){
        final List<String> files = new ArrayList<>();
        for(File f: filepath.listFiles())
            files.add(f.getAbsolutePath());
        return files;
    }

    /**
     * Get all full file links to a given path.
     * @param filePath filepath to scan!
     * @return List with strings of files
     */
    public static List<String> getFileLinks(final String filePath){
        return getFilesLinks(new File(filePath));
    }

    /**
     * Get all files from a folder.
     * @param filepath filepath to scan
     * @return List with files
     */
    public static List<File> getFiles(final File filepath){
        final List<File> files = new ArrayList<>();
        for(File f: filepath.listFiles())
            files.add(f);
        return files;
    }

    /**
     * Get all files from a folder.
     * @param filePath filepath to scan
     * @return List with files
     */
    public static List<File> getFiles(final String filePath){
        return getFiles(new File(filePath));
    }

    /**
     * Get a list of files that contain the given filter.
     * @param filePath filepath to scan
     * @param fileNameFilter filter to scan files for
     * @return List with files
     */
    public static List<File> getFilesFiltered(final File filePath, final String fileNameFilter){
        final List<File> files = new ArrayList<>();
        for(File f: filePath.listFiles())
            if(f.isFile())
                if(f.getName().contains(fileNameFilter))
                    files.add(f);
        return files;
    }

    /**
     * Get a list of files that contain the given filter.
     * @param filePath filepath to scan
     * @param fileNameFilter filter to scan files for
     * @return List with files
     */
    public static List<File> getFilesFiltered(final String filePath, final String fileNameFilter){
        return getFilesFiltered(new File(filePath),fileNameFilter);
    }

    /**
     * Get a list of files that contain the given filter and also have the given extension.
     * @param filePath filepath to scan
     * @param fileNameFilter filter that should be contained
     * @param extension extension that should be used
     * @return List with files
     */
    public static List<File> getFilesFiltered(final File filePath, final String fileNameFilter, final String extension){
        final List<File> files = new ArrayList<>();
        for(File f: filePath.listFiles())
            if(f.isFile())
                if(f.getName().contains(fileNameFilter) && f.isFile())
                    if(f.getName().split("\\.")[1].equals(extension))
                        files.add(f);
        return files;
    }

    /**
     * Get a list of files that contain the given filter and also have the given extension.
     * @param filePath filepath to scan
     * @param fileNameFilter filter that should be contained
     * @param extension extension that should be used
     * @return List with files
     */
    public static List<File> getFilesFiltered(final String filePath, final String fileNameFilter, final String extension){
        return getFilesFiltered(new File(filePath),fileNameFilter,extension);
    }

    /**
     * Get files filtered by file extension.
     * @param filePath filepath to scan
     * @param extension extension to scan for
     * @return List of files
     */
    public static List<File> getFilesFilteredByExtension(final String filePath, final String extension){
        return getFilesFilteredByExtension(new File(filePath),extension);
    }

    /**
     * Get files filtered by file extension.
     * @param filePath filepath to scan
     * @param extension extension to scan for
     * @return List of files
     */
    public static List<File> getFilesFilteredByExtension(final File filePath, final String extension){
        final List<File> files = new ArrayList<>();
        for(File f: filePath.listFiles())
            if(f.isFile())
                if(f.getName().split("\\.")[1].equals(extension))
                    files.add(f);
        return files;
    }

    /**
     * Remove all files inside a given directory.
     * @param directory directory as file
     */
    public static void removeAllFilesInDirectory(final File directory){
        if(null == directory){
            log.log(Level.INFO, "FileManager: given directory is null.");
            return;
        }
        if(directory.exists()){
            for(File f: directory.listFiles()){
                FileManager.removeFile(f);
            }
        }else{
            log.log(Level.WARNING, "Dir is not existing to flush data from. " + directory.getAbsolutePath());
        }

    }

    /**
     * count the existing files in the directory.
     * @param path string path to directory
     * @return number of files or null (value)
     */
    public static int countFilesInDirectory(final String path){
        return countFilesInDirectory(new File(path));
    }

    /**
     * count the existing files in the directory.
     * @param directory directory link to
     * @return number of files or null (value)
     */
    public static int countFilesInDirectory(final File directory){
        if(directory.exists()){
            return directory.listFiles().length;
        }else{
            return 0;
        }
    }

    /**
     * Remove all files inside a given directory.
     * @param directory directory as string
     */
    public static void removeAllFilesInDirectory(final String directory){
        removeAllFilesInDirectory(new File(directory));
    }

    /**
     * Checks if the given path is relative.
     * @param path path that should be checked
     * @return true | false (if not relative)
     */
    public static boolean isPathRelative(final String path){
        if(path.startsWith(".")){
            return true;
        }
        return false;
    }

    /**
     * Transforms a relative path to a path based on top of the working directory.
     * @param path path to convert
     * @return converted path
     */
    public static String transformRelativeToAbsolutPath(final String path){
        final String workingDirectory = System.getProperty("user.dir");
        return workingDirectory + path.substring(1);
    }

    /**
     * Transforms a String path into File path.
     * @param dir directory as String
     * @return directory as File
     */
    public static File transformStringPathToFile(final String dir){
        final File path = new File(dir);
        return path;
    }
}
