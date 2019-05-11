package lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BasicUsageSample {
    public static void main(String[] args) throws IOException, ParseException {
        Indexer indexer = new Indexer(Paths.get("lucene", "index"));
        indexer.indexFiles(Paths.get("lucene", "data"));
        indexer.close();

        Searcher searcher = new Searcher(Paths.get("lucene", "index"));

        searcher.search("fact", 10).forEach(doc -> System.out.println(String.format("Search: %s", doc.get("fileName"))));
        searcher.searchUsingTermQuery("fact", 10).forEach(doc -> System.out.println(String.format("Term query: %s", doc.get("fileName"))));
        searcher.searchUsingWildcardQuery("ch?nk", 10).forEach(doc -> System.out.println(String.format("Wildcard query: %s", doc.get("fileName"))));
        searcher.searchUsingBooleanQuery("fact", "popular", 1, 10).forEach(doc -> System.out.println(String.format("Boolean query: %s", doc.get("fileName"))));
        searcher.searchUsingPhraseQuery("If you are going to", 10).forEach(doc -> System.out.println(String.format("Phrase query: %s", doc.get("fileName"))));
        searcher.searchUsingPrefixQuery("pas", 10).forEach(doc -> System.out.println(String.format("Prefix query: %s", doc.get("fileName"))));

        searcher.close();
    }

    public static class Searcher {
        private static String BODY = "body";

        private Directory indexDirectory;
        private IndexReader indexReader;
        private IndexSearcher indexSearcher;
        private QueryParser queryParser;
        private StandardAnalyzer analyzer;

        public Searcher(Path indexDirectoryPath) throws IOException {
            this.indexDirectory = FSDirectory.open(indexDirectoryPath);
            this.indexReader = DirectoryReader.open(this.indexDirectory);
            this.indexSearcher = new IndexSearcher(this.indexReader);
            this.analyzer = new StandardAnalyzer();
            this.queryParser = new QueryParser(BODY, this.analyzer);


        }

        public void close() throws IOException {
            this.indexReader.close();
        }

        public List<Document> search(String queryString, int documents) throws ParseException, IOException {
            Query query = this.queryParser.parse(queryString);
            TopDocs topDocs = this.indexSearcher.search(query, documents);

            return fromTopDocs(topDocs);
        }

        public List<Document> searchUsingTermQuery(String word, int documents) throws IOException {
            Query query = new TermQuery(new Term(BODY, word));
            TopDocs topDocs =  this.indexSearcher.search(query, documents);

            return fromTopDocs(topDocs);
        }

        public List<Document> searchUsingWildcardQuery(String word, int documents) throws IOException {
            Query query = new WildcardQuery(new Term(BODY, word));
            TopDocs topDocs =  this.indexSearcher.search(query, documents);

            return fromTopDocs(topDocs);
        }

        public List<Document> searchUsingBooleanQuery(String first, String second, int minClauses, int documents) throws IOException {
            Query query = new BooleanQuery.Builder()
                    .setMinimumNumberShouldMatch(minClauses)
                    .add(new TermQuery(new Term(BODY, first)), BooleanClause.Occur.SHOULD)
                    .add(new TermQuery(new Term(BODY, second)), BooleanClause.Occur.SHOULD)
                    .build();
            TopDocs topDocs =  this.indexSearcher.search(query, documents);

            return fromTopDocs(topDocs);
        }

        public List<Document> searchUsingPhraseQuery(String phrase, int documents) throws IOException {
            final PhraseQuery.Builder builder = new PhraseQuery.Builder();

            Arrays.stream(phrase.split("\\s+")).forEach(token -> builder.add(new Term(BODY, token)));

            Query query = builder.build();
            TopDocs topDocs =  this.indexSearcher.search(query, documents);

            return fromTopDocs(topDocs);
        }


        public List<Document> searchUsingPrefixQuery(String prefix, int documents) throws IOException {
            Query query = new PrefixQuery(new Term(BODY, prefix));
            TopDocs topDocs =  this.indexSearcher.search(query, documents);

            return fromTopDocs(topDocs);
        }

        private List<Document> fromTopDocs(TopDocs topDocs) {
            return Arrays.stream(topDocs.scoreDocs)
                    .map(scoreDoc -> {
                        try {
                            return Optional.of(this.indexSearcher.doc(scoreDoc.doc));
                        } catch (IOException e) {
                            System.err.println(e.getLocalizedMessage());
                            return Optional.<Document>empty();
                        }
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
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
