/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.Compare;
import org.vcell.util.Matchable;

import cbit.vcell.math.SubDomain.BoundaryConditionSpec;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SymbolTableEntry;
/**
 * This class was generated by a SmartGuide.
 * 
 */
@SuppressWarnings("serial")
public class PdeEquation extends Equation {
	private Expression diffusionExp = null;
	private boolean bSteady = false;
//	private boolean bFixedDiffusion = false;
	
	private Expression boundaryXm = null;
	private Expression boundaryXp = null;
	private Expression boundaryYm = null;
	private Expression boundaryYp = null;
	private Expression boundaryZm = null;
	private Expression boundaryZp = null;
	private Expression velocityX = null;
	private Expression velocityY = null;
	private Expression velocityZ = null;
	
	private Expression gradientX = null;
	private Expression gradientY = null;
	private Expression gradientZ = null;

	private ArrayList<BoundaryConditionValue> listOfInternalBoundaryValues = new ArrayList<PdeEquation.BoundaryConditionValue>();
	
	public class BoundaryConditionValue implements Matchable, Serializable {
		
		private String subDomainName = null;
		private Expression boundaryConditionExpr = null;

		public BoundaryConditionValue(String name, Expression expression) {
			super();
			this.subDomainName = name;
			this.boundaryConditionExpr = expression;
		}
		
		public String getSubdomainName() {
			return subDomainName;
		}
		
		public Expression getBoundaryConditionExpression() {
			return boundaryConditionExpr;
		}
		
		public boolean compareEqual(Matchable obj) {
			if (obj instanceof BoundaryConditionValue){
				BoundaryConditionValue bcv = (BoundaryConditionValue)obj;
				if(!Compare.isEqual(subDomainName,bcv.subDomainName))  {
					return false;
				}
				if (!Compare.isEqual(getBoundaryConditionExpression(), bcv.getBoundaryConditionExpression())){
					return false;
				}
				return true;
			}
			return false;
		}
		
		public String getVCML() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("\t\t"+VCML.BoundaryConditionValue+"\t"+getSubdomainName()+"\t"+getBoundaryConditionExpression().infix()+";\n");
			return buffer.toString();
		}
	}
/**
 * This method was created by a SmartGuide.
 * @param volVar cbit.vcell.math.VolVariable
 */
public PdeEquation (MemVariable memVar) {
	super(memVar);
	
}


/**
 * PdeEquation constructor comment.
 * @param compartmentSubDomain cbit.vcell.math.CompartmentSubDomain
 * @param var cbit.vcell.math.Variable
 * @param initialExp cbit.vcell.parser.Expression
 * @param rateExp cbit.vcell.parser.Expression
 * @param diffusionRate cbit.vcell.parser.Expression
 */
public PdeEquation(Variable var, boolean steady, Expression initialExp,	Expression rateExp, Expression diffusionRate) {
	super(var, initialExp, rateExp);
	diffusionExp = diffusionRate;
	bSteady = steady;
}


public PdeEquation(Variable var, Expression initialExp, Expression rateExp, Expression diffusionRate) {
	this(var, false, initialExp, rateExp, diffusionRate);
}

/**
 * This method was created by a SmartGuide.
 * @param volVar cbit.vcell.math.VolVariable
 */
public PdeEquation (VolVariable volVar, boolean steady) {
	super(volVar);
	bSteady = steady;
}


/**
 * Insert the method's description here.
 * Creation date: (9/4/2003 12:32:19 PM)
 * @return boolean
 * @param object cbit.util.Matchable
 */
