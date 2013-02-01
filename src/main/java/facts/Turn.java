package facts;

import enums.TurnType;
import agents.NomicAgent;

public class Turn {
	public final int number;

	public final TurnType Type;
	
	public NomicAgent ActivePlayer;

	public Turn(int number, TurnType type) {
		super();
		this.number = number;
		Type = type;
	}

	public int getNumber() {
		return number;
	}

	public TurnType getType() {
		return Type;
	}
}
