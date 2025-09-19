package com.alectriciti.gdx;

public interface Activatable {

	public void activate();
	
	public void deactivate();

	public void addOnActivate(Runnable run_autoclose);

}
