package de.frankfurt.uni.vcp.gui.controllers;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

import de.frankfurt.uni.vcp.audio.Sounds;
import de.frankfurt.uni.vcp.audio.enums.SoundType;
import de.frankfurt.uni.vcp.game.Game;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * <h3>Common abstract super class for all nifty screen controllers.</h3>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public abstract class AbstractScreenController extends AbstractAppState implements ScreenController {
	
	/** Common nifty reference */
	public static Nifty nifty;
	
	/** Common screen reference */
	protected Screen screen;
	
	/** General game reference we need */
	public Game game;
	
	protected Sounds pageTurnSound;
	protected Sounds buttonClickWind;
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		game = (Game) app;
		pageTurnSound = new Sounds(game.getRootNode(), SoundType.PAGE_TURN);
		buttonClickWind = new Sounds(game.getRootNode(), SoundType.BUTTON_CLICK_WIND);
	}
	
	/**
	 * Toggles the visibility on an element.
	 * @param elementId the element id.
	 */
	public void toggleVisibility(String elementId) {
		Element e = screen.findElementByName(elementId);

		if (e.isVisible()) {
			e.hide();
		} else {
			e.show();
		}
	}
	
	/**
	 * Shows an element.
	 * @param elementId Id of the element
	 */
	public void show(String elementId) {
		Element e = findElement(elementId);
		if (e != null) {
			e.show();
		}
	}

	/**
	 * Hides an element
	 * @param elementId
	 */
	public void hide(String elementId) {
		Element e = findElement(elementId);
		if (e != null) {
			e.hide();
		}
	}
	
	/**
	 * Helper method to get a certain element.
	 * @param elementId Id of the element.
	 * @return Reference to the element. Not null if element exists.
	 */
	public Element findElement(String elementId) {
		return screen.findElementByName(elementId);
	}

	public void bind(Nifty nifty, Screen screen) {
		AbstractScreenController.nifty = nifty;
		this.screen = screen;
	}
	
	public void disable(String elementId) {
		findElement(elementId).disable();
	}
	
	/**
	 * Enables an element.
	 * @param elementId The element's id.
	 */
	public void enableElement(String elementId) {
		findElement(elementId).enable();
	}
	
	@Override
	public void onEndScreen() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartScreen() {
		nifty.resolutionChanged();
	}

	/**
	 * This is the main screen which the game shows at start.
	 */
	public void gotoStartScreen() {
		gotoScreen("start");
	}
	
	/**
	 * Moves to the specified screen and plays a sound.
	 * @param screenId Id of the screen attribute.
	 */
	public void gotoScreen(String screenId) {
		pageTurnSound.play();
		nifty.gotoScreen(screenId);
	}
	
}
