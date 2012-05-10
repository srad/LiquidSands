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

package de.frankfurt.uni.vcp.nodes.markers;

import java.io.IOException;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;

import de.frankfurt.uni.vcp.Clickable;
import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.nodes.ActionOptions;

public class MoveIcon extends Node implements Clickable {
	/**
	 * The move icon is being displayed over a selected unit. It triggers the
	 * movement of the unit when beeing clicked or indicates the state of
	 * movement.
	 * 
	 * @throws Exception
	 */
	public MoveIcon() throws Exception {
		Spatial marker = SpatialFactory.create(SpatialTypes.MARKER_MOVE);
		marker.setLocalScale(0.8f);
		this.attachChild(marker);
		this.addControl(new BillboardControl());
	}

	/**
	 * When the move icon has been clicked, this method is called. This handler
	 * calls the move method of the selected unit and triggers the move icons
	 * animation, which moves to the top center of the unit marker(oktahedron).
	 */
	@Override
	public void onClick(String name) throws IOException, StatusError,
			ProtocolError {
		Game.getInstance().getSelectedUnit().move();
		ActionOptions ao = (ActionOptions) this.getParent();
		ao.triggerMove();
	}

}