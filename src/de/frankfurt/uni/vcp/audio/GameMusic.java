package de.frankfurt.uni.vcp.audio;

import de.frankfurt.uni.vcp.audio.enums.SoundType;
import de.frankfurt.uni.vcp.game.Game;

/**
 * Handles the game music.
 */
public class GameMusic extends Sounds {

	/** Determine internally if the music is currently playing. */
	private Boolean isPlaying = false;

	public GameMusic() {
		super(Game.getInstance().getRootNode(), SoundType.MUSIC);
		setLoopMode(true);
	}

	/** Stops the music if it is playing. */
	public void stop() {
		if (this.isPlaying) {
			this.isPlaying = false;
			super.stop();
		}
		else {
			this.play();
		}
	}
	
	/** If the music is playing it will stop. */
	@Override
	public void play() {
		if (!this.isPlaying) {
			super.play();
			this.isPlaying = true;
		}
	}

}
