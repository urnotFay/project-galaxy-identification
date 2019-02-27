package edu.unimelb.utils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class ImagesUtil {
	
    private final static int HASH_SIZE=32;
    
    public static float calculateSimilarity(String dbImagePath, String uploadImagePath ) throws Exception {
    	
    	BufferedImage dbImage = ImageIO.read(new File(dbImagePath));
    	BufferedImage uploadImage = ImageIO.read(new File(uploadImagePath));
    	byte[] dbHashValue = hashValue(dbImage);
    	byte[] uploadHashValue = hashValue(uploadImage);
    	byte[] dbCompact = compact(dbHashValue);
    	byte[] uploadCompact = compact(uploadHashValue);
    	byte[] dbUncompact = uncompact(dbCompact);
    	byte[] uploadUncompact = uncompact(uploadCompact);
    	float compare = compare(dbUncompact,uploadUncompact);
    	return compare;
    	
    }
    
    private static float compare(byte[] f1,byte[] f2){
        if(f1.length!=f2.length)
            throw new IllegalArgumentException("mismatch FingerPrint length");
        int sameCount=0;
        for(int i=0;i<f1.length;++i){
            if(f1[i]==f2[i])++sameCount;
        }
        return (float)sameCount/f1.length;
    }

    private static byte[] hashValue(BufferedImage src){
        BufferedImage hashImage = resize(src,HASH_SIZE,HASH_SIZE);
        byte[] matrixGray = (byte[]) grayScale(hashImage).getData().getDataElements(0, 0, HASH_SIZE, HASH_SIZE, null); 
        return  binaryzation(matrixGray);
    }

    private static BufferedImage resize(Image src,int width,int height){
        BufferedImage result = new BufferedImage(width, height,  
                BufferedImage.TYPE_3BYTE_BGR); 
         Graphics g = result.getGraphics();
         try{
             g.drawImage(src.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
         }finally{
             g.dispose();
         }
        return result;      
    }

    private static byte[] binaryzation(byte[]src){
        byte[] dst = src.clone();
        int mean = mean (src);
        for(int i=0;i<dst.length;++i){
            // 将数组元素转为无符号整数再比较
            dst[i]=(byte) (((int)dst[i]&0xff)>=mean?1:0);
        }
        return dst;

    }
    
    private static  int mean(byte[] src){
        long sum=0;
        // 将数组元素转为无符号整数
        for(byte b:src)sum+=(long)b&0xff;
        return (int) (Math.round((float)sum/src.length));
    }

    private static BufferedImage grayScale(BufferedImage src){
        if(src.getType()==BufferedImage.TYPE_BYTE_GRAY){
            return src;
        }else{
            // 图像转灰
            BufferedImage grayImage = new BufferedImage(src.getWidth(), src.getHeight(),  
                    BufferedImage.TYPE_BYTE_GRAY);
            new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, grayImage);
            return grayImage;       
        }
    }

    private static byte[] compact(byte[] hashValue){
        byte[] result=new byte[(hashValue.length+7)>>3];
        byte b=0;
        for(int i=0;i<hashValue.length;++i){
            if(0==(i&7)){
                b=0;
            }
            if(1==hashValue[i]){
                b|=1<<(i&7);
            }else if(hashValue[i]!=0)
                throw new IllegalArgumentException("invalid hashValue,every element must be 0 or 1");
            if(7==(i&7)||i==hashValue.length-1){
                result[i>>3]=b;
            }
        }
        return result;
    }
    private static byte[] uncompact(byte[] compactValue){
        byte[] result=new byte[compactValue.length<<3];
        for(int i=0;i<result.length;++i){
            if((compactValue[i>>3]&(1<<(i&7)))==0)
                result[i]=0;
            else
                result[i]=1;
        }
        return result;      
    }

    public static boolean changeSize(int newWidth, int newHeight, String path) {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(path));

            //字节流转图片对象
            Image bi = ImageIO.read(in);
            //构建图片流
            BufferedImage tag = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            //绘制改变尺寸后的图
            tag.getGraphics().drawImage(bi, 0, 0, newWidth, newHeight, null);
            //输出流
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
            //JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            //encoder.encode(tag);
            ImageIO.write(tag, "JPG", out);
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
    }
}
