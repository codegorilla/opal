package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class BinaryExpression extends Expression {

  private Expression left;
  private Expression right;

  public BinaryExpression (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

//  public AstNode leftExpression () {
//    return getChild(0);
//  }
//
//  public AstNode rightExpression () {
//    return getChild(1);
//  }

  public Expression getLeft () {
    return left;
  }

  public Expression getRight () {
    return right;
  }

  public void setLeft (Expression expression) {
    left = expression;
  }

  public void setRight (Expression expression) {
    right = expression;
  }



}
