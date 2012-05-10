package de.frankfurt.uni.vcp.units;

import de.frankfurt.uni.vcp.net.TCPClient;

public class Inventory {

	/** List of available good names. */
	private static String[] goodsNames = { "Rubin(e)", "Salz", "Dattel(n)",
			"Tee", "Wasser" };

	public int[] items = new int[5];

	@Override
	public String toString() {
		String s = "[" + items[0];

		for (int i = 1; i < 5; ++i)
			s += "," + items[i];
		s += "]";

		return s;
	}

	public Inventory() {
	}

	public Inventory(String string) {
		string = TCPClient.splitBrace(string).get(0);

		int i = 0;
		for (String s : TCPClient.splitBrace(string))
			items[i++] = Integer.parseInt(s);
	}

	public String toTradeString() {
		String string = "";

		for (int i = 0; i < 5; i++)
			if (items[i] > 0)
				string += " " + items[i] + " " + goodsNames[i] + ",";

		if (string.length() > 1)
			string = string.substring(0, string.length() - 1);

		return string;
	}

	public void add(Inventory other) {
		for (int i = 0; i < 5; ++i)
			items[i] += other.items[i];
	}

	public void remove(Inventory other) {
		for (int i = 0; i < 5; ++i)
			items[i] -= other.items[i];
	}

	public int getWeight() {
		int w = 0;

		for (int i = 0; i < 5; ++i)
			w += items[i];

		return w;
	}

	public boolean canRemove(Inventory other) {
		for (int i = 0; i < 5; ++i)
			if (items[i] - other.items[i] < 0)
				return false;
		return true;
	}
	
	/**
	 * <p>Each position within the {@link #items} array reprents a good.</p>
	 * <p>This method return the good name based on its position</p>	 * 
	 * @param i Index position.
	 * @return Good name.
	 */
	public String getGoodNameByIndex(int i) {
		return goodsNames[i];
	}

	public boolean canAdd(int capacity, Inventory other) {
		if (getWeight() + other.getWeight() <= capacity)
			return true;
		return false;
	}

}