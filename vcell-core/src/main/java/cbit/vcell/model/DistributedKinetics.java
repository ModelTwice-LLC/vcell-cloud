/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.model;

import java.beans.PropertyVetoException;
import java.util.Vector;

import cbit.vcell.math.MathUtilities;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.model.Kinetics.KineticsProxyParameter;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.model.Structure.StructureSize;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.ExpressionUtils;
import cbit.vcell.units.VCUnitDefinition;


/**
 * DistributedKinetics is the abstract superclass of all reaction kinetics that operate locally (can be defined at a point)
 * and form the basis for distributed parameter, spatial modeling.  This is the "text-book" description of chemical 
 * kinetics (in terms of time rate of change of local concentration).
 * 
 * For electrical transport, current density describes the local charge transport across a unit area of membrane rather than
 * the "lumped" description (total current crossing the entire membrane).
 * 
 * For nonspatial descriptions, this is a less convenient form for some users because it requires separately defining
 * compartment size and time rate of change of concentrations, where only their product matters.  For spatial applications,
 * this is the only form that can give rise to spatially inhomogeneous behavior.
 * 
 * A DistributedKinetics may be formed from a corresponding LumpedKinetics by assuming that the LumpedKinetics can be
 * uniformly distributed within a compartment of known size. 
 *
 * @see LumpedKinetics
 *
 */
public abstract class DistributedKinetics extends Kinetics {