public boolean compareEqual(Matchable object) {
	PdeEquation equ = null;
	if (!(object instanceof PdeEquation)){
		return false;
	}else{
		equ = (PdeEquation)object;
	}
	if (bSteady != equ.bSteady) {
		return false;
	}
	if (!compareEqual0(equ)){
		return false;
	}
	if (!Compare.isEqualOrNull(diffusionExp,equ.diffusionExp)){
		return false;
	}
	if (!Compare.isEqualOrNull(boundaryXm,equ.boundaryXm)){
		return false;
	}
	if (!Compare.isEqualOrNull(boundaryXp,equ.boundaryXp)){
		return false;
	}
	if (!Compare.isEqualOrNull(boundaryYm,equ.boundaryYm)){
		return false;
	}
	if (!Compare.isEqualOrNull(boundaryYp,equ.boundaryYp)){
		return false;
	}
	if (!Compare.isEqualOrNull(boundaryZm,equ.boundaryZm)){
		return false;
	}
	if (!Compare.isEqualOrNull(boundaryZp,equ.boundaryZp)){
		return false;
	}
	if (!Compare.isEqualOrNull(velocityX,equ.velocityX)){
		return false;
	}
	if (!Compare.isEqualOrNull(velocityY,equ.velocityY)){
		return false;
	}
	if (!Compare.isEqualOrNull(velocityZ,equ.velocityZ)){
		return false;
	}
	if (!Compare.isEqualOrNull(gradientX,equ.gradientX)){
		return false;
	}
	if (!Compare.isEqualOrNull(gradientY,equ.gradientY)){
		return false;
	}
	if (!Compare.isEqualOrNull(gradientZ,equ.gradientZ)){
		return false;
	}
	
	// boundaryConditionValues
	if (!Compare.isEqualOrNull(listOfInternalBoundaryValues, equ.listOfInternalBoundaryValues)) { 
		return false;
	}

	return true;
}


/**
 * Insert the method's description here.
 * Creation date: (10/10/2002 10:41:10 AM)
 * @param simSymbolTable cbit.vcell.solver.Simulation
 */
