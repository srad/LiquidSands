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
 * <p> An Exception of type  {@link StatusError} will be thrown, whenever the 
 *     client receives a package, that according to the protocol specification
 *     reports an error. 
 * </p>
 * 
 */
public class StatusError extends Exception {
	
	private static final long serialVersionUID = 1L;	
	
	/**
	 * The informational details, that were sent along with this error.
	 */
	String errinfo;
	public String fullMessage;
	
	StatusError (String info, String full) {
		errinfo = info;
		fullMessage  = full;
	}
	
	@Override
	public String getMessage (){
		return errinfo;
	}
}
