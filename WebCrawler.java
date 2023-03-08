import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class WebCrawler extends Crawler {
	public static void main(String... args) throws Exception {
		if (args.length == 0) {
			System.out.println("No URLs specified.");
			System.exit(0);
		}

		List remaining = new LinkedList();
		for (int i = 0; i < args.length; i++)
			remaining.add(args[i]);
		WebCrawler crawler = new WebCrawler();
		crawler.crawl(remaining);
		crawler.getWebIndex().save("index.db");
	}

	/**
	 * Crawls the pages starting with remaining.
	 */
	public void crawl(List remaining)
	{
		while (!remaining.isEmpty()) {
			try {
				remaining.addAll(parse(remaining.remove(0).toString()));
			} catch (MalformedURLException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	private HashSet<String> urlsReached;
	private WebIndex myIndex;
	private String currentURL;
	private URL currentContext;
	private LinkedList urlsToParse;
	public WebCrawler()
	{
		urlsReached = new HashSet<String>();
		myIndex = new WebIndex();
	}

	/**
	 * Called to indicate an HTML document should be parsed.  Parsing is performed
	 * by calling super.parse(url), which drives the callback methods of this class.
	 *
	 * @param url	URL of the HTML document to parse
	 * @return	a List of URLs (in String form) found in the document
	 */
	public List parse(String url) throws IOException, MalformedURLException {
		if (!url.startsWith("file"))
			return new LinkedList(); // throw new RuntimeException("NO INTERNET CRAWLING!");
		if (urlsReached.contains(url))
			return new LinkedList();
		urlsReached.add(url);
		currentURL = url;
		currentContext = new URL(url);
		urlsToParse = new LinkedList();
		try { super.parse(url); }
		catch (IOException e) { /* File does not exist; do nothing */ }
		return urlsToParse;
	}

	/**
	 * Returns the WebIndex object built by this Crawler.
	 *
	 * @return	a WebIndex object covering all pages crawled by this Crawler.
	 */
	public WebIndex getWebIndex() {
		return myIndex;
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
		if (t.equals(HTML.Tag.A))
		{
			Object attr = a.getAttribute(HTML.Attribute.HREF);
			if (attr != null)
				try {
					if (attr instanceof String)
						urlsToParse.add(new URL(currentContext, (String) attr));
					else if (attr instanceof URL)
						urlsToParse.add(new URL(currentContext, ((URL) attr).toString()));
				} catch(MalformedURLException e) {
					// do nothing
				}
		}
	}

	/**
	 * Called when the closing/ending instance of a two-part tag is
	 * encountered, such as <code>&lt;/a&gt;</code> or
	 * <code>&lt;/b&gt;</code>.
	 *
	 * @param t	the tag encountered
	 */
	public void handleEndTag(HTML.Tag t, int pos) {
	}

	/**
	 * Called when a one-part tag is encountered, such as
	 * <code>&lt;hr&gt;</code>.
	 *
	 * @param t	the tag encountered
	 * @param a	the set of attributes specified with the tag
	 */
	public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	}

	/**
	 * Called when content text is encountered.
	 *
	 * @param data	the text encountered
	 */
	public void handleText(char[] data, int pos) {
		myIndex.addPhraseToFile(currentURL, data);
	}

	/**
	 * Called when comment text is encountered.
	 *
	 * @param data	the comment text encountered
	 */
	public void handleComment(char[] data, int pos) {
	}

	/**
	 * Called at the end of parsing to indicate the estimated newline
	 * character sequence.
	 *
	 * @param eol	the estimated newline character sequence
	 */
	public void handleEndOfLineString(String eol) {
	}

	/**
	 * Called if an error occurs during parsing.
	 *
	 * @param errorMsg	a description of the error
	 */
	public void handleError(String errorMsg, int pos) {
	}

	/**
	 * Called at the end of parsing.
	 */
	public void flush() {
		super.flush();
	}
}
