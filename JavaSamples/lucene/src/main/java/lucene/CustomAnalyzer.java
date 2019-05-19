package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class CustomAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final StandardTokenizer tokenizer = new StandardTokenizer();
        TokenStream filters = new LowerCaseFilter(tokenizer);

        return new TokenStreamComponents(tokenizer::setReader, filters);
    }
}
