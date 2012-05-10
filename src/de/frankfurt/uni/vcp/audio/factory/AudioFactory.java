package de.frankfurt.uni.vcp.audio.factory;

import com.jme3.audio.AudioNode;

import de.frankfurt.uni.vcp.audio.Sounds;
import de.frankfurt.uni.vcp.audio.enums.SoundType;
import de.frankfurt.uni.vcp.game.Game;

/**
 * Factory class EXCLUSIVELY used by {@link Sounds} for creating all kind of sounds and music.
 * Anybody who wants to create any audio should instantiate {@link Sounds} object.
 *
 */
public class AudioFactory {

	/**
	 * Factory main method.
	 * @param soundType {@link SoundType} of returned {@link AudioNode}.
	 * @return {@link AudioNode}
	 */
	public static AudioNode create(SoundType soundType) {
		String audioFilePath = "";

		switch (soundType) {
			case MUSIC:
				audioFilePath = "music.ogg";
				break;
			case BUTTON_CLICK_WIND:
				audioFilePath = "short_whoosh.wav";
				break;
			case PAGE_TURN:
				audioFilePath = "page-flip-8.wav";
				break;
			case EXPLOSION:
				audioFilePath = "explosion.ogg";
				break;
			default:
				return null;
		}
		return new AudioNode(Game.getInstance().getAssetManager(), audioFilePath, false);
	}

}
