/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.optimization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.vcell.api.client.VCellApiClient;
import org.vcell.optimization.jtd.OptProblem;
import org.vcell.optimization.jtd.OptResultSet;
import org.vcell.optimization.jtd.Vcellopt;
import org.vcell.optimization.jtd.VcelloptStatus;
import org.vcell.util.ClientTaskStatusSupport;
import org.vcell.util.UserCancelException;

import cbit.vcell.mapping.SimulationContext.MathMappingCallback;
import cbit.vcell.math.Function;
import cbit.vcell.math.FunctionColumnDescription;
import cbit.vcell.math.MathException;
import cbit.vcell.math.ODESolverResultSetColumnDescription;
import cbit.vcell.math.RowColumnResultSet;
import cbit.vcell.modelopt.ParameterEstimationTask;
import cbit.vcell.opt.OptSolverResultSet;
import cbit.vcell.opt.OptSolverResultSet.OptRunResultSet;
import cbit.vcell.opt.OptimizationException;
import cbit.vcell.opt.OptimizationResultSet;
import cbit.vcell.opt.OptimizationStatus;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.resource.PropertyLoader;
import cbit.vcell.solver.SimulationSymbolTable;
import cbit.vcell.solver.ode.ODESolverResultSet;


public class CopasiOptimizationSolver {	
	
