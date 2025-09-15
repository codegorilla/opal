package org.opal;

//import java.util.Deque;
import java.util.LinkedList;

import org.opal.ast.*;

import org.opal.symbol.Scope;
import org.opal.symbol.PrimitiveTypeSymbol;

public class Parser {

  private final int SLEEP_TIME = 100;

  private final LinkedList<Token> input;
  private int position;
  private Token lookahead;

  // Used to pass nodes up and down during tree traversal
  private final LinkedList<AstNode> stack;

  // Used for symbol table operations. Cobalt requires a symbol table during
  // parsing in order to disambiguate a few grammar rules. We cannot wait until
  // the semantic analysis phase to begin constructing symbol tables.
  private final Scope builtinScope;
  private Scope currentScope;

  // Todo: we may also need a 'null_t' type, for which there is exactly one
  // value, which is 'null'. This is to match the C++ 'nullptr_t' type and its
  // corresponding single 'nullptr' value. I am not sure if this is a primitive
  // type or not. Needs research.

  // Todo: We may decide that 'int', 'short', 'float', etc. should just be
  // typealiases for the various fixed size types.

  public Parser (LinkedList<Token> input) {
    this.input = input;
    position = 0;
    lookahead = input.get(position);
    stack = new LinkedList<>();
    builtinScope = new Scope(Scope.Kind.BUILT_IN);
    currentScope = builtinScope;
  }

  private void match (Token.Kind kind) {
    if (lookahead.getKind() == kind)
      consume();
    else
      System.out.println("invalid token: expected " + kind + ", got " + lookahead.getKind());
  }

  private void match (String lexeme) {
    // Note: If re-writing in java, we need to compare using .equals() method
    if (lookahead.getLexeme().equals(lexeme))
      consume();
    else
      System.out.println("invalid token: expected " + lexeme + ", got " + lookahead.getLexeme());
  }

  private void consume () {
    position += 1;
    lookahead = input.get(position);
  }

  public AstNode process () {
    definePrimitiveTypes();
    var node = translationUnit();

    // Inspect builtin scope
//    var s = builtinScope.getSymbolTable().getData;
//    System.out.println(s);

    return node;
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

  //  Visitor pattern?

  private AstNode translationUnit () {
    var n = new TranslationUnit(lookahead);
    var scope = new Scope(Scope.Kind.GLOBAL);
    scope.setEnclosingScope(currentScope);
    currentScope = scope;
    //n.setScope(currentScope);
    n.addChild(declarations());
    return n;
  }

  private AstNode declarations () {
    var n = new Declarations();
    while (lookahead.getKind() != Token.Kind.EOF) {
      // Infinite loop, need to consume
      System.out.println("Sleeping for " + SLEEP_TIME + " seconds in declarations...");
      try {
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      n.addChild(declaration());
    }
    return n;
  }

  private AstNode declaration () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.IMPORT)
      n = importDeclaration();
    else if (lookahead.getKind() == Token.Kind.VAR)
      n = variableDeclaration();
    return n;
  }

  private AstNode importDeclaration () {
    var n = new ImportDeclaration(lookahead);
    match(Token.Kind.IMPORT);
    n.addChild(importName());
    match(Token.Kind.SEMICOLON);
    return n;
  }

