/*
 * Lemmatizing library for Lucene
 * Copyright (C) 2010 Lars Buitinck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cse.unl.edu.Lucene;

import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.util.Version;


public class NLPAnalyzer extends Analyzer {
  
	@Override
	protected TokenStreamComponents createComponents(String field, Reader reader) {
		// TODO Auto-generated method stub
		final Tokenizer stream = new WhitespaceTokenizer(Version.LUCENE_47, reader);
		//    ".*/(CC|DT|[LR]RB|MD|POS|PRP|UH|WDT|WP|WP\\$|WRB|\\$|\\#|\\.|\\,|:)$"
		TokenStream result = new NLPFilter(Version.LUCENE_47, stream,".*/(VB|VBD|VBG|VBN|VBP|VBZ|RB|RBR|RBS|CD|DT|JJ|JJR|JJS|NN|NNS|NNP|NNPS)$");
		result = new LowerCaseFilter(Version.LUCENE_47, result);
		result = new SnowballFilter(result, "English");
		return new TokenStreamComponents(stream, result);
	}
}