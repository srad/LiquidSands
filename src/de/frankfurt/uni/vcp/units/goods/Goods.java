package de.frankfurt.uni.vcp.units.goods;

import com.jme3.scene.Node;

/**
 * This class represents a single object that can be holded by a single player and
 * traded between multiple players.
 * 
 * @author Bernd Späth, Wladimir Spindler and Saman Sedighi Rad
 */
public abstract class Goods extends Node {
	
	/** Name of this good. */
	protected String name;
	
	/** This is the trade price within the game, not used right now. */
	protected int value;
	
	/** Defines how much capacity this good consumes. */
	protected int capacity;
	
	protected static final int DEFAULT_CAPACITY = 1;
	protected static final int DEFAULT_VALUE = 1;
	
	public Goods(String name) {
		this.setCapacity(DEFAULT_CAPACITY);
		this.setValue(DEFAULT_VALUE);
	}
	
	public Goods(String name, int capacity) {
		this.setName(name);
		this.setCapacity(capacity);
		this.setValue(DEFAULT_VALUE);
	}
	
	public Goods(String name, int capacity, int value) {
		this.setName(name);
		this.setCapacity(capacity);
		this.setValue(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

}
