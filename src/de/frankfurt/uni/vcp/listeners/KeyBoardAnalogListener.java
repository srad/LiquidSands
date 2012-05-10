package de.frankfurt.uni.vcp.listeners;

import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;

import de.frankfurt.uni.vcp.game.Game;

/**
 * <h3>  This is the analog listener used for camera movement </h3>
 * 
 * <p> To enable the player to view different portions of the game
 *     map, using the arrow keys will move the camera in the indicated
 *     direction.
 *      </p>
 * 
 * @author wladimir + saman + bernd
 *
 */
public class KeyBoardAnalogListener implements AnalogListener{
	
	final static float CAMERA_STEP = 0.5f;

	@Override
	public void onAnalog(String name, float value, float tpf) {
		
		Vector3f cameraLocation = Game.getInstance().getCamera().getLocation();
		Vector3f newCameraLocation;
		
		if (name.equals("left")) {
			newCameraLocation = cameraLocation.subtract(CAMERA_STEP, 0, 0);
		}
		else if (name.equals("right")) {
			newCameraLocation = cameraLocation.add(CAMERA_STEP, 0, 0);
		}
		else if (name.equals("up")) {
			newCameraLocation = cameraLocation.subtract(0, 0, CAMERA_STEP);
		}
		else if (name.equals("down")) {
			newCameraLocation = cameraLocation.add(0, 0, CAMERA_STEP);
		}
		else {
			return;
		}
		if (newCameraLocation != null ) {
			Game.getInstance().getCamera().setLocation(newCameraLocation);
		}
	}

}
