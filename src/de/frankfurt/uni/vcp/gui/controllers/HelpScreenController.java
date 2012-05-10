package de.frankfurt.uni.vcp.gui.controllers;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;

/**
 * Help screen controller, currently empty, who know probably it's needed at some point.
 * Uses methods from super class.
 */
public class HelpScreenController extends AbstractScreenController {

	@Override
	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen);
	}
	
	// TODO: Nifty BUG not invoked from super class.
	@Override
	public void gotoStartScreen() {
		nifty.gotoScreen("start");
	}
	
}
