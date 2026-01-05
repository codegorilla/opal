package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.symbol.*;
import org.opal.type.PrimitiveType;
import org.opal.type.Type;

// The purpose of this pass is to add types to the symbol table.

// Note: For now, this just adds built-in types. Other types can be added
// later. They might be added in a separate pass, which will require traversing
// the AST.

public class Pass10 extends BaseVisitor {

  private Scope currentScope = null;

  public Pass10 (AstNode input) {
    super(input);
  }

  public void process () {
    // Define primitive types in built-in scope
    var scope = new Scope(Scope.Kind.BUILT_IN);
    scope.define(new TypeSymbol("bool", PrimitiveType.BOOL));
    scope.define(new TypeSymbol("float", PrimitiveType.FLOAT));
    scope.define(new TypeSymbol("float32", PrimitiveType.FLOAT32));
    scope.define(new TypeSymbol("float64", PrimitiveType.FLOAT64));
    scope.define(new TypeSymbol("int", PrimitiveType.INT));
    scope.define(new TypeSymbol("int8", PrimitiveType.INT8));
    scope.define(new TypeSymbol("int16", PrimitiveType.INT16));
    scope.define(new TypeSymbol("int32", PrimitiveType.INT32));
    scope.define(new TypeSymbol("int64", PrimitiveType.INT64));
    scope.define(new TypeSymbol("uint", PrimitiveType.UINT));
    scope.define(new TypeSymbol("uint8", PrimitiveType.UINT8));
    scope.define(new TypeSymbol("uint16", PrimitiveType.UINT16));
    scope.define(new TypeSymbol("uint32", PrimitiveType.UINT32));
    scope.define(new TypeSymbol("uint64", PrimitiveType.UINT64));
    scope.define(new TypeSymbol("void", PrimitiveType.VOID));
    currentScope = scope;
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    var scope = new Scope(Scope.Kind.GLOBAL);
    scope.setEnclosingScope(currentScope);
    node.setScope(scope);
  }

}
