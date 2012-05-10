package de.frankfurt.uni.vcp.gui.controllers;

import java.io.IOException;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.system.AppSettings;

import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.screen.Screen;

/**
 * <h3>Settings screen controller.</h3>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class SettingsScreenController extends AbstractScreenController {

	/** List items for resolution drop down box */
	private DropDown<String> drowDownItemsResolutions;

	/** List items for the anti aliasing drop down box */
	private DropDown<String> dropDownItemsAntiAliasing;

	/** Holds the {@link AppSettings} object. */
	private AppSettings settings;

	/** Check box to toggle full screen mode. */
	private CheckBox checkBoxFullScreen;

	/** Check box to toggle vsync. */
	private CheckBox checkBoxVSync;
	
	/** Determine if the music shall be player. */
	//private CheckBox checkBoxMusic;

	@SuppressWarnings("unchecked")
	@Override
	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen);
		
		drowDownItemsResolutions = screen.findNiftyControl("resolution_dropDown", DropDown.class);
		drowDownItemsResolutions.addItem("800x600");
		drowDownItemsResolutions.addItem("1024x600");
		drowDownItemsResolutions.addItem("1024x768");
		drowDownItemsResolutions.addItem("1366x768");
		drowDownItemsResolutions.addItem("1280x1024");

		AppSettings as = game.getContext().getSettings();
		String res = as.getWidth() + "x" + as.getHeight();

		if (!drowDownItemsResolutions.getItems().contains(res)) {
			drowDownItemsResolutions.addItem(res);
		}
		drowDownItemsResolutions.selectItem(res);

		checkBoxFullScreen = this.screen.findNiftyControl("fullscreen_checkBox", CheckBox.class);
		
		if (as.isFullscreen()) {
			checkBoxFullScreen.check();
		}

		//checkBoxMusic = this.screen.findNiftyControl("mute_music_checkbox", CheckBox.class);
		checkBoxVSync = this.screen.findNiftyControl("vsync_checkBox", CheckBox.class);

		if (as.isVSync()) {
			checkBoxVSync.check();
		}

		dropDownItemsAntiAliasing = this.screen.findNiftyControl("antialias_dropDown", DropDown.class);

		//if (game.getContext().getRenderer().getCaps().contains(Caps.FrameBufferMultisample)) {
			dropDownItemsAntiAliasing.addItem("off");
			dropDownItemsAntiAliasing.addItem("2x");
			dropDownItemsAntiAliasing.addItem("4x");
			dropDownItemsAntiAliasing.addItem("8x");
			dropDownItemsAntiAliasing.selectItem("");
		//}
	}
	
    public void initialize(AppStateManager stateManager, Application app) {
    	super.initialize(stateManager, app);
    	this.game = (Game) app;
    }

	/**
	 * Moves to the start screen.
	 */
	public void leaveSettings() {
		pageTurnSound.play();
		nifty.gotoScreen("start");
	}

	/**
	 * Applies the users settings.
	 */
	public void applySettings() {
		screen.findNiftyControl("resolution_dropDown", DropDown.class);

		// Resolution extraction.
		String res = drowDownItemsResolutions.getSelection();
		String[] ress = res.split("x");		
		int x = Integer.parseInt(ress[0]);
		int y = Integer.parseInt(ress[1]);
		
		this.settings = new AppSettings(true);
		this.settings.setFrameRate(30);
		this.settings.setResolution(x, y);
		this.settings.setFullscreen(checkBoxFullScreen.isChecked());
		this.settings.setVSync(checkBoxVSync.isChecked());
		
		game.setSettings(settings);
		game.restart();
	}
	
	@Override
	public void onStartScreen() {
		super.onStartScreen();

		try {
			setMusicVolume(game.getGameSettings().getMusicVolume());
			setMuteMusic(!game.getGameSettings().isMusicEnabled());
			if (!game.getGameSettings().isMusicEnabled()) {
				game.getGameMusic().stop();
			}
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}

	@NiftyEventSubscriber(id = "mute_music_checkbox")
	public void onMuteMusic(String id, CheckBoxStateChangedEvent event) throws IOException, ClassNotFoundException {
    	if (event.isChecked()) {
    		game.getGameMusic().stop();
		}
    	else if (!event.isChecked()) {
    		game.getGameMusic().play();
    	}
    	game.getGameSettings().setMusicEnabled(!event.isChecked());
	}
	
	@NiftyEventSubscriber(id = "musicVolumeSettings")
	public void onChangeMusicSlider(final String id, SliderChangedEvent event) throws IOException, ClassNotFoundException {
		game.getGameMusic().setVolume(event.getValue());
		game.getGameSettings().setMusicVolume(event.getValue());
	}
	
    public void setMusicVolume(float volume) {
    	screen.findNiftyControl("musicVolumeSettings", Slider.class).setValue(volume);
    }
    
    public void setMuteMusic(boolean isMuted) {
    	screen.findNiftyControl("mute_music_checkbox", CheckBox.class).setChecked(isMuted);
    }

}
