package com.example;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

public class CranfieldIndexer {

    public static void main(String[] args) {
        try {
            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            Directory indexDir = FSDirectory.open(Paths.get("cranfield_index"));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(indexDir, config);

            BufferedReader br = new BufferedReader(new FileReader("/home/azureuser/LuceneProject/cranfield_data/cran/cran.all.1400"));

            String line;
            String docId = "";
            StringBuilder content = new StringBuilder();

            while ((line = br.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (content.length() > 0) {
                        addDocument(writer, docId, content.toString());
                        content.setLength(0);
                    }
                    docId = line.split(" ")[1];
                } else if (line.startsWith(".W")) {
                    continue;
                } else {
                    content.append(line).append(" ");
                }
            }

            if (content.length() > 0) {
                addDocument(writer, docId, content.toString());
            }

            writer.close();
            br.close();
            System.out.println("Indexing completed!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addDocument(IndexWriter writer, String docId, String content) throws Exception {
        Document doc = new Document();
        doc.add(new StringField("docId", docId, StringField.Store.YES));
        doc.add(new TextField("content", content, TextField.Store.YES));
        writer.addDocument(doc);
    }
}

