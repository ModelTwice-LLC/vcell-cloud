package cbit.vcell.message.server.htc.slurm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jdom.Document;
import org.jdom.Element;
import org.vcell.util.ExecutableException;
import org.vcell.util.FileUtils;
import org.vcell.util.PropertyLoader;

import cbit.util.xml.XmlUtil;
import cbit.vcell.message.server.cmd.CommandService;
import cbit.vcell.message.server.cmd.CommandService.CommandOutput;
import cbit.vcell.message.server.htc.HtcException;
import cbit.vcell.message.server.htc.HtcJobID;
import cbit.vcell.message.server.htc.HtcJobNotFoundException;
import cbit.vcell.message.server.htc.HtcJobStatus;
import cbit.vcell.message.server.htc.HtcProxy;
import cbit.vcell.solvers.ExecutableCommand;
import cbit.vcell.tools.PortableCommand;
import cbit.vcell.tools.PortableCommandWrapper;
import edu.uchc.connjur.wb.LineStringBuilder;

public class SlurmProxy extends HtcProxy {
	private final static int SCANCEL_JOB_NOT_FOUND_RETURN_CODE = 1;
	private final static String SCANCEL_UNKNOWN_JOB_RESPONSE = "does not exist";
	protected final static String SLURM_SUBMISSION_FILE_EXT = ".slurm.sub";
	private Map<HtcJobID, JobInfoAndStatus> statusMap;


	// note: full commands use the PropertyLoader.htcPbsHome path.
	private final static String JOB_CMD_SUBMIT = "sbatch";
	private final static String JOB_CMD_DELETE = "scancel";
	private final static String JOB_CMD_STATUS = "sacct";
	//private final static String JOB_CMD_QACCT = "qacct";
	
	//private static String Slurm_HOME = PropertyLoader.getRequiredProperty(PropertyLoader.htcSlurmHome);
	private static String Slurm_HOME = ""; // slurm commands should be in the path (empty prefix)
	private static String htcLogDirString = PropertyLoader.getRequiredProperty(PropertyLoader.htcLogDir);
	private static String MPI_HOME= PropertyLoader.getRequiredProperty(PropertyLoader.MPI_HOME);
	static {
//		if (!Slurm_HOME.endsWith("/")){
//			Slurm_HOME += "/";
//		}
		if (!htcLogDirString.endsWith("/")){
			htcLogDirString = htcLogDirString+"/";
		}
	}

	public SlurmProxy(CommandService commandService, String htcUser) {
		super(commandService, htcUser);
		statusMap = new HashMap<HtcJobID,JobInfoAndStatus>( );
	}

	@Override
	public HtcJobStatus getJobStatus(HtcJobID htcJobId) throws HtcException, ExecutableException {
		if (statusMap.containsKey(htcJobId)) {
			return statusMap.get(htcJobId).status;
		}
		throw new HtcJobNotFoundException("job not found", htcJobId);
	}

	/**
	 * qdel 6894
	 *
vcell has registered the job 6894 for deletion
	 *
	 * qdel 6894
	 *
job 6894 is already in deletion
	 *
	 * qdel 6894
	 *
denied: job "6894" does not exist

	 */


	@Override
	public void killJob(HtcJobID htcJobId) throws ExecutableException, HtcException {

		String[] cmd = new String[]{Slurm_HOME + JOB_CMD_DELETE, Long.toString(htcJobId.getJobNumber())};
		try {
			//CommandOutput commandOutput = commandService.command(cmd, new int[] { 0, QDEL_JOB_NOT_FOUND_RETURN_CODE });

			CommandOutput commandOutput = commandService.command(cmd,new int[] { 0, SCANCEL_JOB_NOT_FOUND_RETURN_CODE });

			Integer exitStatus = commandOutput.getExitStatus();
			String standardOut = commandOutput.getStandardOutput();
			if (exitStatus!=null && exitStatus.intValue()==SCANCEL_JOB_NOT_FOUND_RETURN_CODE && standardOut!=null && standardOut.toLowerCase().contains(SCANCEL_UNKNOWN_JOB_RESPONSE.toLowerCase())){
				throw new HtcJobNotFoundException(standardOut, htcJobId);
			}
		}catch (ExecutableException e){
			e.printStackTrace();
			if (!e.getMessage().toLowerCase().contains(SCANCEL_UNKNOWN_JOB_RESPONSE.toLowerCase())){
				throw e;
			}else{
				throw new HtcJobNotFoundException(e.getMessage(), htcJobId);
			}
		}
	}

