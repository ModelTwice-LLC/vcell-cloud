package cbit.vcell.simdata;
import org.vcell.util.DataAccessException;
import org.vcell.util.document.User;
import org.vcell.util.document.VCDataIdentifier;

import cbit.plot.PlotData;
import cbit.vcell.client.data.OutputContext;
import cbit.vcell.math.Function;
import cbit.vcell.solver.DataProcessingOutput;
import cbit.vcell.solvers.CartesianMesh;
/**
 * Insert the type's description here.
 * Creation date: (10/3/00 3:21:23 PM)
 * @author: 
 */
public class ServerPDEDataContext extends PDEDataContext {
	private DataServerImpl dataServerImpl = null;
	private VCDataIdentifier vcDataID = null;
	private User user = null;
	private OutputContext outputContext = null;

/**
 * Insert the method's description here.
 * Creation date: (3/2/2001 12:38:48 AM)
 * @param dataSetController cbit.vcell.server.DataSetController
 * @param simulationIdentifier java.lang.String
 */
public ServerPDEDataContext(OutputContext outputContext,User user0, DataServerImpl dataServerImpl, VCDataIdentifier vcdID) throws Exception { 
	super();
	user = user0;
	setDataServerImpl(dataServerImpl);
	setVCDataIdentifier(vcdID);
	setOutputContext(outputContext);
	initialize();
}


/**
 * Insert the method's description here.
 * Creation date: (3/2/2001 12:36:13 AM)
 * @return cbit.vcell.server.DataSetController
 */
private DataServerImpl getDataServerImpl() {
	return dataServerImpl;
}


/**
 * gets list of named Functions defined for the resultSet for this Simulation.
 *
 * @returns array of functions, or null if no functions.
 *
 * @throws org.vcell.util.DataAccessException if SimulationInfo not found.
 *
 * @see Function
 */
public cbit.vcell.math.AnnotatedFunction[] getFunctions() throws org.vcell.util.DataAccessException {
	return getDataServerImpl().getFunctions(outputContext,user, vcDataID);
}


/**
 * retrieves a line scan (data sampled along a curve in space) for the specified simulation.
 *
 * @param variable name of variable to be sampled
 * @param time simulation time which is to be sampled.
 * @param spatialSelection spatial curve.
 *
 * @returns annotated array of 'concentration vs. distance' in a plot ready format.
 *
 * @throws org.vcell.util.DataAccessException if SimulationInfo not found.
 *
 * @see PlotData
 */
public cbit.plot.PlotData getLineScan(java.lang.String variable, double time, cbit.vcell.simdata.gui.SpatialSelection spatialSelection) throws org.vcell.util.DataAccessException {
	return getDataServerImpl().getLineScan(getOutputContext(),user, vcDataID, variable, time, spatialSelection);
}


/**
 * Insert the method's description here.
 * Creation date: (3/19/2004 11:31:29 AM)
 * @return cbit.vcell.simdata.SimDataBlock
 * @param varName java.lang.String
 * @param time double
 */
protected ParticleDataBlock getParticleDataBlock(double time) throws org.vcell.util.DataAccessException {
	return getDataServerImpl().getParticleDataBlock(user, vcDataID, time);
}


/**
 * Insert the method's description here.
 * Creation date: (3/19/2004 11:31:29 AM)
 * @return cbit.vcell.simdata.SimDataBlock
 * @param varName java.lang.String
 * @param time double
 */
protected SimDataBlock getSimDataBlock(java.lang.String varName, double time) throws org.vcell.util.DataAccessException {
	return getDataServerImpl().getSimDataBlock(getOutputContext(),user, vcDataID, varName, time);
}

public DataProcessingOutput retrieveDataProcessingOutput() throws org.vcell.util.DataAccessException {
	return getDataServerImpl().getDataProcessingOutput(user, vcDataID);
}


/**
 * retrieves a time series (single point as a function of time) of a specified spatial data set.
 *
 * @param variable name of variable to be sampled
 * @param index identifies index into data array.
 *
 * @returns annotated array of 'concentration vs. time' in a plot ready format.
 *
 * @throws org.vcell.util.DataAccessException if SimulationInfo not found.
 *
 * @see CartesianMesh for transformation between indices and coordinates.
 */
public org.vcell.util.document.TimeSeriesJobResults getTimeSeriesValues(org.vcell.util.document.TimeSeriesJobSpec timeSeriesJobSpec) throws org.vcell.util.DataAccessException {
	return getDataServerImpl().getTimeSeriesValues(getOutputContext(),user, vcDataID, timeSeriesJobSpec);
}


/**
 * Insert the method's description here.
 * Creation date: (3/2/2001 12:39:13 AM)
 * @return java.lang.String
 */
public VCDataIdentifier getVCDataIdentifier() {
	return vcDataID;
}


/**
 * Insert the method's description here.
 * Creation date: (10/3/00 5:16:19 PM)
 */
private void initialize() {
	try {
		setTimePoints(getDataServerImpl().getDataSetTimes(user, getVCDataIdentifier()));
		setDataIdentifiers(getDataServerImpl().getDataIdentifiers(getOutputContext(),user, getVCDataIdentifier()));
		setParticleData(getDataServerImpl().getParticleDataExists(user, getVCDataIdentifier()));
		setCartesianMesh(getDataServerImpl().getMesh(user, getVCDataIdentifier()));
		if (getTimePoints() != null && getTimePoints().length >0) {
			setTimePoint(getTimePoints()[0]);
		}
		if (getVariableNames() != null && getVariableNames().length > 0) {
			setVariableName(getVariableNames()[0]);
		}
	} catch (DataAccessException exc) {
		exc.printStackTrace(System.out);
	}
}


/**
 * This method was created in VisualAge.
 *
 * @param exportSpec cbit.vcell.export.server.ExportSpecs
 */
public void makeRemoteFile(cbit.vcell.export.server.ExportSpecs exportSpecs) throws org.vcell.util.DataAccessException {
	dataServerImpl.makeRemoteFile(getOutputContext(),user, exportSpecs);
}


/**
 * Insert the method's description here.
 * Creation date: (10/3/00 5:03:43 PM)
 */
public void refreshIdentifiers() {
	try {
		setDataIdentifiers(getDataServerImpl().getDataIdentifiers(getOutputContext(),user, vcDataID));
		if ( getVariableName() != null && !org.vcell.util.BeanUtils.arrayContains(getVariableNames(), getVariableName()) )  {
			// This condition occurs if a function has been removed from the dataset (esp. MergedDataset->compare).
			if (getDataIdentifiers() != null && getDataIdentifiers().length > 0) {
				setVariableName(getDataIdentifiers()[0].getName());
			}
		}		
		//
		//Added for cases where variable was set and server couldn't deliver data
		//if after referesh the variable is set again, the property won't propagate
		externalRefresh();
	} catch (DataAccessException exc) {
		exc.printStackTrace(System.out);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (10/3/00 5:03:43 PM)
 */
public void refreshTimes() {
	try {
		setTimePoints(getDataServerImpl().getDataSetTimes(user, getVCDataIdentifier()));
	} catch (DataAccessException exc) {
		exc.printStackTrace(System.out);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (3/2/2001 12:36:13 AM)
 * @param newDataSetController cbit.vcell.server.DataSetController
 */
private void setDataServerImpl(DataServerImpl newDataServerImpl) {
	dataServerImpl = newDataServerImpl;
}


/**
 * Insert the method's description here.
 * Creation date: (3/2/2001 12:39:13 AM)
 * @param newSimulationIdentifier java.lang.String
 */
private void setVCDataIdentifier(VCDataIdentifier newVcdID) {
	vcDataID = newVcdID;
}


private OutputContext getOutputContext() {
	return outputContext;
}


private void setOutputContext(OutputContext outputContext) {
	this.outputContext = outputContext;
}
}