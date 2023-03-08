import java.util.*;

public class WebIndex extends Index {
	/**
	 * Mapping from words to (mapping from files to LinkedList of the indecies in which that word shows up).
	 */
	private HashMap<String, HashMap<String, LinkedList<Integer>>> wordsToFiles;
	/**
	 * Mapping from files to the largest index of the words in the file.
	 */
	private HashMap<String, Integer> allFiles;
	public WebIndex()
	{
		wordsToFiles = new HashMap<String, HashMap<String, LinkedList<Integer>>>();
		allFiles = new HashMap<String, Integer>();
	}

	/**
	 * Associates the word with the file at the current index of the file.
	 * @param word The word to associate
	 * @param file The file to associate with
	 */
	private void addAssociation(String word, String file)
	{
		allFiles.put(file, allFiles.get(file)+1);
		if (wordsToFiles.containsKey(word))
		{
			HashMap<String, LinkedList<Integer>> filesToIndecies = wordsToFiles.get(word);
			if (filesToIndecies.containsKey(file))
				filesToIndecies.get(file).add(allFiles.get(file));
			else
			{
				LinkedList<Integer> indecies = new LinkedList<Integer>();
				indecies.add(allFiles.get(file));
				filesToIndecies.put(file, indecies);
			}
		} else {
			HashMap<String, LinkedList<Integer>> filesToIndecies = new HashMap<String, LinkedList<Integer>>();
			LinkedList<Integer> indecies = new LinkedList<Integer>();
			indecies.add(allFiles.get(file));
			filesToIndecies.put(file, indecies);
			wordsToFiles.put(word, filesToIndecies);
		}
	}

	/**
	 * Adds the phrase to the file, by associating step by step each word in the phrase.
	 * @param file the file containing the phrase.
	 * @param phrase the char[] containing the phrase.
	 */
	public void addPhraseToFile(String file, char[] phrase)
	{
		if (!allFiles.containsKey(file))
			allFiles.put(file, 0);
		int index = 0;
		while (index + 1 < phrase.length)
		{
			char c = phrase[index];
			while (!Character.isLetter(c) && index + 1 < phrase.length)
				c = phrase[++index];
			int initialIndex = index;
			while (Character.isLetter(c) && index + 1 < phrase.length)
				c = phrase[++index];
			if (index > initialIndex)
			{
				String word = new String(phrase, initialIndex, index-initialIndex).toLowerCase();
				addAssociation(word, file);
			}
		}
	}

	/**
	 * Gets the files that contain word.
	 * @param word The word to search for.
	 * @return The collection of filenames containing the word.
	 */
	public Collection getFiles(String word)
	{
		if (wordsToFiles.containsKey(word))
		{
			HashSet<String> files = new HashSet<String>();
			HashMap<String, LinkedList<Integer>> filesToIndecies = wordsToFiles.get(word);
			for (String file : filesToIndecies.keySet())
				files.add(file);
			return files;
		}
		return new HashSet(0);
	}

	/**
	 * Takes the complement of the set of files.
	 * @param original The collection of files to take the complement of.
	 * @return The complement
	 */
	public Collection complement(Collection original)
	{
		HashSet<String> cmplmnt = new HashSet<String>();
		for (String file : allFiles.keySet())
		{
			if (!original.contains(file))
				cmplmnt.add(file);
		}
		return cmplmnt;
	}

	/**
	 * All files not containing word.
	 * @param word
	 */
	public Collection getNegatedFiles(String word)
	{
		return complement(getFiles(word));
	}

	/**
	 * Determines if the file has the phrase.
	 * @param file
	 * @param phrase the LinkedList of words
	 * @return true if the file does contain the phrase, false otherwise
	 */
	public boolean hasPhrase(String file, LinkedList<String> phrase)
	{
		if (phrase.isEmpty()) return true;
		String word = phrase.removeFirst();
		LinkedList<Integer> indecies = wordsToFiles.get(word).get(file);
		for (int i : indecies)
			if (hasPhraseAtIndex(file, phrase, i+1))
				return true;
		return false;
	}
	
	/**
	 * Helper for hasPhrase(). Determines recursively if the file has the phrase at a given location.
	 * @param file
	 * @param phrase LinkedList of words
	 * @param index The index to check
	 * @return true if the file does contain the phrase at that location, false otherwise.
	 */
	private boolean hasPhraseAtIndex(String file, LinkedList<String> phrase, int index)
	{
		if (phrase.isEmpty()) return true;
		String word = phrase.removeFirst();
		boolean ret = wordsToFiles.get(word).get(file).contains(index) && hasPhraseAtIndex(file, phrase, index+1);
		phrase.addFirst(word);
		return ret;
	}
}
