package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.ast.statement.CompoundStatement;

public class RoutineBody extends AstNode {

  CompoundStatement compoundStatement = null;

  public RoutineBody() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public CompoundStatement getCompoundStatement () {
    return compoundStatement;
  }

  public void setCompoundStatement (CompoundStatement compoundStatement) {
    this.compoundStatement = compoundStatement;
  }

}
