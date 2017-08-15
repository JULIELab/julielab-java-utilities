package de.julielab.java.utilities;

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
import java.io.Reader;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class is a collection of useful file-related static methods. Refer to
 * the JavaDoc of the methods for more details.
 * 
 * @author faessler
 *
 */
public class FileUtilities {
	/**
	 * Returns an {@link InputStream} for <tt>file</tt>. Automatically wraps in
	 * an {@link BufferedInputStream} and also in an {@link GZIPInputStream} if
	 * the file name ends with .gz or .gzip.
	 * 
	 * @param file
	 *            The file to read.
	 * @return A buffered input stream.
	 * @throws IOException
	 *             If there is an error during reading.
	 */
	public static InputStream getInputStreamFromFile(File file) throws IOException {
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
	 * @param file
	 *            The file to write.
	 * @return A buffered output stream.
	 * @throws IOException
	 *             If there is an error during stream creation.
	 */
	public static OutputStream getOutputStreamToFile(File file) throws IOException {
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
	 * @param file
	 *            The file to read.
	 * @return A reader for <tt>file</tt>.
	 * @throws IOException
	 *             If opening the file fails.
	 */
	public static Reader getReaderFromFile(File file) throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStreamFromFile(file), "UTF-8"));
	}

	/**
	 * Returns a writer to the file <tt>file</tt> where the destination file may
	 * will be gzipped if <tt>file</tt> has the extension .gz or .gzip.
	 * 
	 * @param file
	 *            The file to write.
	 * @return A writer for <tt>file</tt>.
	 * @throws IOException
	 *             If opening the file fails.
	 */
	public static Writer getWriterToFile(File file) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(getOutputStreamToFile(file), "UTF-8"));
	}
}
