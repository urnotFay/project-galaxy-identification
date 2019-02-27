package edu.unimelb.deeplearning4j.evaluate;

public class EvaluateResult {

	public static void main(String[] args) {
		
		try {
			Evaluate.eval( "NGC 3521");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
