package com.example;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class CranfieldVSMQuery {

    public static void main(String[] args) {
        try {
            // Open the index directory
            Directory indexDir = FSDirectory.open(Paths.get("cranfield_index"));
            IndexReader reader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(reader);

            // Use Vector Space Model (default similarity)
            // Use EnglishAnalyzer to parse the queries
            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            QueryParser parser = new QueryParser("content", analyzer);

            // Create PrintWriter to write results to file
            PrintWriter writer = new PrintWriter(new FileWriter("vsm_results.txt", true));

            // Load and run all queries from cran.qry
            BufferedReader queryReader = new BufferedReader(new FileReader("/home/azureuser/LuceneProject/cranfield_data/cran/cran.qry"));
            String queryLine;
            int queryId = 1;
            while ((queryLine = queryReader.readLine()) != null) {
                Query query = parser.parse(queryLine);
                TopDocs results = searcher.search(query, 50);  // Top 50 results
                ScoreDoc[] hits = results.scoreDocs;

                // Print and store the results for each query
                for (int i = 0; i < hits.length; i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    writer.println(queryId + " Q0 " + doc.get("docId") + " " + (i + 1) + " " + hits[i].score + " STANDARD");
                }
                queryId++;
            }

            // Close the readers and writers
            queryReader.close();
            writer.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

