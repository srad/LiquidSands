/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.frankfurt.uni.vcp.nodes;

import static com.jme3.math.FastMath.sqrt;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import jme3tools.converters.ImageToAwt;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

import de.frankfurt.uni.vcp.config.MapConfig;
import de.frankfurt.uni.vcp.helpers.ImageHelper;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2.</h3>
 * 
 * <p>
 * This class provides the {@link HeightMap} used to draw the topology of the
 * terrain.
 * </p>
 * 
 * <p>
 * The generating the terrain requires two steps:
 * <ol>
 * <li>Generating one image for the {@link HeightMap}...</li>
 * <li>...and to generate an alphamap tp map appropiate textures for different
 * heights within the surface</li>
 * </ol>
 * </p>
 * 
 * @author Bernd Sp�th, Wladimir Spindler and Saman Sedighi Rad
 */
public class DesertHeightMap extends Node {

	public final String MAPS_IMAGE_FOLDER = "data/maps/images/";
	public final String ALPHA_MAP_FILE_NAME = "alphamap";

	public static final int PATCH_SIZE = 65;
	public static final float SQRT_3 = sqrt(3);

	/** Basically the pixel size for the map. */
	public static final int MAP_SIZE = 256;

	public int imageWidth;
	public int imageHeight;

	public MapConfig mapConfig;

	public static final int TOPLEFT = 0;
	public static final int TOPRIGHT = 1;
	public static final int LEFT = 2;
	public static final int CENTER = 3;
	
	/** Used to determine the position of adjacent fields */
	public static int[][] evenOffsets = {
		{+1, +1},
		{0, +1},
		{+1, 0},
	};
	
	/** Used to determine the position of adjacent fields */
	public static int[][] oddOffsets = {
		{0, +1}, 
		{-1, +1},
		{+1, 0},
	};
	
	/**
	 * Creates the complete desert terrain for the map.
	 * @param mapConfig This holds the <i>logical</i> information of the topology of the terrain that is supposed to be generated.
	 * @param assetManager Used to for the required {@link Material} and {@link Texture} for the terrain.
	 * @throws IOException 
	 */
	public DesertHeightMap(MapConfig mapConfig, AssetManager assetManager) throws IOException {
		this.mapConfig = mapConfig;
	    createMapImages (MAP_SIZE, MAP_SIZE);	
	    
		// SHADER
		Material terrainMaterial= new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
		terrainMaterial.setTexture("Alpha", assetManager.loadTexture(ALPHA_MAP_FILE_NAME+".png"));		

		// TEXTURES
	    Texture sand = assetManager.loadTexture("sand.png");
	    sand.setWrap(WrapMode.Repeat);
	    terrainMaterial.setTexture("Tex1", sand);
	    terrainMaterial.setFloat("Tex1Scale", 16f);
	    
	    Texture rock = assetManager.loadTexture("rock.jpg");
	    rock.setWrap(WrapMode.Repeat);
	    terrainMaterial.setTexture("Tex2", rock);
	    terrainMaterial.setFloat("Tex2Scale", 64f);
	    
	    // HEIGHTMAP
	    Texture heightMapImage = assetManager.loadTexture(mapConfig.getMapName() + ".png");
	    
	    AbstractHeightMap heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
	    heightmap.load();

	    int imageWidth = heightMapImage.getImage().getWidth();
	    int imageHeight = heightMapImage.getImage().getHeight();
	    
	    float boardWidth = mapConfig.csv[0].length * SQRT_3 + SQRT_3/2;
	    float boardHeight = 3 * (mapConfig.csv.length/2) + (mapConfig.csv.length % 2) * 2;
	    
	    TerrainQuad terrain = new TerrainQuad("terrain", PATCH_SIZE, imageWidth+1, heightmap.getHeightMap());
	    terrain.setMaterial(terrainMaterial);

	    terrain.setLocalScale (boardWidth/imageWidth, 0.01f, boardHeight/imageHeight);
	    terrain.setLocalTranslation(-boardWidth/2 + SQRT_3, -.1f, -boardHeight/2 + 1f);
	   
	    this.attachChild(terrain);
	}
	
