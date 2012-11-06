package cse.unl.edu.mylyn;

//import org.eclipse.core.resources.IResource;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.resources.ui.ResourcesUiBridgePlugin;
import org.eclipse.mylyn.internal.team.ui.ContextActiveChangeSetManager;
import org.eclipse.mylyn.internal.team.ui.FocusedTeamUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import cse.unl.edu.Framework.DatabaseManager;

//Extend AbstractContextListener to get the list of all .java files as soon as the context is updated
public class MylynContextListenener extends AbstractContextListener {

	@Override
	 public void contextChanged(ContextChangeEvent event) {
	  super.contextChanged(event);
	  

	 for(IInteractionElement interactionElement : ((ContextChangeEvent)event).getElements())
	 {
		 String fileIdentifier = interactionElement.getHandleIdentifier();
		 
		 IResource res = ResourcesUiBridgePlugin.getDefault().getResourceForElement(interactionElement, true);
		 
		 List<IResource> rest = ResourcesUiBridgePlugin.getDefault().getInterestingResources(ContextCore.getContextManager().getActiveContext());

		 //ContextActiveChangeSetManager changeSetManager = (ContextActiveChangeSetManager) FocusedTeamUiPlugin.getDefault().getContextChangeSetManagers().iterator().next();
		 ITask task = TasksUi.getTaskActivityManager().getActiveTask();
		 
		 //IResource [] rest = changeSetManager.getResources(task);
		 
		 System.out.println(task.getSummary() + " " + rest.size() );
		 
		 for(IResource rere : rest)
		 {
			 System.out.println(rere.getName());
		 }
		 
		 //System.out.println("intersed" + interactionElement.getInterest().isInteresting());
		 //System.out.println("Res " + res.exists() + " " +res.isLinked() + " " + res.getProject().getName());
		 
		 if(interactionElement.getContentType().equals("java") && isJavaFile(fileIdentifier))
		 {
			//System.out.println(fileIdentifier);
			//System.out.println(getJavaFileName(fileIdentifier));
		}
		 DatabaseManager.addEvent();
	 }
	}
	
	public String getJavaFileName(String fileName) {
		String[] segments = fileName.split("\\{");
		return segments[segments.length - 1];
	}
	
	public Boolean isJavaFile(String fileName)
	{
		String[] segments = fileName.split("\\.");
		
		if(segments[segments.length - 1].equals("java"))
			return true;
		return false;
	}
	
}
