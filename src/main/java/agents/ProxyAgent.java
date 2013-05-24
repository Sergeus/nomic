package agents;

import java.util.UUID;

import actions.ProposeRuleChange;
import enums.VoteType;

public class ProxyAgent extends NomicAgent {
	
	private NomicAgent owner;
	
	private boolean Winner = false;
	
	private Integer preference = 50;
	
	private boolean preferenceLocked = false;
	
	private boolean avatar;

	public ProxyAgent(UUID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void Win() {
		Winner = true;
	}
	
	public void setOwner(NomicAgent owner) {
		this.owner = owner;
	}
	
	public NomicAgent getOwner() {
		return owner;
	}
	
	public UUID GetOwnerID() {
		return owner.getID();
	}
	
	public boolean isWinner() {
		return Winner;
	}

	public void setWinner(boolean winner) {
		Winner = winner;
	}
	
	@Override
	public void incrementTime() {
		logger.info("EPIC PROXY LOLZ");
		
		super.incrementTime();
	}
	
	@Override
	public String getProxyRulesFile() {
		return owner.getProxyRulesFile();
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange) {
		return super.chooseVote(ruleChange);
	}
	
	/**
	 * Returns true if this agent is the representative of the agent that created
	 * the subsimulation.
	 * @return
	 */
	public boolean IsAvatar() {
		return avatar;
	}
	
	public void SetAvatar(boolean avatar) {
		this.avatar = avatar;
	}

	public Integer getPreference() {
		return preference;
	}

	public void setPreference(Integer preference) {
		if (!preferenceLocked)
			this.preference = preference;
	}
	
	public void increasePreference(Integer amount) {
		if (!preferenceLocked)
			this.preference += amount;
	}
	
	public void decreasePreference(Integer amount) {
		if (!preferenceLocked)
			this.preference -= amount;
	}

	public boolean isPreferenceLocked() {
		return preferenceLocked;
	}

	public void setPreferenceLocked(boolean preferenceLocked) {
		this.preferenceLocked = preferenceLocked;
	}
}
