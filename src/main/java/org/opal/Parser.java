package org.opal;

import org.opal.symbol.Scope;
import org.opal.symbol.PrimitiveTypeSymbol;

import java.util.ArrayList;
import java.util.LinkedList;

public class Parser {

  private final int SLEEP_TIME = 10;

  private final LinkedList<Token> input = null;
  private int position = 0;
  private Token lookahead = null;

  // Used to pass nodes up and down during tree traversal
  private final ArrayList<AstNode> stack = new ArrayList<AstNode>();

  // Used for symbol table operations. Cobalt requires a symbol table during
  // parsing in order to disambiguate a few grammar rules. We cannot wait until
  // the semantic analysis phase to begin constructing symbol tables.
  private final Scope builtinScope = new Scope(Scope.Kind.BUILT_IN);
  private Scope currentScope = builtinScope;

  // Todo: we may also need a 'null_t' type, for which there is exactly one
  // value, which is 'null'. This is to match the C++ 'nullptr_t' type and its
  // corresponding single 'nullptr' value. I am not sure if this is a primitive
  // type or not. Needs research.

  // Todo: We may decide that 'int', 'short', 'float', etc. should just be
  // typealiases for the various fixed size types.

  public Parser () {
  }

  private void definePrimitiveTypes () {
    builtinScope.define(new PrimitiveTypeSymbol("bool"));
    builtinScope.define(new PrimitiveTypeSymbol("int"));
    builtinScope.define(new PrimitiveTypeSymbol("int8"));
    builtinScope.define(new PrimitiveTypeSymbol("int16"));
    builtinScope.define(new PrimitiveTypeSymbol("int32"));
    builtinScope.define(new PrimitiveTypeSymbol("int64"));
    builtinScope.define(new PrimitiveTypeSymbol("float"));
    builtinScope.define(new PrimitiveTypeSymbol("float32"));
    builtinScope.define(new PrimitiveTypeSymbol("float64"));
    builtinScope.define(new PrimitiveTypeSymbol("void"));
  }

}
