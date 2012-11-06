package cse.unl.edu.mylyn;

import java.util.Date;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.monitor.core.IInteractionEventListener;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IWorkbenchPart;

import cse.unl.edu.Framework.DatabaseManager;

public class InteractionContextListener implements IInteractionEventListener {

	@Override
	public void interactionObserved(InteractionEvent event) {
		// TODO Auto-generated method stub
		
		if(event.getKind() == InteractionEvent.Kind.EDIT)
		//{
		
			System.out.println(event.getKind().toString()+ " " + event.getDelta() + " " + event.getStructureHandle());
		//}
		

			DatabaseManager.addEvent();
		
		
	}

	@Override
	public void startMonitoring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopMonitoring() {
		// TODO Auto-generated method stub
	}



	

}
