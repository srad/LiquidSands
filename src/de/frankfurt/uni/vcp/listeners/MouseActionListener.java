package de.frankfurt.uni.vcp.listeners;

import com.jme3.input.controls.ActionListener;
import de.frankfurt.uni.vcp.Clickable;
import de.frankfurt.uni.vcp.game.Game;

/**
 * <h3>  This is the mouse listener of the game </h3>
 * 
 * <p> After a mouse-click all objects implementing the {@link Clickable} 
 * interface will be checked if they were hit. </p>
 *
 * <p> If any hit was detected the {@code OnClick} handler of the object
 *     being hit will be called </p>
 * @author wladimir + saman + bernd
 *
 */
public class MouseActionListener implements ActionListener {

	
	@Override
	public void onAction(String name, boolean keyPressed, float tpf) {
		if(keyPressed)
			return;
		Clickable result = Game.getInstance().pick();
		
		if (result != null)
      try {
        result.onClick(name);
      }
      catch (Exception e) {
        Game.getInstance ().getHud ().errorMessage(e.getMessage ());
        e.printStackTrace ();
      }
	}
}
