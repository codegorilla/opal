package org.opal;

//import java.util.Deque;
import java.util.LinkedList;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

import org.opal.symbol.Scope;
import org.opal.symbol.PrimitiveTypeSymbol;

public class Parser {

  private final int SLEEP_TIME = 10;

  private final LinkedList<Token> input;
  private int position;
  private Token lookahead;

  // Used to pass type nodes up and down during tree traversal
  private final LinkedList<Type> stack;

  // Used to collect modifier nodes in preparation for aggregation into
  // specialized modifiers nodes.
  private final LinkedList<AstNode> modifierStack;

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
    modifierStack = new LinkedList<>();
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

  private AstNode translationUnit () {
    var n = new TranslationUnit(lookahead);
    var scope = new Scope(Scope.Kind.GLOBAL);
    scope.setEnclosingScope(currentScope);
    currentScope = scope;
    //n.setScope(currentScope);
    n.addChild(packageDeclaration());
    if (lookahead.getKind() == Token.Kind.IMPORT)
      n.addChild(importDeclarations());
    n.addChild(declarations());
    return n;
  }

  // DECLARATIONS **************************************************

  // Package declaration is special in that there is only one, and it must
  // appear at the top of the translation unit.

  private AstNode packageDeclaration () {
    var n = new PackageDeclaration(lookahead);
    match(Token.Kind.PACKAGE);
    n.addChild(packageName());
    match(Token.Kind.SEMICOLON);
    return n;
  }

