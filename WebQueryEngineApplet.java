import java.io.*;
import java.util.*;
import javax.swing.*;

public class WebQueryEngineApplet extends JApplet {
	protected WebIndex index = null;
	protected WebQueryEngine engine;

	public String query(String query) {
		if (index == null) {
			String indexFile = getCodeBase().getPath() + "index.db";
			if (indexFile.charAt(0) == '/' && indexFile.indexOf('|') > 0)
				indexFile = indexFile.substring(1);
			indexFile = indexFile.replaceAll("%20", " ");
			index = (WebIndex)Index.load(indexFile);
			if (index == null)
				return "<h4>Unable to load index.db</h4>";
			engine = new WebQueryEngine();
			engine.useWebIndex(index);
		}

		Collection c = engine.query(query);
		if (c == null)
			return "<h4>There was an error in the query engine.</h4>";

		StringBuffer result = new StringBuffer();
		Iterator itr = c.iterator();
		while (itr.hasNext()) {
			String url = (String)itr.next();
			result.append("<a href=\"" + url + "\" onClick=\"parent.location='" + url + "'\">" + url + "</a><br>");
		}
		return result.toString();
	}
}
