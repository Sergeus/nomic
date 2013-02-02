package facts;

import enums.TurnType;
import agents.NomicAgent;

public class Turn {
	public int number;

	public TurnType type;
	
	public NomicAgent activePlayer;

	public Turn(int number, TurnType type, NomicAgent activePlayer) {
		super();
		this.number = number;
		this.type = type;
		this.activePlayer = activePlayer;
	}

	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public TurnType getType() {
		return type;
	}
	
	public void setType(TurnType type) {
		this.type = type;
	}
	
	public NomicAgent getActivePlayer() {
		return activePlayer;
	}
	
	public void setActivePlayer(NomicAgent activePlayer) {
		System.out.println("Turn changing active player from " + this.activePlayer
				+ " to " + activePlayer);
		this.activePlayer = activePlayer;
	}
}
