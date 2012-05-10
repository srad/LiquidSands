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

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.nodes.Field;
import de.frankfurt.uni.vcp.units.Inventory;

/**
 * <h3>This class implements the network capabilities required by a game client</h3>
 * 
 * <p>
 * Request und server replies will automatically checked for correctness
 * according to the protocol specification.
 * </p>
 * 
 * <p>
 * Exceptions of type {@link StatusError} and {@link ProtocolError} will be
 * thrown, if malformed packages are detected.
 * </p>
 * 
 */

public class TCPClient {

	/**
	 * The default clientinfo this client will supply
	 */
	public static final String CLIENT_INFO = "LiquidSandsClient_v0.6.6";

	/**
	 * The list of allowed characters in player defined strings
	 */
	public static final String ALLOWED_CHARS = "01234567890" + "" + "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * The clientId of this client
	 */
	String clientId = "";
	String clientInfo = "";

	/**
	 * The clientkey associated with this client by the server
	 */
	String clientKey = "";

	/**
	 * The server address to connect to
	 */
	public String serverAddress;
	/**
	 * The server port to connect to
	 */
	public int port;

	private String gameOwnerKey;

	/**
	 * To ensure charaters entered by players only contains valid characters
	 * every non-valid charater has to be removed from the string entered.
	 * 
	 * Given any input string, this function will return a copy in which all
	 * occurances of non-valid characters have been replaced by the character _
	 * 
	 * @param string
	 *            The string to be transformed
	 * @return The string with all non-valid characters replaced with _
	 */
	public static String sanitize(String string) {
		char[] c = string.toCharArray();

		for (int i = 0; i < c.length; ++i) {
			if (ALLOWED_CHARS.indexOf(c[i]) < 0)
				c[i] = '_';
		}
		return new String(c);
	}

	public String getGameOwnerKey() {
		return gameOwnerKey;
	}

	/**
	 * Split a string containing of components contained in pairs of opening and
	 * closing braces into individual parts with leading and trailing braces
	 * removed.
	 * 
	 * i.e. the string "[a[bc]][de]" will be split into parts "a[bc]" and "de"
	 * 
	 * @param string
	 *            The string to be split
	 * @return A list of the parts enclosed in matching pairs of braces
	 */
	public static List<String> splitBrace(String string) {
		return splitBrace(string, true);
	}

	/**
	 * Split a string containing of components contained in pairs of opening and
	 * closing braces into individual parts either leaving matching pairs of
	 * braces in place or stripping them.
	 * 
	 * @param string
	 *            The string to be split
	 * @param strip
	 *            If set to {@code true} braces will be stripped. Braces will be
	 *            left in place otherwise.
	 * @return A list of the parts enclosed in matching pairs of braces
	 */
	public static List<String> splitBrace(String string, boolean strip) {
		List<String> list = new LinkedList<String>();

		int start = 0;
		int depth = 0;

		for (int i = 0; i < string.length(); ++i) {
			char c = string.charAt(i);

			switch (c) {
			case '[':
				++depth;
				if (depth == 1)
					start = i;
				break;
			case ']':
				--depth;
				if (depth == 0)
					if (strip)
						list.add(string.substring(start + 1, i));
					else
						list.add(string.substring(start, i + 1));
				break;
			}
		}

		return list;
	}

	/**
	 * Search a given string for a key:value pair with each key and value
	 * consisting of the longest sequence of non-whitespace characters in the
	 * string
	 * 
	 * @param string
	 *            The string to search
	 * @param key
	 *            The key to search for
	 * @return The first corresponding value, if it is found, {@code null}
	 *         otherwise.
	 */
	// "<key>:<value>" -> "<value>" | null
	public static String parseValue(String string, String key) {
		String[] parts = string.split(" ");

		for (String part : parts) {
			String[] components = part.split(":");
			if (components.length != 2)
				continue;

			if (components[0].equals(key))
				return components[1];
		}
		return null;
	}

