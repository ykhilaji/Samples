package lucene;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NearRealTimeIndexing {
    public static void main(String[] args) throws Exception {
        IndexerSearcher indexer = new IndexerSearcher(Paths.get("lucene", "nearRealTimeIndex"));
        DirectoryMonitor monitor = new DirectoryMonitor();
        monitor.start();

        Observable<FileEvent> observable = monitor.addDirectoryForIndexing(Paths.get("lucene", "nearRealTimeData"));
        observable.subscribe(onNext -> {
            switch (onNext.eventType) {
                case CREATE:
                    indexer.indexFile(onNext.path);
                    break;
                case DELETE:
                    indexer.removeFileFromIndex(onNext.path);
                    break;
                case UPDATE:
                    indexer.updateFile(onNext.path);
                    break;
            }
        }, onError -> System.err.println(onError.getLocalizedMessage()));

        CLI cli = new CLI(indexer);

        Thread cliThread = new Thread(cli);

        cliThread.start();
        cliThread.join();

        monitor.stop();
        indexer.close();
    }
}

enum EventType {
    CREATE,
    DELETE,
    UPDATE
}

class CLI implements Runnable {
    private IndexerSearcher searcher;
    private BufferedReader reader;

    public CLI(IndexerSearcher searcher) {
        this.searcher = searcher;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Input term for search:");
            try {
                String term = reader.readLine();

                if (term == null) {
                    break;
                }

                System.out.println("Result: ");
                searcher.searchUsingWildcardQuery(term, 10).forEach(doc -> System.out.println(doc.get("fileName")));
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }
    }
}

class FileEvent {
    final EventType eventType;
    final Path path;

    public FileEvent(EventType eventType, Path path) {
        this.eventType = eventType;
        this.path = path;
    }
}

class DirectoryMonitor {
    private FileAlterationMonitor monitor;
    private FileFilter fileFilter;

    public DirectoryMonitor() {
        this(1000);
    }

    public DirectoryMonitor(long interval) {
        this(interval, new TextFileFilter());
    }

    public DirectoryMonitor(long interval, FileFilter fileFilter) {
        if (interval < 0) {
            throw new RuntimeException("Interval should be > 0");
        }

        monitor = new FileAlterationMonitor(interval);
        this.fileFilter = fileFilter;
    }

    public void start() throws Exception {
        monitor.start();
    }

    public void stop() throws Exception {
        monitor.stop();
    }

    public Observable<FileEvent> addDirectoryForIndexing(Path path) throws Exception {
        FileAlterationObserver observer = observeDirectory(path);
        return Observable.create(subscriber -> addListener(observer, subscriber));
    }

    private void addListener(FileAlterationObserver observer, ObservableEmitter<FileEvent> emitter) {
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onDirectoryDelete(File file) {
                emitter.onComplete();
            }

            @Override
            public void onFileCreate(File file) {
                emitter.onNext(new FileEvent(EventType.CREATE, Paths.get(file.toURI())));
            }

            @Override
            public void onFileChange(File file) {
                emitter.onNext(new FileEvent(EventType.UPDATE, Paths.get(file.toURI())));
            }

            @Override
            public void onFileDelete(File file) {
                emitter.onNext(new FileEvent(EventType.DELETE, Paths.get(file.toURI())));
            }
        });
    }

    private FileAlterationObserver observeDirectory(Path path) throws Exception {
        if (Files.isDirectory(path)) {
            FileAlterationObserver observer = new FileAlterationObserver(path.toFile(), fileFilter);
            monitor.addObserver(observer);

            return observer;
        } else {
            throw new Exception(String.format("Path: %s is not a directory", path.toAbsolutePath().toString()));
        }
    }
}

class TextFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.getName().endsWith(".txt");
    }
}

class IndexerSearcher {
    private static String BODY = "body";
    private static String FILE_NAME = "fileName";

    private Directory indexDirectory;
    private IndexWriter indexWriter;
    private StandardAnalyzer analyzer;
    private DirectoryReader indexReader;

    public IndexerSearcher(Path indexDirectoryPath) throws IOException {
        this.indexDirectory = FSDirectory.open(indexDirectoryPath);
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        this.indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
        this.indexReader = DirectoryReader.open(this.indexWriter);
    }

    public void close() throws IOException {
        indexWriter.close();
    }

    //one email message becomes one document, or one
    //PDF file or web page is one document
    public void indexFile(Path path) throws IOException {
        System.out.println(String.format("Index file: %s", path.toString()));
        Document document = createDocument(Files.newInputStream(path, StandardOpenOption.READ), path.getFileName().toString());
        indexWriter.addDocument(document);
        // commit after each file
        indexWriter.commit();
    }

    public void removeFileFromIndex(Path path) throws IOException {
        System.out.println(String.format("Remove file from index: %s", path.toString()));
        indexWriter.deleteDocuments((new Term(FILE_NAME, path.getFileName().toString())));
        // commit after each file
        indexWriter.commit();
    }

    public void updateFile(Path path) throws IOException {
        System.out.println(String.format("Update file: %s", path.toString()));
        Document document = createDocument(Files.newInputStream(path, StandardOpenOption.READ), path.getFileName().toString());
        indexWriter.updateDocument(new Term(FILE_NAME, path.getFileName().toString()), document);
        // commit after each file
        indexWriter.commit();
    }

    private Document createDocument(InputStream stream, String fileName) {
        Document document = new Document();

        document.add(new TextField(BODY, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
        document.add(new StringField(FILE_NAME, fileName, Field.Store.YES));

        return document;
    }

    public List<Document> searchUsingWildcardQuery(String word, int documents) throws IOException {
        indexReader = DirectoryReader.openIfChanged(indexReader);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new WildcardQuery(new Term(BODY, word));
        TopDocs topDocs = indexSearcher.search(query, documents);

        return fromTopDocs(topDocs, indexSearcher);

    }

    private List<Document> fromTopDocs(TopDocs topDocs, final IndexSearcher indexSearcher) {
        return Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> {
                    try {
                        return Optional.of(indexSearcher.doc(scoreDoc.doc));
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