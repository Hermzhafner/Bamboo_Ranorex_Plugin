package at.Ranorex.hermz.RxBambooPlugin.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ExecuteRanorexTask implements TaskType {
	private static final String RANOREX_SOLUTION_RXSLN_FILE_NOT_FOUND = "Ranorex Solution (*.rxsln) - File not found!";
	private static final String JUNIT_XML = ".junit.xml";
	private static final String TEST_EXECUTED_RESULT = "Test executed. Result: ";
	private static final String EXE = ".exe";
	private static final String DEBUG = "Debug";
	private static final String BIN = "bin";
	private static final String RXSLN = ".rxsln";
	private static final String END_RANOREX_TEST = "End Ranorex Test";
	private static final String START_RANOREX_TEST = "Start Ranorex Test";
	private static final String STANDARD_PARAM = " /zr /ju";
	private static final String RF_PARAM = " /rf:";
	private static final String REPORT_RXLOG = "report.rxlog";

	@SuppressWarnings("deprecation")
	public TaskResult execute(TaskContext taskContext) throws TaskException {
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		int result = -1;
		buildLogger.addBuildLogEntry(START_RANOREX_TEST);

		try {
			result = executeTest(taskContext);
			if (result == 0) {
				return TaskResultBuilder.create(taskContext).success().build();
			} else {
				return TaskResultBuilder.create(taskContext).failed().build();
			}
		} catch (IOException e) {
			buildLogger.addBuildLogEntry(e.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		} catch (InterruptedException e) {
			buildLogger.addBuildLogEntry(e.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		} finally {
			buildLogger.addBuildLogEntry(END_RANOREX_TEST);
		}
	}

	private synchronized int executeTest(TaskContext taskContext) throws IOException, InterruptedException {
		taskContext.getBuildLogger().addBuildLogEntry(taskContext.getRootDirectory().getAbsolutePath());
		File rootDir = taskContext.getRootDirectory();
		File jUnitReportFile = null;
		int result = -1;
		String execFile = getRXexecPath(taskContext, rootDir);
		
		// ends everything (if exec file is null).
		if (execFile == null) {
			return 1;
		}
		
		Runtime rt = Runtime.getRuntime();
		String command = execFile + getParams(taskContext);
		taskContext.getBuildLogger().addBuildLogEntry(command);
		Process process = rt.exec(command);

		while (process.isAlive()) {
			this.wait(1000);
		}
		
		result = process.exitValue();
		taskContext.getBuildLogger().addBuildLogEntry(TEST_EXECUTED_RESULT + result);
		jUnitReportFile = getjUnitReportFile(rootDir, jUnitReportFile);
		removeBOMfromjUnitReport(jUnitReportFile);

		return result;
	}
	
	private String getParams(TaskContext taskContext) {
		String rxParam = taskContext.getConfigurationMap().get("rxparams");
		String rfParam = REPORT_RXLOG;
		if (rxParam != null) {
			String[] splitted = rxParam.split(" ");
			for (String entry : splitted) {
				if (entry.toLowerCase().startsWith("/rf:")) {
					rfParam = (entry.split(":"))[1];
					rxParam = rxParam.replaceAll("/rf:" + rfParam, "");
				}
			}
		}
		
		String reportFile = RF_PARAM + taskContext.getRootDirectory() + File.separator + rfParam;
		return reportFile + STANDARD_PARAM + " " + rxParam;
	}

	private String getRXexecPath(TaskContext taskContext, File f) {
		String slName = getSolutionPath(f);
		if (slName == null) {
			taskContext.getBuildLogger().addBuildLogEntry(RANOREX_SOLUTION_RXSLN_FILE_NOT_FOUND);
			return null;
		} 
		return f.getAbsolutePath() + File.separator + slName + File.separator + BIN + File.separator + DEBUG
				+ File.separator + slName + EXE;
	}

	private String getSolutionPath(File f) {
		for (File file : f.listFiles()) {
			if (file.getName().endsWith(RXSLN)) {
				return file.getName().replaceAll(RXSLN, "");
			}
		}
		return null;
	}
	
	private File getjUnitReportFile(File f, File jFile) {
		for (File currentfile : f.listFiles()) {
			if (currentfile.getName().contains(JUNIT_XML)) {
				jFile = currentfile;
			}
		}
		return jFile;
	}

	private void removeBOMfromjUnitReport(File reportFile) throws IOException, InterruptedException {
		long reportFileSize = 3;
		
		if (reportFile != null) {
			
			// waits for Ranorex to write the respective file and breaks if the size stays the same for more than 10 sec
			while (Files.readAllBytes(reportFile.toPath()).length == (int) reportFileSize) {
				this.wait(10000);
				if (Files.readAllBytes(reportFile.toPath()).length == (int) reportFileSize) {
					break;
				}
			}
			
			byte[] existingReport = Files.readAllBytes(reportFile.toPath());
			byte[] newReport = new byte[existingReport.length - 3];

			for (int i = 0; i < newReport.length; i++) {
				newReport[i] = existingReport[i + 3];
			}

			Path reportPath = Paths.get(reportFile.getAbsolutePath());
			Files.delete(reportPath);
			Files.write(reportPath, newReport);
		}
	}
}
