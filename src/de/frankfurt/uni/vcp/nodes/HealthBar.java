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

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

import de.frankfurt.uni.vcp.enums.MaterialTypes;
import de.frankfurt.uni.vcp.factories.MaterialFactory;

/**
 * <h3>  This is class used to display a units healthbar </h3>
 * 
 * <p> Every unit in the game will have a certain ammount of hitpoints
 *     associated with it. </p>
 *     
 * <p> To display the current value of a units hitpoints an instance of
 *     this class will be placed above the corresponding unit. </p>
 * 
 * <p> All values will be displayed as percentage, showing how much
 *     of a units maximum hitpoints are left. </p>
 *      
 * @author wladimir + saman + bernd
 *
 */
public class HealthBar extends Node {

	public Geometry bar;
	
	/**
	 * Create a new healthbar;
	 * @throws Exception
	 */
	public HealthBar () throws Exception {
			Node node = new Node ();
			node.move(- 64f * 2f / 128f, -0.2f, 0f);
			node.scale (2f/128f);
			
			this.attachChild (node);
			
			// BACKGROUND
			Quad background = new Quad (128, 32);
			
			Geometry g = new Geometry ("healtBarBackground", background);
			g.setMaterial(MaterialFactory.create(MaterialTypes.HEALTHBAR_BACKGROUND));
			g.setQueueBucket(Bucket.Translucent);
			node.attachChild(g);
			
			// BAR
			Quad q = new Quad (116, 11);
			
			bar = new Geometry ("healtBar", q);
			bar.setLocalTranslation (0.1f, .1f, 0.01f);
			bar.setMaterial(MaterialFactory.create(MaterialTypes.HEALTHBAR_FOREGROUND));
			bar.setLocalTranslation(6f, 11f, 0.1f);
			node.attachChild(bar);
			
			this.addControl(new BillboardControl());
	}
	
	/**
	 * Set the percentage displayed in this healthbar to a specified value
	 * @param p The percentage to be displayed in this healthbar
	 */
	public void setPercentage (float p) {
		bar.setLocalScale(p, 1f, 1f);
	}
	
	/**
	 * Don't interfere with any picking taking place
	 */
	@Override
	public int collideWith(Collidable arg0, CollisionResults arg1) {
		return 0;
	}
	
}
