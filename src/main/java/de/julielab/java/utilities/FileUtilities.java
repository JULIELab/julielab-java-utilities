package de.julielab.java.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static de.julielab.java.utilities.UriUtilities.getInputStreamFromUri;

/**
 * This class is a collection of useful file-related static methods. Refer to
 * the JavaDoc of the methods for more details.
 *
 * @author faessler
 */
public class FileUtilities {
    private final static Logger log = LoggerFactory.getLogger(FileUtilities.class);
    /**
     * Returns an {@link InputStream} for <tt>file</tt>. Automatically wraps in
     * an {@link BufferedInputStream} and also in an {@link GZIPInputStream} if
     * the file name ends with .gz or .gzip.
     *
     * @param file The file to read.
     * @return A buffered input stream.
     * @throws IOException If there is an error during reading.
     */
    public static BufferedInputStream getInputStreamFromFile(File file) throws IOException {
        try {
            InputStream is = new FileInputStream(file);
            String lcfn = file.getName().toLowerCase();
            if (lcfn.contains(".gz") || lcfn.contains(".gzip"))
                is = new GZIPInputStream(is);
            return new BufferedInputStream(is);
        } catch (Exception | Error e) {
            throw e;
        }
    }

    /**
     * Returns an {@link OutputStream} for <tt>file</tt>. Automatically wraps in
     * an {@link BufferedOutputStream} and also in an {@link GZIPOutputStream}
     * if the file name ends with .gz or .gzip.
     *
     * @param file The file to write.
     * @return A buffered output stream.
     * @throws IOException If there is an error during stream creation.
     */
    public static BufferedOutputStream getOutputStreamToFile(File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        String lcfn = file.getName().toLowerCase();
        if (lcfn.contains(".gz") || lcfn.contains(".gzip"))
            os = new GZIPOutputStream(os);
        return new BufferedOutputStream(os);
    }

    /**
     * Returns a reader from the file <tt>file</tt> where the file may be a
     * regular file or gzipped. The gzip format is recognized by the file
     * extensions .gz or .gzip.
     *
     * @param file The file to read.
     * @return A reader for <tt>file</tt>.
     * @throws IOException If opening the file fails.
     */
    public static BufferedReader getReaderFromFile(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStreamFromFile(file), "UTF-8"));
    }

    /**
     * Returns a writer to the file <tt>file</tt> where the destination file may
     * will be gzipped if <tt>file</tt> has the extension .gz or .gzip.
     *
     * @param file The file to write.
     * @return A writer for <tt>file</tt>.
     * @throws IOException If opening the file fails.
     */
    public static BufferedWriter getWriterToFile(File file) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(getOutputStreamToFile(file), "UTF-8"));
    }

    /**
     * Creates a JAR file with the given files as content.
     *
     * @param outputFile The destination where the JAR file should be written.
     * @param files      The files to be included into the JAR file.
     * @throws IOException If writing the JAR file fails.
     */
    public static void createJarFile(File outputFile, File... files) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream target = new JarOutputStream(new FileOutputStream(outputFile), manifest)) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                addFileToJarOutputStream(file, new StringBuilder(), target);
            }
        }
    }

    /**
     * Adds a file entry to a JarOutputStream.
     * Source: https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
     *
     * @param source   The file that should be added into the JAR.
     * @param rootPath
     * @param target   The JAR file to add <code>source</code> to.
     * @throws IOException If adding the file fails.
     */
    public static void addFileToJarOutputStream(File source, StringBuilder rootPath, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            String name = rootPath.length() > 0 ? rootPath + source.getName() : source.getName();
            if (source.isDirectory()) {
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                rootPath.append(name);
                for (File nestedFile : source.listFiles())
                    addFileToJarOutputStream(nestedFile, rootPath, target);
                return;
            }

            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null)
                in.close();
        }
    }

    /**
     * Tries to find a resource by the given name. The name may be a path to a regular file, an URI or a classpath
     * resource.
     *
     * @param name The resource name to find.
     * @return The input stream from the found resource or <tt>null</tt> if the resource could not be found.
     * @throws IOException If reading the resource fails.
     */
    public static InputStream findResource(String name) throws IOException {
        InputStream is = null;
        if (is == null) {
            log.debug("Trying to find resource '{}' as a file", name);
            File file = new File(name);
            if (file.exists()) {
                log.debug("Found file '{}'", file);
                is = getInputStreamFromFile(file);
            }
        }
        if (is == null) {
            log.debug("No file at path '{}' was found. Trying to parse as an URI.", name);
            try {
                URI uri = new URI(name);
                // If the URI is not absolute, the conversion to an URL for input stream opening will fail
                if (uri.isAbsolute()) {
                    is = getInputStreamFromUri(uri);
                    if (log.isDebugEnabled() && is != null)
                        log.debug("Found resources at URI '{}'", uri.toString());
                }
            } catch (URISyntaxException e) {
                // nothing, obviously was not a valid URI
            }
        }
        if (is == null) {
            log.debug("Did not find a resource at file or URI '{}', trying as resource on the classpath.", name);
            is = FileUtilities.class.getResourceAsStream(name.startsWith("/") ? name : "/" + name);
            if (log.isDebugEnabled() && is != null)
                log.debug("Found classpath resource at '{}'", name);
            if (is != null && (name.toLowerCase().contains(".gz") || name.toLowerCase().contains(".gzip"))) {
                log.debug("Classpath resource '{}' ending indicates a GZIP resource, ungzipping is added", name);
                is = new GZIPInputStream(is);
            }
        }
        if (log.isDebugEnabled() && is == null)
            log.debug("The resource '{}' could not found as a file, URI or on the classpath. Returning null.", name);
        return is;
    }
}
