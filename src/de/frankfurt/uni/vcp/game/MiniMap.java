package de.frankfurt.uni.vcp.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static com.jme3.math.FastMath.sin;
import static com.jme3.math.FastMath.PI;
import static com.jme3.math.FastMath.ONE_THIRD;

import javax.imageio.ImageIO;

import de.frankfurt.uni.vcp.config.MapConfig;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.nodes.HexMap;

/**
 * Draws a small map representing the unit position on the {@link HexMap}.
 */
public class MiniMap {

	/** Image container. */
	private BufferedImage miniMap;

	/** Image height in pixel. */
	private int imageHeight = 150;

	/** Image width in pixel. */
	private int imageWidth = 178;

	/**
	 * Field count in x direction. TODO: can be set dynamically, we have the
	 * value.
	 */
	private int fieldCountX = 25;

	/**
	 * Field count in y direction. TODO: can be set dynamically, we have the
	 * value.
	 */
	private int fieldCountY = 25;

	/**
	 * h and g are the scaling factors for the x and y axis to map the
	 * individual hexFields onto the miniMap
	 */
	private int h = imageHeight / fieldCountY;
	private int g = imageWidth / fieldCountX;
	public static final float ANGLE = PI * ONE_THIRD;
	private int vs = 2;
	private int[] x = { 0, (int) (-sin(ANGLE) * g / 2f), (int) (-sin(ANGLE) * g / 2f), 0, (int) (sin(ANGLE) * g / 2f), (int) (sin(ANGLE) * g / 2f) };
	private int[] y = { -1 * (int) (h / vs), (int) (-0.5f * h / vs), (int) (0.5f * h / vs), 1 * (int) (h / vs), (int) (0.5f * h / vs),
			(int) (-0.5f * h / vs) };

	/**
	 * Needs a {@link MapConfig} to draw it.
	 * 
	 * @param mapConfig
	 */
	public MiniMap(MapConfig mapConfig) {
		miniMap = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

		int startp = 0;
		for (int j = 0; j < 25; j++) {
			for (int i = 0; i < 25; i++) {
				if (mapConfig.csv[i][j] == 1)
					drawHex(j, i, new Color(0.7f, 0.5f, 0.0f, 1.0f));
			}
			startp = (startp == 0) ? (int) (g / 2f) : 0;
		}
		saveImage();
	}

	/**
	 * Writes the image to a file.
	 */
	public void saveImage() {
		try {
			ImageIO.write(miniMap, "png", new File("data/gui/minimap" + ".png"));
		} catch (IOException e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}

	/**
	 * Draws the actual mini hex map as an image.
	 * 
	 * @param index_i
	 * @param index_j
	 * @param color
	 */
	public void drawHex(int index_i, int index_j, Color color) {

		int startp = (index_j % 2 != 0) ? 0 : (int) (g / 2f);

		index_i = (int) (imageWidth - index_i * g - startp);
		index_j = (int) (imageHeight - index_j * h);

		Graphics2D miniMapGraphic = (Graphics2D) miniMap.getGraphics();

		Polygon p = new Polygon(x, y, 6);
		p.translate((int) (index_i - g / 2f), (int) ((index_j - h / 2f)));

		miniMapGraphic.setPaint(new Color(0f, 0f, 0f, 1f));
		miniMapGraphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		miniMapGraphic.fill(p);
		miniMapGraphic.fill(p);

		miniMapGraphic.setPaint(color);
		miniMapGraphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		miniMapGraphic.fill(p);

	}

	/**
	 * Draws an single hex field which is not occupied.
	 * 
	 * @param index_i
	 * @param index_j
	 */
	public void resetHex(int index_i, int index_j) {
		drawHex(index_i, index_j, new Color(0.7f, 0.5f, 0.0f, 1.0f));
	}

	public BufferedImage getImage() {
		return miniMap;
	}

}
