
package luceneweb;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.net.URL;
import java.util.ArrayList;


public class Main {


	private static IndexWriter writer;		  
	private static ArrayList indexed;
	private static String beginDomain;


	public static void main(String[] args) throws Exception{

		Directory index = new RAMDirectory();
		boolean create = true;
		String link = "https://www.microsoft.com/en-us/DigitalLiteracy/curriculum4.aspx#computerbasics/";
		beginDomain = Domain(link);
		System.out.println(beginDomain);
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		writer = new IndexWriter(index, config);
		indexed = new ArrayList();

		indexDocs(link);

		writer.close();
		System.out.println("done");

		String querystr = args.length > 0 ? args[0] : "Word";
		StandardAnalyzer analyzer = new StandardAnalyzer();

		Query q = new QueryParser("title", analyzer).parse("microsoft");
		System.out.println(q); 


		int hitsPerPage = 10;
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		System.out.println("Found " + hits.length + " hits.");
		
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
		}
		
		reader.close();
		
		System.out.println(writer.toString());

	}


	private static void indexDocs(String url) throws Exception {

		//index page
		Document doc = HTMLDocument.Document(url);
		System.out.println("adding " + doc.get("path"));
		try {
			indexed.add(doc.get("path"));
			writer.addDocument(doc);		  // add docs unconditionally
			//TODO: only add html docs
			//and create other doc types


			//get all links on the page then index them
			LinkParser lp = new LinkParser(url);
			URL[] links = lp.ExtractLinks();

			for (URL l : links) {
				//make sure the url hasnt already been indexed
				//make sure the url contains the home domain
				//ignore urls with a querystrings by excluding "?" 
				System.out.println(l.toString());
				if ((!indexed.contains(l.toURI().toString())) && (l.toURI().toString().contains(beginDomain)) && (!l.toURI().toString().contains("?"))) {
					//don't index zip files
					if (!l.toURI().toString().endsWith(".zip"))
					{
						System.out.println(l.toURI().toString());
						indexDocs(l.toURI().toString());
					}
				}
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	private static String Domain(String url)
	{
		int firstDot = url.indexOf(".");
		int lastDot =  url.lastIndexOf(".");
		return url.substring(firstDot+1,lastDot);
	}


}
