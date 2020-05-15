package org.facade;

public class Controller {

	Player player = new Player();
	Projector projector = new Projector();

	public void work() {
		projector.on();
		projector.focus();
		
		player.on();
		player.play();
	}
	
	public void done() {
		player.off();
		
		projector.off();
	}
}
