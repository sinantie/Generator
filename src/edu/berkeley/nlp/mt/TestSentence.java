package edu.berkeley.nlp.mt;

import java.util.List;

import edu.berkeley.nlp.util.Lists;

/**
 * A sentence and a set of reference sentences that are meant to be used
 * for MT system evaluation.
 * 
 * @author John DeNero
 */
public class TestSentence {
	List<String> foreignSentence;
	List<List<String>> references;

	public TestSentence(List<String> foreignSentence, List<List<String>> references) {
		super();
		this.foreignSentence = foreignSentence;
		this.references = references;
	}
	
	/**
	 * Creates a test sentence with one reference
	 * @param pair
	 */
	public TestSentence(SentencePair pair) {
		this.foreignSentence = pair.getForeignWords();
		this.references = Lists.newList(pair.getEnglishWords());
	}
	

	public List<String> getForeignSentence() {
		return foreignSentence;
	}

	public List<List<String>> getReferences() {
		return references;
	}
}
