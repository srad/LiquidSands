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
import de.frankfurt.uni.vcp.enums.SelectionMode;
import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.nodes.ActionOptions;

public class FightIcon extends Node implements Clickable {

	public FightIcon() throws Exception {
		Spatial marker = SpatialFactory.create(SpatialTypes.MARKER_FIGHT);
		marker.setLocalScale(2f);
		this.attachChild(marker);
		this.addControl(new BillboardControl());
	}

	@Override
	public void onClick(String name) throws IOException, StatusError,
			ProtocolError {
		Game.getInstance().setSelectionMode(SelectionMode.FIGHT);
		ActionOptions ao=(ActionOptions)this.getParent();
		ao.triggerFight();
	}
}
