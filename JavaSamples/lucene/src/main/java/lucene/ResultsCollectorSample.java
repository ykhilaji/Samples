package lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Counter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ResultsCollectorSample {
    public static void main(String[] args) throws IOException {
        Indexer indexer = new Indexer(Paths.get("lucene", "index"));
        indexer.indexFiles(Paths.get("lucene", "data"));
        indexer.close();

        Directory indexDirectory = FSDirectory.open(Paths.get("lucene", "index"));
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // default collector
        TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(10, 1000);
        // thread-safe - false
        Counter counter = Counter.newCounter(false);
        TimeLimitingCollector timeLimitingCollector = new TimeLimitingCollector(topScoreDocCollector, counter, 1000);

        try {
            indexSearcher.search(new TermQuery(new Term("body", "fact")), timeLimitingCollector);
            System.out.println(topScoreDocCollector.getTotalHits());
        } catch (TimeLimitingCollector.TimeExceededException e) {
            e.printStackTrace();
        }
    }

    public static class Indexer {
        private static String BODY = "body";
        private static String FILE_NAME = "fileName";

        private Directory indexDirectory;
        private IndexWriter indexWriter;
        private StandardAnalyzer analyzer;

        public Indexer(Path indexDirectoryPath) throws IOException {
            this.indexDirectory = FSDirectory.open(indexDirectoryPath);
            this.analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            this.indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
        }

        public void close() throws IOException {
            indexWriter.close();
        }

        public void indexFiles(Path dir) throws IOException {
            Files.list(dir)
                    .filter(path -> path.getFileName().toString().endsWith(".txt"))
                    .forEach(path -> {
                        try {
                            System.out.println(String.format("Index file: %s", path.getFileName().toString()));
                            indexFile(path);
                        } catch (IOException e) {
                            System.err.println(e.getLocalizedMessage());
                        }
                    });
        }

        //one email message becomes one document, or one
        //PDF file or web page is one document
        public void indexFile(Path path) throws IOException {
            Document document = createDocument(Files.newInputStream(path, StandardOpenOption.READ), path.getFileName().toString());
            indexWriter.addDocument(document);
            // commit after each file
            indexWriter.commit();
        }

        private Document createDocument(InputStream stream, String fileName) {
            Document document = new Document();

            document.add(new TextField(BODY, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            document.add(new StringField(FILE_NAME, fileName, Field.Store.YES));

            return document;
        }
    }
}
