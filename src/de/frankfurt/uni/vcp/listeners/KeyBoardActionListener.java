package de.frankfurt.uni.vcp.listeners;

import com.jme3.input.controls.ActionListener;

import de.frankfurt.uni.vcp.enums.PlayerStates;
import de.frankfurt.uni.vcp.enums.SelectionMode;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.nodes.movables.Movable;

/**
 * <h3>This is the keyboard listener of the game</h3>
 * 
 * <p>
 * Depending on the key hit by the player the action associated to the pressed
 * key will be taken.
 * </p>
 * 
 * @author wladimir + saman + bernd
 * 
 */
public class KeyBoardActionListener implements ActionListener {

	@Override
	public void onAction(String name, boolean keyPressed, float tpf) {
		Game game = Game.getInstance();
		Movable selectedUnit = game.getSelectedUnit();
		Movable activeUnit = game.getActiveUnit();

		if (name.equals("shift")) {
			boolean isShiftStillPressed = keyPressed;
			game.getFlyByCamera().setEnabled(isShiftStillPressed);
			game.getInputManager().setCursorVisible(!isShiftStillPressed);
		}
		// UNDO
		if (name.equals("undo") && !keyPressed) {
			selectedUnit.undo();
		}
		// REDO
		if (name.equals("redo") && !keyPressed) {
			selectedUnit.redo();
		}
		// FIGHT
		if (name.equals("fight") && !keyPressed && (game.getSelectedUnit() != null)) {
			game.setSelectionMode(SelectionMode.FIGHT);
		}
		// TRADE
		if (name.equals("trade") && !keyPressed && (game.getSelectedUnit() != null)) {
			game.setSelectionMode(SelectionMode.TRADE);
		}

		// PASS
		if (name.equals("pass") && !keyPressed) {
			if (activeUnit != null && activeUnit != selectedUnit)
				return;

			if (selectedUnit != null) {
				activeUnit = selectedUnit;
				selectedUnit.setState(PlayerStates.END);
			}
		}

		// NEXT
		if (name.equals("next")) {
			if (game.getActiveUnit() == null)
				return;

			LogHelper.getLogger().info("Activating NextPlayer");

			try {
				game.nextPlayer();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (name.equals("automate")) {
			if (selectedUnit != null) {
				selectedUnit.automate = !selectedUnit.automate;
			}
		}

	}

}
