package com.zywl.app.base.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
	
	public static final String zip(String str) {
		if (str == null) return null;
		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		String compressedStr = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(str.getBytes());
			zout.closeEntry();
			compressed = out.toByteArray();
			
			compressedStr = Base64.getEncoder().encodeToString(compressed);
		} catch (IOException e) {
			compressed = null;
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return compressedStr;
	}

	public static final String unzip(String compressedStr) {
		if (compressedStr == null) {
			return null;
		}
		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		String decompressed = null;
		try {
			byte[] compressed = Base64.getDecoder().decode(compressedStr);
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toString();
		} catch (IOException e) {
			e.printStackTrace();
			decompressed = null;
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return decompressed;
	}
	
	public static void main(String[] args) {
		String str = "[WARN ] [19:17:50] com.zywl.app.manager.service.SymbolNewsLoaderService - 来源为：华尔街见闻内容解析失败(https://wallstreetcn.com/articles/3030038)[WARN ] [19:17:50] com.zywl.app.manager.service.SymbolNewsLoaderService - 来源为：华尔街见闻内容解析失败(https://wallstreetcn.com/articles/3030038)[WARN ] [19:17:50] com.zywl.app.manager.service.SymbolNewsLoaderService - 来源为：华尔街见闻内容解析失败(https://wallstreetcn.com/articles/3030038)[WARN ] [19:17:50] com.zywl.app.manager.service.SymbolNewsLoaderService - 来源为：华尔街见闻内容解析失败(https://wallstreetcn.com/articles/3030038)[WARN ] [19:17:50] com.zywl.app.manager.service.SymbolNewsLoaderService - 来源为：华尔街见闻内容解析失败(https://wallstreetcn.com/articles/3030038)";
		System.out.println(str.length());
		System.out.println(zip(str));
		System.out.println(zip(str).length());
		
	}
}