  private AstNode packageName () {
    var n = new PackageName(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  private AstNode importDeclarations () {
    var n = new ImportDeclarations();
    while (lookahead.getKind() == Token.Kind.IMPORT)
      n.addChild(importDeclaration());
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

  // To do: Implement use declaration

  private AstNode declaration () {
    AstNode n = null;
    var spec = (lookahead.getKind() == Token.Kind.PRIVATE) ? exportSpecifier() : null;
    if (lookahead.getKind() == Token.Kind.TEMPLATE)
      ; //n = templateDeclaration();
    else {
      modifiers();
      switch (lookahead.getKind()) {
        case Token.Kind.CLASS ->
          n = classDeclaration(spec);
        case Token.Kind.DEF ->
          n = routineDeclaration(spec);
        case Token.Kind.VAL, Token.Kind.VAR ->
          n = variableDeclaration(spec);
        default ->
          n = null;
      }
    }
    return n;
  }

  // EXPORT SPECIFIERS

  // Entities may be declared as private, indicating that they are not
  // exported. Otherwise, they are considered public and exported.

  private ExportSpecifier exportSpecifier () {
    var n = new ExportSpecifier(lookahead);
    match(Token.Kind.PRIVATE);
    return n;
  }

  // MODIFIERS

  // Todo: We might just want to have one kind of modifier node and let the
  // token indicate what kind of modifier it is. The problem with this is that
  // some modifiers are added implicitly (e.g. 'final' in the case of 'val'), so
  // such modifiers do not actually have tokens. We could create a virtual token
  // but it would not have a position in the character stream, so it would lack
  // things like a column and line number. Another issue is the might need to
  // handle modifiers that have arguments, such as 'alignas'. However, the
  // visitor pattern allows custom node types so these issues might not be a
  // problem.

  // I would like the 'final' modifier on variables to be equivalent to using
  // 'const' in C++ because I want to use the 'const' modifier to mean the same
  // as 'constexpr' in C++. This might not be possible because 'const' in C++
  // is not really equivalent to 'final' in Java -- it implies additional
  // constraints on the code, where it requires other things to be 'const' as
  // well. It would be strange if we made 'final' require other things to be
  // 'final' as well, if in that other context, 'final' didn't sound right or
  // 'final' was already defined to mean something else. If we have to use
  // 'const' instead of 'final' then we'll need to find something else to use
  // instead of 'const' for compile-time constants, such as 'comptime'. I have a
  // natural aversion to 'constexpr' for some reason... it's a bit obtuse for my
  // taste.

  private void modifiers () {
    var kind = lookahead.getKind();
    while (
      kind == Token.Kind.ABSTRACT  ||
      kind == Token.Kind.CONST     ||
      kind == Token.Kind.CONSTEXPR ||
      kind == Token.Kind.FINAL     ||
      kind == Token.Kind.OVERRIDE  ||
      kind == Token.Kind.STATIC    ||
      kind == Token.Kind.VIRTUAL   ||
      kind == Token.Kind.VOLATILE
    ) {
      modifier();
      kind = lookahead.getKind();
    }
  }

  private void modifier () {
    var n = new Modifier(lookahead);
    match(lookahead.getKind());
    modifierStack.push(n);
  }

  // CLASS DECLARATIONS

  private AstNode classDeclaration (AstNode exportSpecifier) {
    var n = new ClassDeclaration(lookahead);
    match(Token.Kind.CLASS);
    n.addChild(exportSpecifier);
    n.addChild(classModifiers());
    n.addChild(className());
    n.addChild((lookahead.getKind() == Token.Kind.EXTENDS) ? classExtendsClause() : null);
    n.addChild(classBody());
    return n;
  }

  private AstNode classModifiers () {
    var n = new ClassModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  // Todo: Should symbols point to AST node, and/or vice versa? This might come
  // in handy later on, but wait until its needed before adding the code.

  private AstNode className () {
    var n = new ClassName(lookahead);
    match(Token.Kind.IDENTIFIER);
    //var s = ClassSymbol(n.getToken().lexeme);
    //currentScope.define(s);
    return n;
  }

  private AstNode classExtendsClause () {
    var n = new ClassExtendsClause(lookahead);
    match(Token.Kind.EXTENDS);
    n.addChild(baseClasses());
    return n;
  }

  // For now, we only support public (i.e. "is-a") inheritance. Private (i.e.
  // "is-implemented-in-terms-of") inheritance is NOT supported. Most use cases
  // of private inheritance are better met by composition instead.

  private AstNode baseClasses () {
    var n = new BaseClasses(lookahead);
    n.addChild(baseClass());
    while (lookahead.getKind() == Token.Kind.COMMA) {
      match(Token.Kind.COMMA);
      n.addChild(baseClass());
    }
    return n;
  }

  // To do: Move this to base class name

  private AstNode baseClass () {
    var n = new BaseClass(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  // The token here is simply the curly brace '{'. Do we need to track this?
  // It will depend on whether or not this sort of thing helps with error
  // reporting and debugging.

  private AstNode classBody () {
    var n = new ClassBody(lookahead);
    match(Token.Kind.L_BRACE);
    while (lookahead.getKind() != Token.Kind.R_BRACE)
      n.addChild(memberDeclaration());
    match(Token.Kind.R_BRACE);
    return n;
  }

  // MEMBER DECLARATIONS

  private AstNode memberDeclaration () {
    var kind = lookahead.getKind();
    var spec = (
      kind == Token.Kind.PRIVATE ||
      kind == Token.Kind.PROTECTED
    ) ? memberAccessSpecifier() : null;
    modifiers();
    var n = switch (lookahead.getKind()) {
      case Token.Kind.DEF -> memberRoutineDeclaration(spec);
      case Token.Kind.VAL, Token.Kind.VAR -> memberVariableDeclaration(spec);
      default -> null;
    };
    return n;
  }

  private MemberAccessSpecifier memberAccessSpecifier () {
    var n = new MemberAccessSpecifier(lookahead);
    match(lookahead.getKind());
    return n;
  }

  private AstNode memberVariableDeclaration (MemberAccessSpecifier accessSpecifier) {
    var n = new MemberVariableDeclaration(lookahead);
    match(Token.Kind.VAR);
    n.addChild(accessSpecifier);
    n.addChild(variableModifiers());
    n.addChild(variableName());
    n.addChild((lookahead.getKind() == Token.Kind.COLON) ? variableTypeSpecifier() : null);
    n.addChild((lookahead.getKind() == Token.Kind.EQUAL) ? variableInitializer() : null);
    match(Token.Kind.SEMICOLON);
    return n;
  }

  // To do: Final and override modifiers

  private AstNode memberRoutineDeclaration (MemberAccessSpecifier accessSpecifier) {
    var n = new MemberRoutineDeclaration(lookahead);
    match(Token.Kind.DEF);
    n.addChild(accessSpecifier);
    n.addChild(memberRoutineModifiers());
    n.addChild(routineName());
    n.addChild(routineParameters());
    n.addChild(cvQualifiers());
    n.addChild(refQualifiers());
    n.addChild((lookahead.getKind() == Token.Kind.NOEXCEPT) ? noexceptSpecifier() : null);
    n.addChild((lookahead.getKind() == Token.Kind.MINUS_GREATER) ? routineReturnType() : null);
    n.addChild(routineBody());
//    currentScope = scope.getEnclosingScope();
    return n;
  }

  private AstNode memberRoutineModifiers () {
    var n = new MemberRoutineModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  private AstNode cvQualifiers () {
    var n = new CVQualifiers();
    var kind = lookahead.getKind();
    while (kind == Token.Kind.CONST || kind == Token.Kind.VOLATILE) {
      n.addChild(cvQualifier());
      kind = lookahead.getKind();
    }
    return n;
  }

  private AstNode cvQualifier () {
    var n = new CVQualifier(lookahead);
    match(lookahead.getKind());
    return n;
  }

  private AstNode refQualifiers () {
    var n = new RefQualifiers();
    var kind = lookahead.getKind();
    while (kind == Token.Kind.AMPERSAND || kind == Token.Kind.AMPERSAND_AMPERSAND) {
      n.addChild(refQualifier());
      kind = lookahead.getKind();
    }
    return n;
  }

  private AstNode refQualifier () {
    var n = new RefQualifier(lookahead);
    match(lookahead.getKind());
    return n;
  }

  // ROUTINE DECLARATIONS

  // Todo: We need to push another scope onto the scope stack. Keep in mind that
  // the routine parameters may be in the same exact scope as the routine body
  // (or top-most block of the routine).

  // For now, there are no local routines, so no need to distinguish between
  // global and local routines.

  // To do: Need to handle no return type

  private AstNode routineDeclaration (ExportSpecifier exportSpecifier) {
    var n = new RoutineDeclaration(lookahead);
    match(Token.Kind.DEF);
//    var scope = Scope(Scope.Kind.LOCAL);
//    scope.setEnclosingScope(currentScope);
//    currentScope = scope;
//    n.setScope(currentScope);
    n.addChild(exportSpecifier);
    n.addChild(routineModifiers());
    n.addChild(routineName());
    n.addChild(routineParameters());
    n.addChild((lookahead.getKind() == Token.Kind.NOEXCEPT) ? noexceptSpecifier() : null);
    n.addChild((lookahead.getKind() == Token.Kind.MINUS_GREATER) ? routineReturnType() : null);
    n.addChild(routineBody());
//    currentScope = scope.getEnclosingScope();
    return n;
  }

  private AstNode routineModifiers () {
    var n = new RoutineModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  // Todo: Should symbols point to AST node, and/or vice versa? This might come
  // in handy later on, but wait until its needed before adding the code.

  private AstNode routineName () {
    var n = new RoutineName(lookahead);
    match(Token.Kind.IDENTIFIER);
    //var s = RoutineSymbol(n.getToken().lexeme);
    //currentScope.define(s);
    return n;
  }

  private AstNode routineParameters () {
    var n = new RoutineParameters();
    match(Token.Kind.L_PARENTHESIS);
    if (lookahead.getKind() == Token.Kind.IDENTIFIER)
      n.addChild(routineParameter());
    while (lookahead.getKind() == Token.Kind.COMMA) {
      match(Token.Kind.COMMA);
      n.addChild(routineParameter());
    }
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // Routine parameters are for all intents and purposes local variables

  private AstNode routineParameter () {
    var n = new RoutineParameter();
    n.addChild(routineParameterName());
    n.addChild(routineParameterTypeSpecifier());
    return n;
  }

  private AstNode routineParameterName () {
    var n = new RoutineParameterName(lookahead);
    match(Token.Kind.IDENTIFIER);
    //var s = RoutineParameterSymbol(n.getToken().lexeme);
    //currentScope.define(s);
    return n;
  }

  private AstNode routineParameterTypeSpecifier () {
    var n = new RoutineParameterTypeSpecifier(lookahead);
    match(Token.Kind.COLON);
    n.addChild(type(true));
    return n;
  }

  private AstNode noexceptSpecifier () {
    var n = new NoexceptSpecifier(lookahead);
    match(Token.Kind.NOEXCEPT);
    return n;
  }

  // We need to decide if we want to use an arrow or a colon for the result
  // type. C++, python, ruby, swift, ocaml, haskell, and rust all use an arrow,
  // while scala, kotlin, typescript, and pascal all use a colon. The proper
  // choice probably depends on whether or not the language in question already
  // uses colons and/or arrows for other things (and the amount that the symbol
  // appears in the program text); and whether it would lead to grammar
  // ambiguities. For now, we will use an arrow.

  // We can either treat this like a type specifier or use it as a passthrough
  // to a type specifier.

  private AstNode routineReturnType () {
    var n = new RoutineReturnType();
    if (lookahead.getKind() == Token.Kind.MINUS_GREATER) {
      match(Token.Kind.MINUS_GREATER);
      n.addChild(type(true));
    }
    return n;
  }

  // Do we need to distinguish between a top compound statement and a regular
  // compound statement? The top compound statement does not need to introduce a
  // new scope.

  private AstNode routineBody () {
    var n = new RoutineBody();
    n.addChild(compoundStatement());
    return n;
  }

  // VARIABLE DECLARATIONS

  // To do: Local variables don't have access specifiers. Because children can be accessed by name, we need a separate
  // local variable node type.

  // We put null values into the list of children to ensure a constant node
  // count and node order.

  private AstNode variableDeclaration (ExportSpecifier exportSpecifier) {
    var n = new VariableDeclaration(lookahead);
    match(Token.Kind.VAR);
    n.addChild(exportSpecifier);
    n.addChild(variableModifiers());
    n.addChild(variableName());
    n.addChild((lookahead.getKind() == Token.Kind.COLON) ? variableTypeSpecifier() : null);
    n.addChild((lookahead.getKind() == Token.Kind.EQUAL) ? variableInitializer() : null);
    match(Token.Kind.SEMICOLON);
    return n;
  }

  private AstNode variableModifiers () {
    var n = new VariableModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  private AstNode variableName () {
    var n = new VariableName(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  private AstNode variableTypeSpecifier () {
    var n = new VariableTypeSpecifier(lookahead);
    match(Token.Kind.COLON);
    n.addChild(type(true));
    return n;
  }

  private AstNode variableInitializer () {
    var n = new VariableInitializer(lookahead);
    match(Token.Kind.EQUAL);
    n.addChild(expression(true));
    return n;
  }

  private AstNode localVariableDeclaration () {
    AstNode n = new LocalVariableDeclaration(lookahead);
    match(Token.Kind.VAR);
    n.addChild(variableModifiers());
    n.addChild(variableName());
    n.addChild((lookahead.getKind() == Token.Kind.COLON) ? variableTypeSpecifier() : null);
    n.addChild((lookahead.getKind() == Token.Kind.EQUAL) ? variableInitializer() : null);
    match(Token.Kind.SEMICOLON);
    return n;
  }

  // STATEMENTS **************************************************

  // Notice that we don't include 'if' in the first set for expression
  // statements. This is because we want send the parser towards the statement
  // version of 'if'.

  private AstNode statement () {
    AstNode n = null;
    Token.Kind kind = lookahead.getKind();
    if (
      kind == Token.Kind.BREAK     ||
      kind == Token.Kind.L_BRACE   ||
      kind == Token.Kind.CONTINUE  ||
      kind == Token.Kind.DO        ||
      kind == Token.Kind.FOR       ||
      kind == Token.Kind.LOOP      ||
      kind == Token.Kind.SEMICOLON ||
      kind == Token.Kind.IF        ||
      kind == Token.Kind.RETURN    ||
      kind == Token.Kind.UNTIL     ||
      kind == Token.Kind.WHILE
    ) {
      n = standardStatement();
    } else if (
      kind == Token.Kind.CLASS ||
      kind == Token.Kind.DEF   ||
      kind == Token.Kind.VAR
    ) {
      n = declarationStatement();
    } else if (
      kind == Token.Kind.FALSE             ||
      kind == Token.Kind.TRUE              ||
      kind == Token.Kind.CHARACTER_LITERAL ||
      kind == Token.Kind.FLOAT32_LITERAL   ||
      kind == Token.Kind.FLOAT64_LITERAL   ||
      kind == Token.Kind.INT32_LITERAL     ||
      kind == Token.Kind.INT64_LITERAL     ||
      kind == Token.Kind.NULL              ||
      kind == Token.Kind.STRING_LITERAL    ||
      kind == Token.Kind.UINT32_LITERAL    ||
      kind == Token.Kind.UINT64_LITERAL    ||
      kind == Token.Kind.IDENTIFIER
    ) {
      n = expressionStatement();
    } else {
      System.out.println("Error: invalid statement");
    }
    return n;
  }

  private AstNode standardStatement () {
    AstNode n = null;
    Token.Kind kind = lookahead.getKind();
    switch (kind) {
      case Token.Kind.BREAK ->
        n = breakStatement();
      case Token.Kind.L_BRACE ->
        n = compoundStatement();
      case Token.Kind.CONTINUE ->
        n = continueStatement();
      case Token.Kind.DO ->
        n = doStatement();
      case Token.Kind.SEMICOLON ->
        n = emptyStatement();
      case Token.Kind.FOR ->
        n = forStatement();
      case Token.Kind.IF ->
        n = ifStatement();
      case Token.Kind.LOOP ->
        n = loopStatement();
      case Token.Kind.RETURN ->
        n = returnStatement();
      case Token.Kind.UNTIL ->
        n = untilStatement();
      case Token.Kind.WHILE ->
        n = whileStatement();
      default ->
      {}
    }
    return n;
  }

  private AstNode breakStatement () {
    var n = new BreakStatement(lookahead);
    match(Token.Kind.BREAK);
    match(Token.Kind.SEMICOLON);
    return n;
  }

  private AstNode compoundStatement () {
    var n = new CompoundStatement(lookahead);
    match(Token.Kind.L_BRACE);
    while (lookahead.getKind() != Token.Kind.R_BRACE) {
      n.addChild(statement());
      try {
        System.out.println("Sleeping for " + SLEEP_TIME + " seconds in compoundStatement...");
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    match(Token.Kind.R_BRACE);
    return n;
  }

  private AstNode continueStatement () {
    var n = new ContinueStatement(lookahead);
    match(Token.Kind.CONTINUE);
    match(Token.Kind.SEMICOLON);
    return n;
  }

  // For now we only support variable declaration statements (i.e. local
  // variables). I do not think cobalt needs local classes since they have
  // significant limitations in C++ and only niche use cases. I would like to
  // have nested routines, but since C++ doesn't have them, I need to research
  // the feasibility of that idea. We might be able to compile nested routines
  // into C++ lambda functions.

  // It seems that we can just use the existing variableDeclaration production
  // but if we need to distinguish between local and global variables, then we
  // might need a separate production. Alternatively, we could use a flag to
  // signify one or the other. Or we can defer the question to later phases.

  // Should this just be a passthrough or do we want dedicated AST nodes at the
  // root of all statement sub-trees?

  private AstNode declarationStatement () {
    AstNode n = null;
    modifiers();
    var kind = lookahead.getKind();
    if (kind == Token.Kind.VAL || kind == Token.Kind.VAR)
      n = localVariableDeclaration();
    // To do: Need error checking here
    return n;
  }

  // The do statement is flexible and can either be a "do while" or a "do until"
  // statement, depending on what follows the 'do' keyword.

  private AstNode doStatement () {
    AstNode n = null;
    match(Token.Kind.DO);
    switch (lookahead.getKind()) {
      case Token.Kind.UNTIL ->
        n = doUntilStatement();
      case Token.Kind.WHILE ->
        n = doWhileStatement();
      default ->
        System.out.println("Error - invalid do statement");
    }
    return n;
  }

  private AstNode doUntilStatement () {
    var n = new DoUntilStatement(lookahead);
    match(Token.Kind.UNTIL);
    n.addChild(statementCondition());
    n.addChild(statementBody());
    return n;
  }

  private AstNode doWhileStatement () {
    var n = new DoWhileStatement(lookahead);
    match(Token.Kind.WHILE);
    n.addChild(statementCondition());
    n.addChild(statementBody());
    return n;
  }

  // Note: Microsoft calls this a "null statement", but the offical C++ standard
  // (latest ISO/IEC 14882:2023) has always used the term "empty statement". I
  // like the official term more.

  // Update: The situation might be a little more complex, see ISO/IEC IS 14882
  // Draft C++ standard, N3092.

  // Note: Empty statements may be a type of expression statement under C++
  // rules. I am not sure how much sense that makes because an expression
  // statement should presumably evaluate to some value, but a null statement
  // does not. (On the other hand, a procedure call is considered an expression
  // statement and also does not evaluate to a value.)

  // An empty statement could theoretically produce no AST node at all, since it
  // is a "noop". However, this may be useful to do because it will provide a
  // more faithful translation to C++. It will be optimized out by C++ anyways.

  private AstNode emptyStatement () {
    var n = new EmptyStatement(lookahead);
    match(Token.Kind.SEMICOLON);
    return n;
  }

  // The expression statement is primarily just a passthrough, but we want a
  // dedicated AST node at the root of all statement sub-trees.
  // NOTE: IS ABOVE TRUE?

  private AstNode expressionStatement () {
    var n = new ExpressionStatement();
    n.addChild(expression(true));
    match(Token.Kind.SEMICOLON);
    return n;
  }

  private AstNode forStatement () {
    var n = new ForStatement(lookahead);
    match(Token.Kind.FOR);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(forName());
    match(Token.Kind.IN);
    n.addChild(expression(true));
    match(Token.Kind.R_PARENTHESIS);
    n.addChild(statementBody());
    return n;
  }

  private AstNode forName () {
    var n = new Name(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  private AstNode ifStatement () {
    var n = new IfStatement(lookahead);
    match(Token.Kind.IF);
    n.addChild(statementCondition());
    n.addChild(statementBody());
    if (lookahead.getKind() == Token.Kind.ELSE)
      n.addChild(elseClause());
    return n;
  }

  private AstNode elseClause () {
    var n = new ElseClause(lookahead);
    match(Token.Kind.ELSE);
    if (lookahead.getKind() == Token.Kind.IF)
      n.addChild(ifStatement());
    else
      n.addChild(statementBody());
    return n;
  }

  private AstNode loopStatement () {
    var n = new LoopStatement(lookahead);
    match(Token.Kind.LOOP);
    if (lookahead.getKind() == Token.Kind.L_PARENTHESIS)
      n.addChild(loopControl());
    else
      n.addChild(null);
    n.addChild(loopBody());
    return n;
  }

  private AstNode loopControl () {
    var n = new LoopControl(lookahead);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(lookahead.getKind() != Token.Kind.SEMICOLON ? loopInitializer() : null);
    match(Token.Kind.SEMICOLON);
    n.addChild(lookahead.getKind() != Token.Kind.SEMICOLON ? loopCondition() : null);
    match(Token.Kind.SEMICOLON);
    n.addChild(lookahead.getKind() != Token.Kind.R_PARENTHESIS ? loopUpdate() : null);
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // To do: There can be multiple initializer expressions separated by commas.

  private AstNode loopInitializer () {
    var n = new LoopInitializer();
    n.addChild(expression(true));
    return n;
  }

  private AstNode loopCondition () {
    var n = new LoopCondition();
    n.addChild(expression(true));
    return n;
  }

  // To do: There can be multiple update expressions separated by commas.

  private AstNode loopUpdate () {
    var n = new LoopUpdate();
    n.addChild(expression(true));
    return n;
  }

  private AstNode loopBody () {
    if (lookahead.getKind() == Token.Kind.L_BRACE)
      return compoundStatement();
    else {
      // Insert fabricated compound statement
      var n = new CompoundStatement(null);
      n.addChild(statement());
      return n;
    }
  }

  private AstNode returnStatement () {
    var n = new ReturnStatement(lookahead);
    match(Token.Kind.RETURN);
    if (lookahead.getKind() != Token.Kind.SEMICOLON) {
      n.addChild(expression(true));
      match(Token.Kind.SEMICOLON);
    }
    return n;
  }

  // C++ doesn't have 'until' statements, but I would like to have them. I often
  // need an "until (done) doSomething();" loop. Yes, that requirement can be
  // met by a 'while' statement such as "while (!done) doSomething();" but I
  // prefer to have the option to use either one. If the 'until' statement
  // proves controversial or unpopular (or is deemed to not be orthogonal
  // enough) then it can be removed later.

  // In C++26 the condition can be an expression or a declaration. For now, we
  // will only support expressions, and use the rule as a passthrough.

  private AstNode untilStatement () {
    var n = new UntilStatement(lookahead);
    match(Token.Kind.UNTIL);
    n.addChild(statementCondition());
    n.addChild(statementBody());
    return n;
  }

  // In C++26 the condition can be an expression or a declaration. For now, we
  // will only support expressions, and use the rule as a passthrough.

  private AstNode whileStatement () {
    var n = new WhileStatement(lookahead);
    match(Token.Kind.WHILE);
    n.addChild(statementCondition());
    n.addChild(statementBody());
    return n;
  }

  private AstNode statementCondition () {
    match(Token.Kind.L_PARENTHESIS);
    var n = expression(true);
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // Notice that fabricated AST nodes do not have tokens. This could be a
  // source of bugs, so we need to be careful.

  private AstNode statementBody () {
    if (lookahead.getKind() == Token.Kind.L_BRACE)
      return statement();
    else {
      // Insert fabricated compound statement
      var n = new CompoundStatement(null);
      n.addChild(statement());
      return n;
    }
  }

  // EXPRESSIONS **************************************************

  // The root of every expression sub-tree has an explicit 'expression' AST
  // node, where the final computed type and other synthesized attributes can be
  // stored to aid in such things as type-checking. This might not actually be
  // necessary. We can use 'instanceof' to know if any expression node is the
  // root expression node or not.

  private AstNode expression (boolean root) {
    var n = assignmentExpression();
    if (root) {
      var p = n;
      n = new Expression();
      n.addChild(p);
    }
    return n;
  }

  // We may wish to add a 'walrus' operator (:=), which can be used inside a
  // conditional statement to indicate that the developer truly intends to have
  // an assignment rather than an equality check.

  private AstNode assignmentExpression () {
    var n = logicalOrExpression();
    while (
        lookahead.getKind() == Token.Kind.EQUAL ||
        lookahead.getKind() == Token.Kind.ASTERISK_EQUAL ||
        lookahead.getKind() == Token.Kind.SLASH_EQUAL ||
        lookahead.getKind() == Token.Kind.PERCENT_EQUAL ||
        lookahead.getKind() == Token.Kind.PLUS_EQUAL ||
        lookahead.getKind() == Token.Kind.MINUS_EQUAL ||
        lookahead.getKind() == Token.Kind.LESS_LESS_EQUAL ||
        lookahead.getKind() == Token.Kind.GREATER_GREATER_EQUAL ||
        lookahead.getKind() == Token.Kind.AMPERSAND_EQUAL ||
        lookahead.getKind() == Token.Kind.CARET_EQUAL ||
        lookahead.getKind() == Token.Kind.BAR_EQUAL
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      p = logicalOrExpression();
      n.addChild(p);
    }
    return n;
  }

  private AstNode logicalOrExpression () {
    var n = logicalAndExpression();
    while (lookahead.getKind() == Token.Kind.OR) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(Token.Kind.OR);
      n.addChild(logicalAndExpression());
    }
    return n;
  }

  private AstNode logicalAndExpression () {
    var n = inclusiveOrExpression();
    while (lookahead.getKind() == Token.Kind.AND) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(Token.Kind.AND);
      n.addChild(inclusiveOrExpression());
    }
    return n;
  }

  private AstNode inclusiveOrExpression () {
    var n = exclusiveOrExpression();
    while (lookahead.getKind() == Token.Kind.BAR) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(Token.Kind.BAR);
      n.addChild(exclusiveOrExpression());
    }
    return n;
  }

  private AstNode exclusiveOrExpression () {
    var n = andExpression();
    while (lookahead.getKind() == Token.Kind.CARET) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(Token.Kind.CARET);
      n.addChild(andExpression());
    }
    return n;
  }

  private AstNode andExpression () {
    var n = equalityExpression();
    while (lookahead.getKind() == Token.Kind.AMPERSAND) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(Token.Kind.AMPERSAND);
      n.addChild(equalityExpression());
    }
    return n;
  }

  private AstNode equalityExpression () {
    var n = relationalExpression();
    while (
      lookahead.getKind() == Token.Kind.EQUAL_EQUAL ||
      lookahead.getKind() == Token.Kind.EXCLAMATION_EQUAL
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      n.addChild(relationalExpression());
    }
    return n;
  }

  private AstNode relationalExpression () {
    var n = shiftExpression();
    while (
      lookahead.getKind() == Token.Kind.GREATER ||
      lookahead.getKind() == Token.Kind.LESS ||
      lookahead.getKind() == Token.Kind.GREATER_EQUAL ||
      lookahead.getKind() == Token.Kind.LESS_EQUAL
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      n.addChild(shiftExpression());
    }
    return n;
  }

  private AstNode shiftExpression () {
    var n = additiveExpression();
    while (
      lookahead.getKind() == Token.Kind.GREATER_GREATER ||
      lookahead.getKind() == Token.Kind.LESS_LESS
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      n.addChild(additiveExpression());
    }
    return n;
  }

  private AstNode additiveExpression () {
    var n = multiplicativeExpression();
    while (
      lookahead.getKind() == Token.Kind.PLUS ||
      lookahead.getKind() == Token.Kind.MINUS
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      n.addChild(multiplicativeExpression());
    }
    return n;
  }

  private AstNode multiplicativeExpression () {
    var n = unaryExpression();
    while (
      lookahead.getKind() == Token.Kind.ASTERISK ||
      lookahead.getKind() == Token.Kind.SLASH ||
      lookahead.getKind() == Token.Kind.PERCENT
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      n.addChild(unaryExpression());
    }
    return n;
  }

  // Why recursion here instead of iteration? Does it matter?

  private AstNode unaryExpression () {
    AstNode n = null;
    var kind = lookahead.getKind();
    if (
      kind == Token.Kind.ASTERISK ||
      kind == Token.Kind.MINUS ||
      kind == Token.Kind.PLUS ||
      kind == Token.Kind.EXCLAMATION ||
      kind == Token.Kind.TILDE
    ) {
      n = new UnaryExpression(lookahead);
      match(kind);
      n.addChild(unaryExpression());
    }
    else if (
      kind == Token.Kind.CAST ||
      kind == Token.Kind.DIVINE ||
      kind == Token.Kind.TRANSMUTE
    ) {
      n = castExpression();
    }
    else {
      n = postfixExpression();
    }
    return n;
  }

  private AstNode castExpression () {
    var n = new CastExpression(lookahead);
    match(lookahead.getKind());
    match(Token.Kind.LESS);
    n.addChild(type(true));
    match(Token.Kind.GREATER);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(expression(true));
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // The postfix expression grammar below permits semantically invalid results.
  // For example, primary expressions can include integer literals, so the
  // parser will successfully parse '5++', even though that isn't semantically
  // sound because integer literals are not l-values. This issue is addressed
  // during a semantic analysis pass (which is how it seems to be dealt with in
  // C++ and some other languages). Although we might be able to adjust the
  // grammar to avoid this problem, the for now we will just stick with the
  // traditional design.

  private AstNode postfixExpression () {
    var node = primaryExpression();
    while (
      lookahead.getKind() == Token.Kind.L_BRACKET ||
      lookahead.getKind() == Token.Kind.MINUS_GREATER ||
      lookahead.getKind() == Token.Kind.PERIOD ||
      lookahead.getKind() == Token.Kind.L_PARENTHESIS
    ) {
      switch (lookahead.getKind()) {
        case Token.Kind.L_BRACKET ->
          node = arraySubscript(node);
        case Token.Kind.MINUS_GREATER ->
          node = dereferencingMemberAccess(node);
        case Token.Kind.PERIOD ->
          node = memberAccess(node);
        case Token.Kind.L_PARENTHESIS ->
          node = routineCall(node);
        default ->
          System.out.println("Error: No viable alternative in postfixExpression");
      }
    }
    return node;
  }

  private AstNode arraySubscript (AstNode nameExpr) {
    var node = new ArraySubscript(lookahead);
    match(Token.Kind.L_BRACKET);
    node.addChild(nameExpr);
    node.addChild(expression(false));
    match(Token.Kind.R_BRACKET);
    return node;
  }

  private AstNode dereferencingMemberAccess (AstNode nameExpr) {
    var node = new DereferencingMemberAccess(lookahead);
    match(Token.Kind.MINUS_GREATER);
    node.addChild(nameExpr);
    node.addChild(name());
    return node;
  }

  // To do: We might need to use the symbol table to determine if the member is
  // an object, package, or type. This is to support static members and implicit
  // namespacing of packages.

  private AstNode memberAccess (AstNode nameExpr) {
    var node = new MemberAccess(lookahead);
    match(Token.Kind.PERIOD);
    node.addChild(nameExpr);
    node.addChild(name());
    return node;
  }

  // Subroutines (or 'routines' for short) may be classified as 'functions',
  // which return a result; or 'procedures', which do not. Furthermore, routines
  // that are members of a class are known as 'methods'. However, we do not
  // distinguish between all these types of routines using different keywords.

  private AstNode routineCall (AstNode nameExpr) {
    var node = new RoutineCall(lookahead);
    node.addChild(nameExpr);
    node.addChild(routineArguments());
    return node;
  }

  private AstNode routineArguments () {
    var node = new RoutineArguments(lookahead);
    match(Token.Kind.L_PARENTHESIS);
    if (lookahead.getKind() != Token.Kind.R_PARENTHESIS) {
      node.addChild(routineArgument());
      while (lookahead.getKind() == Token.Kind.COMMA) {
        match(Token.Kind.COMMA);
        node.addChild(routineArgument());
      }
    }
    match(Token.Kind.R_PARENTHESIS);
    return node;
  }

  // This could just be a passthrough, but we create a dedicated node to be
  // consistent with other parts of the parser (e.g. template argument).

  private AstNode routineArgument () {
    var n = new RoutineArgument();
    n.addChild(expression(false));
    return n;
  }

  // To do: Putting new expression here for now. Research correct location.

  private AstNode primaryExpression () {
    AstNode n = null;
    var kind = lookahead.getKind();
    if (
      kind == Token.Kind.FALSE ||
      kind == Token.Kind.TRUE ||
      kind == Token.Kind.CHARACTER_LITERAL ||
      kind == Token.Kind.FLOAT32_LITERAL ||
      kind == Token.Kind.FLOAT64_LITERAL ||
      kind == Token.Kind.INT32_LITERAL ||
      kind == Token.Kind.INT64_LITERAL ||
      kind == Token.Kind.NULL ||
      kind == Token.Kind.STRING_LITERAL ||
      kind == Token.Kind.UINT32_LITERAL ||
      kind == Token.Kind.UINT64_LITERAL
    ) {
      n = literal();
    } else if (lookahead.getKind() == Token.Kind.THIS)
      n = this_();
    else if (lookahead.getKind() == Token.Kind.IDENTIFIER) {
      // Test this -- is this not working?
      n = name();
    }
    else if (lookahead.getKind() == Token.Kind.DELETE) {
      n = deleteExpression();
    }
    else if (lookahead.getKind() == Token.Kind.NEW) {
      n = newExpression();
    }
    // Defer implementing if expressions
//    else if (lookahead.getKind() == Token.Kind.IF)
//      n = ifExpression();
    else if (lookahead.getKind() == Token.Kind.L_PARENTHESIS)
      n = parenthesizedExpression();
    else
      System.out.println("ERROR - INVALID PRIMARY EXPRESSION");
    return n;
  }

  private AstNode literal () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.FALSE || lookahead.getKind() == Token.Kind.TRUE)
      n = booleanLiteral();
    else if (lookahead.getKind() == Token.Kind.CHARACTER_LITERAL)
      n = characterLiteral();
    else if (lookahead.getKind() == Token.Kind.FLOAT32_LITERAL || lookahead.getKind() == Token.Kind.FLOAT64_LITERAL)
      n = floatingPointLiteral();
    else if (lookahead.getKind() == Token.Kind.INT32_LITERAL || lookahead.getKind() == Token.Kind.INT64_LITERAL)
      n = integerLiteral();
    else if (lookahead.getKind() == Token.Kind.NULL)
      n = nullLiteral();
    else if (lookahead.getKind() == Token.Kind.STRING_LITERAL)
      n = stringLiteral();
    else if (lookahead.getKind() == Token.Kind.UINT32_LITERAL || lookahead.getKind() == Token.Kind.UINT64_LITERAL)
      n = unsignedIntegerLiteral();
    return n;
  }

  private AstNode booleanLiteral () {
    var n = new BooleanLiteral(lookahead);
    consume();
    return n;
  }

  private AstNode characterLiteral () {
    var n = new CharacterLiteral(lookahead);
    consume();
    return n;
  }

  private AstNode floatingPointLiteral () {
    var n = new FloatingPointLiteral(lookahead);
    consume();
    return n;
  }

  private AstNode integerLiteral () {
    var n = new IntegerLiteral(lookahead);
    consume();
    return n;
  }

  private AstNode nullLiteral () {
    var n = new NullLiteral(lookahead);
    consume();
    return n;
  }

  private AstNode stringLiteral () {
    var n = new StringLiteral(lookahead);
    consume();
    return n;
  }

  private AstNode unsignedIntegerLiteral () {
    var n = new UnsignedIntegerLiteral(lookahead);
    consume();
    return n;
  }

  // Note: In C++, 'this' is a pointer, but in cppfront, it is not. Its unclear
  // if we can achieve the same thing in cobalt. For now, just assume it is a
  // pointer.

  private AstNode this_ () {
    var n = new This(lookahead);
    match(Token.Kind.THIS);
    return n;
  }

  private AstNode parenthesizedExpression () {
    match(Token.Kind.L_PARENTHESIS);
    var n = expression(false);
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // To do: Finish delete and new expressions

  private AstNode deleteExpression () {
    var n = new NewExpression(lookahead);
    match(Token.Kind.NEW);
    return n;
  }

  private AstNode newExpression () {
    var n = new NewExpression(lookahead);
    match(Token.Kind.NEW);
    return n;
  }

  private AstNode name () {
    var n = new Name(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  // TYPES **************************************************

  // Type processing is interesting because Cobalt uses a form of the
  // C-declaration style, so parsing types requires following the "spiral rule".
  // To make this easier, we make use of stack and queue types provided by the
  // language rather than complicating AST node class definition with parent
  // links. We can re-think this in the future if we wish.

  // Will we ever use this type() function?

  private Type type () {
    return type(false);
  }

  private Type type (boolean root) {
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
    n.setRoot(root);
    // If program crashes, check here, this is just for testing.
    System.out.println("***");
    System.out.println(n);
    System.out.println("***");
    return n;
  }

  private void directType () {
    var left   = leftFragment();
    var center = centerFragment();
    var right  = rightFragment();
    // Move type fragments to parsing stack in "spiral rule" order
    while (!right.isEmpty())
      stack.push(right.poll());
    while (!left.isEmpty())
      stack.push(left.pop());
    stack.push(center);
  }

  private LinkedList<Type> leftFragment () {
    var fragment = new LinkedList<Type>();
    while (lookahead.getKind() == Token.Kind.ASTERISK)
      fragment.push(pointerType());
    return fragment;
  }

  private LinkedList<Type> rightFragment () {
    var fragment = new LinkedList<Type>();
    while (lookahead.getKind() == Token.Kind.L_BRACKET)
      fragment.offer(arrayType());
    return fragment;
  }

  private Type centerFragment () {
    Type fragment = null;
    var kind = lookahead.getKind();
    if (kind == Token.Kind.CARET) {
      fragment = routinePointerType();
    }
    else if (
      kind == Token.Kind.BOOL    ||
      kind == Token.Kind.INT     ||
      kind == Token.Kind.INT8    ||
      kind == Token.Kind.INT16   ||
      kind == Token.Kind.INT32   ||
      kind == Token.Kind.INT64   ||
      kind == Token.Kind.UINT    ||
      kind == Token.Kind.UINT8   ||
      kind == Token.Kind.UINT16  ||
      kind == Token.Kind.UINT32  ||
      kind == Token.Kind.UINT64  ||
      kind == Token.Kind.FLOAT   ||
      kind == Token.Kind.DOUBLE  ||
      kind == Token.Kind.FLOAT32 ||
      kind == Token.Kind.FLOAT64 ||
      kind == Token.Kind.VOID
    ) {
      fragment = primitiveType();
    }
    else if (lookahead.getKind() == Token.Kind.IDENTIFIER) {
      // Update: If using angle brackets (e.g. "<>") then I don't know if we still need to consult the symbol table.

      // Need to look up name in symbol table to tell what kind it is (e.g. class, template). If it is defined as a
      // class, then a left bracket following indicates an array of that class type. If it is not defined at all, then
      // assume it is a class and treat it as such. If it is defined as a class template, then a left bracket following
      // denotes class template parameters.

      // Todo: Hard-coded "Token here". This needs to be fixed.
      // COMMENTED WHEN DOING SCOPES - NEEDS FIX
      // currentScope.define(Symbol(Symbol.Kind.CLASS_TEMPLATE, "Token"))
      var symbol = currentScope.resolve(lookahead.getLexeme(), true);
      // COMMENTED WHEN DOING SCOPES - NEEDS FIX
      // if symbol == null then
      //   // Nominal types include classes and enums. They do NOT include
      //   // primitive types or template types.
      //   fragment = nominalType()
      // else
      //   if symbol.getKind() == Symbol.Kind.CLASS_TEMPLATE then
      //     fragment = templateType()
      //   else
      //     fragment = nominalType()
      fragment = nominalType();
    }
    else if (lookahead.getKind() == Token.Kind.L_PARENTHESIS) {
      match(Token.Kind.L_PARENTHESIS);
      directType();
      fragment = stack.pop();
      match(Token.Kind.R_PARENTHESIS);
    }
    return fragment;
  }

  private Type arrayType () {
    var n = new ArrayType(lookahead);
    match(Token.Kind.L_BRACKET);
    if (lookahead.getKind() != Token.Kind.R_BRACKET) {
      n.addChild(expression(true));
    }
    match(Token.Kind.R_BRACKET);
    return n;
  }

  private Type nominalType () {
    Type n = new NominalType(lookahead);
    match(Token.Kind.IDENTIFIER);
    if (lookahead.getKind() == Token.Kind.LESS)
      n = templateInstantiation(n);
    return n;
  }

  private Type routinePointerType () {
    var n = new RoutinePointerType(lookahead);
    match(Token.Kind.CARET);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(type());
    while (lookahead.getKind() == Token.Kind.COMMA) {
      match(Token.Kind.COMMA);
      n.addChild(type());
    }
    match(Token.Kind.R_PARENTHESIS);
    match(Token.Kind.MINUS_GREATER);
    n.addChild(type());
    System.out.println("TYPE IS STILL " + n.getToken());
    return n;
  }

  // Should the template instantiation token be the opening angle bracket? That will also be used by the template
  // arguments node.

  private Type templateInstantiation (Type nomType) {
    var n = new TemplateInstantiation(lookahead);
    n.addChild(nomType);
    n.addChild(templateArguments());
    return n;
  }

  private AstNode templateArguments () {
    var n = new TemplateArguments(lookahead);
    match(Token.Kind.LESS);
    n.addChild(templateArgument());
    while (lookahead.getKind() == Token.Kind.COMMA) {
      match(Token.Kind.COMMA);
      n.addChild(templateArgument());
    }
    match(Token.Kind.GREATER);
    return n;
  }

  // A template argument may either be a type or an expression, which creates a
  // parsing problem for LL(k) grammar. How do we know whether we should parse
  // X in Some<X> as a type or an expression? The answer is that we look up the
  // template in the symbol table. The template definition tells us the kind of
  // each type parameter, i.e. whether it is a type or expression. The parser
  // proceeds based on this information.

  // For now, just assume it is a type.

  // Should template argument have token? It will wind up being the same token
  // used by its content.

  // I think we need to keep track of whether it is a root node here

  private AstNode templateArgument () {
    var n = new TemplateArgument(lookahead);
    n.addChild(type());
    return n;
  }

  private Type pointerType () {
    var n = new PointerType(lookahead);
    match(Token.Kind.ASTERISK);
    return n;
  }

  private Type primitiveType () {
    var n = new PrimitiveType(lookahead);
    match(lookahead.getKind());
    return n;
  }

}
