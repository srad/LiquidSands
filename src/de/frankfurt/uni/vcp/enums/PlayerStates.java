package de.frankfurt.uni.vcp.enums;

/**
 * <h3>These states reflect the current player state.</h3>
 * 
 * <p>The player can switch into certain states within the game which could be for example a TRADE oder a MOVE action.</p>

 * <p>Until a player has not finished an action these actions need to be tracked.</p>
 *  
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public enum PlayerStates {
	START, MOVE, REACHED, TRADE, FIGHT, END, AUTOMATIC_MOVE, MANUAL_MOVE
}
