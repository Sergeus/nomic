package agents;

import java.util.UUID;

public class ProxyAgent extends NomicAgent {
	
	NomicAgent Owner;
	
	boolean Winner = false;

	public ProxyAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void Win() {
		Winner = true;
	}
	
	public void SetOwner(NomicAgent owner) {
		Owner = owner;
	}
	
	public NomicAgent GetOwner() {
		return Owner;
	}
	
	public UUID GetOwnerID() {
		return Owner.getID();
	}
	
	public boolean isWinner() {
		return Winner;
	}

	public void setWinner(boolean winner) {
		Winner = winner;
	}

}
