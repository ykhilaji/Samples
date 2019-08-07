package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryIndexTest {
    @Test
    public void inMemoryUrlsTest() throws ParseException {
        MemoryIndex idx = new MemoryIndex();
        Analyzer analyzer = new StandardAnalyzer();

        List<String> strings = new ArrayList<>();
        strings.add("abc.com");
        strings.add("cba.abc.com");
        strings.add("site1.net");
        strings.add("site1.site2.site3.net");
        strings.add("12345site.com");
        strings.add("site12345.net");
        strings.add("x.z.y.site1.com");

        idx.addField("url", idx.keywordTokenStream(strings));
        // make this index fully thread-safe
        idx.freeze();

        QueryParser parser = new QueryParser("url", analyzer);

        assertTrue(idx.search(parser.parse("abc.com")) > 0);
        assertTrue(idx.search(parser.parse("xyz.abc.com")) == 0);
    }
}
