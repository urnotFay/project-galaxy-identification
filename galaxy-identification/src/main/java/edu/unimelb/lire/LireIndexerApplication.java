package edu.unimelb.lire;

import java.util.Map;

import edu.unimelb.utils.LireUtil;

public class LireIndexerApplication {

	public static void main(String[] args) {
		try {
			LireUtil.createIndex();
			
//			Map<String, String> result = LireUtil.search();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
