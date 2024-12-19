package com.example.lucene;

import java.io.IOException;
import java.io.*;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;

class DocIndexer {

    enum DocContentType {
        ID,
        TITLE,
        AUTHOR,
        BIB,
        CONTENT 
    }
    
    private static String INDEX_DIRECTORY = "cranfield_index";
    private static String DATA_FILE = "/Users/hariharan_g/dtrin/irws/lucene-cranfield/corpus/cran.all.1400";


    public static void indexDocuments() throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        // Index all the documents in the Cranfield Collection
        indexDocuments(writer, new File(DATA_FILE));

        // Close the writer when done
        writer.close();
        System.out.println("Documents indexed.");
    }

    // Method to parse and index all documents in the Cranfield Collection
    public static void indexDocuments(IndexWriter writer, File dataFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataFile));

        String line;
        String docId = null, title = null, author = null, bibliography = null, content = null;
        StringBuilder contentBuilder = new StringBuilder();
        
        DocContentType typeParsed = null;
        String tempString = "null";
        String nextLine = reader.readLine();
        while (nextLine != null) {
            line = nextLine;
            if (line.startsWith(".I")) {
                tempString = null;
                // Process the previous document if it exists
                if (docId != null) {
                    indexDocument(writer, docId, title, author, bibliography, content);
                    docId = null;
                    title = null;
                    author = null;
                    bibliography = null;
                    content = null;
                }
                typeParsed = DocContentType.ID;
                // Reset the fields and get the new document ID
                docId = line.split(" ")[1].trim();
            } else if (line.startsWith(".T")) {
                // Read the title, pass the first line after ".T
                typeParsed = DocContentType.TITLE;
                tempString = "";
            } else if (line.startsWith(".A")) {
                tempString = "";
                typeParsed = DocContentType.AUTHOR;
                // Read the author, pass the first line after ".A"

            } else if (line.startsWith(".B")) {
                tempString = "";
                typeParsed = DocContentType.BIB;
                // Read the bibliography, pass the first line after ".B"

            } else if (line.startsWith(".W")) {
                tempString = "";
                typeParsed = DocContentType.CONTENT;                // Read the content, pass the first line after ".W"
            } else {
                tempString = tempString.concat(line);
                switch (typeParsed) {
                    case TITLE:
                        title = tempString;
                        break;
                    case AUTHOR:
                        author = tempString;
                        break;
                    case BIB:
                        bibliography = tempString;
                        break;
                    case CONTENT:
                        content = tempString;
                        break;
                        default: break;
                }
                }
                nextLine = reader.readLine();
            }

        // Index the last document
        if (docId != null) {
            indexDocument(writer, docId, title, author, bibliography, content);
        }

        reader.close();
    }

    // Method to index a single document
    public static void indexDocument(IndexWriter writer, String docId, String title, String author, String bibliography, String content) throws IOException {
        // Create a new document for Lucene
        Document doc = new Document();

    

        doc.add(new StringField("docId", docId, Field.Store.YES));             // Store the document ID
        if (title != null) doc.add(new TextField("title", title, Field.Store.YES));  // Store and index the title
        if (author != null) doc.add(new TextField("author", author, Field.Store.YES));  // Store and index the author
        if (bibliography != null) doc.add(new TextField("bibliography", bibliography, Field.Store.YES));  // Store and index the bibliography
        if (content != null) doc.add(new TextField("content", content, Field.Store.YES));  // Store and index the content

        // Add the document to the index
        writer.addDocument(doc);
 
    }
}