	public DistributedKinetics(String name, ReactionStep reactionStep) {
		super(name, reactionStep);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (10/14/2003 8:53:00 AM)
	 * @return cbit.vcell.model.Parameter
	 */
	public final KineticsParameter getCurrentDensityParameter() {
		return getKineticsParameterFromRole(ROLE_CurrentDensity);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (10/14/2003 8:53:00 AM)
	 * @return cbit.vcell.model.Parameter
	 */
	public final KineticsParameter getReactionRateParameter() {
		return getKineticsParameterFromRole(ROLE_ReactionRate);
	}
	
	public final KineticsParameter getAuthoritativeParameter(){
		if (getKineticsDescription().isElectrical()){
			return getCurrentDensityParameter();
		}else{
			return getReactionRateParameter();
		}
	}
	
	private Expression getInwardCurrentDensity() throws ExpressionException, PropertyVetoException {
		//
		// lumped kinetics always have same units (molecules/s) or (pA) whether used in a FluxReaction or a SimpleReaction
		//
		// CurrentMagnitude = reactionRate * valence * F * unitFactor
		//
		// for FluxReaction
		//
		Model model = getReactionStep().getModel();
		Expression F = getSymbolExpression(model.getFARADAY_CONSTANT());
		Expression reactionRate = getSymbolExpression(getReactionRateParameter());
		Expression z = getSymbolExpression(getChargeValenceParameter());
		Expression tempInwardCurrentDensityExpression = Expression.mult(z, F, reactionRate);

		VCUnitDefinition tempRateUnit = getReactionRateParameter().getUnitDefinition().multiplyBy(model.getFARADAY_CONSTANT().getUnitDefinition()).multiplyBy(getChargeValenceParameter().getUnitDefinition());
		VCUnitDefinition desiredUnit = getCurrentDensityParameter().getUnitDefinition();
		Expression unitFactorExp = getElectricalUnitFactor(desiredUnit.divideBy(tempRateUnit));
		if (unitFactorExp != null){
			tempInwardCurrentDensityExpression = Expression.mult(tempInwardCurrentDensityExpression,unitFactorExp);
		}		
		
		if (hasOutwardFlux()){
			tempInwardCurrentDensityExpression = Expression.negate(tempInwardCurrentDensityExpression);
		}
		return tempInwardCurrentDensityExpression;
	}
	
	private Expression getReactionRate() throws ExpressionException, PropertyVetoException {
		//
		// lumped kinetics always have same units (molecules/s) or (pA) whether used in a FluxReaction or a SimpleReaction
		//
		// RateMagnitude = inwardCurrent  / (valence*F_NMOLE)
		//
		// for FluxReaction, we are using the flux Carrier
		//
		Model model = getReactionStep().getModel();
		Expression F = getSymbolExpression(model.getFARADAY_CONSTANT());
		Expression inwardCurrentDensity = getSymbolExpression(getCurrentDensityParameter());
		Expression z = getSymbolExpression(getChargeValenceParameter());
		Expression tempRateExpression = Expression.div(inwardCurrentDensity, Expression.mult(z, F));
		
		VCUnitDefinition tempRateUnit = getCurrentDensityParameter().getUnitDefinition().divideBy(model.getFARADAY_CONSTANT().getUnitDefinition());
		VCUnitDefinition desiredUnit = getReactionRateParameter().getUnitDefinition();
		Expression unitFactorExp = getElectricalUnitFactor(desiredUnit.divideBy(tempRateUnit));
		if (unitFactorExp != null){
			tempRateExpression = Expression.mult(tempRateExpression,unitFactorExp);
		}
		
		if (hasOutwardFlux()){
			tempRateExpression = Expression.negate(tempRateExpression);
		}
		return tempRateExpression;
	}

	protected void updateReactionRatesFromInwardCurrentDensity() throws ExpressionException, PropertyVetoException {
		KineticsParameter currentParm = getCurrentDensityParameter();
		KineticsParameter rateParm = getReactionRateParameter();
		if (currentParm==null && rateParm==null){
			return;
		}

		if (getReactionStep().getPhysicsOptions() == ReactionStep.PHYSICS_MOLECULAR_AND_ELECTRICAL){
			Expression tempRateExpression = getReactionRate();
			if (rateParm == null){
				Model model = getReactionStep().getModel();
				VCUnitDefinition rateUnit = null;
				if (getReactionStep() instanceof SimpleReaction){
					rateUnit = model.getUnitSystem().getMembraneReactionRateUnit();
				}else if (getReactionStep() instanceof FluxReaction){
					rateUnit = model.getUnitSystem().getFluxReactionUnit();
				}else{
					rateUnit = model.getUnitSystem().getInstance_TBD();
				}
				addKineticsParameter(new KineticsParameter(getDefaultParameterName(ROLE_LumpedReactionRate),tempRateExpression,ROLE_LumpedReactionRate, rateUnit));
			}else{
				rateParm.setExpression(tempRateExpression);
			}
		}else{
			if (rateParm != null && !rateParm.getExpression().isZero()){
				//removeKineticsParameter(rateParm);
				rateParm.setExpression(new Expression(0.0));
			}
			KineticsParameter unitParam = getKineticsParameterFromRole(ROLE_ElectricalUnitFactor);
			if (unitParam!=null){
				removeKineticsParameter(unitParam);
			}
		}
	}


	protected void updateInwardCurrentDensityFromReactionRate() throws ExpressionException, PropertyVetoException {
		KineticsParameter rateParmInput = getReactionRateParameter();
		KineticsParameter currentParm = getCurrentDensityParameter();
		if (currentParm==null && rateParmInput==null){
			return;
		}

		if (getReactionStep().getPhysicsOptions() == ReactionStep.PHYSICS_MOLECULAR_AND_ELECTRICAL){
			Expression tempCurrentDensityExpression = getInwardCurrentDensity();
			if (currentParm == null){
				Model model = getReactionStep().getModel();
				VCUnitDefinition currentUnit = model.getUnitSystem().getCurrentDensityUnit();
				addKineticsParameter(new KineticsParameter(getDefaultParameterName(ROLE_CurrentDensity),tempCurrentDensityExpression,ROLE_CurrentDensity, currentUnit));
			}else{
				currentParm.setExpression(tempCurrentDensityExpression);
			}
		}else{
			if (currentParm != null && !currentParm.getExpression().isZero()){
				//removeKineticsParameter(rateParm);
				currentParm.setExpression(new Expression(0.0));
			}
			KineticsParameter unitParam = getKineticsParameterFromRole(ROLE_ElectricalUnitFactor);
			if (unitParam!=null){
				removeKineticsParameter(unitParam);
			}
		}
	}


	public static DistributedKinetics toDistributedKinetics(LumpedKinetics origLumpedKinetics, boolean bForceScale) throws ExpressionException, PropertyVetoException {
		KineticsParameter[] origLumpedKineticsParms = origLumpedKinetics.getKineticsParameters();
		ReactionStep reactionStep = origLumpedKinetics.getReactionStep();
		Vector<KineticsParameter> parmsToAdd = new Vector<KineticsParameter>();
		
		DistributedKinetics distributedKinetics = null;
		if (origLumpedKinetics.getKineticsDescription().isElectrical()){
			if (origLumpedKinetics.getReactionStep() instanceof FluxReaction){
				distributedKinetics = new GeneralCurrentKinetics((FluxReaction)reactionStep);
			}else if (origLumpedKinetics.getReactionStep() instanceof SimpleReaction){
				distributedKinetics = new GeneralCurrentKinetics((SimpleReaction)reactionStep);
			}else{
				throw new RuntimeException("DistributedKinetics.toDistributedKinetics("+origLumpedKinetics.getReactionStep()+") not supported");
			}
		}else{
			if (origLumpedKinetics.getReactionStep() instanceof FluxReaction){
				distributedKinetics = new GeneralKinetics((FluxReaction)reactionStep);
			}else if (origLumpedKinetics.getReactionStep() instanceof SimpleReaction){
				distributedKinetics = new GeneralKinetics((SimpleReaction)reactionStep);
			}else{
				throw new RuntimeException("DistributedKinetics.toDistributedKinetics("+origLumpedKinetics.getReactionStep()+") not supported");
			}
		}
		
		KineticsParameter distAuthoritativeParam = distributedKinetics.getAuthoritativeParameter();

		// preserve user-defined rate parameter name
		String origRateParamName = origLumpedKinetics.getAuthoritativeParameter().getName();
		// need to protect from name conflict between local parameter and names of predefined role parameters in new kinetics
		reactionStep.setKinetics(origLumpedKinetics);
		for (int i = 0; i < origLumpedKineticsParms.length; i++) {
			if (origLumpedKineticsParms[i].getRole()!=Kinetics.ROLE_LumpedReactionRate &&
					origLumpedKineticsParms[i].getRole()!=Kinetics.ROLE_LumpedCurrent &&
					origLumpedKineticsParms[i].getName().equals(distAuthoritativeParam.getName())){
				    origLumpedKineticsParms[i].setName("_"+origLumpedKineticsParms[i].getName()+"_");
			}
		}
		reactionStep.setKinetics(distributedKinetics);
		origLumpedKineticsParms = origLumpedKinetics.getKineticsParameters(); //if a name change was done above the params were cloned in the process...
		
		StructureSize structureSize = origLumpedKinetics.getReactionStep().getStructure().getStructureSize();
		KineticsParameter lumpedAuthoritativeParm = origLumpedKinetics.getAuthoritativeParameter();
		Expression sizeExp = new Expression(structureSize.getName());
		Expression substitutedRate = MathUtilities.substituteModelParameters(lumpedAuthoritativeParm.getExpression(), reactionStep, false, false);
		Expression lumpedExpressionWithoutSizeScale = ExpressionUtils.getLinearFactor(substitutedRate, structureSize.getName());
		if (lumpedExpressionWithoutSizeScale == null){
			// try again with model namescope (if it was a global param, prb from SBML import)
			lumpedExpressionWithoutSizeScale = ExpressionUtils.getLinearFactor(substitutedRate, new Expression(structureSize, structureSize.getStructure().getModel().getNameScope()).infix());
		}
		if (lumpedExpressionWithoutSizeScale == null){
			if (bForceScale) {
				lumpedExpressionWithoutSizeScale = Expression.div(lumpedAuthoritativeParm.getExpression(), sizeExp);
			} else {
				throw new ExpressionException("linear factor was null, lumped reaction "+origLumpedKinetics.getReactionStep().getName()+" could not be transformed to distributed");
			}
		}
		VCUnitDefinition sizeUnit = structureSize.getUnitDefinition();
		Expression unitFactor = new Expression(distributedKinetics.getAuthoritativeParameter().getUnitDefinition().multiplyBy(sizeUnit).divideBy(origLumpedKinetics.getAuthoritativeParameter().getUnitDefinition()).getDimensionlessScale());
		Expression newDistributedAuthoritativeExp = Expression.mult(unitFactor,lumpedExpressionWithoutSizeScale).flattenSafe();
		parmsToAdd.add(distributedKinetics.new KineticsParameter(distAuthoritativeParam.getName(),newDistributedAuthoritativeExp,distAuthoritativeParam.getRole(),distAuthoritativeParam.getUnitDefinition()));
		
		for (int i = 0; i < origLumpedKineticsParms.length; i++) {
			if (origLumpedKineticsParms[i].getRole()!=Kinetics.ROLE_LumpedReactionRate &&
					origLumpedKineticsParms[i].getRole()!=Kinetics.ROLE_LumpedCurrent){
				KineticsParameter kp = distributedKinetics.new KineticsParameter(origLumpedKineticsParms[i].getName(),new Expression(origLumpedKineticsParms[i].getExpression()),Kinetics.ROLE_UserDefined,origLumpedKineticsParms[i].getUnitDefinition());
				parmsToAdd.add(kp);
			}
		}
		distributedKinetics.addKineticsParameters(parmsToAdd.toArray(new KineticsParameter[parmsToAdd.size()]));
		distAuthoritativeParam = distributedKinetics.getAuthoritativeParameter();
		for (KineticsParameter kp : distributedKinetics.getKineticsParameters()) {
			if (kp.getRole() == Kinetics.ROLE_UserDefined && distAuthoritativeParam.getExpression().infix().equals(kp.getName())) {
				distAuthoritativeParam.setExpression(kp.getExpression());
				break;
			}
		}
		// apply preserved user-defined rate parameter name
		if (!origRateParamName.equals(origLumpedKinetics.getDefaultParameterName(Kinetics.ROLE_LumpedReactionRate))) {
			distAuthoritativeParam.setName(origLumpedKinetics.getAuthoritativeParameter().getName());
		}

		return distributedKinetics;
	}

}
