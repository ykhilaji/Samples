package lucene;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.StringReader;

import static org.apache.lucene.analysis.en.EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

public class PrintingTermsDuringAnalyzeProcess {
    public static void main(String[] args) throws IOException {
        String TEXT = "The quick brown fox jumps over the lazy dog. XYZ123@mail.com";
        CharArraySet STOP_WORDS = new CharArraySet(3, true);
        STOP_WORDS.add("over");
        STOP_WORDS.add("the");
        STOP_WORDS.add("a");

        printTokens("WhitespaceAnalyzer", new WhitespaceAnalyzer().tokenStream("field", new StringReader(TEXT)));
        printTokens("SimpleAnalyzer", new SimpleAnalyzer().tokenStream("field", new StringReader(TEXT)));
        printTokens("StopAnalyzer + custom stop words", new StopAnalyzer(STOP_WORDS).tokenStream("field", new StringReader(TEXT)));
        printTokens("StopAnalyzer", new StopAnalyzer(ENGLISH_STOP_WORDS_SET).tokenStream("field", new StringReader(TEXT)));
        printTokens("KeywordAnalyzer", new KeywordAnalyzer().tokenStream("field", new StringReader(TEXT)));
        printTokens("StandardAnalyzer", new StandardAnalyzer().tokenStream("field", new StringReader(TEXT)));
    }

    public static void printTokens(String analyzerName, TokenStream stream) throws IOException {
        System.out.println(String.format("Analyzer: %s", analyzerName));
        OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);

        stream.reset();
        while (stream.incrementToken()) {
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            String term = charTermAttribute.toString();

            System.out.println(String.format("Start: %d End: %d Term: %s", startOffset, endOffset, term));
        }

        stream.close();
        System.out.println();
    }
}
