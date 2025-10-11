package org.opal.ast.statement;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ForStatement extends AstNode {

  public ForStatement (Token token) {
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

  public AstNode forCondition () {
    return getChild(1);
  }

  public AstNode forInitializer () {
    return getChild(0);
  }

  public AstNode forUpdate () {
    return getChild(2);
  }

  public AstNode forBody () {
    return getChild(3);
  }

  public boolean hasForCondition () {
    return getChild(1) != null;
  }

  public boolean hasForInitializer () {
    return getChild(0) != null;
  }

  public boolean hasForUpdate () {
    return getChild(2) != null;
  }

}
