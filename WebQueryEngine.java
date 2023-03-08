import java.util.*;

public class WebQueryEngine {
	private WebIndex index;

	/**
	 * Selects the WebIndex from which answers to queries will be constructed.
	 *
	 * @param index	the WebIndex this WebQueryEngine should use
	 */
	public void useWebIndex(WebIndex index) {
		this.index = index;
	}

	static final Token AND_TOKEN = new Token(true, "&");
	static final Token OR_TOKEN = new Token(true, "|");
	static final Token LEFT_PAREN_TOKEN = new Token(true, "(");
	static final Token RIGHT_PAREN_TOKEN = new Token(true, ")");
	static final Token EXCLAIM_TOKEN = new Token(true, "!");
	static final Token QUOTE_TOKEN = new Token(true, "\"");

	/**
	 * Returns a Collection of URLs (as Strings) of web pages satisfying
	 * the query expresion.
	 *
	 * @param query	a query expression
	 * @return	a Collection of URLs of web pages satisfying the query
	 */
	public Collection query(String qury) {
		return query(parseQuery(qury, 0));
	}

	/**
	 * Takes a tree, and does query(String)'s work.
	 */
	public Collection query(Tree qury)
	{
		if (qury.data.otherTokens == null) {
			if (!qury.data.token.isOperator)
			{
				return qury.negated ? index.getNegatedFiles(qury.data.token.repr) : index.getFiles(qury.data.token.repr);
			}
			else
			{
				if (qury.data.token == AND_TOKEN)
				{
					Collection left = query(qury.left);
					Collection right = query(qury.right);
					Collection intersection = new HashSet();
					for (Object o : left)
						if (right.contains(o))
							intersection.add(o);
					return intersection;
				} else if (qury.data.token == OR_TOKEN) {
					Collection left = query(qury.left);
					Collection right = query(qury.right);
					Collection union = new HashSet();
					union.addAll(left);
					union.addAll(right);
					return union;
				}
			}
		} else {
			Collection intersection = index.getFiles(qury.data.token.repr);
			for (Token t : qury.data.otherTokens)
			{
				intersection.removeAll(index.getNegatedFiles(t.repr));
			}
			if (!qury.isPhrase)
				return qury.negated ? index.complement(intersection) : intersection;
			else
			{
				// Phrase search; need to check that the exact phrase exists in the file.
				LinkedList<String> phrase = new LinkedList<String>();
				phrase.add(qury.data.token.repr);
				for (Token t : qury.data.otherTokens)
					phrase.add(t.repr);
				Collection toRemove = new HashSet();
				for (Object file : intersection)
					if (!index.hasPhrase((String) file, phrase))
						toRemove.add(file);
				intersection.removeAll(toRemove);
				return intersection;
			}
		}
		return new HashSet();
	}

	/**
	 * A word or operator that might show up in the query.
	 */
	public static class Token
	{
		/**
		 * Whether or not this token is an operator.
		 */
		public boolean isOperator;

		/**
		 * The string representation of this token.
		 */
		public String repr;

		/**
		 * Convenient constructor method.
		 */
		public Token(boolean isOp, String rep)
		{
			isOperator = isOp;
			repr = rep;
		}
	}

	/**
	 * A specific instance of a word or operator that did
	 * show up in the query.
	 */
	public static class QueryToken
	{
		/**
		 * The token.
		 */
		Token token;

		/**
		 * Possibly more tokens.
		 */
		LinkedList<Token> otherTokens;

		/**
		 * The starting index of this token in the query, inclusive.
		 */
		int startIndex;

		/**
		 * The ending index of this token in the query, exclusive.
		 */
		int endIndex;

		/**
		 * Constructor methods.
		 */
		public QueryToken(Token tk, int start, int end)
		{
			token = tk;
			startIndex = start;
			endIndex = end;
		}

		public QueryToken(String word, int start, int end)
		{
			token = new Token(false, word);
			startIndex = start;
			endIndex = end;
		}

		private String strForm;
		/**
		 * Convenient method for getting the
		 * string form of this token.
		 */
		public String getToken()
		{
			if (strForm != null)
			{ /* Do nothing */ }
			else if (otherTokens == null)
				strForm = token.repr;
			else
			{
				StringBuffer str = new StringBuffer();
				str.append(token.repr);
				for (Token t : otherTokens)
				{
					str.append(' ');
					str.append(t.repr);
				}
				strForm = str.toString();
			}
			return strForm; 
		}
	}

	public static class Tree
	{
		/**
		 * Standard elements of a node in a tree.
		 */
		public Tree left;
		public QueryToken data;
		public Tree right;

		/**
		 * The portion of the query covered by the tree.
		 */
		int startIndex;
		int endIndex;
		
		/**
		 * Negate the meaning of the tree.
		 */
		public boolean negated;

		/**
		 * When QueryToken holds more than one word, this
		 * determines whether they mean all words should be
		 * found or the exact phrase should be found.
		 */
		public boolean isPhrase;