void flatten(MathSymbolTable simSymbolTable, boolean bRoundCoefficients) throws cbit.vcell.parser.ExpressionException, MathException {
	super.flatten0(simSymbolTable,bRoundCoefficients);
	
	diffusionExp = getFlattenedExpression(simSymbolTable,diffusionExp,bRoundCoefficients);
	velocityX = getFlattenedExpression(simSymbolTable,velocityX,bRoundCoefficients);
	velocityY = getFlattenedExpression(simSymbolTable,velocityY,bRoundCoefficients);
	velocityZ = getFlattenedExpression(simSymbolTable,velocityZ,bRoundCoefficients);
	boundaryXm = getFlattenedExpression(simSymbolTable,boundaryXm,bRoundCoefficients);
	boundaryXp = getFlattenedExpression(simSymbolTable,boundaryXp,bRoundCoefficients);
	boundaryYm = getFlattenedExpression(simSymbolTable,boundaryYm,bRoundCoefficients);
	boundaryYp = getFlattenedExpression(simSymbolTable,boundaryYp,bRoundCoefficients);
	boundaryZm = getFlattenedExpression(simSymbolTable,boundaryZm,bRoundCoefficients);
	boundaryZp = getFlattenedExpression(simSymbolTable,boundaryZp,bRoundCoefficients);
	gradientX = getFlattenedExpression(simSymbolTable,gradientX,bRoundCoefficients);
	gradientY = getFlattenedExpression(simSymbolTable,gradientY,bRoundCoefficients);
	gradientZ = getFlattenedExpression(simSymbolTable,gradientZ,bRoundCoefficients);
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getBoundaryXm() {
	return boundaryXm;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getBoundaryXp() {
	return boundaryXp;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getBoundaryYm() {
	return boundaryYm;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getBoundaryYp() {
	return boundaryYp;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getBoundaryZm() {
	return boundaryZm;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getBoundaryZp() {
	return boundaryZp;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getDiffusionExpression() {
	return diffusionExp;
}


public BoundaryConditionValue getBoundaryConditionValue(int index) {
	if (index < listOfInternalBoundaryValues.size())
		return (BoundaryConditionValue)listOfInternalBoundaryValues.get(index);
	return null;
}


/**
 * Get a boundaryConditionValue from list by boundaryConditionValue's name.
 * @return boundaryConditionValue
 * @param varName java.lang.String
 */
public BoundaryConditionValue getBoundaryConditionValue(String boundaryCondnValueName) {
	for (BoundaryConditionValue bcv : listOfInternalBoundaryValues){
		if (bcv.getSubdomainName().equals(boundaryCondnValueName)) {
			return bcv;
		}
	}
	return null;
}


/**
 * Return the reference of the boundaryConditionValues list.
 * @return java.util.Vector
 */
public List<BoundaryConditionValue> getBoundaryconditionValues() {
	return Collections.unmodifiableList(listOfInternalBoundaryValues);
}

/**
 * Remove a BoundaryConditionValue from list by it's index.
 * @param index int
 */
public void removeBoundaryConditionValue(int index) {
	listOfInternalBoundaryValues.remove(index);
}


/**
 * Remove a BoundaryConditionValue from list by it's name in the list.
 * @param subDomainName java.lang.String
 */
public void removeBoundaryConditionValue(String bcValueName) {
	for (BoundaryConditionValue bcs : listOfInternalBoundaryValues){
		if(bcs.getSubdomainName().equals(bcValueName)){
			listOfInternalBoundaryValues.remove(bcs);
		}
	}	
}


/**
 * empty theBoundaryConditionValue list
 */
public void removeBoundaryConditionValues() {
	listOfInternalBoundaryValues.clear();
}

/**
 * This method was created by a SmartGuide.
 * @return java.util.Vector
 */
public Vector<Expression> getExpressions(MathDescription mathDesc) {
	Vector<Expression> list = new Vector<Expression>();
	
	if (getBoundaryXm()!=null)		list.addElement(getBoundaryXm());
	if (getBoundaryXp()!=null)		list.addElement(getBoundaryXp());
	if (getBoundaryYm()!=null)		list.addElement(getBoundaryYm());
	if (getBoundaryYp()!=null)		list.addElement(getBoundaryYp());
	if (getBoundaryZm()!=null)		list.addElement(getBoundaryZm());
	if (getBoundaryZp()!=null)		list.addElement(getBoundaryZp());

	if (getVelocityX()!=null)		list.addElement(getVelocityX());
	if (getVelocityY()!=null)		list.addElement(getVelocityY());
	if (getVelocityZ()!=null)		list.addElement(getVelocityZ());
	
	if (getGradientX()!=null)		list.addElement(getGradientX());
	if (getGradientY()!=null)		list.addElement(getGradientY());
	if (getGradientZ()!=null)		list.addElement(getGradientZ());
	
	if (getRateExpression()!=null)		list.addElement(getRateExpression());
	if (getInitialExpression()!=null)	list.addElement(getInitialExpression());
	if (getExactSolution()!=null)		list.addElement(getExactSolution());

	list.addElement(diffusionExp);

	//
	// get Parent Subdomain
	//
	SubDomain parentSubDomain = null;
	Enumeration<SubDomain> enum1 = mathDesc.getSubDomains();
	while (enum1.hasMoreElements()){
		SubDomain subDomain = enum1.nextElement();
		if (subDomain.getEquation(getVariable()) == this){
			parentSubDomain = subDomain;
			break;
		}
	}

	if (getVariable() instanceof VolVariable){
		try {
			MembraneSubDomain membranes[] = mathDesc.getMembraneSubDomains((CompartmentSubDomain)parentSubDomain);
			for (int i = 0; membranes!=null && i < membranes.length; i++){
				JumpCondition jump = membranes[i].getJumpCondition((VolVariable)getVariable());
				if (jump != null) {
					if (membranes[i].getInsideCompartment()==parentSubDomain){
						list.addElement(jump.getInFluxExpression());
					}else{
						list.addElement(jump.getOutFluxExpression());
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace(System.out);
		}
	}

	return list;
}


/**
 * This method was created by a SmartGuide.
 * @return java.util.Enumeration
 */
public Enumeration<Expression> getTotalExpressions() throws ExpressionException {
	Vector<Expression> vector = new Vector<Expression>();
	vector.addElement(getTotalRateExpression());
	vector.addElement(getTotalInitialExpression());
	Expression solutionExp = getTotalSolutionExpression();
	if (solutionExp!=null){
		vector.addElement(solutionExp);
	}	
	return vector.elements();
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
private Expression getTotalRateExpression() throws ExpressionException {
	Expression lvalueExp = Expression.derivative("t",new Expression(getVariable().getName()+";"));
//	Expression lvalueExp = new Expression("d/dt*"+getVariable().getName()+";");
	Expression laplacianExp = Expression.laplacian(new Expression(getVariable().getName()+";"));
	Expression diffExp = Expression.mult(new Expression(getDiffusionExpression()),laplacianExp);
	Expression rvalueExp = Expression.add(new Expression(diffExp),new Expression(getRateExpression()));
	Expression totalExp = Expression.assign(lvalueExp,rvalueExp);
	totalExp.bindExpression(null);
	totalExp.flatten();
	return totalExp;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getVCML() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+VCML.PdeEquation + (bSteady ? " " + VCML.Steady : "") + " " + getVariable().getName() + " {\n");
	if (boundaryXm != null){
		buffer.append("\t\t"+VCML.BoundaryXm+" "+boundaryXm.infix()+";\n");
	}	
	if (boundaryXp != null){
		buffer.append("\t\t"+VCML.BoundaryXp+" "+boundaryXp.infix()+";\n");
	}	
	if (boundaryYm != null){
		buffer.append("\t\t"+VCML.BoundaryYm+" "+boundaryYm.infix()+";\n");
	}	
	if (boundaryYp != null){
		buffer.append("\t\t"+VCML.BoundaryYp+" "+boundaryYp.infix()+";\n");
	}	
	if (boundaryZm != null){
		buffer.append("\t\t"+VCML.BoundaryZm+" "+boundaryZm.infix()+";\n");
	}	
	if (boundaryZp != null){
		buffer.append("\t\t"+VCML.BoundaryZp+" "+boundaryZp.infix()+";\n");
	}	
	
	// BoundaryconditionValues
	if (getBoundaryconditionValues().size() > 0) {
		for (BoundaryConditionValue bcv : getBoundaryconditionValues()) {
			buffer.append(bcv.getVCML());
		}
		buffer.append("\n");
	}

	if (velocityX != null){
		buffer.append("\t\t"+VCML.VelocityX+"\t "+velocityX.infix()+";\n");
	}	
	if (velocityY != null){
		buffer.append("\t\t"+VCML.VelocityY+"\t "+velocityY.infix()+";\n");
	}	
	if (velocityZ != null){
		buffer.append("\t\t"+VCML.VelocityZ+"\t "+velocityZ.infix()+";\n");
	}	
	if (getRateExpression() != null){
		buffer.append("\t\t"+VCML.Rate+"\t "+getRateExpression().infix()+";\n");
	}else{
		buffer.append("\t\t"+VCML.Rate+"\t "+"0.0;\n");
	}
	if (diffusionExp != null){
		buffer.append("\t\t"+VCML.Diffusion+"\t "+diffusionExp.infix()+";\n");
	}else{
		buffer.append("\t\t"+VCML.Diffusion+"\t "+"0.0;\n");
	}
	if (initialExp != null){
		buffer.append("\t\t"+VCML.Initial+"\t "+initialExp.infix()+";\n");
	}
	switch (solutionType){
		case UNKNOWN_SOLUTION:{
			if (initialExp == null){
				buffer.append("\t\t"+VCML.Initial+"\t "+"0.0;\n");
			}
			break;
		}
		case EXACT_SOLUTION:{
			buffer.append("\t\t"+VCML.Exact+"\t "+exactExp.infix()+";\n");
			break;
		}
	}				
	if (gradientX != null){
		buffer.append("\t\t"+VCML.GradientX + "\t" + gradientX.infix() + ";\n");
	}	
	if (gradientY != null){
		buffer.append("\t\t"+VCML.GradientY + "\t" + gradientY.infix() + ";\n");
	}	
	if (gradientZ != null){
		buffer.append("\t\t"+VCML.GradientZ + "\t" + gradientZ.infix() + ";\n");
	}
	buffer.append("\t}\n");
	return buffer.toString();		
}


/**
 * Insert the method's description here.
 * Creation date: (5/17/2004 11:18:22 AM)
 * @return cbit.vcell.parser.Expression
 */
public Expression getVelocityX() {
	return velocityX;
}


/**
 * Insert the method's description here.
 * Creation date: (5/17/2004 11:18:22 AM)
 * @return cbit.vcell.parser.Expression
 */
public Expression getVelocityY() {
	return velocityY;
}


/**
 * Insert the method's description here.
 * Creation date: (5/17/2004 11:18:22 AM)
 * @return cbit.vcell.parser.Expression
 */
public Expression getVelocityZ() {
	return velocityZ;
}


/**
 * This method was created by a SmartGuide.
 * @param tokens java.util.StringTokenizer
 * @exception java.lang.Exception The exception description.
 */
public void read(CommentStringTokenizer tokens, MathDescription mathDesc) throws MathFormatException, MathException, ExpressionException {
	String token = null;
	token = tokens.nextToken();
	if (!token.equalsIgnoreCase(VCML.BeginBlock)){
		throw new MathFormatException("unexpected token "+token+" expecting "+VCML.BeginBlock);
	}			
	while (tokens.hasMoreTokens()){
		token = tokens.nextToken();
		if (token.equalsIgnoreCase(VCML.EndBlock)){
			break;
		}			
		if (token.equalsIgnoreCase(VCML.Initial)){
			initialExp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}
		if (token.equalsIgnoreCase(VCML.Diffusion)){
			diffusionExp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}
		if (token.equalsIgnoreCase(VCML.Rate)){
			Expression exp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			setRateExpression(exp);
			continue;
		}
		if (token.equalsIgnoreCase(VCML.Exact)){
			exactExp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			solutionType = EXACT_SOLUTION;
			continue;
		}
		if (token.equalsIgnoreCase(VCML.BoundaryXm)){
			boundaryXm = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryXp)){
			boundaryXp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryYm)){
			boundaryYm = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryYp)){
			boundaryYp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryZm)){
			boundaryZm = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryZp)){
			boundaryZp = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}
		
		// boundaryConditionValues
		if (token.equalsIgnoreCase(VCML.BoundaryConditionValue)){
			String name = tokens.nextToken();
			Expression expression = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			BoundaryConditionValue bcv = new BoundaryConditionValue(name, expression);
			addBoundaryConditionValue(bcv);
			continue;
		}			

		if (token.equalsIgnoreCase(VCML.VelocityX)){
			velocityX = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.VelocityY)){
			velocityY = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.VelocityZ)){
			velocityZ = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.GradientX)){
			gradientX = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.GradientY)){
			gradientY = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.GradientZ)){
			gradientZ = MathFunctionDefinitions.fixFunctionSyntax(tokens);
			continue;
		}			
		throw new MathFormatException("unexpected identifier "+token);
	}	
		
}


