package com.example;

import java.io.IOException;
import java.io.*;
import java.nio.file.Paths;
import java.io.BufferedReader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException; // <-- Make sure this import is present
import org.apache.lucene.search.similarities.BM25Simil;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

 
public class App_vsm
{
    
    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "cranfield_index";
    private static final String QUERY_FILE = "/home/azureuser/LuceneProject/cranfield_data1/cran/cran.qry";    // File containing the 225 queries
    private static final String RESULT_FILE = "bm_results1.txt"; // File to store the results for TREC Eval
    private static final String DATA_FILE = "/home/azureuser/LuceneProject/cranfield_data1/cran/cran.all.1400";          // File containing all 1400 documents
   
    // Method to process each query and write the results to the result file
    public static void processQuery(IndexSearcher searcher, StandardAnalyzer analyzer, int queryId, String queryText, BufferedWriter resultWriter) throws IOException {
        // Sanitize the query before parsing
        String sanitizedQuery = sanitizeQuery(queryText);

        try {
            // Parse the sanitized query
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(sanitizedQuery);

            // Execute the search and retrieve top 50 results
            TopDocs results = searcher.search(query, 50);
            System.out.println("Query ID: " + queryId + " found " + results.totalHits + " hits.");

            // Write the results in TREC Eval format: query_id 0 doc_id rank score run_id
            int rank = 1;
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String docId = doc.get("filename").replaceAll("\\D+", "");  // Extract numeric doc ID
                resultWriter.write(queryId + "@0@" + docId + "@" + rank + "@" + scoreDoc.score + "@run1\n");
                rank++;
            }
        } catch (ParseException e) {
            System.err.println("Failed to parse query: " + sanitizedQuery);
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) throws Exception
    {

        parseQueries();
       
    }
    
    public static void parseQueries() throws Exception {
       try {
            // Parse and process the queries
            File queryFile = new File(QUERY_FILE);
            List<QueryTuple> queryTuples = getAllQueries(queryFile);

            // Initialize Lucene IndexSearcher
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY))));
            searcher.setSimilarity(new BM25Similarity());

            StandardAnalyzer analyzer = new StandardAnalyzer();
            BufferedWriter resultWriter = new BufferedWriter(new FileWriter(RESULT_FILE));

            for (QueryTuple tuple : queryTuples) {
                // Get query text and process it
                // QueryTuple tuple = queryTuples.get(0);
                // String queryText = tuple.getQueryText();
                searchLucene1(tuple, searcher, analyzer, resultWriter);
            }
            resultWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Print parsed queries (tuple format)
    }

        // Method to parse queries and return a list of query tuples
    public static List<QueryTuple> getAllQueries(File queryFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(queryFile));
        List<QueryTuple> queryTuples = new ArrayList<>();

        String line;
        StringBuilder queryText = new StringBuilder();
        int seqQueryId = 1;  // This is the sequential query ID
        int qrelId = 0;      // This will store the QREL ID from the ".I" line

        while ((line = reader.readLine()) != null) {
            if (line.startsWith(".I")) {
                // New query starts, process the previous query if one exists
                if (qrelId != 0) {
                    // Add the tuple (sequential ID, QREL ID, query text)
                    queryTuples.add(new QueryTuple(seqQueryId, qrelId, queryText.toString().trim()));
                    seqQueryId++;  // Increment the sequential query ID
                    queryText.setLength(0);  // Clear the query text for the next query
                }
                // Extract the QREL ID from the ".I" line
                qrelId = Integer.parseInt(line.split(" ")[1].trim());
            } else if (line.startsWith(".W")) {
                // Start of query text, continue reading until the next marker
                queryText = new StringBuilder();  // Reset the query text buffer
            } else {
                // Append the query text (accumulate all lines following ".W")
                queryText.append(line).append(" ");
            }
        }

        // Add the last query tuple
        if (qrelId != 0) {
            queryTuples.add(new QueryTuple(seqQueryId, qrelId, queryText.toString().trim()));
        }

        reader.close();
        return queryTuples;
    }
    
    public static String sanitizeQuery(String query) {
        // Replace '?' and '*' characters to avoid invalid wildcard errors
        query = query.replaceAll("\\?", "");  // Remove '?' characters
        query = query.replaceAll("\\*", "");  // Remove '*' characters
        return query;
    }

    public static void searchLucene(QueryTuple queryTuple, IndexSearcher searcher, StandardAnalyzer analyzer,BufferedWriter resultWriter) {
        try {
            // Parse the query
            QueryParser parser = new QueryParser("content", analyzer);  // 'content' is the field in the index
            Query query = parser.parse(sanitizeQuery(queryTuple.getQueryText()));

            // Execute search and get top 50 results
            TopDocs results = searcher.search(query, 50);
            int queryId = queryTuple.getSeqQueryId();

            System.out.println("Query ID: " + queryId + " found " + results.totalHits + " hits.");

            // Write the results in TREC Eval format: query_id 0 doc_id rank score run_id
            
            String parsedDocID = null;
            int rank = 1;
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String docId = doc.get("docId").replaceAll("\\D+", ""); 
                if (parsedDocID != null && parsedDocID == docId){
                    rank++;
                } else {
                    if (parsedDocID != null) {
                        System.out.println(queryId + " 0 " + parsedDocID + " " + rank + " " + scoreDoc.score + " run1\n");
                        resultWriter.write(queryId + " 0 " + parsedDocID + " " + rank + " " + scoreDoc.score + " run1\n");
                    }
                    rank = 1;
                }
                parsedDocID = docId;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void searchLucene1(QueryTuple queryTuple, IndexSearcher searcher, StandardAnalyzer analyzer, BufferedWriter resultWriter) {
    try {
        // Parse the query
        QueryParser parser = new QueryParser("content", analyzer);  // 'content' is the field in the index
        Query query = parser.parse(sanitizeQuery(queryTuple.getQueryText()));

        // Execute search and get top 50 results
        TopDocs results = searcher.search(query, 1000);
        int queryId = queryTuple.getSeqQueryId();

        System.out.println("Query ID: " + queryId + " found " + results.totalHits + " hits.");

        // Use a map to store results temporarily (docId -> rank, score)
        Map<String, Float> uniqueResults = new LinkedHashMap<>();  // LinkedHashMap to preserve order

        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            String docId = doc.get("docId").replaceAll("\\D+", ""); // Remove non-digit characters
            
            // Only store the first occurrence of each docId
            if (!uniqueResults.containsKey(docId)) {
                uniqueResults.put(docId, scoreDoc.score);  // Store docId and its score
            }
        }

        // Now, write all unique results after processing
        int rank = 1;
        for (Map.Entry<String, Float> entry : uniqueResults.entrySet()) {
            String docId = entry.getKey();
            float score = entry.getValue();
            
            // Write the results in TREC Eval format: query_id 0 doc_id rank score run_id
            resultWriter.write(queryId + " 0 " + docId + " " + rank + " " + score + " run1\n");
            System.out.println(queryId + " 0 " + docId + " " + rank + " " + score + " run1");
            rank++;  // Increment rank for the next document
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