	/**
	 * Adds one single spot which represent an raising height within the
	 * terrain.<br/>
	 * This is required for the heightmap and the alphamap with the exakt same
	 * value but different colors.
	 * 
	 * @param heightMapImage Image which holds the grayscale heightmap image.
	 * @param alphaMapImage Image which holds the colored alphamap for the textures.
	 * @param radius Radius on the raising.
	 * @param i Horizontal logical index.
	 * @param j Vertical logical index.
	 * @param location The location of the raising area.
	 */
	private void addHeight(Graphics2D heightMapImage, Graphics2D alphaMapImage, float radius, int i, int j, int location) {
		float[] dist = {0.0f, 1.0f};
		double alpha = Math.random() / 2 + 0.1;
		Color[] colors = {new Color(1f, 1f, 1f, (float) alpha), new Color (1f, 1f, 1f, 0f)};
		Color[] colorsTextureMapHills = {new Color(0f, 1f, 0f, (float) alpha), new Color (0f, 1f, 0f, 0f)};
		
		float y = ((float) imageHeight) / mapConfig.height * (mapConfig.height - j -1);
		float x = (mapConfig.width - i -1) * radius;
		if (j%2 == 1)
			x += radius/2;
		
		if (location == LEFT) {
			x -= radius/2;
		}
		if (location == TOPLEFT){
			x -= radius/4;
			y -= ((float) imageHeight) / mapConfig.height * 0.5f;
		}
		if (location == TOPRIGHT){
			x += radius/4;
			y -= ((float) imageHeight) / mapConfig.height * 0.5f;	
		}
		
		Point2D center = new Point2D.Float(x+radius/2, y+radius/2);
		RadialGradientPaint paint = new RadialGradientPaint(center, radius/2, dist, colors);
		heightMapImage.setPaint(paint);
		heightMapImage.fillOval((int) (x-radius), (int) (y-radius), (int) radius*2, (int) radius*2);
		
		RadialGradientPaint paintTextureMapHills = new RadialGradientPaint(center, radius/2, dist, colorsTextureMapHills);
		alphaMapImage.setPaint(paintTextureMapHills);
		alphaMapImage.fillOval((int) (x-radius), (int) (y-radius), (int) radius*2, (int) radius*2);
		alphaMapImage.setBackground(Color.red);
	}
	
	/**
	 * Generate the images which for the heightmap and the alphamap of the terrain.
	 * @param imageWidth
	 * @param imageHeight
	 */
	public void createMapImages(int imageWidth, int  imageHeight) throws IOException {
		int width = mapConfig.width;
		int height = mapConfig.height;
		
		this.imageWidth = imageWidth;
		this.imageHeight = imageWidth;

		float radius = ((float) imageWidth) / (width + 0.5f);

		BufferedImage heightMapImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		BufferedImage alphaMapImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D heightMapGraphic = (Graphics2D) heightMapImage.getGraphics();		
		Graphics2D alphaMapGraphic = (Graphics2D) alphaMapImage.getGraphics();
		
		alphaMapGraphic.setPaint (Color.RED);
		alphaMapGraphic.setBackground(Color.RED);
		alphaMapGraphic.fillRect ( 0, 0, alphaMapImage.getWidth(), alphaMapImage.getHeight() );
		
		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		heightMapGraphic.setComposite(composite);
		
		for (int j=0; j<height; ++j){
			for (int i=0; i<width; ++i) {
				if (! mapConfig.isUsable(i, j)) {
					addHeight (heightMapGraphic, alphaMapGraphic, radius, i, j, CENTER);
					int [][]offsets = (j%2 == 0)? evenOffsets : oddOffsets;
					for (int l=TOPLEFT; l <= LEFT; ++l)
						if (! mapConfig.isUsable(i+offsets[l][0], j+offsets[l][1]))
							addHeight (heightMapGraphic, alphaMapGraphic, radius, i, j, l);
				}
			}
		}
		heightMapImage = ImageHelper.getBlurredImage(heightMapImage);
		ImageIO.write(heightMapImage, "png", new File(MAPS_IMAGE_FOLDER + mapConfig.getMapName() + ".png"));
		ImageIO.write(alphaMapImage, "png", new File(MAPS_IMAGE_FOLDER + ALPHA_MAP_FILE_NAME +".png"));		
	}	

}