public void addBoundaryConditionValue(BoundaryConditionValue newBoundaryConditionValue) throws MathException {
	if(getBoundaryConditionValue(newBoundaryConditionValue.getSubdomainName())!=null)
		throw new MathException("BoundaryCondition value for : '"+ newBoundaryConditionValue.getSubdomainName()+"' already exists");
	listOfInternalBoundaryValues.add(newBoundaryConditionValue);
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/00 2:58:12 PM)
 */
public void setBoundaryXm(Expression exp) {
	boundaryXm = exp;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/00 2:58:12 PM)
 */
public void setBoundaryXp(Expression exp) {
	boundaryXp = exp;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/00 2:58:12 PM)
 */
public void setBoundaryYm(Expression exp) {
	boundaryYm = exp;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/00 2:58:12 PM)
 */
public void setBoundaryYp(Expression exp) {
	boundaryYp = exp;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/00 2:58:12 PM)
 */
public void setBoundaryZm(Expression exp) {
	boundaryZm = exp;
}


/**
 * Insert the method's description here.
 * Creation date: (5/24/00 2:58:12 PM)
 */
public void setBoundaryZp(Expression exp) {
	boundaryZp = exp;
}


/**
 * Insert the method's description here.
 * Creation date: (7/6/00 9:32:03 AM)
 * @param diffusionExpression cbit.vcell.parser.Expression
 */
