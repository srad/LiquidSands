package de.frankfurt.uni.vcp.helpers;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Common class for any image processing helper methods.
 */
public class ImageHelper {

	/**
	 * Processes a simple blur filter on any image.
	 * 
	 * @param bufferedImage
	 * @return BlurredImage
	 */
	public static BufferedImage getBlurredImage(BufferedImage bufferedImage) {
		float[] blurKernel = {
				1 / 10f, 1 / 10f, 1 / 10f,
				1 / 10f, 1 / 10f, 1 / 10f,
				1 / 10f, 1 / 10f, 1 / 10f
		};
		BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
		return blur.filter(
				bufferedImage,
				new BufferedImage(bufferedImage.getWidth(), bufferedImage
						.getHeight(), bufferedImage.getType()));
	}

}
