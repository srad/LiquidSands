package de.frankfurt.uni.vcp.enums;

/**
 *
 * <h3> Enumeration used to differentiate between the follow modes that
 * can be set for an individual unit </h3>
 * 
 * <p> Note that at the time this feature is NOT implemented. </p>
 * 
 * <p> Descriptions given here only represent their inteded use </P>
 *
 * @author wladimir + saman + bernd
 */
public enum FollowMode {
  /**
   * This unit follows another unit.
   * This means it will move to every field the unit it is following
   * already moved on.
   */
	FOLLOW,
  /**
   * This unit is trying to intercept another unit.
   * This means it will move to field the unit it is intercepting
   * is currently standing upon.
   */
	INTERCEPT,
  /**
   * This unit is not in any way following any other unit.
   */
	NO_FOLLOW,
  /**
   * This unit follows another unit.
   * In contrast to {@code FOLLOW} the user will not be bothered to decide
   * every turn if this unit should still follow.
   * Instead the unit will continue following without user interaction.
   */
	AUTOMATIC_FOLLOW
}
