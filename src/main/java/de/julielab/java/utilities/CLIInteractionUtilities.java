package de.julielab.java.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class offers helpful methods for standard command line interface
 * applications. Refer to the method descriptions for more information.
 * 
 * @author faessler
 *
 */
public class CLIInteractionUtilities {

	/**
	 * Reads a line from standard input.
	 * 
	 * @return The read line.
	 * @throws IOException
	 *             If reading the line fails.
	 */
	public static String readLineFromStdIn() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return br.readLine();
	}

	/**
	 * Prints a message to standard output and then reads a line from standard
	 * input.
	 * 
	 * @param message
	 *            The message to print.
	 * @return The read line.
	 * @throws IOException
	 *             If reading fails.
	 */
	public static String readLineFromStdInWithMessage(String message) throws IOException {
		System.out.println(message);
		return readLineFromStdIn();
	}

	/**
	 * Prints a message and also displays a default response that is
	 * automatically if the user does not explicitly specifies a response, i.e.
	 * only hits enter. Then reads a line from standard input.
	 * 
	 * @param message
	 *            The message to print.
	 * @param defaultResponse
	 *            The default response.
	 * @return The read line or the default response if the user response is
	 *         empty.
	 * @throws IOException
	 *             If reading the input fails.
	 */
	public static String readLineFromStdInWithMessage(String message, String defaultResponse) throws IOException {
		System.out.println(message + " (" + defaultResponse + ")");
		String input = readLineFromStdIn();
		if (input.trim().length() == 0)
			return defaultResponse;
		return input;
	}

	/**
	 * Prints a message and appends the string "(y/n)" to indicate a y(es) or
	 * n(o) response where the long forms are also accepted. Then reads lines
	 * from standard input until the user specifies one of the valid responses.
	 * 
	 * @param message
	 *            The message to display.
	 * @return True or false depending on the user response.
	 * @throws IOException
	 *             If reading the input fails.
	 */
	public static boolean readYesNoFromStdInWithMessage(String message) throws IOException {
		String response = "";
		while (!"y".equals(response) && !"yes".equals(response) && !"n".equals(response) && !"no".equals(response)) {
			response = readLineFromStdInWithMessage(message + " (y/n)");
			response = response.toLowerCase();
		}
		return "y".equals(response) || "yes".equals(response);
	}

	/**
	 * Prints a message and reads a yes or no answer where the defaultResponse
	 * it used if the user just hits enter without typing a response. Then reads
	 * the response.
	 * 
	 * @param message
	 *            The message to print.
	 * @param defaultResponse
	 *            The default response.
	 * @return True or false depending on user input.
	 * @throws IOException
	 *             If reading the line fails.
	 */
	public static boolean readYesNoFromStdInWithMessage(String message, boolean defaultResponse) throws IOException {
		String response = "";
		String defaultMarker = defaultResponse ? "y" : "n";
		do {
			response = readLineFromStdInWithMessage(message + " (y/n)[" + defaultMarker + "]");
			response = response.toLowerCase();
		} while (!"y".equals(response) && !"yes".equals(response) && !"n".equals(response) && !"no".equals(response)
				&& response.trim().length() > 0);

		return "y".equals(response) || "yes".equals(response) || !response.trim().isEmpty();
	}
}
