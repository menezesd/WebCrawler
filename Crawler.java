import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

public abstract class Crawler extends HTMLEditorKit.ParserCallback {
	protected HTMLEditorKit.Parser parser = new ParserDelegator();

	/**
	 * Returns the WebIndex object built by this Crawler.
	 *
	 * @return	a WebIndex object covering all pages crawled by this Crawler.
	 */
	abstract public WebIndex getWebIndex();

	/**
	 * Begins parsing of an HTML document.  Parsing drives the
	 * callback methods defined in this class and subclasses.
	 *
	 * @param url	URL of the HTML document to parse
	 * @return	an empty List
	 */
	public List parse(String url) throws MalformedURLException, IOException {
		System.out.println("STARTED PARSING " + url + " ...");
		Reader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(Urls.create(url, Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS).openStream()));
			parser.parse(in, this, true);
			flush();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
		return new LinkedList();
	}

	/**
	 * Called when the opening/starting instance of a two-part tag is
	 * encountered, such as <code>&lt;a&gt;</code> or
	 * <code>&lt;b&gt;</code>.
	 *
	 * @param t	the tag encountered
	 * @param a	the set of attributes specified with the tag
	 */
	public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
		System.out.print("StartTag  <" + t);
		if (a.getAttributeNames().hasMoreElements())
			System.out.print(" " + a.toString().trim());
		System.out.println(">");
	}

	/**
	 * Called when the closing/ending instance of a two-part tag is
	 * encountered, such as <code>&lt;/a&gt;</code> or
	 * <code>&lt;/b&gt;</code>.
	 *
	 * @param t	the tag encountered
	 */
	public void handleEndTag(HTML.Tag t, int pos) {
		System.out.println("EndTag    </" + t + ">");
	}

	/**
	 * Called when a one-part tag is encountered, such as
	 * <code>&lt;hr&gt;</code>.
	 *
	 * @param t	the tag encountered
	 * @param a	the set of attributes specified with the tag
	 */
	public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
		System.out.print("SimpleTag <" + t);
		if (a.getAttributeNames().hasMoreElements())
			System.out.print(" " + a.toString().trim());
		System.out.println(">");
	}

	/**
	 * Called when content text is encountered.
	 *
	 * @param data	the text encountered
	 */
	public void handleText(char[] data, int pos) {
		System.out.print("Text      '");
		for (int i = 0; i < data.length; i++)
			System.out.print(data[i]);
		System.out.print("'\n");
	}

	/**
	 * Called when comment text is encountered.
	 *
	 * @param data	the comment text encountered
	 */
	public void handleComment(char[] data, int pos) {
		System.out.print("Comment   <!--");
		for (int i = 0; i < data.length; i++)
			System.out.print(data[i]);
		System.out.print("-->\n");
	}

	/**
	 * Called at the end of parsing to indicate the estimated newline
	 * character sequence.
	 *
	 * @param eol	the estimated newline character sequence
	 */
	public void handleEndOfLineString(String eol) {
		// The MCP says...
		//System.out.println("EndOfLine: '" + eol + "'");
	}

	/**
	 * Called if an error occurs during parsing.
	 *
	 * @param errorMsg	a description of the error
	 */
	public void handleError(String errorMsg, int pos) {
		System.out.println("!ERROR!   " + errorMsg);
	}

	/**
	 * Called at the end of parsing.
	 */
	public void flush() {
		System.out.println("...FINISHED PARSING");
	}
}