	/**
	 * adding MPICH command if necessary
	 * @param ncpus if != 1, {@link #MPI_HOME} command prepended
	 * @param cmds command set
	 * @return new String
	 */
	private final String buildExeCommand(int ncpus,String command) {
		if (ncpus == 1) {
			return command;
		}
		final char SPACE = ' ';
		StringBuilder sb = new StringBuilder( );
		sb.append(MPI_HOME);
		sb.append("/bin/mpiexec -np ");
		sb.append(ncpus);
		sb.append(SPACE);
		sb.append(command);
		return sb.toString().trim( );
	}

	@Override
	public HtcProxy cloneThreadsafe() {
		return new SlurmProxy(getCommandService().clone(), getHtcUser());
	}

	@Override
	public String getSubmissionFileExtension() {
		return SLURM_SUBMISSION_FILE_EXT;
	}

	/**
	 * @param jobNamePrefix
	 * return jobs that start with prefix for current user
	 */
	@Override
	public List<HtcJobID> getRunningJobIDs(String jobNamePrefix) throws ExecutableException {
		String[] cmds = {Slurm_HOME + JOB_CMD_STATUS,"-f","-xml"};
		CommandOutput commandOutput = commandService.command(cmds);

		String output = commandOutput.getStandardOutput();
		return parseXML(output,jobNamePrefix);
	}

	@Override
	public Map<HtcJobID,HtcJobInfo> getJobInfos(List<HtcJobID> htcJobIDs) throws ExecutableException {
		HashMap<HtcJobID,HtcJobInfo> jobInfoMap = new HashMap<HtcJobID,HtcJobInfo>();
		for (HtcJobID htcJobID : htcJobIDs){
			HtcJobInfo htcJobInfo = getJobInfo(htcJobID);
			if (htcJobInfo!=null){
				jobInfoMap.put(htcJobID,htcJobInfo);
			}
		}
		return jobInfoMap;
	}

	private static final String PSYM_QINFO = "queue_info";
	private static final String PSYM_QLIST = "Queue-List";
	//private static final String PSYM_NAME = "name";
	private static final String PSYM_JLIST = "job_list";
	private static final String PSYM_JNAME = "JB_name";
	private static final String PSYM_JNUMBER = "JB_job_number";
	private static final String PSYM_STATE = "state";
	private static final String PSYM_JINFO = "job_info";

	/**
	 * @param xmlString string to parse (qstat output
	 * @param prefix to look for
	 * @return list of jobs, except ones already marked for deletion
	 */
	private List<HtcJobID> parseXML(String xmlString, String prefix) {
		if (LG.isTraceEnabled()) {
			LG.trace(xmlString);
		}
		try {
		statusMap.clear();
		List<HtcJobID>  jobList = new ArrayList<>();
		Document qstatDoc = XmlUtil.stringToXML(xmlString, null);
		Element rootElement = qstatDoc.getRootElement();
		Element qElement = rootElement.getChild(PSYM_QINFO);
		for (Element qList : XmlUtil.getChildren(qElement,PSYM_QLIST,Element.class) ) {
			//String name = qList.getChildText(PSYM_NAME);
			for (Element jlist : XmlUtil.getChildren(qList, PSYM_JLIST, Element.class)) {
				JobInfoAndStatus jias = parseJobInfo(jlist,prefix);
				if (jias != null) {
					HtcJobID id = jias.info.getHtcJobID();
					statusMap.put(id, jias);
					jobList.add(id);
				}
			}
		}
		for (Element ji : XmlUtil.getChildren(rootElement,PSYM_JINFO,Element.class) ) {
			for (Element jlist : XmlUtil.getChildren(ji, PSYM_JLIST, Element.class)) {
				parseJobInfo(jlist,prefix); //logs job submit errors to server log
			}
		}
		return jobList;
		} catch (Error e) {
			e.printStackTrace();
			throw e;
		}
	}

