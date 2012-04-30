/* Generated By:JJTree: Do not edit this line. ASTExponentExpression.java */

package cbit.vcell.units.parser;

import cbit.vcell.matrix.RationalNumber;

public class ASTExponentExpression extends SimpleNode {
  public ASTExponentExpression(int id) {
    super(id);
  }

  public ASTExponentExpression(UnitSymbolParser p, int id) {
    super(p, id);
  }

public String toInfix(RationalNumber power) {
	return jjtGetChild(0).toInfix(power);
}

public String toSymbol(RationalNumber power) {
	return jjtGetChild(0).toSymbol(power);
}

}
