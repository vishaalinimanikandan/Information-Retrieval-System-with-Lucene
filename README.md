# Information-Retrieval-System-with-Lucene
Implementation of an Information Retrieval System leveraging Lucene to parse, index, and query datasets using multiple similarity models (BM25, VSM, Boolean, LMDirichlet) and analyzers (Standard, English, Whitespace). Includes precision-recall evaluation and performance analysis.

## Overview
This repository contains the implementation and evaluation of an information retrieval system using **Apache Lucene**. The system parses and indexes four datasets: **FBIS**, **Federal Register (FR94)**, **Financial Times (FT)**, and **LA Times**, allowing efficient querying and ranking of documents using various similarity models and analyzers.

## Features
- **Data Parsing**:Parsing the data for effective retrieval.
- **Indexing**: Supports different analyzers (Standard, English, and Whitespace).
- **Querying**: Batch and interactive search modes with various similarity models, including BM25, Vector Space Model (VSM), Boolean, and LMDirichlet.
- **Evaluation**: TREC Eval-compatible results for precision, recall, MAP, and other metrics.

## Dataset Description

The **Cranfield Collection** serves as a benchmark dataset for evaluating information retrieval systems. It contains:

- **Documents**: 1,400 scientific abstracts from various fields.
- **Queries**: A set of 225 well-defined queries to evaluate retrieval performance.
- **Relevance Judgements (cranqrel)**: Graded relevance scores that map queries to relevant documents, allowing detailed evaluation.

This dataset was selected for its structured nature and compatibility with TREC evaluation metrics, making it ideal for comparing retrieval models like BM25 and Vector Space Model (VSM). The preprocessing step utilizes the EnglishAnalyzer for tokenization, stop-word removal, and stemming, ensuring consistency in the retrieval process.


## Methodology
1. **Preprocessing**: Parsed documents using Jsoup for XML-like structures.
2. **Indexing**: Indexed the datasets with Apache Lucene using different analyzers.
3. **Querying**: Executed both batch and interactive searches with different similarity models.
4. **Evaluation**: Evaluated the retrieval effectiveness using TREC Eval for metrics like MAP, Precision@K, and Recall.

## How to Run
### Pre-requisites
- **Java Development Kit (JDK)**: Version 11 or above.
- **Apache Maven**: For managing dependencies and building the project.
- **TREC Eval**: For evaluating the retrieval results.

### Steps to Run
1. **Clone the Repository**:
    ```bash
    git clone https://github.com/<username>/<repository_name>.git
    cd <repository_name>
    ```

2. **Compile the Project**:
    ```bash
    mvn clean compile
    ```

3. **Run Indexing**:
    ```bash
    mvn exec:java -Dexec.mainClass="com.example.CreateIndex" -Dexec.args="english"
    ```

4. **Run Querying**:
    - Interactive Mode:
      ```bash
      mvn exec:java -Dexec.mainClass="com.example.QueryIndex" -Dexec.args="interactive bm25 english"
      ```
    - Batch Mode:
      ```bash
      mvn exec:java -Dexec.mainClass="com.example.QueryIndex" -Dexec.args="batch bm25 english"
      ```

5. **Evaluate Results with TREC Eval**:
    ```bash
    ./trec_eval qrels.assignment2.part1 results/bm25_results.txt
    ```
## Results
### Metrics
| Metric        | BM25    | VSM     | Boolean | LMDirichlet | 
|---------------|---------|---------|---------|-------------|
| MAP           | 0.3513  | 0.2153  | 0.0946  | 0.2987      | 
| R-Precision   | 0.3950  | 0.2785  | 0.1404  | 0.3455      |
| Precision@5   | 0.7360  | 0.4640  | 0.2480  | 0.6080      |
| Precision@10  | 0.6320  | 0.4520  | 0.2400  | 0.5640      | 

### Precision-Recall Curve
<img src="https://github.com/user-attachments/assets/b052e44d-c866-4802-9c91-b78665b2f88b" alt="Precision-Recall Curve" width="700">

### Analyzers Comparison
<img src="https://github.com/user-attachments/assets/240608d3-0709-43d0-97e0-0b1a93b7cc0c" alt="Analyzers Comparison" width="700">
