package org.opal.ast.statement;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class LoopStatement extends Statement {

  public LoopStatement (Token token) {
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

  public boolean hasLoopControl () {
    return getChild(0) != null;
  }

  public AstNode loopControl () {
    return getChild(0);
  }

//  public AstNode forCondition () {
//    return getChild(1);
//  }
//
//  public AstNode forInitializer () {
//    return getChild(0);
//  }
//
//  public AstNode forUpdate () {
//    return getChild(2);
//  }

  public AstNode loopBody () {
    return getChild(1);
  }

//  public boolean hasLoopCondition () {
//    return getChild(1) != null;
//  }
//
//  public boolean hasLoopInitializer () {
//    return getChild(0) != null;
//  }
//
//  public boolean hasLoopUpdate () {
//    return getChild(2) != null;
//  }

}
