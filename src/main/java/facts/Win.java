package facts;

import agents.NomicAgent;

public class Win {
	NomicAgent winner;
	
	public Win(NomicAgent winner) {
		this.winner = winner;
		this.winner.Win();
	}
}