  private AstNode importName () {
    var n = new ImportName(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  // VARIABLE DECLARATIONS

  private AstNode variableDeclaration () {
    var n = new VariableDeclaration(lookahead);
    match(Token.Kind.VAR);
    n.addChild(variableName());
    n.addChild(variableTypeSpecifier());
    n.addChild(variableInitializer());
    match(Token.Kind.SEMICOLON);
    return n;
  }

  private AstNode variableName () {
    var n = new VariableName(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  private AstNode variableTypeSpecifier () {
    var n = new VariableTypeSpecifier();
    if (lookahead.getKind() == Token.Kind.COLON) {
      match(Token.Kind.COLON);
      n.addChild(typeRoot());
    }
    return n;
  }

  private AstNode variableInitializer () {
    var n = new VariableInitializer();
    if (lookahead.getKind() == Token.Kind.EQUAL) {
      match(Token.Kind.EQUAL);
      // Put expression here
    }
    return n;
  }

  // TYPES

  // Type processing is interesting because Cobalt uses a form of the
  // C-declaration style, so parsing types requires following the "spiral rule".
  // To make this easier, we make use of stack and queue types provided by the
  // language rather than complicating AST node class definition with parent
  // links. We can re-think this in the future if we wish.

  // Do we need a separate typeRoot node, or can we just use type_?

  private AstNode typeRoot () {
    var n = new TypeRoot();
    n.addChild(type());
    return n;
  }

  private AstNode type () {
    directType();
    // Need to remove items from parsing stack and construct final type. With
    // just arrays and pointers it should be easy. Once other types are added,
    // it will require slightly more processing.
    // Stack must have at least one element, e.g. primitive type
    var n = stack.pop();
    while (!stack.isEmpty()) {
      var p = n;
      n = stack.pop();
      n.addChild(p);
    }
    // Now trace through the type expression and print it out
    // while n.getChildCount() != 0 do
    //   println(n)
    //   if n.getKind() == AstNode.Kind.ARRAY_TYPE then
    //     n = n.getChild(1)
    //   else if n.getKind() == AstNode.Kind.POINTER_TYPE then
    //     n = n.getChild(0)
    // println(n)
    return n;
  }

  private void directType () {
    System.out.println("DIRECT_TYPE");
    // Build left type fragment
    var leftFragment = new LinkedList<AstNode>();
    while (lookahead.getKind() == Token.Kind.ASTERISK) {
      leftFragment.push(pointerType());
    }
    // Build center type fragment
    AstNode centerFragment = null;
    if (lookahead.getKind() == Token.Kind.CARET) {
//      // TBD
//      centerFragment = routinePointerType();
    }
    else if (
        lookahead.getKind() == Token.Kind.BOOL    ||
        lookahead.getKind() == Token.Kind.INT     ||
        lookahead.getKind() == Token.Kind.INT8    ||
        lookahead.getKind() == Token.Kind.INT16   ||
        lookahead.getKind() == Token.Kind.INT32   ||
        lookahead.getKind() == Token.Kind.INT64   ||
        lookahead.getKind() == Token.Kind.UINT    ||
        lookahead.getKind() == Token.Kind.UINT8   ||
        lookahead.getKind() == Token.Kind.UINT16  ||
        lookahead.getKind() == Token.Kind.UINT32  ||
        lookahead.getKind() == Token.Kind.UINT64  ||
        lookahead.getKind() == Token.Kind.FLOAT   ||
        lookahead.getKind() == Token.Kind.FLOAT32 ||
        lookahead.getKind() == Token.Kind.FLOAT64 ||
        lookahead.getKind() == Token.Kind.VOID)
    {
      centerFragment = primitiveType();
    }
    else if (lookahead.getKind() == Token.Kind.IDENTIFIER) {
      // Need to look up name in symbol table to tell what kind it is (e.g.
      // class, template). If it is defined as a class, then a left bracket
      // following indicates an array of that class type. If it is not defined
      // at all, then assume it is a class and treat it as such. If it is
      // defined as a class template, then a left bracket following denotes
      // class template parameters.

      // Todo: Hard-coded "Token here". This needs to be fixed.
      // COMMENTED WHEN DOING SCOPES - NEEDS FIX
      // currentScope.define(Symbol(Symbol.Kind.CLASS_TEMPLATE, "Token"))
      var symbol = currentScope.resolve(lookahead.getLexeme(), true);
      // COMMENTED WHEN DOING SCOPES - NEEDS FIX
      // if symbol == null then
      //   // Nominal types include classes and enums. They do NOT include
      //   // primitive types or template types.
      //   centerFragment = nominalType()
      // else
      //   if symbol.getKind() == Symbol.Kind.CLASS_TEMPLATE then
      //     centerFragment = templateType()
      //   else
      //     centerFragment = nominalType()
      centerFragment = nominalType();
    }
    else if (lookahead.getKind() == Token.Kind.L_PARENTHESIS) {
      match(Token.Kind.L_PARENTHESIS);
      directType();
      centerFragment = stack.pop();
      match(Token.Kind.R_PARENTHESIS);
    }
    // Build right type fragment
    var rightFragment = new LinkedList<AstNode>();
    while (lookahead.getKind() == Token.Kind.L_BRACKET) {
      rightFragment.offer(arrayType());
      // Move type fragments to parsing stack in "spiral rule" order
    }
    while (!rightFragment.isEmpty()) {
      stack.push(rightFragment.poll());
    }
    while (!leftFragment.isEmpty()) {
      stack.push(leftFragment.pop());
    }
    stack.push(centerFragment);

  }

  private AstNode arrayType () {
    var n = new ArrayType(lookahead);
    match(Token.Kind.L_BRACKET);
    if (lookahead.getKind() != Token.Kind.R_BRACKET) {
      //n.addChild(expression(root=true));
    }
    match(Token.Kind.R_BRACKET);
    return n;
  }

  private AstNode nominalType () {
    var n = new NominalType(lookahead);
    match(Token.Kind.IDENTIFIER);
    System.out.println("FOUND NOMINAL TYPE");
    // Need to eventually allow for type parameters. (This would allow
    // us to know that this was a class type, if that matters.)
    return n;
  }

  private AstNode pointerType () {
    var n = new PointerType(lookahead);
    match(Token.Kind.ASTERISK);
    return n;
  }

  private AstNode primitiveType () {
    var n = new PrimitiveType(lookahead);
    match(lookahead.getKind());
    return n;
  }



}
