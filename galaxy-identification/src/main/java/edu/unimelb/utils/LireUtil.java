package edu.unimelb.utils;



import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;


import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @ClassName: LireUtil
 * @Description: TODO
 * @author Xiaorong Yang
 * @date Jan 17, 2019 5:11:07 PM
 *
 */
public class LireUtil {

	private static final String INDEX_PATH = "src/main/resources/conf/lire/index";

	private static String INDEX_FILE_PATH = "src/main/resources/conf/lire/image";

	private static String SEARCH_FILE = "src/main/resources/conf/data/Sample/Galaxy_0094.jpg";

	public static void createIndex() throws Exception {

		ArrayList<String> images = FileUtils.getAllImages(new File(INDEX_FILE_PATH), true);
		// Creating a CEDD document builder and indexing all files.
		DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
		// Creating an Lucene IndexWriter
		IndexWriterConfig conf = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,
				new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
		IndexWriter iw = new IndexWriter(FSDirectory.open(new File(INDEX_PATH)), conf);
		// Iterating through images building the low level features
		for (Iterator<String> it = images.iterator(); it.hasNext();) {
			String imageFilePath = it.next();
			System.out.println("Indexing " + imageFilePath);
			try {
				BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
				Document document = builder.createDocument(img, imageFilePath);
				iw.addDocument(document);
			} catch (Exception e) {
				System.err.println("Error reading image or indexing it.");
				e.printStackTrace();
			}
		}
		// closing the IndexWriter
		iw.close();
		System.out.println("Finished indexing.");

	}

	public static List<String> search(String searchFilePath) throws IOException {
		
		IndexReader ir = DirectoryReader.open(FSDirectory.open(new File(INDEX_PATH)));
//        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(52);
        ImageSearcher searcher = new GenericFastImageSearcher(3, CEDD.class);

        File file = new File(searchFilePath);
        System.out.println( file.getAbsolutePath());
        BufferedImage img = ImageIO.read(file);

        ImageSearchHits hits = searcher.search(img, ir);
        
        List<String> resultList = new ArrayList<String>();
        for (int i = 0; i < hits.length(); i++) {
            String fileName = hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
            if(!(hits.score(i)== 0.0)) {
                resultList.add(fileName);

            }
        }
        return resultList;
	}
	
	
	public static Map<String, String> search() throws IOException {
		IndexReader ir = DirectoryReader.open(FSDirectory.open(new File(INDEX_PATH)));
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(52);

        File file = new File(SEARCH_FILE);
        System.out.println( file.getAbsolutePath());
        BufferedImage img = ImageIO.read(file);

        ImageSearchHits hits = searcher.search(img, ir);
        
        Map<String, String> result  = new HashMap<String, String>();
        for (int i = 0; i < hits.length(); i++) {
            String fileName = hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
            result.put(fileName, String.valueOf(hits.score(i)));
        }
        return result;
	}
	
	
}
