package com.zywl.app.base.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * 
 * @author FXBTG Doe.
 *
 */
public class Base64Util {
	
	public static double DEFAULT_COM_BASE = 800d;
	
	public static double DEFAULT_SCALE = -1d;
	
	/**
	 * 压缩base64 Image String
	 * @author Doe.
	 * @param base64String
	 * @return
	 * @throws Exception
	 */
	public static String getMinBase64(String base64String) throws Exception{
		return getMinBase64(base64String, DEFAULT_COM_BASE, DEFAULT_SCALE);
	}
	
	/**
	 * @param base64 Image String
	 * @param comBase 压缩基数
	 * @param scale 压缩限制(宽/高)比例  一般用1：
	 * 当scale>=1,缩略图height=comBase,width按原图宽高比例;若scale<1,缩略图width=comBase,height按原图宽高比例
	 * @throws Exception
	 */
	public static String getMinBase64(String base64String, double comBase, double scale) throws Exception {
		BufferedImage src = base64Str2Image(base64String);
		if(src == null)
			return base64String;
		int srcHeight = src.getHeight(null);
		int srcWidth = src.getWidth(null);
		int deskWidth,deskHeight = 0;// 缩略图宽，高
		double srcScale = (double) srcHeight / srcWidth; //原图宽高比
		
		/**缩略图宽高算法*/
		if((double) srcHeight > comBase || (double) srcWidth > comBase) {
			if (srcScale >= scale || 1 / srcScale > scale){
				if(srcScale >= scale){
					deskHeight = (int) comBase;
					deskWidth = srcWidth * deskHeight / srcHeight;
				}else{
					deskWidth = (int) comBase;
					deskHeight = srcHeight * deskWidth / srcWidth;
				}
			}else{
				if((double) srcHeight > comBase){
					deskHeight = (int) comBase;
					deskWidth = srcWidth * deskHeight / srcHeight;
				}else{
					deskWidth = (int) comBase;
					deskHeight = srcHeight * deskWidth / srcWidth;
				}
			}
		}else{
			deskHeight = srcHeight;
			deskWidth = srcWidth;
		}
		BufferedImage bufferedImage = new BufferedImage(deskWidth, deskHeight, BufferedImage.TYPE_3BYTE_BGR);
		bufferedImage.getGraphics().drawImage(src, 0, 0, deskWidth, deskHeight, null); //绘制缩小后的图
		
		return Base64.getEncoder().encodeToString(getBufferedImageByteArray(bufferedImage));
	}
	
	public static String inputStream2Base64Str(InputStream inputStream) throws IOException{
		byte[] bytes = new byte[inputStream.available()];
		inputStream.read(bytes);
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	/**
	 * base64转图片流
	 * @author Doe.
	 * @param base64String
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage base64Str2Image(String base64String) throws IOException{
		try(ByteArrayInputStream base64Str2ByteArrayInputStream = base64Str2ByteArrayInputStream(base64String);){
			return ImageIO.read(base64Str2ByteArrayInputStream);
		}
	}
	
	/**
	 * Base64转ByteArray
	 * @author Doe.
	 * @param base64String
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayInputStream base64Str2ByteArrayInputStream(String base64String) throws IOException{
		return new ByteArrayInputStream(base64Str2ByteArray(base64String));
	}
	
	public static byte[] base64Str2ByteArray(String base64String) throws IOException{
		base64String = formatBase64Str(base64String);
		byte[] decodeBuffer = org.apache.commons.codec.binary.Base64.decodeBase64(base64String);
//		byte[] decodeBuffer = Base64.getDecoder().decode(base64String);
		return decodeBuffer;
	}
	
	/**
	 * 格式化Base64
	 * @author Doe.
	 * @param base64String
	 * @return
	 */
	public static String formatBase64Str(String base64String){
		if(base64String.contains("data:") && base64String.indexOf(",") > 0){
			return base64String.substring(base64String.indexOf(",") + 1);
		}
		return base64String;
	}
	
	public static String getBase64URIScheme(String base64String){
		if(base64String.contains("data:") && base64String.indexOf(",") > 0){
			return base64String.substring(0 ,base64String.indexOf(",")+1);
		}
		return null;
	}
	
	public static String getBase4ImageSuffix(String base64String){
		String scheme = getBase64URIScheme(base64String);
		if(scheme != null){
			if(scheme.contains("gif")){
				return "gif";
			}
			if(scheme.contains("png")){
				return "png";
			}
		}
		return "jpg";
	}
	
	/**
	 * 获取base64图片大小
	 * @author Doe.
	 * @param base64String
	 * @return (b字节)
	 * @throws IOException 
	 */
	public static long getBase64ImageSize(String base64String) throws IOException{
		long strLength = formatBase64Str(base64String).length();
		return strLength - ( strLength /8) * 2;
	}

	/**
	 * 获取BufferedImage的byte数组
	 * @author Doe.
	 * @param bufferedImage
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBufferedImageByteArray(BufferedImage bufferedImage) throws IOException{
		ByteArrayOutputStream deskImage = new ByteArrayOutputStream(4096);
		
		ImageIO.write(bufferedImage, "JPEG", deskImage);
		try{
			return deskImage.toByteArray();
		}finally{
			if(deskImage != null) deskImage.close(); 
		}
	}
	
	public static void main(String[] args) {
		try {
			String base64Str = "data:image/png;base64,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";
			System.out.println(getBase64ImageSize(base64Str));
			System.out.println(getMinBase64(base64Str));
			System.out.println(getBase64ImageSize(getMinBase64(base64Str)));
//			String minBase64 = getMinBase64(base64Str);
//			FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\DOE\\Desktop\\蜜桃\\座驾icon\\雷麒麟_icon.png"));
//			String inputStream2Base64Str = inputStream2Base64Str(fileInputStream);
//			System.out.println(inputStream2Base64Str);
//			byte[] base64Str2ByteArray = base64Str2ByteArray(inputStream2Base64Str);
//			FileUtils.writeByteArrayToFile(new File("C:\\\\Users\\\\DOE\\\\Desktop\\\\蜜桃\\\\座驾icon\\\\雷麒麟_icon_11.png"), base64Str2ByteArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