	private JobInfoAndStatus parseJobInfo(Element ji, String prefix) {
		String jname = ji.getChildText(PSYM_JNAME);
		if (prefix != null  && !jname.startsWith(prefix)) {
			return null;
		}
		String jn = ji.getChildText(PSYM_JNUMBER);
		String state = ji.getAttributeValue(PSYM_STATE);
		Element stateCodeE = ji.getChild(PSYM_STATE);
		String stateCode = stateCodeE.getValue();

		SlurmJobID id = new SlurmJobID(jn);
		HtcJobInfo hji = new HtcJobInfo(id,true,jname,null,null);
		SlurmJobStatus stat = SlurmJobStatus.parseStatus(state,stateCode);
		if (LG.isDebugEnabled()) {
			LG.debug("job " + jname + ' ' + state + ", " + stateCode + stat);
		}
		switch (stat) {
		case RUNNING:
		case PENDING:
		case EXITED:
			HtcJobStatus status = new HtcJobStatus(stat);
			return new JobInfoAndStatus(hji, status);
		case ERROR:
			LG.error("EXCEPTION-ERROR: Job "+ id.getJobNumber() + " Error " + XmlUtil.xmlToString(ji) );
		case DELETING:
		}
		return null;
	}

	/**
	 * @param htcJobID
	 * @return job info or null
	 */
	public HtcJobInfo getJobInfo(HtcJobID htcJobID) {
		return statusMap.get(htcJobID).info;
	}

	public String[] getEnvironmentModuleCommandPrefix() {
//		ArrayList<String> ar = new ArrayList<String>();
//		ar.add("source");
//		ar.add("/etc/profile.d/modules.sh;");
//		ar.add("module");
//		ar.add("load");
//		ar.add(PropertyLoader.getProperty(PropertyLoader.slurmModulePath, "htc/slurm")+";");
//		return ar.toArray(new String[0]);
		return new String[0];
	}

