package de.frankfurt.uni.vcp;

import java.io.IOException;

import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;


/**
 * <h3>  This is the public interface implemented by all clickable objects in the game </h3>
 * 
 * <p> Once clicked upon any object implementing the {@link Clickable} interface will call
 *     their {@code onClick} handler. </p>
 *     
 * <p> Depending on the {@code name} of the action that toggled this handler different actions can
 *     be taken. </p>
 * 
 * @author wladimir + saman + bernd
 *
 */
public interface Clickable {
  
  /**
   * The method to be called, when the user clicks on the action 
   * 
   * @param name name of the action, that was triggered by the mous-click
   * @throws IOException
   * @throws StatusError
   * @throws ProtocolError
   */
	public void onClick (String name) throws IOException, StatusError, ProtocolError;
}
