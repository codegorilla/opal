package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;
import org.opal.symbol.*;

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
    scope.define(new PrimitiveTypeSymbol("bool"));
    scope.define(new PrimitiveTypeSymbol("float"));
    scope.define(new PrimitiveTypeSymbol("float32"));
    scope.define(new PrimitiveTypeSymbol("float64"));
    scope.define(new PrimitiveTypeSymbol("int"));
    scope.define(new PrimitiveTypeSymbol("int8"));
    scope.define(new PrimitiveTypeSymbol("int16"));
    scope.define(new PrimitiveTypeSymbol("int32"));
    scope.define(new PrimitiveTypeSymbol("int64"));
    scope.define(new PrimitiveTypeSymbol("void"));
    currentScope = scope;
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    var scope = new Scope(Scope.Kind.GLOBAL);
    scope.setEnclosingScope(currentScope);
    node.setScope(scope);
  }

}
