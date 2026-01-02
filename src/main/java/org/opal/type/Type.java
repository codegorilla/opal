package org.opal.type;

import org.opal.Visitor;

public abstract class Type {

  public abstract void accept (TypeVisitor v);

}