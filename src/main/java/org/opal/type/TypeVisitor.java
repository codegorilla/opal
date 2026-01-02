package org.opal.type;

public interface TypeVisitor {

  public void visit (ArrayType node);
  public void visit (NominalType node);
  public void visit (PointerType node);
  public void visit (PrimitiveType node);

}
