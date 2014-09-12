package cse.unl.edu.Lucene;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.Version;

public class NLPFilter extends FilteringTokenFilter {

	CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private static Pattern unwantedPosRE;

	String regexp;

	public NLPFilter(Version version, TokenStream in) {
		super(version, in);
		// TODO Auto-generated constructor stub
	}

	public NLPFilter(Version version, TokenStream in, String regexp) {
		super(version, in);
		//".*/(VBP|CC|DT|[LR]RB|MD|POS|PRP|UH|WDT|WP|WP\\$|WRB|\\$|\\#|\\.|\\,|:)$"
		unwantedPosRE = Pattern.compile(regexp);
		this.regexp = regexp;
		// TODO Auto-generated constructor stub
	}

	/*
	@Override
	public final boolean incrementToken() throws IOException {
		boolean enablePositionIncrements = true;
		if (enablePositionIncrements) {
			int skippedPositions = 0;
			while (input.incrementToken()) {
				if (accept()) {
					if (skippedPositions != 0) {
						posIncrAtt.setPositionIncrement(posIncrAtt
								.getPositionIncrement() + skippedPositions);
					}
					return true;
				}
				skippedPositions += posIncrAtt.getPositionIncrement();
			}
		} else {
			while (input.incrementToken()) {
				if (accept()) {
					return true;
				}
			}
		}
		// reached EOS -- return false
		return false;
	}

*/
	protected boolean accept() {
		if (unwantedPosRE.matcher(termAtt.toString()).matches()) {
			int index = termAtt.toString().lastIndexOf('/');
			if(index == -1)
				return false;
			termAtt.setLength(index);
			return true;

		} else
			return false;
	}

}