public void setDiffusionExpression(Expression diffusionExpression) {
	this.diffusionExp = diffusionExpression;
}


/**
 * Insert the method's description here.
 * Creation date: (5/17/2004 11:18:22 AM)
 * @param newVelocityX cbit.vcell.parser.Expression
 */
public void setVelocityX(Expression newVelocityX) {
	if (!(getVariable() instanceof VolVariable)){
		throw new RuntimeException("only Volume Variables can have advection term in PdeEquation");
	}
	if (bSteady) {
		throw new RuntimeException("advection in steady state pde equation is not supported");
	}
	velocityX = newVelocityX;
}


/**
 * Insert the method's description here.
 * Creation date: (5/17/2004 11:18:22 AM)
 * @param newVelocityY cbit.vcell.parser.Expression
 */
public void setVelocityY(Expression newVelocityY) {
	if (!(getVariable() instanceof VolVariable)){
		throw new RuntimeException("only Volume Variables can have advection term in PdeEquation");
	}
	if (bSteady) {
		throw new RuntimeException("advection in steady state pde equation is not supported");
	}
	velocityY = newVelocityY;
}


/**
 * Insert the method's description here.
 * Creation date: (5/17/2004 11:18:22 AM)
 * @param newVelocityZ cbit.vcell.parser.Expression
 */
public void setVelocityZ(Expression newVelocityZ) {
	if (!(getVariable() instanceof VolVariable)){
		throw new RuntimeException("only Volume Variables can have advection term in PdeEquation");
	}
	if (bSteady) {
		throw new RuntimeException("advection in steady state pde equation is not supported");
	}
	velocityZ = newVelocityZ;
}

