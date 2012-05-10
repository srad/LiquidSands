package de.frankfurt.uni.vcp.net;

public class MapPreview {
 
    public int[][] terrainMap;
    public int[][] unitMap;
    public int numberOfPlayers;
    
    public MapPreview(int[][] t, int[][] u, int i) {
        terrainMap = t;
        unitMap = u;
        numberOfPlayers = i;
    }  
    
}
