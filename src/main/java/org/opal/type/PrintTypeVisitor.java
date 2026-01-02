package org.opal.type;

public class PrintTypeVisitor implements TypeVisitor {

  public void visit (ArrayType typeNode) {
    System.out.println("Array");
    typeNode.getElementType().accept(this);
  }

  public void visit (NominalType typeNode) {
    System.out.println("NominalType");
  }

  public void visit (PointerType typeNode) {
    System.out.println("PointerType");
    typeNode.getPointeeType().accept(this);
  }

  public void visit (PrimitiveType typeNode) {
    System.out.println("PrimitiveType");
  }

}
