package de.frankfurt.uni.vcp.statistics;

import java.io.Serializable;

/**
 * Is embedded in {@link Statistics} and represents the individual statistics
 * for each player or unit.
 */
public class SingleStatistic implements Serializable {

	private static final long serialVersionUID = 2485737918489955168L;

	/** Numbers of attacks */
	private int attackCount = 0;

	/** Number of traded objects */
	private int tradedObjectsCount = 0;

	/** Players/Units hit rate */
	private float hitRate = 0.0f;

	/** Number of deaths */
	private int deaths = 0;

	/** Number of wins */
	private int wins = 0;

	/**
	 * Increments the {@linkplain #attackCount} by exactly +1.
	 */
	public void incrementAttacks() {
		attackCount += 1;
	}

	/**
	 * Increments the {@linkplain #attackCount} by a given value.
	 * 
	 * @param value
	 *            Any integer.
	 */
	public void incrementAttacks(int value) {
		attackCount += value;
	}

	/**
	 * Increment the {@linkplain #tradedObjectsCount} by +1.
	 */
	public void incrementTradedObjects() {
		tradedObjectsCount += 1;
	}

	public void incrementTradedObjects(int value) {
		tradedObjectsCount += value;
	}

	public float getHitRate() {
		return hitRate;
	}

	public void setHitRate(float hitRate) {
		this.hitRate = hitRate;
	}

	/**
	 * Increment the deatch count by +1.
	 */
	public void incrementDeaths() {
		deaths += 1;
	}

	public void incrementDeaths(int value) {
		deaths += value;
	}

	/**
	 * Increment the number of wins by +1.
	 */
	public void incrementWins() {
		wins += 1;
	}

	public void incrementWins(int value) {
		wins += value;
	}

	public int getAttackCount() {
		return attackCount;
	}

	public int getTradedObjectsCount() {
		return tradedObjectsCount;
	}

	public int getDeaths() {
		return deaths;
	}

	public int getWins() {
		return wins;
	}

}
