package org.sunysb.cse549.database;

public class Profile {

	private int profileId;
	
	private String name;
	private int volume;
	private boolean vibratory;
	private int mode;
	
	
	public int getProfileId() {
		return profileId;
	}
	public void setProfileId(int profileId) {
		this.profileId = profileId;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public boolean isVibratory() {
		return vibratory;
	}
	public void setVibratory(boolean vibratory) {
		this.vibratory = vibratory;
	}
	
	public String toString() {
		
		String mode = null;
		switch(this.getMode())
		{
			case 0:
				mode = "Sil";
				break;
			case 1:
				mode = "Vib";
				break;
			case 2:
				mode = "Nor";
				break;
			default:
				mode = "Nor";
		}
						
		return "N:"+ this.getName() + " Vol:"+this.getVolume()+", M: "+mode+"";
	}
	
}
