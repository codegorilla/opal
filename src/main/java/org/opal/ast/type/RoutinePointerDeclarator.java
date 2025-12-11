package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutinePointerDeclarator extends Declarator {

  public RoutinePointerDeclarator (Token token) {
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

  // Depends how many children there are. There can only be a maximum of two
  // children. Should we throw exception if there are more than two children?

  public AstNode parameterTypes () {
//    var count = getChildCount();
//    if (count == 1)
//      return getChild(0);
//    else if (count == 2)
//      return getChild(1);
//    else
    return null;
  }

  // The expression is optional. If there is only one child, then the
  // expression does not exist. Should we throw exception if there is only one
  // child?

  public AstNode returnType () {
    return getChild(getChildCount() - 1);
  }

}
