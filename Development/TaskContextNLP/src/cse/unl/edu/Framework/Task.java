package cse.unl.edu.Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Task {

	public String taskId;
	public String shortDescription;
	public String longDescription;

	public ArrayList nouns;
	public ArrayList verbs;
	public ArrayList spKeywords;

	// public ArrayList contextIds;
	public List<File> files;
	List<String> allfiles;
	List<String> filteredFiles;
	public String filteredDescription;
	public String comments;
	public String fromDate;
	public String toDate;
	public String firstTask;

	public Task() {
		nouns = new ArrayList();
		verbs = new ArrayList();
		// contextIds = new ArrayList();
		// files = new ArrayList();
		spKeywords = new ArrayList();
	}

	public Task(String taskId, String longDescription, String fromDate,
			String toDate, String i) {
		this();
		this.taskId = taskId;
		this.longDescription = longDescription;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.firstTask = i;
	}

	public List<String> getFileNamesList(boolean filtered) {
		if (filtered) {

			if (filteredFiles != null)
				return filteredFiles;

			filteredFiles = new ArrayList();
			for (File fil : this.files) {
				if (fil.status.equals("0"))
					filteredFiles.add(fil.fileName);
			}
			return filteredFiles;

		} else {
			if (allfiles != null)
				return allfiles;

			allfiles = new ArrayList();
			for (File fil : this.files) {
				allfiles.add(fil.fileName);
			}
			return allfiles;
		}
	}

	public List<String> getCommitsForFileName(String fileName) {
		for (File fil : this.files) {
			if (fil.fileName.equals(fileName))
				return fil.commits;
		}
		return null;
	}

	public void loadFiles(String filesCSV) {
		String[] filesCommits = filesCSV.split(",");
		this.files = new ArrayList();
		String fileCom;
		File obj;
		String contents[];
		for (int i = 0; i < filesCommits.length; i++) {
			fileCom = filesCommits[i];
			contents = fileCom.split(":");

			if (contents.length != 3)
				System.out.println("Error " + fileCom + " task id : "
						+ this.taskId);

			obj = new File(contents[0], contents[1], contents[2]);
			if (this.files.contains(obj)) {
				int index = files.indexOf(obj);
				files.get(index).commits.add(contents[1]);
				if (contents[2].equals("1")
						&& files.get(index).status.equals("0"))
					files.get(index).status = "1";
			} else {
				files.add(obj);
			}
		}
	}

	public class File {
		public String fileName;
		public List<String> commits;
		public String status = "0";

		public File(String fileName, String commit, String status) {
			this.fileName = fileName;
			this.commits = new ArrayList();
			this.commits.add(commit);
			this.status = status;
		}

		@Override
		public boolean equals(Object obj) {
			final File other = (File) obj;
			return this.fileName.equals(other.fileName);
		}

		@Override
		public int hashCode() {
			int hash = fileName.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			return this.fileName;
		}
	}

	public static void main(String args[]) {
		Task t = new Task();

		Task.File f1 = t.new File(
				"org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java",
				"6698", "1");
		Task.File f2 = t.new File(
				"org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java",
				"7474", "0");
		Task.File f3 = t.new File(
				"org.eclipse.mylyn.java.ui/src/org/sample.java", "6698", "1");

		t.loadFiles("org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/preferences/MylarPreferencePage.java:6708:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/tasks/RelatedLinks.java:17092:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/tasks/ui/TaskSummaryEditor.java:17092:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/tasks/util/XmlUtil.java:17092:1,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/pde/PdeStructureBridge.java:6682:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/core/MylarPlugin.java:6656:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/internal/UiUpdateListener.java:6656:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/preferences/MylarPreferencePage.java:6656:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/ui/wizards/MylarPreferenceWizard.java:6656:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/ui/wizards/MylarPreferenceWizardPage.java:6656:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/bugzilla/ui/editor/AbstractBugEditor.java:17056:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/bugzilla/ui/editor/ExistingBugEditor.java:17056:0,org.eclipse.mylyn.resources.tests/src/org/eclipse/mylyn/xml/tests/XMLResultUpdaterSearchListener.java:6492:0,org.eclipse.mylyn.resources.tests/src/org/eclipse/mylyn/xml/tests/XMLTestActiveSearchListener.java:6492:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/XmlNodeHelper.java:6492:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/XmlReferencesProvider.java:6492:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/ant/AntEditingMonitor.java:6492:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/ant/AntStructureBridge.java:6492:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/pde/PdeStructureBridge.java:6492:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/core/IMylarStructureBridge.java:6493:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/core/MylarPlugin.java:6493:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/core/resources/ResourceStructureBridge.java:6493:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/actions/ApplyMylarToProblemsListAction.java:6493:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/internal/views/ProblemsListInterestFilter.java:6493:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/internal/views/ProblemsListLabelProvider.java:6493:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/JavaStructureBridge.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/XmlNodeHelper.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/XmlReferencesProvider.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/ant/AntEditingMonitor.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/ant/AntStructureBridge.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/ant/ui/AntUiBridge.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/pde/PdeEditingMonitor.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/pde/PdeStructureBridge.java:6493:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/xml/pde/ui/PdeUiBridge.java:6493:0,org.eclipse.mylyn.tasks.core/src/org/eclipse/mylyn/bugs/BugzillaStructureBridge.java:16837:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarChangeSetManager.java:6153:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/core/internal/MylarContextManager.java:6099:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/actions/ApplyMylarToOutlineAction.java:6099:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/ui/internal/ContentOutlineManager.java:6099:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/ui/JavaUiBridge.java:6099:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/tasklist/internal/TaskListManager.java:16423:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/provisional/ui/MylarUiPlugin.java:5769:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/MylarContextManager.java:5772:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/provisional/ui/MylarUiPlugin.java:5772:0,org.eclipse.mylyn.context.ui/plugin.xml:5773:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/actions/ContextAttachAction.java:5773:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/DegreeOfInterest.java:5856:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/MylarContext.java:5856:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/dt/MylarInterest.java:5856:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/dt/MylarWebRef.java:5856:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/actions/ContextAttachAction.java:5856:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/actions/ContextRetrieveAction.java:5856:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/ui/views/ActiveSearchView.java:5856:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/internal/java/ui/views/ActiveHierarchyView.java:5856:0,org.eclipse.mylyn.help.ui/doc/new.html:15584:0,org.eclipse.mylyn.help.ui/doc/new.html:15585:0,org.eclipse.mylyn.help.ui/doc/new.html:15586:0,org.eclipse.mylyn.help.ui/doc/new.html:15587:0,org.eclipse.mylyn.help.ui/doc/new-0.4.0.html:15589:0,org.eclipse.mylyn.help.ui/doc/new-0.5.0.html:15589:0,org.eclipse.mylyn.help.ui/doc/new.html:15589:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextRetrieveWizard.java:15599:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/tasklist/BugzillaRepositoryConnector.java:15605:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/provisional/tasklist/AbstractRepositoryConnector.java:15606:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/RepositoryTaskDecorator.java:15607:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/RepositoryTaskDecorator.java:15609:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/TaskListImages.java:15609:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/internal/bugzilla/core/BugzillaRepositoryUtil.java:15619:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/tasklist/BugzillaRepositoryConnector.java:15619:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextRetrieveWizardPage.java:15620:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/internal/bugzilla/core/BugzillaRepositoryUtil.java:15621:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/tasklist/BugzillaRepositoryConnector.java:15621:0,org.eclipse.mylyn.help.ui/doc/new.html:15621:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextAttachWizard.java:15621:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextAttachWizardPage.java:15621:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextRetrieveWizard.java:15621:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/AllBugzillaTests.java:15785:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/AllBugzillaTests.java:15787:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/Bugzilla220ParserTest.java:15787:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/ReportAttachmentTest.java:15787:1,org.eclipse.mylyn.bugzilla.tests/testdata/contexts/downloadedContext.xml:15787:0,org.eclipse.mylyn.bugzilla.core/plugin.xml:15788:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/bugzilla/core/BugReport.java:15788:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/bugzilla/core/BugzillaRemoteContextDelegate.java:15788:1,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/bugzilla/core/Comment.java:15788:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/internal/bugzilla/core/BugzillaRepositoryUtil.java:15788:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/internal/bugzilla/core/IBugzillaConstants.java:15788:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/Bugzilla220ParserTest.java:15788:0,org.eclipse.mylyn.bugzilla.tests/testdata/contexts/downloadedContext.xml:15788:1,org.eclipse.mylyn.bugzilla.tests/testdata/pages/test-report-222attachment.html:15788:1,org.eclipse.mylyn.bugzilla.ui/plugin.xml:15788:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/AbstractBugEditor.java:15788:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/ExistingBugEditor.java:15788:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/tasklist/BugzillaRepositoryConnector.java:15788:0,org.eclipse.mylyn.help.ui/doc/faq.html:15788:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/planner/ui/TaskPlannerEditorPart.java:15788:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/views/TaskActivityView.java:15788:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/views/TaskListView.java:15788:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextAttachWizard.java:15788:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextAttachWizardPage.java:15788:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextRetrieveWizard.java:15788:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/wizards/ContextRetrieveWizardPage.java:15788:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/provisional/tasklist/AbstractRepositoryConnector.java:15788:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/provisional/tasklist/IRemoteContextDelegate.java:15788:1,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/tasklist/BugzillaRepositoryConnector.java:15749:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ScheduledTaskListRefreshJob.java:15749:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/ui/editors/TaskInfoEditor.java:15749:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/provisional/tasklist/AbstractRepositoryConnector.java:15749:0,org.eclipse.mylyn.monitor.ui/src/org/eclipse/mylyn/internal/monitor/InteractionEventLogger.java:3545:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/MylarContextManager.java:5774:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/ShellLifecycleListener.java:5774:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/provisional/core/MylarPlugin.java:5774:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/preferences/MylarPreferencePage.java:5774:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/MylarEditorManager.java:5774:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/MylarContextManager.java:5783:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/actions/AbstractInterestManipulationAction.java:5783:0,org.eclipse.mylyn.ide.tests/src/org/eclipse/mylyn/ide/tests/ResourcesContextTest.java:5783:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/InterestManipulatingEditorTracker.java:5783:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/ui/views/ActiveViewDropAdapter.java:5783:0,org.eclipse.mylyn.java.tests/src/org/eclipse/mylyn/java/tests/InterestManipulationTest.java:5783:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/core/ScalingFactors.java:5787:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/TaskUiBridge.java:5787:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/ui/preferences/MylarPreferencePage.java:5787:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/provisional/ui/IMylarUiBridge.java:5787:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/provisional/ui/MylarUiPlugin.java:5787:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/InterestManipulatingEditorTracker.java:5787:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/ui/ResourceUiBridge.java:5787:0,org.eclipse.mylyn.java.tests/src/org/eclipse/mylyn/java/tests/EditorManagementTest.java:5787:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/internal/java/ui/JavaUiBridge.java:5787:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/xml/ant/AntUiBridge.java:5787:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/xml/pde/PdeUiBridge.java:5787:0,org.eclipse.mylyn.help.ui/doc/new.html:15623:0,org.eclipse.mylyn.help.ui/doc/new.html:15645:0,org.eclipse.mylyn.tasks.core/src/org/eclipse/mylyn/internal/bugs/BugzillaUiBridge.java:15645:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasklist/util/TaskListWriter.java:15645:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/DuplicateDetetionTest.java:14963:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/ExistingBugEditor.java:14963:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/NewBugEditor.java:14963:0,org.eclipse.mylyn.help.ui/doc/new.html:14963:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/AllBugzillaTests.java:14979:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/BugzillaConfigurationTest.java:14979:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/DuplicateDetetionTest.java:14979:1,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/NewBugEditor.java:14979:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/headless/BugzillaDuplicateDetectionTest.java:14991:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/ExistingBugEditor.java:14991:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/NewBugEditor.java:14991:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/wizard/BugzillaProductPage.java:14991:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/wizard/NewBugzillaReportWizard.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/NewTaskFromErrorAction.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/AbstractDuplicateDetectingReportWizard.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/DisplayRelatedReportsPage.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/DuplicateDetectionData.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/FindRelatedReportsPage.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/MultiRepositoryAwareWizard.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewRepositoryTaskPage.java:14991:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewRepositoryTaskWizard.java:14991:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/headless/BugzillaDuplicateDetectionTest.java:15000:1,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/editor/ExistingBugEditor.java:15000:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/wizard/BugzillaProductPage.java:15000:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/wizard/NewBugzillaReportWizard.java:15000:0,org.eclipse.mylyn.help.ui/doc/new.html:15000:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/NewTaskFromErrorAction.java:15000:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/editors/AbstractRepositoryTaskEditor.java:15000:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/AbstractDuplicateDetectingReportWizard.java:15000:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/DisplayRelatedReportsPage.java:15000:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/DuplicateDetectionData.java:15000:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/FindRelatedReportsPage.java:15000:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/MultiRepositoryAwareWizard.java:15000:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewRepositoryTaskPage.java:15000:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewRepositoryTaskWizard.java:15000:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/SelectRepositoryPage.java:15000:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/InterestFilter.java:5373:0,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/xml/pde/PdeStructureBridge.java:5373:0,org.eclipse.mylyn.java.tests/src/org/eclipse/mylyn/java/tests/TypeHistoryManagerTest.java:5404:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/internal/java/MylarJavaPlugin.java:5404:0,org.eclipse.mylyn.java.tests/src/org/eclipse/mylyn/java/tests/TypeHistoryManagerTest.java:5405:0,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/internal/java/MylarJavaPlugin.java:5405:0,org.eclipse.mylyn.help.ui/doc/new.html:14298:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/commands/RemoteTaskSelectionDialog.java:14298:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/commands/RemoteTaskSelectionDialog.java:14301:0,org.eclipse.mylyn.help.ui/doc/new.html:14338:0,org.eclipse.mylyn.tasks.ui/plugin.xml:14338:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/AddExistingTaskJob.java:14338:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/AddRepositoryAction.java:14338:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/commands/AddTaskRepositoryHandler.java:14338:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/commands/OpenRemoteTaskHandler.java:14338:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/commands/RemoteTaskSelectionDialog.java:14338:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/commands/TaskRepositoryParameterValues.java:14338:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/AbstractRepositoryClientWizard.java:14338:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/CommonAddExistingTaskWizard.java:14338:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewRepositoryWizard.java:14338:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/tasks/ui/TaskCommandIds.java:14338:1,org.eclipse.mylyn.ide.ui/src/org/eclipse/mylyn/internal/ide/xml/ant/AntEditingMonitor.java:5235:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/internal/bugzilla/core/BugzillaCorePlugin.java:13435:0,org.eclipse.mylyn.bugzilla.tests/src/org/eclipse/mylyn/bugzilla/tests/NewBugWizardTest.java:13435:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/wizard/BugzillaProductPage.java:13435:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TaskSearchPage.java:12865:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TaskSearchPage.java:12867:0,org.eclipse.mylyn.tasks.core/src/org/eclipse/mylyn/internal/tasks/core/TaskGroup.java:12888:1,org.eclipse.mylyn.tasks.core/src/org/eclipse/mylyn/tasks/core/TaskGroup.java:12888:0,org.eclipse.mylyn.tasks.ui/plugin.xml:12888:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TaskSearchPage.java:12888:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/SearchForRepositoryTask.java:12888:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/TaskSelectionDialog.java:12888:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/views/AbstractFilteredTree.java:12888:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/views/CustomTaskListDecorationDrawer.java:12888:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/views/TaskListFilteredTree.java:12888:0,org.eclipse.mylyn.tasks.core/src/org/eclipse/mylyn/internal/tasks/core/LocalRepositoryConnector.java:13119:0,org.eclipse.mylyn.tasks.ui/plugin.xml:13119:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TaskSearchPage.java:13119:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/OpenTaskSearchAction.java:13119:1,org.eclipse.mylyn-feature/feature.xml:13124:0,org.eclipse.mylyn.bugzilla.ui/src/org/eclipse/mylyn/internal/bugzilla/ui/search/BugzillaSearchPage.java:13124:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TaskSearchPage.java:13124:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/DeleteAction.java:13124:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/resources/ui/EditorInteractionMonitor.java:4811:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/ContextUiPlugin.java:4814:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/context/ui/ContextUiPrefContstants.java:4814:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/context/ui/preferences/ContextUiPreferencePage.java:4814:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/resources/ui/EditorInteractionMonitor.java:4814:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/ContextUiPlugin.java:4822:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/context/ui/ContextUiPrefContstants.java:4822:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/context/ui/preferences/ContextUiPreferencePage.java:4822:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/resources/ui/EditorInteractionMonitor.java:4822:0,org.eclipse.mylyn.help.ui/doc/new.html:12747:0,org.eclipse.mylyn.tasks.core/src/org/eclipse/mylyn/internal/tasks/core/TaskActivityManager.java:12239:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TasksUiImages.java:12231:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/ScreenshotAttachmentPage.java:12231:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/ColorCanvas.java:12232:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/ColorSelectionWindow.java:12232:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/IImageCreator.java:12232:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/ImageAttachment.java:12232:1,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewAttachmentPage.java:12232:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/NewAttachmentWizard.java:12232:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/PreviewAttachmentPage.java:12232:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/wizards/ScreenshotAttachmentPage.java:12232:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/RepositorySearchResultView.java:11506:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/RepositorySearchResultView.java:11930:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/RepositorySearchResultView.java:11931:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/SearchResultsLabelProvider.java:11931:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/RepositorySearchResultView.java:11937:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/SearchResultTreeContentProvider.java:11937:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/TaskSearchPage.java:11992:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/actions/OpenTaskSearchAction.java:11992:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/RepositorySearchResultView.java:11992:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/SearchResultTreeContentProvider.java:11992:0,org.eclipse.mylyn.tasks.ui/src/org/eclipse/mylyn/internal/tasks/ui/search/SearchResultsLabelProvider.java:11992:0,org.eclipse.mylyn.monitor.ui/src/org/eclipse/mylyn/monitor/ui/MonitorUi.java:3022:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/internal/context/ui/ContextEditorManager.java:4526:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/AbstractAutoFocusViewAction.java:4386:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/AbstractFocusViewAction.java:4386:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/AbstractFocusViewAction.java:4393:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/AbstractFocusViewAction.java:4394:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/AbstractFocusViewAction.java:4395:0,org.eclipse.mylyn.bugzilla.core/src/org/eclipse/mylyn/internal/bugzilla/core/BugzillaTaskDataHandler.java:11023:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/context/core/InteractionContextManager.java:4187:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/resources/ui/ResourceStructureBridge.java:4187:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/context/core/AbstractContextStructureBridge.java:4206:0,org.eclipse.mylyn.context.core/src/org/eclipse/mylyn/internal/context/core/InteractionContextManager.java:4206:0,org.eclipse.mylyn.context.ui/src/org/eclipse/mylyn/context/ui/InterestFilter.java:4206:0,org.eclipse.mylyn.java.tests/src/org/eclipse/mylyn/java/tests/InteractionContextManagerTest.java:4206:0,org.eclipse.mylyn.resources.ui/src/org/eclipse/mylyn/internal/resources/ui/ResourceStructureBridge.java:4206:0,org.eclipse.mylyn.debug.tests/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsContextUtilTest.java:3794:0,org.eclipse.mylyn.debug.tests/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsStateUtilTest.java:3794:0,org.eclipse.mylyn.debug.tests/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsStructureBridgeTest.java:3794:0,org.eclipse.mylyn.debug.tests/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsTestUtil.java:3794:0,org.eclipse.mylyn.debug.ui/plugin.xml:3794:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsContextContributor.java:3794:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsPreferencePage.java:3794:1,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsStructureBridge.java:3794:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/Messages.java:3794:1,org.eclipse.mylyn.debug.tests/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsStructureBridgeTest.java:3796:1,org.eclipse.mylyn.debug.ui/plugin.xml:3796:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsContextContributor.java:3796:1,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsInterestFilter.java:3796:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsListener.java:3796:1,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/BreakpointsStructureBridge.java:3796:1,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/DebugUiPlugin.java:3796:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/FocusBreakpointsViewAction.java:3796:0,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/cnf/BreakpointManagerContentProvider.java:3796:1,org.eclipse.mylyn.debug.ui/src/org/eclipse/mylyn/internal/debug/ui/cnf/BreakpointManagerLabelProvider.java:3796:1,org.eclipse.mylyn.reviews.ui/src/org/eclipse/mylyn/internal/reviews/ui/views/ReviewExplorer.java:8048:0");

		List<String> st = t.getFileNamesList(false);
		Collections.sort(st);

		for (String fil : st)
			System.out.println(fil);

		System.out.println(st.size());
		System.out.println("");
		st = t.getFileNamesList(true);
		Collections.sort(st);

		for (String fil : st)
			System.out.println(fil);
		System.out.println(st.size());

	}

	public List<String> getFirstCommitFile(List<String> initList) {

		List<String> fils = new ArrayList();
		for (File file : files) {
			if (initList.contains(file.fileName)
					&& file.commits.contains(this.firstTask)) {
				fils.add(file.fileName);
			}
		}

		return fils;
	}

}
