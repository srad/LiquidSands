package de.frankfurt.uni.vcp.game;

import java.io.Serializable;

import com.jme3.input.KeyInput;

/**
 * Stores the game settings which will be restores on the next game start.
 */
public class GameSettings implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Game server uri */
	private String gameServer = "localhost";
	
	/** The players name */
	private String playerName = "";

	/** Volumes */
	private float soundVolume = 1, musicVolume = 1;

	/** Is sound is activated */
	private boolean soundEnabled = true, musicEnabled = true;

	/** GPU vsync option by default disabled */
	private boolean enableVSync = false;

	/** 1024x768 is the default resolution */
	private int hResolution = 800, vResolution = 600;

	/** Default in window mode */
	private boolean fullScreen = false;

	/** Key mappings, default is WASD for moving. */
	private int upKey = KeyInput.KEY_W, downKey = KeyInput.KEY_S,
			leftKey = KeyInput.KEY_A, rightKey = KeyInput.KEY_D;

	public String getGameServer() {
		return gameServer;
	}

	public void setGameServer(String gameServer) {
		this.gameServer = gameServer;
	}

	public float getSoundVolume() {
		return soundVolume;
	}

	public void setSoundVolume(int soundVolume) {
		this.soundVolume = soundVolume;
	}

	public float getMusicVolume() {
		return musicVolume;
	}

	public void setMusicVolume(float volume) {
		this.musicVolume = volume;
	}

	public boolean isSoundEnabled() {
		return soundEnabled;
	}

	public void setSoundEnabled(boolean soundEnabled) {
		this.soundEnabled = soundEnabled;
	}

	public boolean isMusicEnabled() {
		return musicEnabled;
	}

	public void setMusicEnabled(boolean musicEnabled) {
		this.musicEnabled = musicEnabled;
	}

	public boolean isEnableVSync() {
		return enableVSync;
	}

	public void setEnableVSync(boolean enableVSync) {
		this.enableVSync = enableVSync;
	}

	public int gethResolution() {
		return hResolution;
	}

	public void sethResolution(int hResolution) {
		this.hResolution = hResolution;
	}

	public int getvResolution() {
		return vResolution;
	}

	public void setvResolution(int vResolution) {
		this.vResolution = vResolution;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public int getUpKey() {
		return upKey;
	}

	public void setUpKey(int upKey) {
		this.upKey = upKey;
	}

	public int getDownKey() {
		return downKey;
	}

	public void setDownKey(int downKey) {
		this.downKey = downKey;
	}

	public int getLeftKey() {
		return leftKey;
	}

	public void setLeftKey(int leftKey) {
		this.leftKey = leftKey;
	}

	public int getRightKey() {
		return rightKey;
	}

	public void setRightKey(int rightKey) {
		this.rightKey = rightKey;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

}
