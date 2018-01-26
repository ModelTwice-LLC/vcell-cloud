/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.server;

import org.vcell.solver.nfsim.NFSimMolecularConfigurations;
import org.vcell.util.DataAccessException;
import org.vcell.util.document.VCDataIdentifier;
import org.vcell.vis.io.VtuFileContainer;
import org.vcell.vis.io.VtuVarInfo;

import cbit.plot.PlotData;
import cbit.rmi.event.ExportEvent;
import cbit.vcell.export.server.ExportSpecs;
import cbit.vcell.field.io.FieldDataFileOperationResults;
import cbit.vcell.field.io.FieldDataFileOperationSpec;
import cbit.vcell.message.server.bootstrap.client.RemoteProxyVCellConnectionFactory.RemoteProxyException;
import cbit.vcell.simdata.DataIdentifier;
import cbit.vcell.simdata.DataOperation;
import cbit.vcell.simdata.DataOperationResults;
import cbit.vcell.simdata.DataSetMetadata;
import cbit.vcell.simdata.DataSetTimeSeries;
import cbit.vcell.simdata.OutputContext;
import cbit.vcell.simdata.SpatialSelection;
import cbit.vcell.solver.AnnotatedFunction;
import cbit.vcell.solvers.CartesianMesh;
/**
 * This interface was generated by a SmartGuide.
 * 
 */
public interface DataSetController {
public FieldDataFileOperationResults fieldDataFileOperation(FieldDataFileOperationSpec fieldDataFileOperationSpec) throws RemoteProxyException, DataAccessException;


/**
 * This method was created by a SmartGuide.
 * @exception RemoteProxyException The exception description.
 */
public DataIdentifier[] getDataIdentifiers(OutputContext outputContext, VCDataIdentifier vcdataID) throws RemoteProxyException, DataAccessException;
/**
 * This method was created by a SmartGuide.
 * @exception RemoteProxyException The exception description.
 */
public double[] getDataSetTimes(VCDataIdentifier vcdataID) throws RemoteProxyException, DataAccessException;

public double[] getVtuTimes(VCDataIdentifier vcdataID) throws RemoteProxyException, DataAccessException;
/**
 * Insert the method's description here.
 * Creation date: (10/11/00 6:21:10 PM)
 * @param function cbit.vcell.math.Function
 * @exception org.vcell.util.DataAccessException The exception description.
 * @exception RemoteProxyException The exception description.
 */
 AnnotatedFunction[] getFunctions(OutputContext outputContext,VCDataIdentifier vcdataID) throws DataAccessException, RemoteProxyException; 
/**
 * This method was created by a SmartGuide.
 * @return cbit.plot.PlotData
 * @param variable java.lang.String
 * @param time double
 * @param spatialSelection cbit.vcell.simdata.gui.SpatialSelection
 * @exception RemoteProxyException The exception description.
 */
public PlotData getLineScan(OutputContext outputContext, VCDataIdentifier vcdataID, String variable, double time, SpatialSelection spatialSelection) throws RemoteProxyException, DataAccessException;
/**
 * @param outputContext 
 * This method was created in VisualAge.
 * @return CartesianMesh
 * @throws  
 */

public VtuFileContainer getEmptyVtuMeshFiles(VCDataIdentifier vcdataID, int timeIndex) throws RemoteProxyException, DataAccessException;


public double[] getVtuMeshData(OutputContext outputContext, VCDataIdentifier vcdataID, VtuVarInfo var, double time) throws RemoteProxyException, DataAccessException;

public NFSimMolecularConfigurations getNFSimMolecularConfigurations(VCDataIdentifier vcdataID) throws RemoteProxyException, DataAccessException;

CartesianMesh getMesh(VCDataIdentifier vcdataID) throws RemoteProxyException, DataAccessException;
/**
 * Insert the method's description here.
 * Creation date: (1/13/00 6:21:10 PM)
 * @param odeSimData cbit.vcell.export.data.ODESimData
 * @exception org.vcell.util.DataAccessException The exception description.
 * @exception RemoteProxyException The exception description.
 */
cbit.vcell.solver.ode.ODESimData getODEData(VCDataIdentifier vcdataID) throws DataAccessException, RemoteProxyException;
/**
 * This method was created in VisualAge.
 * @return ParticleData
 * @param time double
 * @exception org.vcell.util.DataAccessException The exception description.
 * @exception RemoteProxyException The exception description.
 */
cbit.vcell.simdata.ParticleDataBlock getParticleDataBlock(VCDataIdentifier vcdataID, double time) throws DataAccessException, RemoteProxyException;
/**
 * This method was created in VisualAge.
 * @return boolean
 */

public DataSetMetadata getDataSetMetadata(VCDataIdentifier vcdataID) throws DataAccessException, RemoteProxyException;

public DataSetTimeSeries getDataSetTimeSeries(VCDataIdentifier vcdataID, String[] variableNames) throws DataAccessException, RemoteProxyException;

public DataOperationResults doDataOperation(DataOperation dataOperation) throws DataAccessException, RemoteProxyException;


public boolean getParticleDataExists(VCDataIdentifier vcdataID) throws DataAccessException, RemoteProxyException;
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 * @exception RemoteProxyException The exception description.
 */
public cbit.vcell.simdata.SimDataBlock getSimDataBlock(OutputContext outputContext, VCDataIdentifier vcdataID, String varName, double time) throws RemoteProxyException, DataAccessException;
/**
 * This method was created by a SmartGuide.
 * @return double[]
 * @param varName java.lang.String
 * @param x int
 * @param y int
 * @param z int
 * @exception RemoteProxyException The exception description.
 */
public org.vcell.util.document.TimeSeriesJobResults getTimeSeriesValues(OutputContext outputContext, VCDataIdentifier vcdataID, org.vcell.util.document.TimeSeriesJobSpec timeSeriesJobSpec) throws RemoteProxyException, DataAccessException;
/**
 * Insert the method's description here.
 * Creation date: (10/22/2001 2:53:44 PM)
 * @return cbit.rmi.event.ExportEvent
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 * @exception org.vcell.util.DataAccessException The exception description.
 * @exception RemoteProxyException The exception description.
 */
ExportEvent makeRemoteFile(OutputContext outputContext,ExportSpecs exportSpecs) throws DataAccessException, RemoteProxyException;


public VtuVarInfo[] getVtuVarInfos(OutputContext outputContext,	VCDataIdentifier vcDataIdentifier) throws DataAccessException, RemoteProxyException;

}