	/**
	 * write bash script for submission
	 * @param jobName
	 * @param sub_file
	 * @param commandSet
	 * @param ncpus
	 * @param memSize
	 * @param postProcessingCommands
	 * @return String containing script
	 */
	String generateScript(String jobName, ExecutableCommand.Container commandSet, int ncpus, double memSize, Collection<PortableCommand> postProcessingCommands) {
		final boolean isParallel = ncpus > 1;


		LineStringBuilder lsb = new LineStringBuilder();

		lsb.write("#!/bin/bash");
		String partition = "amd";
		lsb.write("#SBATCH -partition " + partition);
		lsb.write("#SBATCH -J " + jobName);
		lsb.write("#SBATCH -o " + htcLogDirString+jobName+".slurm.log");
		lsb.write("#SBATCH -e " + htcLogDirString+jobName+".slurm.log");
		//			sw.append("#$ -l mem=" + (int)(memSize + SLURM_MEM_OVERHEAD_MB) + "mb");

		//int JOB_MEM_OVERHEAD_MB = Integer.parseInt(PropertyLoader.getRequiredProperty(PropertyLoader.jobMemoryOverheadMB));

		//long jobMemoryMB = (JOB_MEM_OVERHEAD_MB+((long)memSize));
//		lsb.write("#$ -j y");
		//		    sw.append("#$ -l h_vmem="+jobMemoryMB+"m\n");
//		lsb.write("#$ -cwd");

		if (isParallel) {
			// #SBATCH
//			lsb.append("#$ -pe mpich ");
//			lsb.append(ncpus);
//			lsb.newline();
			
			lsb.append("#SBATCH -n " + ncpus);
			lsb.newline();

			lsb.append("#$ -v LD_LIBRARY_PATH=");
			lsb.append(MPI_HOME);
			lsb.write("/lib");
		}
		lsb.newline();
		final boolean hasExitProcessor = commandSet.hasExitCodeCommand();
		if (hasExitProcessor) {
			ExecutableCommand exitCmd = commandSet.getExitCodeCommand();
			lsb.write("callExitProcessor( ) {");
			lsb.append("\techo exitCommand = ");
			lsb.write(exitCmd.getJoinedCommands("$1"));
			lsb.append('\t');
			lsb.write(exitCmd.getJoinedCommands());
			lsb.write("}");
			lsb.write("echo");
		}

		for (ExecutableCommand ec: commandSet.getExecCommands()) {
			lsb.write("echo");
			String cmd= ec.getJoinedCommands();
			if (ec.isParallel()) {
				if (isParallel) {
					cmd = buildExeCommand(ncpus, cmd);
				}
				else {
					throw new UnsupportedOperationException("parallel command " + ec.getJoinedCommands() + " called in non-parallel submit");
				}
			}
			lsb.append("echo command = ");
			lsb.write(cmd);

			lsb.write(cmd);
			lsb.write("stat=$?");

			lsb.append("echo ");
			lsb.append(cmd);
			lsb.write("returned $stat");

			lsb.write("if [ $stat -ne 0 ]; then");
			if (hasExitProcessor) {
				lsb.write("\tcallExitProcessor $stat");
			}
			lsb.write("\techo returning $stat to Slurm");
			lsb.write("\texit $stat");
			lsb.write("fi");
		}

		Objects.requireNonNull(postProcessingCommands);
		PortableCommandWrapper.insertCommands(lsb.sb, postProcessingCommands);
		lsb.newline();
		if (hasExitProcessor) {
			lsb.write("callExitProcessor 0");
		}
		lsb.newline();
		return lsb.sb.toString();
	}

	@Override
	public SlurmJobID submitJob(String jobName, String sub_file, ExecutableCommand.Container commandSet, int ncpus, double memSize, Collection<PortableCommand> postProcessingCommands) throws ExecutableException {
		try {
			String text = generateScript(jobName, commandSet, ncpus, memSize, postProcessingCommands);

			File tempFile = File.createTempFile("tempSubFile", ".sub");

			writeUnixStyleTextFile(tempFile, text);

			// move submission file to final location (either locally or remotely).
			if (LG.isDebugEnabled()) {
				LG.debug("<<<SUBMISSION FILE>>> ... moving local file '"+tempFile.getAbsolutePath()+"' to remote file '"+sub_file+"'");
			}
			commandService.pushFile(tempFile,sub_file);
			if (LG.isDebugEnabled()) {
				LG.debug("<<<SUBMISSION FILE START>>>\n"+FileUtils.readFileToString(tempFile)+"\n<<<SUBMISSION FILE END>>>\n");
			}
			tempFile.delete();
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
			return null;
		}

		String[] completeCommand = new String[] {Slurm_HOME + JOB_CMD_SUBMIT, "-S","/bin/bash","-terse", sub_file};
		CommandOutput commandOutput = commandService.command(completeCommand);
		String jobid = commandOutput.getStandardOutput().trim();

		return new SlurmJobID(jobid);
	}

	/**
	 * package job info and status
	 */
	private static class JobInfoAndStatus {
		final HtcJobInfo info;
		final HtcJobStatus status;
		/**
		 * @param info not null
		 * @param status not null
		 */
		JobInfoAndStatus(HtcJobInfo info, HtcJobStatus status) {
			Objects.requireNonNull(info);
			Objects.requireNonNull(status);
			this.info = info;
			this.status = status;
		}
		@Override
		public String toString() {
			return info.toString() + ": "  + status.toString();
		}

	}
}
