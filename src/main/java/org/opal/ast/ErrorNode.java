package org.opal.ast;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;

// It would be more consistent to just call this error, but we want to
// disambiguate from java.lang.error, org.opal.error, etc. so for now
// we will name it ErrorNode until a better name is decided upon.

public class ErrorNode extends AstNode {

  public ErrorNode (Token token) {
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

}
