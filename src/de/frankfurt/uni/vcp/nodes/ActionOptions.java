package de.frankfurt.uni.vcp.nodes;

import static com.jme3.math.FastMath.sin;
import static com.jme3.math.FastMath.cos;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;

import de.frankfurt.uni.vcp.enums.PlayerStates;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.nodes.markers.FightIcon;
import de.frankfurt.uni.vcp.nodes.markers.MoveIcon;
import de.frankfurt.uni.vcp.nodes.markers.TradeIcon;

/**
 * Provides the buttons that surround the player units to enble the available actions. 
 */
public class ActionOptions extends Node {

	private static final float PIm2d3 = FastMath.PI * 2f / 3f;

	private final Vector3f posFightMarker = new Vector3f(sin(PIm2d3) * 0.9f, 0f, cos(PIm2d3) * 0.9f);
	private final Vector3f posTradeMarker = new Vector3f(sin(PIm2d3 * 2) * 0.9f, 0f, cos(PIm2d3 * 2) * 0.9f);
	private final Vector3f posMoveMarker = new Vector3f(sin(PIm2d3 * 3) * 0.9f, 0f, cos(PIm2d3 * 3) * 0.9f);
	private final Vector3f centerTop = new Vector3f(0f, 0.8f, 0f);
	private final Vector3f centerBottom = new Vector3f(0f, -0.5f, 0f);

	/** Fight icon position */
	private Vector3f targetF = new Vector3f();
	
	/** Trade icon position */
	private Vector3f targetT = new Vector3f();
	
	/** Move icon position */
	private Vector3f targetM = new Vector3f();

	/** Scale of the fight button */
	private float scaleTargetF;
	
	/** Scale of the trade button */
	private float scaleTargetT;
	
	/** Scale of the move button */
	private float scaleTargetM;

	/** Fight button node */
	public FightIcon fightMarker;
	
	/** Trade button node */
	public TradeIcon tradeMarker;
	
	/** Move button node */
	public MoveIcon moveMarker;

	public boolean animDone = true;

	/**
	 * 
	 */
	public ActionOptions() {
		this.setName("options");
		try {
			fightMarker = new FightIcon();
			tradeMarker = new TradeIcon();
			moveMarker = new MoveIcon();
		} catch (Exception e) {
			LogHelper.getLogger().error("marker creation failed: " + e.getCause());
		}

		initMarker();

		setShadowMode(ShadowMode.Off);
		this.attachChild(fightMarker);
		this.attachChild(moveMarker);
		this.attachChild(tradeMarker);
		this.setLocalTranslation(0f, 3f, 0f);
	}

	/**
	 * This method animates the various icon movements. First the position of an
	 * icon is being set a bit towards the target position depending on the tpf.
	 * Then the icon is scaled a bit towards the target scale. The method is
	 * being called repeatedly from the games simpleUpdate until the target
	 * position is reached.
	 * 
	 * @param tpf
	 *            time per frame
	 */
	public void animateAction(float tpf) {
		float step = tpf * 2f;

		if (fightMarker.getLocalTranslation().distance(targetF) < 0.1f) {
			animDone = true;
			return;
		}

		fightMarker.move(targetF.subtract(fightMarker.getLocalTranslation()).mult(step));
		tradeMarker.move(targetT.subtract(tradeMarker.getLocalTranslation()).mult(step));
		moveMarker.move(targetM.subtract(moveMarker.getLocalTranslation()).mult(step));

		fightMarker.setLocalScale(scaleTargetF);
		tradeMarker.setLocalScale(scaleTargetT);
		moveMarker.setLocalScale(scaleTargetM);
	}

	/**
	 * This method initializes the position and scale of all icons.
	 */
	public void initMarker() {
		fightMarker.setLocalTranslation(centerBottom);
		tradeMarker.setLocalTranslation(centerBottom);
		moveMarker.setLocalTranslation(centerBottom);

		tradeMarker.setLocalScale(0.0f);
		fightMarker.setLocalScale(0.0f);
		moveMarker.setLocalScale(0.0f);
	}

	/**
	 * This method moves the fight icon to the top center of the unit marker and
	 * hides all the other. This happens to indicate fight.
	 */
	public void triggerFight() {
		targetF = centerTop;
		targetT = centerBottom;
		targetM = centerBottom;
		scaleTargetF = 1f;
		scaleTargetT = scaleTargetM = 0f;
		animDone = false;
	}

	/**
	 * This method moves the trade icon to the top center of the unit marker and
	 * hides all the other. This happens to indicate trade.
	 */
	public void triggerTrade() {
		targetF = centerBottom;
		targetT = centerTop;
		targetM = centerBottom;
		scaleTargetF = scaleTargetM = 0f;
		scaleTargetT = 1f;
		animDone = false;
	}

	/**
	 * This method moves the move icon to the top center of the unit marker and
	 * hides all the other. This happens to indicate movement.
	 */
	public void triggerMove() {
		targetF = centerBottom;
		targetT = centerBottom;
		targetM = centerTop;
		scaleTargetF = scaleTargetT = 0f;
		scaleTargetM = 1f;
		animDone = false;
	}

	/**
	 * This method shows all the options by moving them to their specified
	 * positions and scaling them to factor of one.
	 */
	public void unfoldOptions(PlayerStates ps) {
		if (ps.equals(PlayerStates.START)) {
			targetF = posFightMarker;
			targetT = posTradeMarker;
			targetM = posMoveMarker;
			scaleTargetF = scaleTargetT = scaleTargetM = 1f;
		} else if (ps.equals(PlayerStates.REACHED)) {
			targetF = posFightMarker;
			targetT = posTradeMarker;
			targetM = centerBottom;
			scaleTargetF = scaleTargetT = 1f;
			scaleTargetM = 0f;
		}

		animDone = false;
	}

	/**
	 * This method hides all the action options by moving them to the center and
	 * scaling them to factor of zero.
	 */
	public void foldOptions() {
		targetF = centerBottom;
		targetT = centerBottom;
		targetM = centerBottom;
		scaleTargetF = scaleTargetM = scaleTargetT = 0f;
		animDone = false;
	}
}
