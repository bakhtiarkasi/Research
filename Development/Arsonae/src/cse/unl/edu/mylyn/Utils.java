package cse.unl.edu.mylyn;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.ui.PlatformUI;



public class Utils {

	private TaskRepository repository;
	private ITask task;

	protected static final int BUFFER_SIZE = 1024;
	
	public static String taskxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tasks>";

	@SuppressWarnings("restriction")
	public Utils(ITask task) {
		this.task = task;
		this.repository = TasksUi.getRepositoryManager()
		.getRepository(task.getConnectorKind(), task.getRepositoryUrl());

		List<ITaskAttachment> contextAttachments = AttachmentUtil.getContextAttachments(repository, task);

		System.out.print("task: " + task.getTaskId());
		for (ITaskAttachment attachment : contextAttachments) {

			System.out.print("  Creation Date: " + attachment.getCreationDate());
			IRepositoryPerson author = attachment.getAuthor();
			if (author != null) {
				System.out.print("  Author: " + author.getName());
			}
			System.out.print("  Attachment Description: " + task.getSummary());

			//AttachmentUtil.downloadContext(attachment.getTask(), attachment,  (IRunnableContext) new NullProgressMonitor());

			TaskAttribute attachmentAttribute = attachment.getTaskAttribute();

			TaskRepository taskRepository = attachment.getTaskRepository();
			AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(
					taskRepository.getConnectorKind());
			AbstractTaskAttachmentHandler handler = connector.getTaskAttachmentHandler();
			try 
			{
				File targetFile = new File("C:\\Users\\sweuser\\Documents\\MyLyn\\ContextFiles", task.getTaskId()+".xml.rar");
				File xmlfile = null;
			
				/*
				 * encoded = URLEncoder.encode(handleIdentifier, InteractionContextManager.CONTEXT_FILENAME_ENCODING);
			File contextDirectory = getContextDirectory();
			File contextFile = new File(contextDirectory, encoded + InteractionContextManager.CONTEXT_FILE_EXTENSION);
			return contextFile;
				 */

				OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));

				InputStream in = handler.getContent(taskRepository, task, attachmentAttribute, new NullProgressMonitor());
				byte[] buffer = new byte[BUFFER_SIZE];
				while (true) {

					int count = in.read(buffer);
					if (count == -1) 
					{
						out.flush();
						out.close();

						ZipFile zip = new ZipFile(targetFile);
						xmlfile = new File("C:\\Users\\sweuser\\Documents\\MyLyn\\ContextFiles\\Context", task.getTaskId()+".xml");
						
						String summary = task.getSummary();
						
						summary = summary.replaceAll("&", "&amp;");	
						summary = summary.replaceAll("<", "&lt;");
						summary = summary.replaceAll(">", "&gt;");
						summary = summary.replaceAll("\"", "&quot;");
						summary = summary.replaceAll("\'", "&apos;");
						
						taskxml += "\n <task id=\""+task.getTaskId()+"\" author=\""+ author.getName()+"\" description=\""+summary+ "\" createdOn=\""+task.getCreationDate()+"\" completedOn=\""+task.getCompletionDate()+"\"></task>";
						
						unzip(zip, xmlfile);
						System.out.println();
						return;
					}
					out.write(buffer, 0, count);
				}

			}				
			catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println();
		}
	}


	private static void unzip(ZipFile zipFile, File dstFile) throws IOException {

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		try {
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String entryName = entry.getName();
			
				InputStream src = null;
				OutputStream dst = null;
				try {
					src = zipFile.getInputStream(entry);
					dst = new FileOutputStream(dstFile);
					transferData(src, dst);
				} finally {
					if (dst != null) {
						try {
							dst.close();
						} catch (IOException e) {
							// don't need to catch this
						}
					}
					if (src != null) {
						try {
							src.close();
						} catch (IOException e) {
							// don't need to catch this
						}
					}
				}
			}
		} finally {
			try {
				zipFile.close();
			} catch (IOException e) {
				// don't need to catch this
			}
		}
	}

	private static void transferData(InputStream in, OutputStream out) throws IOException 
	{
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}
	
	public static int getRandomNumber(int start, int end, ArrayList<Integer> exclude)
	{
		
		Random rand = new Random(); 
		 int pickedNumber = -1;
		 do
		 {
			 pickedNumber = rand.nextInt(end - start + 1) + start;
			 if(exclude != null && exclude.contains(pickedNumber))
				 pickedNumber = -1;
		 }while(pickedNumber == -1);
		
		 return pickedNumber;
	}
}