	/**
	 * Search a given string for a key value pair with each key and value
	 * consisting of the longest sequence of non-whitespace characters in the
	 * string using a given character as separator between key and value.
	 * 
	 * @param string
	 *            The string to search
	 * @param key
	 *            The key to search for
	 * @return The first corresponding value, if it is found, {@code null}
	 *         otherwise.
	 */
	// "<key><separator><value>" -> "<value>" | null
	public static String parseValue(String string, String separator, String key) {
		String[] parts = string.split(" ");

		for (String part : parts) {
			String[] components = part.split(separator);
			if (components.length != 2)
				continue;

			if (components[0].equals(key))
				return components[1];
		}
		return null;
	}

	/**
	 * Search a given string for a key value pair with each key and value
	 * consisting of the longest sequence of non-whitespace characters in the
	 * string using a given character as separator between key and value.
	 * 
	 * @param string
	 *            The string to search
	 * @param key
	 *            The key to search for
	 * @return The first corresponding value, if it is found, {@code null}
	 *         otherwise.
	 */
	// { "..." ... "<prefix>..." "..." ... } -> "<prefix>..." | null
	public static String getPart(String[] parts, String prefix) {

		for (String s : parts) {
			if (s.substring(0, prefix.length()).equals(prefix))
				return s;
		}
		return null;
	}

	/**
	 * Parse a 2 dimensional array of int values from a string consisting of a
	 * sequence of row descriptions enclosed in braces. Each row description of
	 * the input string hast to consist of a string representation of the stored
	 * int values enclosed in braces.
	 * 
	 * @param string
	 *            The string to convert
	 * @return The Array consisting of the values extracted from the string
	 */
	public static int[][] parseIntTable(String string) {
		int depth = 0;
		List<int[]> rows = new LinkedList<int[]>();
		List<Integer> row = null;

		for (int pos = 0; pos < string.length(); ++pos) {
			char c = string.charAt(pos);
			switch (c) {
			case '[':
				++depth;
				if (depth == 1)
					row = new ArrayList<Integer>();
				break;
			case ']':
				--depth;
				if (depth == 1 && string.charAt(pos + 1) == ']') {
					int[] rowData = new int[row.size()];
					for (int i = 0; i < row.size(); ++i)
						rowData[i] = row.get(i);
					rows.add(rowData);
				}
				break;
			default:
				int start = pos;
				while (Character.isDigit(string.charAt(pos + 1)))
					++pos;
				row.add(Integer.parseInt(string.substring(start, pos + 1)));
				break;
			}
		}

		int csv[][] = new int[rows.size()][];
		for (int i = 0; i < rows.size(); ++i)
			csv[i] = rows.get(i);

		int width = csv.length;
		int height = csv[0].length;

		int correctCsv[][] = new int[height][];

		for (int i = 0; i < height; ++i)
			correctCsv[i] = new int[width];

		for (int i = 0; i < width; ++i)
			for (int j = 0; j < height; ++j)
				correctCsv[i][j] = csv[j][i];

		return correctCsv;
	}

	/**
	 * Construct a new TCPClient Instance
	 * 
	 * @param serverAddress
	 *            The server address to use
	 * @param port
	 *            The Port to use in network communication
	 * @param clientInfo
	 *            The textstring identifing this client
	 */
	// CONSTRUCTOR
	public TCPClient(String serverAddress, int port, String clientInfo) {
		this.serverAddress = serverAddress;
		this.port = port;

		UUID uuid = UUID.randomUUID();
		this.clientId = uuid.toString();

		this.clientInfo = clientInfo;
	}