		/**
		 * Convenient constructor method.
		 */
		public Tree(Tree lft, QueryToken dat, Tree rght)
		{
			left = lft;
			data = dat;
			right = rght;
			if (left != null)
				startIndex = left.startIndex;
			else
				startIndex = data.startIndex;
			if (right != null)
				endIndex = right.endIndex;
			else
				endIndex = data.endIndex;
			negated = false;
			isPhrase = false;
		}

		/**
		 * Convenient method for getting the string form of the
		 * token at this node.
		 */
		public String getToken()
		{ return data.getToken(); }

		/**
		 * Recursive method for printing a nice version of the query.
		 */
		public String getQuery()
		{
			if (data.token.isOperator)
				return "(" + left.getQuery() + " " + getToken() + " " + right.getQuery() + ")";
			return getToken();
		}
	}

	/**
	 * Returns true if the character is part of a word, false otherwise.
	 */
	public boolean isWordCharacter(char c)
	{
		return Character.isLetter(c);
	}

	/**
	 * Gets the next token, starting at index.
	 */
	public QueryToken getToken(String query, int index)
	{
		if (index >= query.length())
			return null;
		char c = query.charAt(index);
		while (Character.isWhitespace(c))
		{
			index++;
			if (index < query.length())
				c = query.charAt(index);
			else
				return null;
		}
		if (c == '&')
			return new QueryToken(AND_TOKEN, index, index+1);
		else if (c == '|')
			return new QueryToken(OR_TOKEN, index, index+1);
		else if (c == '(')
			return new QueryToken(LEFT_PAREN_TOKEN, index, index+1);
		else if (c == ')')
			return new QueryToken(RIGHT_PAREN_TOKEN, index, index+1);
		else if (c == '!')
			return new QueryToken(EXCLAIM_TOKEN, index, index+1);
		else if (c == '"')
			return new QueryToken(QUOTE_TOKEN, index, index+1);
		else
		{
			while (!isWordCharacter(c))
				index++;
			int startIndex = index;
			while (isWordCharacter(c))
			{
				index++;
				if (index < query.length())
					c = query.charAt(index);
				else
					c = ')'; // not a word character
			}
			int endIndex = index;
			String word = (query.substring(startIndex, endIndex)).toLowerCase();
			return new QueryToken(word, startIndex, endIndex);
		}
	}

	/**
	 * Parses the query into a tree, starting at index.
	 */
	public Tree parseQuery(String query, int index)
	{
		// TODO: Fix exception and consider null pointer cases.
		QueryToken t = getToken(query, index);
		if (t == null)
			return null;
		index = t.endIndex;
		if (t.token.isOperator && "(".equals(t.token.repr))
		{
			Tree left = parseQuery(query, index);
			index = left.endIndex;
			QueryToken tok = getToken(query, index);
			index = tok.endIndex;
			Tree right = parseQuery(query, index);
			QueryToken rightParen = getToken(query, right.endIndex);
			if (!")".equals(rightParen.getToken()))
				throw new IllegalArgumentException("Where'd the right parenthesies go???");
			index = rightParen.endIndex;
			Tree tr = new Tree(left, tok, right);
			tr.startIndex = t.startIndex;
			tr.endIndex = index;
			return tr;
		}
		else if (t.token.isOperator && "!".equals(t.token.repr))
		{
			Tree toNegate = parseQuery(query, index);
			toNegate.startIndex = t.startIndex;
			toNegate.negated = !toNegate.negated;
			return toNegate;
		}
		else if (t.token.isOperator && "\"".equals(t.token.repr))
		{
			Tree phrase = parseQuery(query, index);
			if (phrase.data.token.isOperator)
				throw new IllegalArgumentException("What do you think you're doing, putting an operator in the middle of a phrase?");
			index = phrase.endIndex;
			QueryToken rightQuote = getToken(query, index);
			if (!"\"".equals(rightQuote.getToken()))
				throw new IllegalArgumentException("Where'd the right quote go???");
			index = rightQuote.endIndex;
			phrase.startIndex = t.startIndex;
			phrase.endIndex = index;
			phrase.isPhrase = true;
			return phrase;
		}
		else if (!t.token.isOperator)
		{
			// Return possibly more than one word in the QueryToken.
			Tree tr = new Tree(null, t, null);
			t = getToken(query, index);
			LinkedList<Token> moreTokens = new LinkedList<Token>();
			while (t != null && !t.token.isOperator)
			{
				moreTokens.add(t.token);
				index = t.endIndex;
				t = getToken(query, index);
			}
			if (!moreTokens.isEmpty())
			{
				tr.data.otherTokens = moreTokens;
				tr.data.endIndex = index;
				tr.endIndex = index;
			}
			return tr;
		}
		else
		{
			System.err.println("Floating operator!");
			System.exit(1);
			return null; // Prevents the compiler from complaining.
		}
	}
}
