package reactive.common;

import reactive.View.Gui;
import reactive.control.SharedContext;

public class main {
	
	public static void main(String[] args) {
		
		//SharedContext controller
		SharedContext context = SharedContext.getIstance();
		
		new Gui(1024, 768, context);


	}
}