	/**
	 * Send a message to the server, and wait for the server reply, checking the
	 * server reply for possible errors.
	 * 
	 * @param message
	 *            The message string to send to the server
	 * @return The reply sent by the server
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// SEND
	String sendMessage(String message) throws IOException, StatusError, ProtocolError {

		Socket socket = new Socket(serverAddress, port);
		DataOutputStream oStream = new DataOutputStream(new DataOutputStream(socket.getOutputStream()));
		DataInputStream iStream = new DataInputStream(new DataInputStream(socket.getInputStream()));

		// REQUEST
		LogHelper.getLogger().debug("WRITE: " + message);
		oStream.writeUTF(message);
		oStream.flush();

		// REPLY
		String reply;
		reply = iStream.readUTF();

		LogHelper.getLogger().debug("READ:  " + reply);

		socket.close();

		// ERROR
		String[] parts = reply.split(" ");
		if (parts[0].equals("status:ok"))
			return reply;
		else if (parts[0].equals("status:error")) {
		    String msg = parts[1].split(":")[1];
		    for (int i=2; i<parts.length; ++i)
		        msg += " " + parts[i];
			throw new StatusError(msg, reply);
		}
		else
			throw new ProtocolError(message, reply);

	}

	// REQUESTS
/*
DATA-FORMAT:
===========

ATOM-LIST :: e | ATOM-LIST-1
ATOM-LIST-1 :: ATOM | ATOM ATOM-LIST-1


VALUE-LIST<ELEMENT> :: e | VALUE | VALUE VALUE-LIST
VALUE :: '[' <ELEMENT> ']'


VALUE-LIST-OR-NIL :: e | '[' VALUE-LIST ']'


MAP-GRID :: '[' VALUE-LIST<VALUE-LIST<INTEGER>>']'



REPLY-FORMAT:
============

status:ok INFO end:end

status:error errinfo:ERRINFO end:end



REQUESTS
========

getlients clientid:CLIENTID
   :  clients[ATOM-LIST<CLIENTID>]

 
logon clientid:CLIENTID clientinfo:ATOM
   :  clientkey:CLIENTKEY

------------

getinfo clientid:CLIENTID clientkey:CLIENTKEY requestclientid:ATOM
   :  clientinfo:ATOM clientlast:ATOM  clienttargets[ATOM-LIST<?>]  clientdataitems[ATOM-LIST<?>]
   !! invalid_clientkey 


getdata clientid:CLIENTID clientkey:CLIENTKEY
   :  data[[incomingtime:1326996658522 senderid:Client0001 data[[system][senderplayerid=Sam1326996639716 playeridreciever=Max1326996640647 message=[Unit 1 has been attacked by unit 6]]]]]
   !! invalid_clientkey 



SENDGAMEDATA 1  (w/o PLAYERID)
--------------
sendgamedata clientid:CLIENTID clientkey:CLIENTKEY [data][ ... ]



   [info] [ ... ]

      infotype=gamelist
         :  gamelist=[VALUE-LIST<GAMEID>]
         !! invalid_clientkey 

      infotype=maplist
         :  maplist=[VALUE-LIST<MAPID>]
         !! invalid_clientkey 


      infotype=mappreview mapid=MAPID
         :  [mappreview][ ... ]
               terrainmappreview=MAP-GRID
               terrainmapinvpreview=MAP-GRID
               unitmapprevieffw=MAP-GRID
               unitmapinvpreview=MAP-GRID
         !! invalid_clientkey 
         !! previewrequest_for_unknown_map
 

      infotype=unittype mapid=MAPID
         :  unittypeinfo=[types[[ATOM-LIST<STRING>]]
              [NAME][maxhitpoints=INTEGER maxfirepower=INTEGER maxcargo=INTEGER maxmovement=INTEGER] ... ]
         -- unittypeinfo=[types[[]] ]  (!! for MAPIDs unknown to the server)


   [option] [ ... ]

      [creatagame] [mapid=MAPID gamename=ATOM gameownerkey=GAMEOWNERKEY]
         :  gameid=GAMEID
         !! invalid_clientkey 
         !! gamename_not_unique
         -- java.io.FileNotFoundException:


      [addplayer] [gameid=GAMEID playername=PLAYERNAME] 
         :  playerid:PLAYERID
         !! invalid_clientkey 
         !! playername_already_exists


      [startgmame] [gameid=GAMEID gameownerkey=GAMEOWNERKEY] 
         :
         !! invalid_clientkey 
         !! unkown_gameid
         !! invalid_ownerkey_to_start_game
         !! not_enough_players_to_start_the_game


      [delplayer] [gameid=GAMEID playerdelid=PLAYERID gameownerkey=GAMEOWNERKEY]
         :
         !! invalid_clientkey 
         !! invalid_playerdelid
         !! invalid_gameownerkey_or_palyerid_to_delplayer         


      [removegame] [gameid=GAMEID gameownerkey=GAMEOWNERKEY]
         :
         !! invalid_clientkey 
         !! gamename_unknown
         !! gameownerkey_not_valid



SENDGAMEDATA 2  (with PLAYERID)
--------------n
sendgamedata clientid:CLIENTID clientkey:CLIENTKEY playerid:PLAYERID [data][ ... ]
------------------------

   [request] [ ... ]
   
      [rtyppe=gameinfo gameid=GAMEID]
         :  [gameinfo][gameid=GAMEID name=ATOM gamestatus=ATOM turn=ATOM winner=PLAYERNAME activeunit=ATOM  activeunitslastaction=??
                      tradepartner=PLAYERNAME tradepartnerunit=ATOM
                      tradegive=VALUE-LIST-OR-NIL<INTEGER> tradeget=VALUE-LIST-OR-NIL<INTEGER> tradestatuslast=ATOM
                      playernames=VALUE-LIST<STRING> turnsTaken=VALUE-LIST<'true'|'false'>
         !! invalid_clientkey
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid


      [rtype=terrainmap gameid=GAMEID]
         :  terrainmap=MAP-GRID terrainmapinv=MAP-GRID
         !! invalid_clientkey 
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid


      [rtype=unitmap gameid=GAMEID]
         :  unitmap=MAP-GRID unitmapinv=MAP-GRID
         !! invalid_clientkey 
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid
 
 
      [rtype=unitinfo unitid=ATOM gameid=GAMEID]
         :  [unitinfo][unitid=UNITID owner=PLAYERNAME utype=ATOM destroyed=ATOM movement=INTEGER
                       lastmovement=[VALUE-LIST<INTEGER, INTEGER>]]
         !! invalid_clientkey 
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid
         !! request_unitinfo_for_unknown_unitid


      [rtype=playerinfo playeridrequest=PLAYERID gameid=GAMEID]
         :  [playerinfo][playerid=PLAYERID playername=ATOM clientid=CLIENTID  ]
         !! invalid_clientkey
         !! PLAYERID_is_not_a_vaild_player
 

   [action] [ ... ]
 
      [move] [unitid=ATIOM path=[VALUE-LIST<INT,INT>] gameid=GAMEID ]
         :
         !! invalid_clientkey          
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid
         !! playerid_is_valid_but_only_active_player_or_trade_partner_can_make_action_command
         !! unit_is_not_active_unit
         !! illegal_move_from_INTEGERxINTEGER_to_INTEGERxINTEGER
         -- java.lang.ArrayIndexOutOfBoundsException: -1  !! bei ungültiger unitid !!
         
         
      [trade] [gameid=GAMEID tradeofferunit=ATOM tradepartnerunit=ATOM givegoods=[ATOM-LIST<INTEGER>] getgoods=[ATOM-LIST<INTEGER>]]
         :
         !! invalid_clientkey          
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid
         !! playerid_is_valid_but_only_active_player_or_trade_partner_can_make_action_command
         !! wrong_user_PLAYERID_for_unit_UNITID
         !! amount_of_transfered_goods_in_trade_not_possible
         -- java.lang.ArrayIndexOutOfBoundsException: -1
         
      [attack] [attackerid=ATOM defenderid=ATOM gameid=GAMEID]
         :  damage:ATOM
         !! invalid_clientkey
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid
         !! PLAYERID_is_not_a_vaild_player
         !! playerid_is_valid_but_only_active_player_or_trade_partner_can_make_action_command
         !! wrong_user_PLAYERID_for_unit_UNITID
         !! unit_UNITID_trys_attacking_non_neightbouring_unit_UNITID
         -- java.lang.ArrayIndexOutOfBoundsException: -1
         

      [endturn] [unitid=.... gameid=GAMEID]
         :  actualplayerid:PLAYERID roundNo=INTEGER
         !! invalid_clientkey
         !! PLAYERID_is_not_a_vaild_player         
         !! unkown_gameid
         !! playerid_is_valid_but_only_active_player_or_trade_partner_can_make_action_command

 
   [reply] [ ... ]
 
      [tradereply] [gameid=GAMEID response=...]
         :
         !! invalid_clientkey
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid         
         !! playerid_is_not_tradepartnerid
         !! tradereply_contained_unknown_response
 
 
   [option] [ ... ]

      [delplayer] [gameid=GAMEID playerdelid=PLAYERID]
         :
         !! invalid_clientkey
         !! PLAYERID_is_not_a_vaild_player
         !! unkown_gameid                  
         !! invalid_playerdelid
         !! invalid_gameownerkey_or_palyerid_to_delplayer         

 
   [data][ ... ]

      [chat] [senderplayerid=PLAYERID message=[ ... ]]
         :
         -- ILLEGAL playerid accepted !
      
      [chat] [senderplayerid=PLAYERID gameidreceiver=GAMEID message=[ ... ]]
         !! no_reciever_found_no_messages_send
         -- java.lang.NullPointerException

      [chat] [senderplayerid=PLAYERID playeridreceiver=PLAYERID message=[ ... ]]
         !! message_send_failed_-_client_not_found
         -- java.lang.NullPointerException
 

  
 */

	
	
	
	/**
	 * Request a list of all clients currently connected to the server
	 * 
	 * @return The list of clients currently connected to the server
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public List<String> getclients() throws IOException, StatusError, ProtocolError {
		String message = "type:getclients clientid:" + clientId + " end:end";
		String reply = sendMessage(message);

		String clients = getPart(reply.split(" "), "clients");
		if (clients == null)
			throw new ProtocolError(message, reply);

		clients = clients.substring("clients".length() + 1, clients.length() - 1);
		return Arrays.asList(clients.split(","));
	}

	/**
	 * Request a client id from the server
	 * 
	 * @return The client id assigned by the server
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public void logon() throws IOException, StatusError, ProtocolError {
		String message = "type:logon clientid:" + clientId + " clientinfo:" + clientInfo + " end:end";
		String reply = sendMessage(message);

		clientKey = parseValue(reply, "clientkey");
	}

	/**
	 * Interestingly enough at the time of writing this comment nobody seems to
	 * know for whatever reason anyone might want to send this request.
	 * 
	 * @return {@code null}
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public String getinfo() throws IOException, StatusError, ProtocolError {
		String message = "type:getinfo clientid:" + clientId + " clientkey:" + clientKey + " requestclientid:" + clientId + " end:end";

		@SuppressWarnings("unused")
		String reply = sendMessage(message);

		return null;
	}

	/**
	 * Request a list of all chat messages to deliver to this clients
	 * 
	 * @return The list of messages to deliver
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public List<MessageInfo> getdata() throws IOException, StatusError, ProtocolError {
		String message = "type:getdata clientid:" + clientId + " clientkey:" + clientKey + " end:end";
		String reply = sendMessage(message);

		String string = splitBrace(reply).get(0);
		List<MessageInfo> list = new LinkedList<MessageInfo>();
		for (String s : splitBrace(string)) {
			String incomingtime = parseValue(s, "incomingtime");
			String senderid = parseValue(s, "senderid");

			String data = splitBrace(splitBrace(s).get(0)).get(1);
			String messageString = splitBrace(data).get(0);

			MessageInfo info = new MessageInfo(incomingtime, senderid, messageString, data);
			list.add(info);
		}

		return list;
	}

	// FIXME: MOST PROBABLY UNNEEDED ??
	// public void config () throws IOException, StatusError, ProtocolError {
	// String message = "type:config clientid:" + clientId + " clientkey:" +
	// clientKey +
	// " datatargets[Client0001,Client0002] dataitems[x1,xx2,Time,Out,OP1] end:end";
	//
	// String reply = sendMessage (message);
	// if (reply == null); // SUPPRESS WARNING
	// }

	// ---------------------------

	/**
	 * Request a list of all games currently running on the server
	 * 
	 * @return A list of the names of the games currently running on the server
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// [INFO]
	public List<String> gamelist() throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[info][infotype=gamelist]] end:end";
		String reply = sendMessage(message);

		String gameString = getPart(reply.split(" "), "gamelist=");
		if (gameString == null)
			throw new ProtocolError(message, reply);

		gameString = gameString.substring(10, gameString.length() - 1);

		String[] games = gameString.split("\\[|\\]");
		List<String> gameList = new ArrayList<String>();
		for (String s : games)
			if (!s.equals(""))
				gameList.add(s);

		return gameList;
	}

	/**
	 * Request a list of all maps known to the server
	 * 
	 * @return The list of maps
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public List<String> maplist() throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[info][infotype=maplist]] end:end";
		String reply = sendMessage(message);

		String mapString = getPart(reply.split(" "), "maplist=");
		if (mapString == null)
			throw new ProtocolError(message, reply);

		mapString = mapString.substring(9, mapString.length() - 1);

		String[] maps = mapString.split("\\[|\\]");
		List<String> mapList = new ArrayList<String>();
		for (String s : maps)
			if (!s.equals(""))
				mapList.add(s);

		return mapList;
	}

	/**
	 * Request a preview of a named map
	 * 
	 * @return An array consisting of the terrainmap and the unitmap
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public MapPreview mappreview(String mapId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[info][infotype=mappreview" + " mapid="
				+ mapId + "]] end:end";
		String reply = sendMessage(message);

		reply = reply.substring(23, reply.length() - 1);

		String terrainmap = parseValue(reply, "=", "terrainmappreview");
		String unitmap = parseValue(reply, "=", "unitmappreview");

		// TODO: Was soll bitte damit werden?:
		// int[][][] info = new int[2][][];

		return new MapPreview(parseIntTable(terrainmap.substring(1, terrainmap.length() - 1)), parseIntTable(unitmap.substring(1,
				unitmap.length() - 1)), 2);
	}

	/**
	 * Request info about the unit types present on a given map
	 * 
	 * @param mapId
	 *            The name of the map to request information about.
	 * @return A List of {@link TypeInfo} objects, each describing a particluar
	 *         unittype in the game
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public List<TypeInfo> unittype(String mapId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[info][infotype=unittype" + " mapid=" + mapId
				+ "]] end:end";
		String reply = sendMessage(message);

		List<TypeInfo> list = new LinkedList<TypeInfo>();

		if (splitBrace(reply).isEmpty())
			throw new ProtocolError(message, reply);

		String string = splitBrace(reply).get(0);

		List<String> parts = splitBrace(string);
		if (parts.size() == 0 || parts.size() % 2 != 1)
			throw new ProtocolError(message, reply);

		Iterator<String> i = parts.iterator();
		i.next();
		while (i.hasNext()) {
			String name = i.next();
			String values = i.next();

			TypeInfo info = new TypeInfo("name=" + name + " " + values);
			list.add(info);
		}
		return list;
	}

	/**
	 * Request to create a game with a given name on a given map
	 * 
	 * @param mapId
	 *            The name of the map
	 * @param gameName
	 *            The name of the game
	 * @return The id associated with the createt game as reported by the server
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// [OPTION
	public String creategame(String mapId, String gameName) throws IOException, StatusError, ProtocolError {

		String key = UUID.randomUUID().toString();
		gameOwnerKey = key;

		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[option][[creategame][mapid=" + mapId
				+ " gamename=" + gameName + " gameownerkey=" + key + "]]] end:end";
		String reply = sendMessage(message);

		return parseValue(reply, "=", "gameid");
	}

	/**
	 * Add a player to the game
	 * 
	 * @param The
	 *            id of the game to add this player to
	 * @param playerName
	 *            The name to give to this player
	 * @return The id given to this player by the server
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public String addplayer(String gameId, String playerName) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[option][[addplayer][gameid=" + gameId
				+ " playername=" + playerName + "]]] end:end";

		String reply = sendMessage(message);
		return parseValue(reply, "playerid");
	}

	/**
	 * Start a game
	 * 
	 * @param gameId
	 *            The id of the game to start
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public void startgame(String gameId) throws IOException, StatusError, ProtocolError {
		sendMessage("type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[option][[startgame][gameid=" + gameId
				+ " gameownerkey=" + gameOwnerKey + "]]] end:end");
	}


	   /**
     * Delete a player from the game
     * 
     * @param gameId
     *            The id of the game from which to remove the player
     * @param playerDelId
     *            The id of the player to remove
     * @throws StatusError
     *             The server reply reports an error in the previous
     *             transmission
     * @throws ProtocolError
     *             The reply does not conform to the protocol specification
     */	
    public void delplayer(String gameId, String playerId, String playerDelName) throws IOException, StatusError, ProtocolError {
        String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId;
        message += " data[[option][[delplayer][gameid=" + gameId 
                + " playerdelname=" + playerDelName + " gameownerkey=" + gameOwnerKey + "]]] end:end";
        
        sendMessage(message);
    }
    
