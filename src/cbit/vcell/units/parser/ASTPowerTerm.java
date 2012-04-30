/* Generated By:JJTree: Do not edit this line. ASTPowerTerm.java */

package cbit.vcell.units.parser;

import cbit.vcell.matrix.RationalNumber;

public class ASTPowerTerm extends SimpleNode {
  public ASTPowerTerm(int id) {
    super(id);
  }

  public ASTPowerTerm(UnitSymbolParser p, int id) {
    super(p, id);
  }

  public String toInfix(RationalNumber power) {
		if (jjtGetChild(0) instanceof ASTIdNode || jjtGetChild(0) instanceof ASTIntegerBaseNode){
			if (jjtGetNumChildren()==1){
				if (power.equals(RationalNumber.ONE)){
					return jjtGetChild(0).toInfix(power);
				}else{
					return jjtGetChild(0).toInfix(RationalNumber.ONE)+"^"+power;
				}
			}else{
				return jjtGetChild(0).toInfix(RationalNumber.ONE)+"^"+jjtGetChild(1).toInfix(power);
			}
		}else if (jjtGetNumChildren()==1){
			return jjtGetChild(0).toInfix(power);
		}else if (jjtGetNumChildren()==2){
			if (jjtGetChild(1) instanceof ASTNegative){
				ASTNegative negNode = (ASTNegative)jjtGetChild(1);
				RationalNumber exponent = negNode.getRationalNumber();
				return jjtGetChild(0).toInfix(power.mult(exponent));
			}else if (jjtGetChild(1) instanceof ASTRationalNumberExponent){
				ASTRationalNumberExponent rationalNumberExponent = (ASTRationalNumberExponent)jjtGetChild(1);
				RationalNumber exponent = rationalNumberExponent.value;
				return jjtGetChild(0).toInfix(power.mult(exponent));
			}else{
				throw new RuntimeException("unexpected second child "+jjtGetChild(1).getClass().getName());
			}
		}else{
			throw new RuntimeException("unexpected unit format");
		}
	}

	public String toSymbol(RationalNumber power) {
		if (jjtGetChild(0) instanceof ASTIdNode || jjtGetChild(0) instanceof ASTIntegerBaseNode){
			if (jjtGetNumChildren()==1){
				if (power.equals(RationalNumber.ONE)){
					return jjtGetChild(0).toSymbol(power);
				}else{
					return jjtGetChild(0).toSymbol(power)+power;
				}
			}else{
				return jjtGetChild(0).toSymbol(power)+jjtGetChild(1).toSymbol(power);
			}
		}else if (jjtGetNumChildren()==1){
			return jjtGetChild(0).toSymbol(power);
		}else if (jjtGetNumChildren()==2){
			if (jjtGetChild(1) instanceof ASTNegative){
				ASTNegative negNode = (ASTNegative)jjtGetChild(1);
				RationalNumber exponent = negNode.getRationalNumber();
				return jjtGetChild(0).toSymbol(power.mult(exponent));
			}else if (jjtGetChild(1) instanceof ASTRationalNumberExponent){
				ASTRationalNumberExponent rationalNumberExponent = (ASTRationalNumberExponent)jjtGetChild(1);
				RationalNumber exponent = rationalNumberExponent.value;
				return jjtGetChild(0).toSymbol(power.mult(exponent));
			}else{
				throw new RuntimeException("unexpected second child "+jjtGetChild(1).getClass().getName());
			}
		}else{
			throw new RuntimeException("unexpected unit format");
		}
	}

}
