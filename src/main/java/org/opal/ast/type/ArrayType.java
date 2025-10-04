package org.opal.ast.type;

import org.opal.Token;
import org.opal.ast.AstNode;
import org.opal.ResultVisitor;
import org.opal.Visitor;

public class ArrayType extends Type {

  public ArrayType (Token token) {
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

  public AstNode baseType () {
    var count = getChildCount();
    if (count == 1)
      return getChild(0);
    else if (count == 2)
      return getChild(1);
    else
      return null;
  }

  // The expression is optional. If there is only one child, then the
  // expression does not exist. Should we throw exception if there is only one
  // child?

  public AstNode expression () {
    return getChild(0);
  }

}