    public void delplayer(String gameId, String playerId) throws IOException, StatusError, ProtocolError {
        String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId;
        message += " data[[option][[delplayer][gameid=" + gameId + " playerdelid=" + playerId + "]]] end:end";
        
        sendMessage(message);
    }
    
    
  

    /**
     * Remove a game
     * 
     * @param gameId
     *            The id of the game to remove
     * @throws StatusError
     *             The server reply reports an error in the previous
     *             transmission
     * @throws ProtocolError
     *             The reply does not conform to the protocol specification
     */    
    public void removegame(String gameId) throws IOException, StatusError, ProtocolError {
        sendMessage("type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " data[[option][[removegame][gameid=" + gameId
                + " gameownerkey=" + gameOwnerKey + "]]] end:end");
        gameOwnerKey = null;
    }
	
    
    
	
	// type:sendgamedata clientid:CLIENTID clientkey:CLIENTKEY playerid:...
	// [data] [ ... ]

	/**
	 * Request info about a given game
	 * 
	 * @param gameId
	 * @param playerId
	 * @return The status of the game as {@link GameInfo}
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// [REQUEST]
	public GameInfo gameinfo(String gameId, String playerId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[request][rtype=gameinfo gameid=" + gameId + "]] end:end";
		String reply = sendMessage(message);

		reply = reply.substring(21, reply.length() - 9);
		GameInfo info = new GameInfo(reply);

		return info;
	}

	/**
	 * Request info about the terrainmap of a given game
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @return An array of {@code int} values representing the terrainmap
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public int[][] terrainmap(String gameId, String playerId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[request][rtype=terrainmap gameid=" + gameId + "]] end:end";
		String reply = sendMessage(message);

		String terrainmap = parseValue(reply, "=", "terrainmap");

		return parseIntTable(terrainmap.substring(1, terrainmap.length() - 1));
	}

	/**
	 * Request info about the unitmap of a given game
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @return An array of {@code int} values representing the unitmap
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public int[][] unitmap(String gameId, String playerId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[request][rtype=unitmap gameid=" + gameId + "]] end:end";
		String reply = sendMessage(message);

		String unitmap = parseValue(reply, "=", "unitmap");
		return parseIntTable(unitmap.substring(1, unitmap.length() - 1));
	}

	/**
	 * Request information about a specific unit in a specific game
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @param unitId
	 *            THe id of the unit
	 * @return The status of the unit as {@link UnitInfo}
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public UnitInfo unitinfo(String gameId, String playerId, int unitId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[request][rtype=unitinfo unitid=" + unitId + " gameid=" + gameId + "]] end:end";
		String reply = sendMessage(message);

		reply = reply.substring(21, reply.length() - 9);

		UnitInfo info = new UnitInfo(reply);
		return info;
	}

	/**
	 * As it seems this request doesn't supply any information we don't already
	 * have it seems quite useless, so we will just return {@code null}
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @param requestId
	 *            The id of the player to get information about
	 * @return {@code null}
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public String playerinfo(String gameId, String playerId, String requestId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[request][rtype=playerinfo playeridrequest=" + requestId + " gameid=" + gameId + "]] end:end";
		String reply = sendMessage(message);

		String string = splitBrace(reply).get(1);

		// FIXME: it seems this request ist absolutely useless, as we already
		// have to know
		// everything we get information about here !!
		// ... so, what to return ???

		@SuppressWarnings("unused")
		String playerid = parseValue(string, "=", "playerid");
		@SuppressWarnings("unused")
		String playername = parseValue(string, "=", "playername");
		@SuppressWarnings("unused")
		String clientid = parseValue(string, "=", "playername");

		return null;
	}

	/**
	 * Move a specific unit along a specified path
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @param unitId
	 *            The id of the unit
	 * @param fields
	 *            A list of the fields to move this unit along
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// [ACTION]
	public void move(String gameId, String playerId, int unitId, List<Field> fields) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[action][[move][unitid=" + unitId + " path=[";
		for (Field f : fields)
			message += "[" + f.iIndex + "," + f.jIndex + "]";
		message += "] gameid=" + gameId + "]]] end:end";

		@SuppressWarnings("unused")
		String reply = sendMessage(message);
	};

	/**
	 * Send a trade offer to the server
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @param tradeOfferUnit
	 *            The id of the unit sending the offer
	 * @param tradPartnerUnit
	 *            The id of the unit to make the offer to
	 * @param giveGoods
	 *            The items offered to the other unit
	 * @param getGoods
	 *            The items demanded from the other unit
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public void trade(String gameId, String playerId, int tradeOfferUnit, int tradePartnerUnit, Inventory giveGoods, Inventory getGoods)
			throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[action][[trade][gameid=" + gameId + " tradeoffererunitid=" + tradeOfferUnit + " tradepartnerunitid=" + tradePartnerUnit
				+ " givegoods=" + giveGoods + " getgoods=" + getGoods + " ]]] end:end";
		@SuppressWarnings("unused")
		String reply = sendMessage(message);
	};

	/**
	 * Attack a specific unit in the game
	 * 
	 * @param gameId
	 *            The id of the game
	 * @param playerId
	 *            The id of the player performing the request
	 * @param attckerId
	 *            The id of the unit performing the attack
	 * @param defenderId
	 *            The id of thee unit being attacked
	 * @return The amount of damage inflicted to the attacked unit
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public int attack(String gameId, String playerId, int attackerId, int defenderId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[action][[attack][attackerid=" + attackerId + " defenderid=" + defenderId + " gameid=" + gameId + "]]] end:end";

		String reply = sendMessage(message);

		return Integer.parseInt(parseValue(reply, "damage"));
	};

	/**
	 * End a players turn
	 * 
	 * @param playerId
	 *            The id of the player performing the request
	 * @param unitId
	 *            THe id of the unit that moved in this turn
	 * @param gameId
	 *            The id of the game
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	public void endturn(String playerId, int unitId, String gameId) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[action][[endturn][unitid=" + unitId + " gameid=" + gameId + "]]] end:end";
		@SuppressWarnings("unused")
		String reply = sendMessage(message);
	}

	/**
	 * Reply to a trade offer made by another player
	 * 
	 * @param playerId
	 *            The id of the player performing the request
	 * @param gameId
	 *            The id of the game
	 * @param response
	 *            The response to send
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// [REPLY]
	public void tradereply(String playerId, String gameId, String response) throws IOException, StatusError, ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[reply][[tradereply][gameid=" + gameId + " response=" + response + "]]] end:end";
		@SuppressWarnings("unused")
		String reply = sendMessage(message);
	}
	

	/**
	 * Send a chat message, either to a specific game or to a specific player
	 * 
	 * @param playerId
	 *            The id of the player performing the request
	 * @param targetPlayerId
	 *            The id of the player to send this message to, {@code null} if
	 *            no specific player should be targetet.
	 * @param gameId
	 *            The id of the game to send this message to, {@code null} if no
	 *            specific game should be targetet.
	 * @param messagge
	 *            The message to send
	 * @throws StatusError
	 *             The server reply reports an error in the previous
	 *             transmission
	 * @throws ProtocolError
	 *             The reply does not conform to the protocol specification
	 */
	// [CHAT]
	public void chat(String playerId, String targetPlayerId, String targetGameId, String messageString) throws IOException, StatusError,
			ProtocolError {
		String message = "type:sendgamedata clientid:" + clientId + " clientkey:" + clientKey + " playerid:" + playerId
				+ " data[[chat][senderplayerid=" + playerId;
		if (targetGameId != null)
			message += " gameidreceiver=" + targetGameId;
		if (targetPlayerId != null)
			message += " playeridreceiver=" + targetPlayerId;
		message += " message=[" + messageString + "]]] end:end";

		@SuppressWarnings("unused")
		String reply = sendMessage(message);
	}

	// MAIN
	public static void main(String argv[]) throws Exception {

		TCPClient client1 = new TCPClient("localhost", 1504, CLIENT_INFO);
        TCPClient client2 = new TCPClient("localhost", 1504, CLIENT_INFO);

		
		// LOGON
		client1.logon();
		client2.logon();
		
		String gameId = client1.creategame("Two", "TESTGAME" + UUID.randomUUID());

		String playerNameA = "PLAYER_A_";
		String playerIdA = client1.addplayer(gameId, playerNameA);
		
		String playerNameB = "PLAYER_B_";
        String playerIdB = client1.addplayer(gameId, playerNameB);
        
	
	}
}


