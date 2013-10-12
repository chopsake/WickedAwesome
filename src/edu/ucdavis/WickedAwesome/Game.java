/* begin: Game.java */
package edu.ucdavis.WickedAwesome;

public class Game {
    private String gameType;
    private int hid;
    private String gameName;

    public Game(String gameType, int hid, String gameName) {
	this.gameType = gameType;
	this.hid = hid;
	this.gameName = gameName;
    }

    public void setGameType(String gameType) {
	this.gameType = gameType;
    }

    public String getGameType() {
	return gameType;
    }

    public void setGameName(String gameName) {
	this.gameName = gameName;
    }

    public String getGameName() {
	return gameName;
    }

    public void setHid(int hid) {
	this.hid = hid;
    }

    public int getHid() {
	return hid;
    }

    @Override
    public String toString() {
	if (gameType != "")
	    return gameName + "\n" + gameType;
	else
	    return gameName;
    }
}
/* end: Game.java */