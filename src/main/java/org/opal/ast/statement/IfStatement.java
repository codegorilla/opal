package org.opal.ast.statement;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class IfStatement extends AstNode {

  public IfStatement (Token token) {
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

  public AstNode ifCondition () {
    return getChild(0);
  }

  public AstNode ifBody () {
    return getChild(1);
  }

  public AstNode elseClause () {
    return getChild(2);
  }

}
