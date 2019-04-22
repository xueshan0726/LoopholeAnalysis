package Common;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestLucene {
    public static void main(String[] args) {
        search("");
    }

    public static void IndexWriterOpt(String indexName,BiConsumer<IndexWriter,Directory> indexWriterConsumer) {
        Analyzer analyzer=new StandardAnalyzer();
        IndexWriter iwriter=null;
        DirectoryReader ireader=null;
        IndexSearcher isearcher=null;
        Directory directory=null;
        try {
            directory= FSDirectory.open(Paths.get(indexName));
            IndexWriterConfig config=new IndexWriterConfig(analyzer);
            iwriter=new IndexWriter(directory,config);
            indexWriterConsumer.accept(iwriter,directory);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(iwriter!=null) {
                    iwriter.close();
                }
                if(directory!=null){
                    directory.close();
                }
                if(ireader!=null){
                    ireader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void insert(String indexname) {
        IndexWriterOpt(indexname,((indexWriter, directory) -> {
            Document document=new Document();
            document.add(new StringField("name","tom", Field.Store.YES));
            document.add(new StringField("status","10", Field.Store.YES));
            document.add(new Field("description","Would you like to have dinner with me? Yes,I would",TextField.TYPE_STORED));
            document.add(new LongPoint("time",System.currentTimeMillis()));
            try {
                indexWriter.addDocument(document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void search(String indexname) {
        IndexWriterOpt(indexname,(indexWriter, directory) -> {
            try {
                StandardQueryParser standardQueryParser = new StandardQueryParser();
                Query query = standardQueryParser.parse("description : tom ", "time");
                DirectoryReader directoryReader=DirectoryReader.open(directory);
                IndexSearcher indexSearcher=new IndexSearcher(directoryReader);
                TopFieldDocs search = indexSearcher.search(query, Integer.MAX_VALUE, Sort.INDEXORDER);
                ScoreDoc[] scoreDocs = search.scoreDocs;
                for(ScoreDoc scoreDoc:scoreDocs){
                    int id=scoreDoc.doc;
                    Document doc = indexSearcher.doc(id);
                    System.out.println(doc.get("name"));
                    System.out.println(doc.getBinaryValue("time"));
                    System.out.println(doc.get("description"));
                    System.out.println(doc.get("status"));

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (QueryNodeException e) {
                e.printStackTrace();
            }
        });
    }

    public static void search(String indexname,String keyword,Consumer<ScoreDoc[]> consumer) {
        IndexWriterOpt(indexname,(indexWriter, directory) -> {
            try {
                StandardQueryParser standardQueryParser = new StandardQueryParser();
                Query query = standardQueryParser.parse("description : "+keyword, "time");
                DirectoryReader directoryReader=DirectoryReader.open(directory);
                IndexSearcher indexSearcher=new IndexSearcher(directoryReader);
                TopFieldDocs search = indexSearcher.search(query, Integer.MAX_VALUE, Sort.INDEXORDER);
                ScoreDoc[] scoreDocs = search.scoreDocs;
                consumer.accept(scoreDocs);
                for(ScoreDoc scoreDoc:scoreDocs){
                    int id=scoreDoc.doc;
                    Document doc = indexSearcher.doc(id);
                    System.out.println(doc.get("name"));
                    System.out.println(doc.getBinaryValue("time"));
                    System.out.println(doc.get("description"));
                    System.out.println(doc.get("status"));

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (QueryNodeException e) {
                e.printStackTrace();
            }
        });
    }
}
