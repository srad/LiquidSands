/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.frankfurt.uni.vcp.net;

/**
 * <h3> This class is used for networking Exception handling </h3>
 *
 * <p> An Exception of type  {@link ProtocolError} will be thrown, whenever the 
 *     client receives a package, that doesn't seem to conform to the game protocol
 *     specification.
 * </p>
 */
public class ProtocolError extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The request, that was sent to the server, prior to receiving the malformed response
	 */
	String request;
	
	/**
	 * The reply, as it was received from the server.
	 */
	String reply;
	
	ProtocolError (String request, String reply) {
		this.request = request;
		this.reply = reply;
	}
}