public boolean isSteady() {
	return bSteady;
}

private boolean testZero(MathSymbolTable simSymbolTable, Expression exp) {
	if (exp == null) {
		return true;
	}
	if (exp.isZero()) {
		return true;
	}
	Expression newExp = new Expression(exp);	
	try {
		newExp.bindExpression(simSymbolTable);
		newExp = simSymbolTable.substituteFunctions(newExp);
		newExp = newExp.flatten();
	} catch (ExpressionException ex) {
		return false;
	} catch (MathException ex) {
		return false;
	}
	return newExp.isZero();
}

private boolean testConstant(MathSymbolTable simSymbolTable, Expression exp) {
	if (exp == null) {
		return true;
	}
	if (exp.isZero()) {
		return true;
	}
	Expression newExp = new Expression(exp);	
	try {
		newExp.bindExpression(simSymbolTable);
		newExp = simSymbolTable.substituteFunctions(newExp);
		newExp = newExp.flatten();
		newExp.evaluateConstant();
	} catch (ExpressionException ex) {
		return false;
	} catch (MathException ex) {
		return false;
	}
	return true;
}

public boolean isDummy(MathSymbolTable simSymbolTable, CompartmentSubDomain thisCompartment) {
	if (!(getVariable() instanceof VolVariable)) {
		return false;
	}

	VolVariable volVar = (VolVariable)getVariable();
	
	// during math generation, dummy equations are created with diffusion rates copied from other compartments
	// for efficient numerics, we only solve for regions where there are real equations.
	// in any case the initial conditions will be respected.
	if (testZero(simSymbolTable, getRateExpression()) 
			&& testZero(simSymbolTable, velocityX) && testZero(simSymbolTable, velocityY) && testZero(simSymbolTable, velocityZ)
			&& testZero(simSymbolTable, gradientX) && testZero(simSymbolTable, gradientY) && testZero(simSymbolTable, gradientZ)) {			
		if (testZero(simSymbolTable, diffusionExp)) {
			return true; // zero rate, velocity and diffusion
		} else {
			// for non-zero diffusion, look for non zero fluxes or non-uniform initial conditions.
			if (testConstant(simSymbolTable, getInitialExpression())
				&& testZero(simSymbolTable, boundaryXm) && testZero(simSymbolTable, boundaryXp)
				&& testZero(simSymbolTable, boundaryYm) && testZero(simSymbolTable, boundaryYp)
				&& testZero(simSymbolTable, boundaryZm) && testZero(simSymbolTable, boundaryZp)) {
				// 1. get THIS compartment
				// 2. get all membranes that touch this compartment
				// 3. get jump condition for this variable
				// 4. check either influx or outflux 
		  		Enumeration<SubDomain> subDomainEnum = simSymbolTable.getMathDescription().getSubDomains();		  		
		  		while (subDomainEnum.hasMoreElements()){
			  		SubDomain subDomain = (SubDomain)subDomainEnum.nextElement();
			  		if (subDomain instanceof MembraneSubDomain){
			  			MembraneSubDomain memSubDomain = (MembraneSubDomain)subDomain;
				  		if (memSubDomain.getInsideCompartment() == thisCompartment) {
				  			JumpCondition jump = memSubDomain.getJumpCondition(volVar);
				  			if (jump != null && !testZero(simSymbolTable, jump.getInFluxExpression())) {
				  				return false; // non zero jump condition
				  			}
				  		} else 	if (memSubDomain.getOutsideCompartment() == thisCompartment) {
				  			JumpCondition jump = memSubDomain.getJumpCondition(volVar);
				  			if (jump != null && !testZero(simSymbolTable, jump.getOutFluxExpression())) {
				  				return false; // non zero jump condition
				  			}
				  		}
				  		// check if BoundaryConditionValue is defined for var on membrane
						BoundaryConditionValue boundaryValue = getBoundaryConditionValue(memSubDomain.getName());
						if (boundaryValue != null) {
							Expression boundaryValExpr = boundaryValue.getBoundaryConditionExpression();
							BoundaryConditionSpec boundarySpec = thisCompartment.getBoundaryConditionSpec(memSubDomain.getName());
							if (boundarySpec != null) {
								boolean bZeroFlux = boundarySpec.getBoundaryConditionType().isNEUMANN() && testZero(simSymbolTable, boundaryValExpr);
								boolean bUniformDirichlet = boundarySpec.getBoundaryConditionType().isDIRICHLET() && testZero(simSymbolTable, Expression.add(boundaryValExpr, Expression.negate(getInitialExpression())));
								if (!bZeroFlux && !bUniformDirichlet) {
									return false;
								}
							}
						}
			  		}
		  		}
		  		// check fast system if jump conditions are all zero
		  		FastSystem fastSystem = thisCompartment.getFastSystem();
		  		if (fastSystem != null) {
		  			Enumeration<FastInvariant> fastInvariants = fastSystem.getFastInvariants();
		  			while (fastInvariants.hasMoreElements()) {
		  				FastInvariant fi = fastInvariants.nextElement();
		  				try {			  			
		  					// look for fast invariants that involve only this variable
		  					// which means this variable is not affected by fast system		
		  					Expression exp = new Expression(fi.getFunction());
		  					exp.bindExpression(simSymbolTable);
		  					exp = exp.flatten();
		  					SymbolTableEntry ste = exp.getSymbolBinding(volVar.getName());
		  					if (ste != null && exp.getSymbols().length == 1) {
		  						return true;			  						
		  					}
		  				} catch (ExpressionException ex) {
		  					ex.printStackTrace(System.out);
		  					throw new RuntimeException("PdeEquation::isDummy(), not expected: " + ex.getMessage());
		  				}
		  			}
		  			return false; // the variable is not found to be invariant in this fast system, might be changed by fast system
		  		}
		  		return true; // not changed by jump conditions, boundary conditions and fast system even if diffusion rate is non zero
			}
		}
	}
	return false; // non zero rate or velocity
}

