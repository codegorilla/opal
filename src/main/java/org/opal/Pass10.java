package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;
import org.opal.symbol.*;
import org.opal.type.PrimitiveType;
import org.opal.type.Type;
import org.opal.type.ArrayType;
import org.opal.type.PointerType;

import java.util.LinkedList;

// The purpose of this pass is to add types to the symbol table

public class Pass10 extends BaseVisitor {

  private final LinkedList<Type> typeStack = new LinkedList<>();

  private final Scope scope = new Scope(Scope.Kind.BUILT_IN);

  public Pass10 (AstNode input) {
    super(input);
  }

  public void process () {
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    // Define primitive types
    scope.define(new PrimitiveTypeSymbol("bool"));
    scope.define(new PrimitiveTypeSymbol("float"));
    scope.define(new PrimitiveTypeSymbol("float32"));
    scope.define(new PrimitiveTypeSymbol("float64"));
    scope.define(new PrimitiveTypeSymbol("int"));
    scope.define(new PrimitiveTypeSymbol("int8"));
    scope.define(new PrimitiveTypeSymbol("int16"));
    scope.define(new PrimitiveTypeSymbol("int32"));
    scope.define(new PrimitiveTypeSymbol("int64"));
    node.setScope(scope);
  }

}
