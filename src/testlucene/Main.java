package testlucene;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import luceneweb.HTMLDocument;

import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException, ParseException {
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    StandardAnalyzer analyzer = new StandardAnalyzer();

    // 1. create the index
    Directory index = new RAMDirectory();

    IndexWriterConfig config = new IndexWriterConfig(analyzer);

    IndexWriter w = new IndexWriter(index, config);
    addDoc(w, "Lucene in Action", "193398817");
    addDoc(w, "Lucene for Dummies", "55320055Z");
    addDoc(w, "Managing Gigabytes", "55063554A");
    addDoc(w, "The Art of Computer Science", "9900333X");
    w.close();

    // 2. query
    String querystr = args.length > 0 ? args[0] : "lucene";

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    Query q = new QueryParser("title", analyzer).parse(querystr);

    // 3. search
    int hitsPerPage = 10;
    IndexReader reader = DirectoryReader.open(index);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
    searcher.search(q, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
    TopScoreDocCollector collector2 = TopScoreDocCollector.create(hitsPerPage);
    searcher.search(q, collector2);
    ScoreDoc[] hits2 = collector2.topDocs().scoreDocs;
    int docId2 = hits2[0].doc;
    Document d2 = searcher.doc(docId2);
    System.out.println(d2.get("title"));

    // 4. display results
    System.out.println("Found " + hits.length + " hits.");
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
    }


    // reader can only be closed when there
    // is no need to access the documents any more.
    reader.close();
  }

  private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("title", title, Field.Store.YES));

    // use a string field for isbn because we don't want it tokenized
    doc.add(new StringField("isbn", isbn, Field.Store.YES));
    w.addDocument(doc);
  }
  
//  private static void indexDocs(String url) throws Exception {
//
//      //index page
//      Document doc = HTMLDocument.Document(url);
//      System.out.println("adding " + doc.get("path"));
//      try {
//          indexed.add(doc.get("path"));
//          writer.addDocument(doc);          // add docs unconditionally
//          //TODO: only add HTML docs
//          //and create other doc types
//
//          //get all links on the page then index them
//          LinkParser lp = new LinkParser(url);
//          URL[] links = lp.ExtractLinks();
//
//          for (URL l : links) {
//              //make sure the URL hasn't already been indexed
//              //make sure the URL contains the home domain
//              //ignore URLs with a querystrings by excluding "?"
//              if ((!indexed.contains(l.toURI().toString())) &&
//                  (l.toURI().toString().contains(beginDomain)) &&
//                  (!l.toURI().toString().contains("?"))) {
//                  //don't index zip files
//                  if (!l.toURI().toString().endsWith(".zip")) {
//                      System.out.print(l.toURI().toString());
//                      indexDocs(l.toURI().toString());
//                  }
//              }
//          }
//
//      } catch (Exception e) {
//          System.out.println(e.toString());
//      }
//  }
}