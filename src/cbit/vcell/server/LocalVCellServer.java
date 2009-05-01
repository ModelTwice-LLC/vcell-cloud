package cbit.vcell.server;
import cbit.vcell.messaging.admin.ManageUtils;
import cbit.vcell.messaging.admin.ServerPerformance;
import cbit.vcell.messaging.server.LocalVCellConnectionMessaging;
import cbit.vcell.solvers.SolverController;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

import org.vcell.util.document.User;

import cbit.vcell.simdata.*;
import cbit.vcell.modeldb.ResultSetCrawler;
import cbit.vcell.export.server.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class LocalVCellServer extends UnicastRemoteObject implements VCellServer {
	private java.util.Vector<VCellConnection> vcellConnectionList = new Vector<VCellConnection>();
	private String hostName = null;
	private boolean bRemoteMode = false;
	private AdminDatabaseServer adminDbServer = null;
	private SessionLog sessionLog = null;
	private ConnectionPool connectionPool = null;
	private ResultSetCrawler resultSetCrawler = null;
	private Cachetable dataCachetable = null;
	private DataSetControllerImpl dscImpl = null;
	private SimulationControllerImpl simControllerImpl = null;
	private boolean bPrimaryServer = false;
	private User adminUser = null;
	private cbit.vcell.messaging.JmsConnectionFactory fieldJmsConnFactory = null;
	private String adminPassword = null;
	private ExportServiceImpl exportServiceImpl = null;
	private java.util.Date bootTime = new java.util.Date();

	private long CLEANUP_INTERVAL = 600*1000;	
	
/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public LocalVCellServer(boolean bPrimaryServer, String argHostName, cbit.vcell.messaging.JmsConnectionFactory jmsConnFactory, AdminDatabaseServer dbServer, ResultSetCrawler argResultSetCrawler, boolean bRemoteMode) throws RemoteException, FileNotFoundException {
	super(PropertyLoader.getIntProperty(PropertyLoader.rmiPortVCellServer,0));
	this.hostName = argHostName;
	this.fieldJmsConnFactory = jmsConnFactory;
	this.bRemoteMode = bRemoteMode;
	this.resultSetCrawler = argResultSetCrawler;
	adminDbServer = dbServer;
	this.bPrimaryServer = bPrimaryServer;
	this.sessionLog = new StdoutSessionLog(PropertyLoader.ADMINISTRATOR_ACCOUNT);
	this.dataCachetable = new Cachetable(10*Cachetable.minute);
	this.dscImpl = new DataSetControllerImpl(sessionLog,dataCachetable, 
			new File(PropertyLoader.getRequiredProperty(PropertyLoader.primarySimDataDirProperty)), 
			new File(PropertyLoader.getRequiredProperty(PropertyLoader.secondarySimDataDirProperty)));
	this.simControllerImpl = new SimulationControllerImpl(sessionLog,adminDbServer, this);
	this.exportServiceImpl = new ExportServiceImpl(sessionLog);
	if (bPrimaryServer){
		this.connectionPool = new ConnectionPool(sessionLog);
		this.connectionPool.ping();		
	}

	if (fieldJmsConnFactory != null) {
		Thread cleanupThread = new Thread() { 
			public void run() {
				setName("CleanupThread");
				cleanupConnections();
			}
		};
		cleanupThread.start();
	}
}


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public LocalVCellServer(boolean bPrimaryServer, String argHostName, AdminDatabaseServer dbServer, ResultSetCrawler argResultSetCrawler, boolean bRemoteMode) throws RemoteException, FileNotFoundException {
	this(bPrimaryServer, argHostName, null, dbServer, argResultSetCrawler, bRemoteMode);
}


/**
 * This method was created in VisualAge.
 * @param userid java.lang.String
 * @param password java.lang.String
 */
private synchronized void addVCellConnection(User user, String password) throws RemoteException, java.sql.SQLException, FileNotFoundException, javax.jms.JMSException {
	if (getVCellConnection0(user) == null) {
		VCellConnection localConn = null;
		if (fieldJmsConnFactory == null){
			localConn = new LocalVCellConnection(user, password, hostName, new StdoutSessionLog(user.getName()), this);
		} else {
			localConn = new cbit.vcell.messaging.server.LocalVCellConnectionMessaging(user, password, hostName, new StdoutSessionLog(user.getName()), fieldJmsConnFactory, this);
		}
		vcellConnectionList.addElement(localConn);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (4/16/2004 10:19:42 AM)
 */
public void cleanupConnections() {	
	if (fieldJmsConnFactory == null) {
		return;
	}
	
	while (true) {
		try {
			Thread.sleep(CLEANUP_INTERVAL);
		} catch (InterruptedException ex) {
		}

		sessionLog.print("Starting to clean up stale connections...");
		VCellConnection[] connections = new VCellConnection[vcellConnectionList.size()];
		vcellConnectionList.copyInto(connections);

		for (int i = 0; i < connections.length; i++){
			try {
				if (connections[i] instanceof cbit.vcell.messaging.server.LocalVCellConnectionMessaging) {
					LocalVCellConnectionMessaging messagingConnection = (LocalVCellConnectionMessaging)connections[i];

					if (messagingConnection != null && messagingConnection.isTimeout()) {
						synchronized (this) {
							vcellConnectionList.remove(messagingConnection);
							messagingConnection.close();							
						}
						sessionLog.print("Removed connection from " + messagingConnection.getUser());						
					}

				}
			} catch (Throwable ex) {
				sessionLog.exception(ex);
			}
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/01 6:04:11 PM)
 * @exception java.rmi.RemoteException The exception description.
 */
public SolverController createSolverController(User user, cbit.vcell.solver.SimulationJob simulationJob) throws cbit.vcell.solvers.SimExecutionException, cbit.vcell.solver.SolverException {
	try {
		cbit.vcell.solvers.SolverProxy solverProxy = getSimulationControllerImpl().getSolverProxy(user,simulationJob,new StdoutSessionLog(user.getName()));
		return solverProxy.getSolverController();
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.server.AdminDatabaseServer
 */
public AdminDatabaseServer getAdminDatabaseServer() {
	try {
		return adminDbServer;
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.CacheStatus
 */
public CacheStatus getCacheStatus() {
	try {
		return dataCachetable.getCacheStatus();
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.server.ConnectionPoolStatus
 * @exception java.rmi.RemoteException The exception description.
 */
public User[] getConnectedUsers() {
	try {
		Vector<User> userList = new Vector<User>();
		for (VCellConnection vcConn : vcellConnectionList) {
			if (!userList.contains(vcConn.getUser())){
				userList.addElement(vcConn.getUser());
			}
		}
		return (User[])org.vcell.util.BeanUtils.getArray(userList,User.class);
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataSetControllerImpl
 */
ConnectionPool getConnectionPool() {
	return connectionPool;
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.server.ConnectionPoolStatus
 * @exception java.rmi.RemoteException The exception description.
 */
public ConnectionPoolStatus getConnectionPoolStatus() {
	try {
		return connectionPool.getStatus();
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataSetControllerImpl
 */
public DataSetControllerImpl getDataSetControllerImpl() {
	return dscImpl;
}


/**
 * Insert the method's description here.
 * Creation date: (3/29/2001 4:04:58 PM)
 * @return cbit.vcell.export.server.ExportServiceImpl
 */
public ExportServiceImpl getExportServiceImpl() {
	return exportServiceImpl;
}


/**
 * This method was created in VisualAge.
 * @return int
 * @exception java.rmi.RemoteException The exception description.
 */
private int getNumLocalJobs() throws RemoteException {

	int numControllers = 0;
	int numRunning = 0;
	
	cbit.vcell.solvers.SolverControllerInfo infos[] = getSimulationControllerImpl().getSolverControllerInfos();
	if (infos!=null){
		for (int j=0;j<infos.length;j++){
			numControllers++;
			if (infos[j].isRunning()){
				numRunning++;
			}
		}
	}

	return numRunning;
}


/**
 * This method was created in VisualAge.
 * @return int
 */
private int getNumProcessors() {
	int numProcs = 1;
	try {
		String numProcessors = System.getProperty(PropertyLoader.numProcessorsProperty);
		numProcs = Integer.parseInt(numProcessors);
	}catch (Exception e){
		System.out.println("localVCellServer.getNumProcessors(), property not set, assuming numProcessors=1");
	}
	return numProcs;
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.server.ProcessStatus
 * @exception java.rmi.RemoteException The exception description.
 */
public ProcessStatus getProcessStatus() {
	try {
		ServerPerformance sp = ManageUtils.getDaemonPerformance();
		return new ProcessStatus(getNumLocalJobs(),getNumProcessors(),sp.getFractionFreeCPU(),sp.getFreeMemoryBytes(),sp.getFreeJavaMemoryBytes(),sp.getTotalJavaMemoryBytes(),sp.getMaxJavaMemoryBytes(),bootTime);
	} catch (Throwable e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * Insert the method's description here.
 * Creation date: (2/14/01 9:46:18 AM)
 * @return cbit.vcell.modeldb.ResultSetCrawler
 */
ResultSetCrawler getResultSetCrawler() {
	return resultSetCrawler;
}


/**
 * Insert the method's description here.
 * Creation date: (12/9/2002 12:58:00 AM)
 * @return cbit.vcell.server.ServerInfo
 */
public ServerInfo getServerInfo() {
	return new ServerInfo(hostName,getCacheStatus(),getProcessStatus(),getConnectedUsers());
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.simdata.DataSetControllerImpl
 */
SimulationControllerImpl getSimulationControllerImpl() {
	return simControllerImpl;
}


/**
 * Insert the method's description here.
 * Creation date: (12/9/2002 12:58:00 AM)
 * @return cbit.vcell.server.ServerInfo
 */
public ServerInfo[] getSlaveServerInfos() {
	ConnectionPoolStatus connPoolStatus = getConnectionPoolStatus();
	ComputeHost activeHosts[] = connPoolStatus.getActiveHosts();
	Vector<ServerInfo> slaveServerInfoList = new Vector<ServerInfo>();
	for (int i = 0;activeHosts!=null && i < activeHosts.length; i++){
		try {
			VCellServer slaveVCellServer = getSlaveVCellServer(activeHosts[i].getHostName());
			ServerInfo slaveServerInfo = slaveVCellServer.getServerInfo();
			slaveServerInfoList.add(slaveServerInfo);
		}catch(ConnectionException e){
			sessionLog.alert("active computeHost "+activeHosts[i].getHostName()+" not available: "+e.getMessage());
		}catch(RemoteException e){
			sessionLog.alert("active computeHost "+activeHosts[i].getHostName()+" not available: "+e.getMessage());
		}catch(Throwable e){
			sessionLog.exception(e);
			sessionLog.alert("failure retrieving ServerInfo for active computeHost "+activeHosts[i].getHostName());
		}
	}
	return (ServerInfo[])org.vcell.util.BeanUtils.getArray(slaveServerInfoList,ServerInfo.class);
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.server.VCellServer
 * @param host java.lang.String
 */
public VCellServer getSlaveVCellServer(String host) throws ConnectionException, AuthenticationException, DataAccessException {
	try {
		RMIVCellServerFactory factory = new RMIVCellServerFactory(host,adminUser,adminPassword);
		VCellServer remoteServer = factory.getVCellServer();
		if (remoteServer.isPrimaryServer()){
			throw new AuthenticationException("VCellServer.getSlaveVCellServer() cannot return a primary server");
		}
		return remoteServer;
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * Insert the method's description here.
 * Creation date: (7/18/01 12:50:06 PM)
 * @return cbit.vcell.solvers.SolverControllerInfo
 */
public cbit.vcell.solvers.SolverControllerInfo[] getSolverControllerInfos() {
	try {
		return simControllerImpl.getSolverControllerInfos();
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
public VCellConnection getVCellConnection(User user) {
	try {
		return getVCellConnection0(user);
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
VCellConnection getVCellConnection(String userid, String password) throws RemoteException, java.sql.SQLException, DataAccessException, FileNotFoundException, AuthenticationException, javax.jms.JMSException {
	VCellConnection localConnection = null;
	//Authenticate User
	User user = null;
	try{
		synchronized (adminDbServer) {
			user = adminDbServer.getUser(userid, password);
			if (user == null){
				throw new AuthenticationException("The userid (" + userid + ") or password you entered is not correct. Please go to Server->Change User... to reenter your userid and password or click \"Forgot Login Password\"");
			}
		}
	}catch(DataAccessException e){
		throw new DataAccessException("getVcellConnection User Authentication Database Access SQL Error " + e.getMessage());
	}
	//
	// get existing VCellConnection
	//
	localConnection = getVCellConnection0(user);

	//
	// if doesn't exist, create new one
	//
	if (localConnection == null) {
		addVCellConnection(user,password);
		localConnection = getVCellConnection0(user);
		if (localConnection==null){
			sessionLog.print("LocalVCellServer.getVCellConnecytion("+user.getName()+") unable to create VCellConnection");
			throw new DataAccessException("unable to create VCellConnection");
		}
	}

	if (bRemoteMode) {
		sessionLog.print("getVCellConnection(" + user.getName() + "), returning remote copy of VCellConnection");
	} else {
		sessionLog.print("getVCellConnection(" + user.getName() + "), returning local copy of VCellConnection");
	}
	//
	//Update UserStat.  Do not fail login if UserStat fails.
	//
	try{
		getAdminDatabaseServer().updateUserStat(user.getName());
	}catch(Exception e){
		e.printStackTrace();
		//Ignore
	}
	return localConnection;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
private synchronized VCellConnection getVCellConnection0(User user) {
	//
	// Lookup existing VCellConnections
	//
	for (VCellConnection vcc : vcellConnectionList) {
		if (vcc instanceof LocalVCellConnection){
			LocalVCellConnection lvcc = (LocalVCellConnection)vcc;
			if (lvcc.getUser().compareEqual(user)) {
				return lvcc;
			}
		}else if (vcc instanceof cbit.vcell.messaging.server.LocalVCellConnectionMessaging){
			cbit.vcell.messaging.server.LocalVCellConnectionMessaging lvccm = (cbit.vcell.messaging.server.LocalVCellConnectionMessaging)vcc;
			if (lvccm.getUser().compareEqual(user)) {
				return lvccm;
			}
		}
	}

	return null;
}


/**
 * This method was created in VisualAge.
 * @return boolean
 * @exception java.rmi.RemoteException The exception description.
 */
public boolean isPrimaryServer() {
	try {
		return bPrimaryServer;
	}catch (Throwable e){
		sessionLog.exception(e);
		throw new RuntimeException(e.getMessage());
	}
}


/**
 * This method was created in VisualAge.
 * @param userid java.lang.String
 * @param password java.lang.String
 */
void setAdminAccount(User user, String password) {
	this.adminUser = user;
	this.adminPassword = password;
}


/**
 * Insert the method's description here.
 * Creation date: (1/30/2003 10:46:58 AM)
 * @exception java.rmi.RemoteException The exception description.
 */
public void shutdown() throws java.rmi.RemoteException {
	String tempHost = "local";
	try {
		tempHost = getClientHost();
	}catch(ServerNotActiveException e){
		e.printStackTrace(System.out);
	}
	final String clientHost = tempHost;
	
	Runnable killJob = new Runnable() {
		public void run() {
			try {
				sessionLog.alert("LocalVCellServer.shutdown():  killing all running Simulations (on "+hostName+"), request from "+clientHost);
				try {
					SimulationControllerImpl simControllerImpl = getSimulationControllerImpl();
					cbit.vcell.solvers.SolverControllerInfo solverControllerInfos[] = simControllerImpl.getSolverControllerInfos();
					for (int i = 0; i < solverControllerInfos.length; i++){
						try {
							cbit.vcell.solver.SimulationInfo simInfo = solverControllerInfos[i].getSimulationInfo();
							User jobOwner = simInfo.getVersion().getOwner();
							cbit.vcell.solver.SolverStatus solverStatus = simControllerImpl.getSolverStatus(jobOwner,simInfo, solverControllerInfos[i].jobIndex);
							if (solverStatus.isRunning()){
								sessionLog.alert("LocalVCellServer.shutdown(): aborting running simulation "+simInfo.getAuthoritativeVCSimulationIdentifier()+" owned by user "+jobOwner);
								simControllerImpl.stopSimulation(jobOwner,simInfo.getAuthoritativeVCSimulationIdentifier(),solverControllerInfos[i].jobIndex);
							}
						}catch (Throwable e){
							sessionLog.alert("LocalVCellServer.shutdown(): failure shutting down or getting status on job "+solverControllerInfos[i]);
							e.printStackTrace(System.out);
						}
					}
				}catch (Throwable e){
					sessionLog.alert("LocalVCellServer.shutdown(): failure getting simulations to shutdown");
					e.printStackTrace(System.out);
				}
				
				sessionLog.alert("LocalVCellServer.shutdown():  waiting for job abort messages to be sent (30 seconds)");
				System.out.flush();
				try {
					Thread.sleep(30000);
				}catch (InterruptedException e){
				}
				sessionLog.alert("LocalVCellServer.shutdown():  shutting down server (on "+hostName+") in 5 seconds, request from "+clientHost);
				System.out.flush();
				try {
					Thread.sleep(5000);
				}catch (InterruptedException e){
				}
			}finally{
				System.exit(1);
			}
		}
	};
	Thread killThread = new Thread(killJob);
	killThread.start();
}
}