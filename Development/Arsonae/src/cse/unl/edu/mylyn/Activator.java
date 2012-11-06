package cse.unl.edu.mylyn;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.ui.IStartup;
import org.osgi.framework.BundleContext;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import cse.unl.edu.Framework.DatabaseManager;


/**
 * The activator class controls the plug-in life cycle
 */
//Implement IStartup interface to make sure this plugin runs as soon as workbench initializes
public class Activator extends Plugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.mylyncontext"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		//Add an event listener to the MyLyn context manager, get list of all tasks
		// whenever there is a change in cotext.
		ContextCore.getContextManager().addListener(new MylynContextListenener());
		//MonitorUi.getActivityContextManager()..getDefault().notifyInteractionObserved(InteractionEvent). InteractionEvents
		
		MonitorUiPlugin.getDefault().addInteractionListener(new InteractionContextListener());
		
		TaskList tasks = TasksUiPlugin.getTaskList();
		System.out.println("no of tasks: "+ tasks.getAllTasks().size());
		
		String insert = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		for(ITask task : tasks.getAllTasks())
		{
			String createDate = dateFormat.format(task.getCreationDate()).toString();
			String completeDate = task.getCompletionDate() == null ? "null" : dateFormat.format(task.getCompletionDate()).toString();
			
			String createTime = timeFormat.format(task.getCreationDate()).toString();
			String completeTime = task.getCompletionDate() == null ? "null" : timeFormat.format(task.getCompletionDate()).toString();
			
			insert += "INSERT INTO alltasks (taskid, author, description, createdate, createtime, completedate, completetime) " + 
					"	VALUES('"+ task.getTaskId() +"', '" + task.getOwner() +"','" + task.getSummary().replace("'", "''") + "', '" + createDate + "', '" + createTime + "', '" + completeDate + "', '" + completeTime +"');";
			insert += "\n";
		}
		/*	new Utils(task);
		
		File taskFile = new File("C:\\Users\\sweuser\\Documents\\MyLyn\\ContextFiles\\Context", "tasks.xml");
		DataOutputStream out = new DataOutputStream(new FileOutputStream(taskFile));
		
		Utils.taskxml += "\n</tasks>";
		out.writeBytes(Utils.taskxml);*/
		//out.flush();
		//out.close();
		
		//org.eclipse.mylyn.internal.tasks.ui.CategorizedPresentation
		
		System.out.println(insert);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
	}

}



