package com.zywl.app.base.util;

import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {
	
	public static void zoomImageScale(File imageFile, String newPath, int newWidth) throws IOException {
		if (!imageFile.canRead())
			return;
		BufferedImage bufferedImage = ImageIO.read(imageFile);
		if (null == bufferedImage)
			return;

		int originalWidth = bufferedImage.getWidth();
		int originalHeight = bufferedImage.getHeight();
		double scale = (double) originalWidth / (double) newWidth; // 缩放的比例

		int newHeight = (int) (originalHeight / scale);

		zoomImageUtils(imageFile, newPath, bufferedImage, newWidth, newHeight);
	}

	private static void zoomImageUtils(File imageFile, String newPath, BufferedImage bufferedImage, int width, int height) throws IOException {
		String suffix = StringUtils.substringAfterLast(imageFile.getName(), ".");
		if(suffix != null){
			suffix = suffix.trim().toLowerCase();
			// 处理 png 背景变黑的问题
			if (suffix.endsWith("png") || suffix.endsWith("gif")) {
				BufferedImage to = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = to.createGraphics();
				to = g2d.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
				g2d.dispose();
				g2d = to.createGraphics();
				Image from = bufferedImage.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
				g2d.drawImage(from, 0, 0, null);
				g2d.dispose();
				ImageIO.write(to, suffix, new File(newPath));
			} else {
				BufferedImage newImage = new BufferedImage(width, height, bufferedImage.getType());
				Graphics g = newImage.getGraphics();
				g.drawImage(bufferedImage, 0, 0, width, height, null);
				g.dispose();
				ImageIO.write(newImage, suffix, new File(newPath));
			}
		}
	}
}