	public static OptimizationResultSet solveLocalPython(ParameterEstimationTaskSimulatorIDA parestSimulator, ParameterEstimationTask parameterEstimationTask, CopasiOptSolverCallbacks optSolverCallbacks, MathMappingCallback mathMappingCallback) 
							throws IOException, ExpressionException, OptimizationException {
		
		File dir = Files.createTempDirectory("parest",new FileAttribute<?>[] {}).toFile();
		try {
			String prefix = "testing_"+Math.abs(new Random().nextInt(10000));

			parameterEstimationTask.refreshMappings();

			OptProblem optProblem = CopasiUtils.paramTaskToOptProblem(parameterEstimationTask);

			Vcellopt optRun = CopasiUtils.runCopasiParameterEstimation(optProblem);

			OptResultSet optResultSet = optRun.getOptResultSet();
			int numFittedParameters = optResultSet.getOptParameterValues().size();
			String[] paramNames = new String[numFittedParameters];
			double[] paramValues = new double[numFittedParameters];
			int pIndex=0;
			for (Map.Entry<String, Double> entry : optResultSet.getOptParameterValues().entrySet()){
				paramNames[pIndex] = entry.getKey();
				paramValues[pIndex] = entry.getValue();
				pIndex++;
			}

			OptimizationStatus status = new OptimizationStatus(OptimizationStatus.NORMAL_TERMINATION, optRun.getStatusMessage());
			OptRunResultSet optRunResultSet = new OptRunResultSet(paramValues,optResultSet.getObjectiveFunction(),optResultSet.getNumFunctionEvaluations(),status);
			OptSolverResultSet copasiOptSolverResultSet = new OptSolverResultSet(paramNames, optRunResultSet);
			RowColumnResultSet copasiRcResultSet = parestSimulator.getRowColumnRestultSetByBestEstimations(parameterEstimationTask, paramNames, paramValues);
			OptimizationResultSet copasiOptimizationResultSet = new OptimizationResultSet(copasiOptSolverResultSet, copasiRcResultSet);

			return copasiOptimizationResultSet;
		} catch (Throwable e){
			e.printStackTrace(System.out);
			throw new OptimizationException(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		} finally {
			if (dir!=null && dir.exists()){
				FileUtils.deleteDirectory(dir);
			}
		}
	}

	private static final String STOP_REQUESTED = "stop requested";
	public static OptimizationResultSet solveRemoteApi(
			ParameterEstimationTaskSimulatorIDA parestSimulator,
			ParameterEstimationTask parameterEstimationTask, 
			CopasiOptSolverCallbacks optSolverCallbacks,
			MathMappingCallback mathMappingCallback,
			ClientTaskStatusSupport clientTaskStatusSupport) 
					throws IOException, ExpressionException, OptimizationException {

		return solveLocalPython(parestSimulator,parameterEstimationTask, optSolverCallbacks, mathMappingCallback);

//		try {
//			if(clientTaskStatusSupport != null) {
//				clientTaskStatusSupport.setMessage("Generating opt problem...");
//			}
//			OptProblem optProblem = CopasiUtils.paramTaskToOptProblem(parameterEstimationTask);
//
//			boolean bIgnoreCertProblems = PropertyLoader.getBooleanProperty(PropertyLoader.sslIgnoreCertProblems,false);
//			boolean bIgnoreHostMismatch = PropertyLoader.getBooleanProperty(PropertyLoader.sslIgnoreHostMismatch,false);;
//
//			// e.g. vcell.serverhost=vcellapi.cam.uchc.edu:443
//			String serverHost = PropertyLoader.getRequiredProperty(PropertyLoader.vcellServerHost);
//			String[] parts = serverHost.split(":");
//			String host = parts[0];
//			int port = Integer.parseInt(parts[1]);
//			VCellApiClient apiClient = new VCellApiClient(host, port, bIgnoreCertProblems, bIgnoreHostMismatch);
//
//			Gson gson = new Gson();
//			String optProblemJson = gson.toJson(optProblem);
//
//			if(clientTaskStatusSupport != null) {
//				clientTaskStatusSupport.setMessage("Submitting opt problem...");
//			}
//			//Submit but allow user to get out from restlet blocking call
//			final String[] optIdHolder = new String[] {null};
//			final Exception[] exceptHolder = new Exception[] {null};
//			Thread submitThread = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						optIdHolder[0]= apiClient.submitOptimization(optProblemJson);
//						if(optSolverCallbacks.getStopRequested()) {
//							apiClient.getOptRunJson(optIdHolder[0],optSolverCallbacks.getStopRequested());
//						}
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						exceptHolder[0] = e;
//					}
//				}
//			});
//			submitThread.setDaemon(true);
//			submitThread.start();
//
//			while(optIdHolder[0] == null && exceptHolder[0]==null && !optSolverCallbacks.getStopRequested()) {
//				Thread.sleep(200);
//			}
//			if(exceptHolder[0]!=null) {
//				throw exceptHolder[0];
//			}
//			if(optSolverCallbacks.getStopRequested()) {
//				throw UserCancelException.CANCEL_GENERIC;
//			}
//			final long TIMEOUT_MS = 1000*200; // 200 second timeout
//			long startTime = System.currentTimeMillis();
//			Vcellopt optRun = null;
//			if(clientTaskStatusSupport != null) {
//				clientTaskStatusSupport.setMessage("Waiting for progress...");
//			}
//			while ((System.currentTimeMillis()-startTime)<TIMEOUT_MS) {
//				String optRunJson = apiClient.getOptRunJson(optIdHolder[0],optSolverCallbacks.getStopRequested());
//				if (optSolverCallbacks.getStopRequested()){
//					throw UserCancelException.CANCEL_GENERIC;
//				}
//				if(optRunJson.startsWith(VcelloptStatus.QUEUED.name()+":")) {
//					SwingUtilities.invokeLater(new Runnable() {
//						@Override
//						public void run() {
//							optSolverCallbacks.setEvaluation(0, 0, 0, optSolverCallbacks.getEndValue(), 0);
//						}
//					});
//					if(clientTaskStatusSupport != null) {
//						clientTaskStatusSupport.setMessage("Queued...");
//					}
//
//				}else if(optRunJson.startsWith("Failed:") || optRunJson.startsWith("exception:") || optRunJson.startsWith("Exception:")) {
//					SwingUtilities.invokeLater(new Runnable() {
//						@Override
//						public void run() {
//							optSolverCallbacks.setEvaluation(0, 0, 0, optSolverCallbacks.getEndValue(), 0);
//						}
//					});
//					if(clientTaskStatusSupport != null) {
//						clientTaskStatusSupport.setMessage(optRunJson);
//					}
//
//				}else if(optRunJson.startsWith(VcelloptStatus.RUNNING.name()+":")) {
//					SwingUtilities.invokeLater(new Runnable() {
//						@Override
//						public void run() {
//							try {
//								StringTokenizer st = new StringTokenizer(optRunJson," :\t\r\n");
//								if(st.countTokens() != 4) {
//									System.out.println(optRunJson);
//									return;
//								}
//								st.nextToken();//OptRunStatus mesg
//								int runNum = Integer.parseInt(st.nextToken());
//								double objFunctionValue = Double.parseDouble(st.nextToken());
//								int numObjFuncEvals = Integer.parseInt(st.nextToken());
//								SwingUtilities.invokeLater(new Runnable() {
//									@Override
//									public void run() {
//										optSolverCallbacks.setEvaluation(numObjFuncEvals, objFunctionValue, 1.0, null, runNum);
//									}
//								});
//							} catch (Exception e) {
//								System.out.println(optRunJson);
//								e.printStackTrace();
//							}
//						}
//					});
//					if(clientTaskStatusSupport != null) {
//						clientTaskStatusSupport.setMessage("Running...");
//					}
//
//				}else {
//					optRun = gson.fromJson(optRunJson,Vcellopt.class);
//					VcelloptStatus status = optRun.getStatus();
//					String statusMessage = optRun.getStatusMessage();
//					if (statusMessage != null && (statusMessage.toLowerCase().startsWith(VcelloptStatus.COMPLETE.name().toLowerCase()))) {
//						final Vcellopt or2 = optRun;
//						SwingUtilities.invokeLater(new Runnable() {
//							@Override
//							public void run() {
////								Double endValue = null;
////								for (org.vcell.optimization.thrift.CopasiOptimizationParameter cop : or2.getOptProblem().getOptimizationMethod().getOptimizationParameterList()) {
////									if (cop.getParamType().name().equals(CopasiOptimizationParameterType.Number_of_Generations.name())
////											|| cop.getParamType().name().equals(CopasiOptimizationParameterType.IterationLimit.name())){
////										endValue = cop.getValue();
////										break;
////									}
////								}
//								optSolverCallbacks.setEvaluation((int)or2.getOptResultSet().getNumFunctionEvaluations(), or2.getOptResultSet().getObjectiveFunction(),
//										1.0, null, or2.getOptProblem().getNumberOfOptimizationRuns());
//							}
//						});
//
////						}
//					}
//					if (status==VcelloptStatus.COMPLETE){
//						System.out.println("job "+optIdHolder[0]+": status "+status+" "+optRun.getOptResultSet().toString());
//						if(clientTaskStatusSupport != null) {
//							clientTaskStatusSupport.setProgress(100);
//						}
//						break;
//					}
//					if (status==VcelloptStatus.FAILED){
//						throw new RuntimeException("optimization failed, message="+optRun.getStatusMessage());
//					}
//					System.out.println("job "+optIdHolder[0]+": status "+status);
//				}
//				try {
//					Thread.sleep(2000);
//				}catch (InterruptedException e){}
//			}
//			if((System.currentTimeMillis()-startTime) >= TIMEOUT_MS) {
//				throw new RuntimeException("optimization timed out.");
//			}
//			System.out.println("done with optimization");
//			OptResultSet optResultSet = optRun.getOptResultSet();
//			if(optResultSet == null) {
//				throw new RuntimeException("optResultSet is null, status is " + optRun.getStatusMessage());
//			}
//			if(optResultSet != null && optResultSet.getOptParameterValues() == null) {
//				throw new RuntimeException("getOptParameterValues is null, status is " + optRun.getStatusMessage());
//			}
//			if(clientTaskStatusSupport != null) {
//				clientTaskStatusSupport.setMessage("Done, getting results...");
//			}
//
//			int numFittedParameters = optResultSet.getOptParameterValues().size();
//			String[] paramNames = new String[numFittedParameters];
//			double[] paramValues = new double[numFittedParameters];
//			int pIndex = 0;
//			for (Map.Entry<String, Double> entry : optResultSet.getOptParameterValues().entrySet()){
//				paramNames[pIndex] = entry.getKey();
//				paramValues[pIndex] = entry.getValue();
//				pIndex++;
//			}
//
//			OptimizationStatus status = new OptimizationStatus(OptimizationStatus.NORMAL_TERMINATION,optRun.getStatusMessage());
//			OptRunResultSet optRunResultSet = new OptRunResultSet(paramValues, optResultSet.getObjectiveFunction(),optResultSet.getNumFunctionEvaluations(), status);
//			OptSolverResultSet copasiOptSolverResultSet = new OptSolverResultSet(paramNames, optRunResultSet);
//			RowColumnResultSet copasiRcResultSet = parestSimulator.getRowColumnRestultSetByBestEstimations(parameterEstimationTask, paramNames, paramValues);
//			OptimizationResultSet copasiOptimizationResultSet = new OptimizationResultSet(copasiOptSolverResultSet,copasiRcResultSet);
//
//			System.out.println("-----------SOLUTION FROM VCellAPI---------------\n" + optResultSet.toString());
//
//			return copasiOptimizationResultSet;
//		}catch(UserCancelException e) {
//			throw e;
//		} catch (Exception e) {
//			e.printStackTrace(System.out);
//			throw new OptimizationException(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
//		}
	}
		
	private static ODESolverResultSet getOdeSolverResultSet(RowColumnResultSet rcResultSet, SimulationSymbolTable simSymbolTable, String[] parameterNames, double[] parameterValues){
		//
		// get simulation results - copy from RowColumnResultSet into OdeSolverResultSet
		//
		
		ODESolverResultSet odeSolverResultSet = new ODESolverResultSet();
		for (int i = 0; i < rcResultSet.getDataColumnCount(); i++){
			odeSolverResultSet.addDataColumn(new ODESolverResultSetColumnDescription(rcResultSet.getColumnDescriptions(i).getName()));
		}
		for (int i = 0; i < rcResultSet.getRowCount(); i++){
			odeSolverResultSet.addRow(rcResultSet.getRow(i));
		}
		//
		// add appropriate Function columns to result set
		//
		Function functions[] = simSymbolTable.getFunctions();
		for (int i = 0; i < functions.length; i++){
			if (SimulationSymbolTable.isFunctionSaved(functions[i])){
				Expression exp1 = new Expression(functions[i].getExpression());
				try {
					exp1 = simSymbolTable.substituteFunctions(exp1).flatten();
					//
					// substitute in place all "optimization parameter" values.
					//
					for (int j = 0; parameterNames!=null && j < parameterNames.length; j++) {
						exp1.substituteInPlace(new Expression(parameterNames[j]), new Expression(parameterValues[j]));
					}
				} catch (MathException e) {
					e.printStackTrace(System.out);
					throw new RuntimeException("Substitute function failed on function "+functions[i].getName()+" "+e.getMessage());
				} catch (ExpressionException e) {
					e.printStackTrace(System.out);
					throw new RuntimeException("Substitute function failed on function "+functions[i].getName()+" "+e.getMessage());
				}
				
				try {
					FunctionColumnDescription cd = new FunctionColumnDescription(exp1.flatten(),functions[i].getName(), null, functions[i].getName(), false);
					odeSolverResultSet.addFunctionColumn(cd);
				}catch (ExpressionException e){
					e.printStackTrace(System.out);
				}
			}
		}
		return odeSolverResultSet;
	}
	
}
