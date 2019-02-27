package edu.unimelb.utils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 
 * @ClassName: FileUtil
 * @Description: TODO
 * @author Xiaorong Yang
 * @date Jan 22, 2019 8:34:58 PM
 *
 */
public class FileUtil {
	static String dictory = "src/main/resources/temp/upload/";



	public static String uploadFile(byte[] file, String fileName) throws Exception { 
		
		
        File targetFile = new File(dictory);  
        if(!targetFile.exists()){    
            targetFile.mkdirs();    
        }       
        
        String desPath =  dictory+fileName;
        FileOutputStream out = new FileOutputStream(desPath);
        out.write(file);
        out.flush();
        out.close();
        
        return desPath;
    }
	
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

	public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;    
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

}