@Override
public void checkValid(MathDescription mathDesc, SubDomain subDomain) throws MathException, ExpressionException {
	checkInitialCondition(mathDesc);
	if (getVariable() instanceof VolVariable) {
		ArrayList<Expression> expList = new ArrayList<Expression>();
		expList.add(getBoundaryXm());
		expList.add(getBoundaryXp());
		expList.add(getBoundaryYm());
		expList.add(getBoundaryYp());
		expList.add(getBoundaryZm());
		expList.add(getBoundaryZp());

		expList.add(getVelocityX());
		expList.add(getVelocityY());
		expList.add(getVelocityZ());
				
		expList.add(getGradientX());
		expList.add(getGradientY());
		expList.add(getGradientZ());
				
		expList.add(getRateExpression());
		expList.add(getExactSolution());

		expList.add(diffusionExp);
		checkValid_Volume(mathDesc, expList, (CompartmentSubDomain)subDomain);
		
		// jump condition can have membrane variable in it
		MembraneSubDomain membranes[] = mathDesc.getMembraneSubDomains((CompartmentSubDomain)subDomain);
		for (int i = 0; membranes!=null && i < membranes.length; i++){
			JumpCondition jump = membranes[i].getJumpCondition((VolVariable)getVariable());
			if (jump != null) {
				jump.checkValid(mathDesc,membranes[i]);
			} 
		}		
	} else if (getVariable() instanceof MemVariable) {
		if (getVelocityX() != null 
			|| getVelocityY() != null
			|| getVelocityZ() != null) {
			throw new MathException("Velocity is not supported for membrane variables");
		}
		
		if (getGradientX() != null 
				|| getGradientY() != null
				|| getGradientZ() != null) {
			throw new MathException("Gradient is not supported for membrane variables");
		}
		
		ArrayList<Expression> expList = new ArrayList<Expression>();
		expList.add(getBoundaryXm());
		expList.add(getBoundaryXp());
		expList.add(getBoundaryYm());
		expList.add(getBoundaryYp());
		expList.add(getBoundaryZm());
		expList.add(getBoundaryZp());
				
		expList.add(getRateExpression());
		expList.add(getExactSolution());

		expList.add(diffusionExp);
		
		checkValid_Membrane(mathDesc, expList, (MembraneSubDomain)subDomain);
	}
}


public final Expression getGradientX() {
	return gradientX;
}


public final void setGradientX(Expression gradX) {
	this.gradientX = gradX;
}


public final Expression getGradientY() {
	return gradientY;
}


public final void setGradientY(Expression gradY) {
	this.gradientY = gradY;
}


public final Expression getGradientZ() {
	return gradientZ;
}


public final void setGradientZ(Expression gradZ) {
	this.gradientZ = gradZ;
}
}
