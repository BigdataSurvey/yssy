package com.zywl.app.server.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.zywl.app.base.util.Base64Util;
import com.zywl.app.base.util.GZIPUtils;

public class TestMain3 {
	
	public static void main(String[] args) throws Exception {
		/*List<File> files = Lists.newArrayList(new File("D:\\b\\f"), new File("D:\\b"));
		
		for (File file : getAllFile(files)) {
			System.out.println(file.getName());
		}*/
		FileInputStream inputStream = new FileInputStream(new File("E:\\REC20180503165247.mp3"));
		String base64Str = Base64Util.inputStream2Base64Str(inputStream);
		System.out.println("原长度：" + base64Str.length() / 1024);
		System.out.println("压缩后：" + GZIPUtils.compress(base64Str).length / 1024);
		
	}
	
	public static List<File> getAllFile(List<File> files){
		List<File> result = new ArrayList<File>();
		for (File file : files) {
			if(file.isFile()){
				result.add(file);
			}else{
				result.addAll(getAllFile(file));
			}
		}
		return result;
	}
	
	public static List<File> getAllFile(File dir){
		File[] files = dir.listFiles();
		return getAllFile(Lists.newArrayList(files));
	}
}
