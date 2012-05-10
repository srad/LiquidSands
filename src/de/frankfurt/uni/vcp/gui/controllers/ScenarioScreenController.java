package de.frankfurt.uni.vcp.gui.controllers;

/**
 * <h3>Scenario screen controller.</h3>
 * <p>Doesn't do much yet.</p>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class ScenarioScreenController extends AbstractScreenController {
	
	/**
	 * Moves to start screen.
	 */
	public void leaveScenario() {
		pageTurnSound.play();
		nifty.gotoScreen("start");
	}

}
