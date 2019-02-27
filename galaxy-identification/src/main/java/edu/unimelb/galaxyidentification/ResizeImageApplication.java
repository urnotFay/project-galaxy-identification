package edu.unimelb.galaxyidentification;

import edu.unimelb.utils.ImagesUtil;

public class ResizeImageApplication {

	public static void main(String[] args) {

		String path = "C:\\Users\\urnot-Shawn\\Desktop\\Images\\";
		for(int i= 0; i<89;i++) {
			if(i>=10) {
				boolean changeSize = ImagesUtil.changeSize(416, 416, path+"Galaxy_00"+i+".jpg");
				System.out.println(changeSize);

			}else {
				boolean changeSize = ImagesUtil.changeSize(416, 416, path+"Galaxy_000"+i+".jpg");

				System.out.println(changeSize);

			}

			
	    }
	}

}
