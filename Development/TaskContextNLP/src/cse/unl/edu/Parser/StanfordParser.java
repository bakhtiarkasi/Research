package cse.unl.edu.Parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.IOException;
import java.io.StringReader;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordParser {



	/**
	 * The main method demonstrates the easiest way to load a parser. Simply
	 * call loadModel and specify the path of a serialized grammar model, which
	 * can be a file, a resource on the classpath, or even a URL. For example,
	 * this demonstrates loading from the models jar file, which you therefore
	 * need to include in the classpath for StanfordParser to work.
	 */
/*
	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		if (args.length > 0) {
			parseFile(args[0]);
			// } else {
			//System.out.println(parseString("This is another sentence. \n hi too you all "));
		}
	}

	public static void parseFile(String filename) {
		LexicalizedParser lps = StanfordParser.getLexicalParser();
		// This option shows loading, sentence-segmenting and tokenizing
		// a file using DocumentPreprocessor.
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		// You could also create a tokenizer here (as below) and pass it
		// to DocumentPreprocessor
		Tree parse = null;
		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
			parse = lps.apply(sentence);
			 		

			System.out.println(parse.taggedYield());

		}

		System.out.println("finally " + parse.taggedYield());
	}

	/*public static ArrayList<TaggedWord> parseString(String sentence) {

		LexicalizedParser lps = StanfordParser.getLexicalParser();
		// This option shows loading and using an explicit tokenizer
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(sentence));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lps.apply(rawWords2);
		
		return parse.taggedYield();

	}*/
/*	
	public static String tagString(String text)
	{
		MaxentTagger tagger;
		try {
			tagger = new MaxentTagger(
			        "/Users/bkasi/Downloads/temp/stanford-postagger-2011-04-20/models/bidirectional-distsim-wsj-0-18.tagger");
			return tagger.tagString(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


	private StanfordParser() {
	} // static methods only
*/
}
