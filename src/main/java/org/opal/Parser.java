package org.opal;

//import java.util.Deque;
import java.util.LinkedList;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.statement.*;

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

  // To do: Implement use declaration

  private AstNode declaration () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.IMPORT)
      n = importDeclaration();
    else {
      var spec = accessSpecifier();
      if (lookahead.getKind() == Token.Kind.TEMPLATE)
        ; //n = templateDeclaration();
      else {
        var mods = modifiers();
        switch (lookahead.getKind()) {
          case Token.Kind.CLASS -> {
            System.out.println("CLASS TBD");
            n = null;
          }
          case Token.Kind.DEF ->
            n = routineDeclaration(spec, mods);
          case Token.Kind.VAL ->
            n = variableDeclaration(spec, mods);
          case Token.Kind.VAR ->
            n = variableDeclaration(spec, mods);
          default ->
            n = null;
        }
      }
    }
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

  // ACCESS SPECIFIERS AND MODIFIERS

  // Callers can explicitly request an empty access specifier. This is useful
  // for template declarations, where the access specifier is on the template
  // declaration itself rather than the templated entity. In this case, the
  // templated entity's AST node will still have an access specifier node, but
  // it will not have any children. This will be interpreted later to mean that
  // there is no access specifier. I prefer to handle it this way instead of not
  // having an access specifier node at all because it avoids having to create
  // specialized grammar rules for each case. However, I may change this later
  // if the alternative proves better.

  // Update: Just use the token as the discriminator. A missing token means that
  // the access specifier was not specified. Also might need to add protected as
  // another option.

  // Note: Parameter is false by default in scala. But the parameter doesn't seem
  // to be used.

  private AstNode accessSpecifier () {
    var n = new AccessSpecifier();
    if (lookahead.getKind() == Token.Kind.PRIVATE || lookahead.getKind() == Token.Kind.PUBLIC) {
      n.setToken(lookahead);
      match(lookahead.getKind());
    }
    return n;
  }

  // According to Parr, there is no need to have an AstNode kind -- you can just
  // use the token to determine what kind of node it is. This works only for
  // Simple cases. Sometimes, there is no corresponding token. So for that
  // reason, we choose to have a AstNode kind field. That said, this means that
  // sometimes we can leave the kind field generic and distinguish with more
  // granularity by looking at the token.

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

  private AstNode modifiers () {
    var n = new Modifiers();
    while (
      lookahead.getKind() == Token.Kind.ABSTRACT  ||
      lookahead.getKind() == Token.Kind.CONST     ||
      lookahead.getKind() == Token.Kind.CONSTEXPR ||
      lookahead.getKind() == Token.Kind.FINAL     ||
      lookahead.getKind() == Token.Kind.OVERRIDE  ||
      lookahead.getKind() == Token.Kind.STATIC    ||
      lookahead.getKind() == Token.Kind.VIRTUAL   ||
      lookahead.getKind() == Token.Kind.VOLATILE
    ) {
      var p = new Modifier(lookahead);
      match(lookahead.getKind());
      n.addChild(p);
      try {
        System.out.println("Sleeping for " + SLEEP_TIME + " seconds in modifiers...");
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return n;
  }

  // ROUTINE DECLARATIONS

  // Todo: We need to push another scope onto the scope stack. Keep in mind that
  // the routine parameters may be in the same exact scope as the routine body
  // (or top-most block of the routine).

  private AstNode routineDeclaration (AstNode accessSpecifier, AstNode modifiers) {
    var n = new RoutineDeclaration(lookahead);
//    var scope = Scope(Scope.Kind.LOCAL);
//    scope.setEnclosingScope(currentScope);
//    currentScope = scope;
//    n.setScope(currentScope);
    match(Token.Kind.DEF);
    n.addChild(accessSpecifier);
    n.addChild(modifiers);
    n.addChild(routineName());
    n.addChild(routineParameters());
    n.addChild(routineReturnType());
    n.addChild(routineBody());
//    currentScope = scope.getEnclosingScope();
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
    match(Token.Kind.COLON);
    n.addChild(typeRoot());
    return n;
  }

  private AstNode routineParameterName () {
    var n = new RoutineParameterName(lookahead);
    match(Token.Kind.IDENTIFIER);
    //var s = RoutineParameterSymbol(n.getToken().lexeme);
    //currentScope.define(s);
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
      n.addChild(typeRoot());
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

  // To do: Local variables shouldn't have access specifiers. Because children can be accessed by name, we'll probably
  // need a separate local variable node type. For now, just handle global variables.

  private AstNode variableDeclaration (AstNode accessSpecifier, AstNode modifiers) {
    var n = new VariableDeclaration(lookahead);
    match(Token.Kind.VAR);
    n.addChild(accessSpecifier);
    n.addChild(modifiers);
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
      n.addChild(expression(true));
    }
    return n;
  }

  // STATEMENTS

  // Notice that we don't include 'if' in the first set for expression
  // statements. This is because we want send the parser towards the statement
  // version of 'if'.

  private AstNode statement () {
    AstNode n = null;
    Token.Kind kind = lookahead.getKind();
    if (
      kind == Token.Kind.BREAK ||
      kind == Token.Kind.L_BRACE ||
      kind == Token.Kind.CONTINUE
    ) {
      n = standardStatement();
    } else if (
      kind == Token.Kind.CLASS ||
      kind == Token.Kind.DEF ||
      kind == Token.Kind.VAR
    ) {
      n = declarationStatement();
    } else if (
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
      n = expressionStatement();
    } else {
      System.out.println("Error: invalid statement");
    }
    return n;
  }

  // To do: Implement for and foreach statements

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
      case Token.Kind.IF ->
        n = ifStatement();
      case Token.Kind.RETURN ->
        n = returnStatement();
      case Token.Kind.SEMICOLON ->
        n = emptyStatement();
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
    return n;
  }

  // The do statement is flexible and can either be a "do while" or a "do until"
  // statement, depending on what follows the 'do' keyword.

  private AstNode doStatement () {
    var n = new DoStatement(lookahead);
    match(Token.Kind.DO);
    if (lookahead.getKind() == Token.Kind.UNTIL)
      n.addChild(untilStatement());
    else if (lookahead.getKind() == Token.Kind.WHILE)
      n.addChild(whileStatement());
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

  private AstNode ifStatement () {
    var n = new IfStatement(lookahead);
    match(Token.Kind.IF);
    n.addChild(ifCondition());
    n.addChild(ifBody());
    if (lookahead.getKind() == Token.Kind.ELSE)
      n.addChild(elseClause());
    return n;
  }

  private AstNode ifCondition () {
    match(Token.Kind.L_PARENTHESIS);
    var n = expression(true);
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  private AstNode ifBody () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.L_BRACE)
      n = compoundStatement();
    else {
      // Insert fabricated compound statement
      n = new CompoundStatement(null);
      n.addChild(statement());
    }
    return n;
  }

  private AstNode elseClause () {
    var n = new ElseClause(lookahead);
    match(Token.Kind.ELSE);
    n.addChild(elseBody());
    return n;
  }

  private AstNode elseBody () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.L_BRACE)
      n = compoundStatement();
    else {
      // Insert fabricated compound statement
      n = new CompoundStatement(null);
      n.addChild(statement());
    }
    return n;
  }

  private AstNode returnStatement () {
    var n = new ReturnStatement(lookahead);
    match(Token.Kind.RETURN);
    // Should we explicitly check FIRST, or is it ok to just check FOLLOW?
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

  private AstNode untilStatement () {
    var n = new UntilStatement(lookahead);
    match(Token.Kind.UNTIL);
    n.addChild(untilCondition());
    n.addChild(untilBody());
    return n;
  }

  // In C++26 the condition can be an expression or a declaration. For now, we
  // will only support expressions, and use the rule as a passthrough.

  // We can handle transformation to a 'while' statement here by inserting an
  // AST node that complements the expression. However, the parser should not
  // concern itself with the details of the target language. The lack of an
  // 'until' statement is a concern of the target language, so we will leave it
  // up to a separate transformation or generation phase to make that change.

  private AstNode untilCondition () {
    match(Token.Kind.L_PARENTHESIS);
    var n = expression(true);
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // Notice that fabricated AST nodes do not have tokens. This could be a
  // source of bugs, so we need to be careful.

  private AstNode untilBody () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.L_BRACE)
      n = statement();
    else {
      // Insert fabricated compound statement
      n = new CompoundStatement(null);
      n.addChild(statement());
    }
    return n;
  }

  private AstNode whileStatement () {
    var n = new UntilStatement(lookahead);
    match(Token.Kind.UNTIL);
    n.addChild(whileCondition());
    n.addChild(whileBody());
    return n;
  }

  // In C++26 the condition can be an expression or a declaration. For now, we
  // will only support expressions, and use the rule as a passthrough.

  private AstNode whileCondition () {
    match(Token.Kind.L_PARENTHESIS);
    var n = expression(true);
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // Notice that fabricated AST nodes do not have tokens. This could be a
  // source of bugs, so we need to be careful.

  private AstNode whileBody () {
    AstNode n = null;
    if (lookahead.getKind() == Token.Kind.L_BRACE)
      n = statement();
    else {
      // Insert fabricated compound statement
      n = new CompoundStatement(null);
      n.addChild(statement());
    }
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
    var mods = modifiers();
    var kind = lookahead.getKind();
    if (kind == Token.Kind.VAL || kind == Token.Kind.VAR)
      n = variableDeclaration(null, mods);
    // To do: Need error checking here
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

  // EXPRESSIONS

  // The root of every expression sub-tree has an explicit 'expression' AST
  // node, where the final computed type and other synthesized attributes can be
  // stored to aid in such things as type-checking.

  private AstNode expression (boolean root) {
    if (root) {
      var n = new Expression();
      n.addChild(assignmentExpression());
      return n;
    }
    else
      return assignmentExpression();
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
    if (
        lookahead.getKind() == Token.Kind.ASTERISK ||
        lookahead.getKind() == Token.Kind.MINUS ||
        lookahead.getKind() == Token.Kind.PLUS ||
        lookahead.getKind() == Token.Kind.EXCLAMATION ||
        lookahead.getKind() == Token.Kind.TILDE
    ) {
      n = new BinaryExpression(lookahead);
      match(lookahead.getKind());
      n.addChild(unaryExpression());
    }
    else
      n = postfixExpression();
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
        lookahead.getKind() == Token.Kind.MINUS_GREATER ||
        lookahead.getKind() == Token.Kind.PERIOD ||
        lookahead.getKind() == Token.Kind.L_PARENTHESIS ||
        lookahead.getKind() == Token.Kind.L_BRACKET
    ) {
      switch (lookahead.getKind()) {
        case Token.Kind.MINUS_GREATER:
          node = dereferencingMemberAccess(node);
        case Token.Kind.PERIOD:
          node = memberAccess(node);
        case Token.Kind.L_PARENTHESIS:
          node = routineCall(node);
        case Token.Kind.L_BRACKET:
          node = arraySubscript(node);
        default:
          System.out.println("Error: No viable alternative in postfixExpression");
      }
    }
    return node;
  }

  private AstNode dereferencingMemberAccess (AstNode nameExpr) {
    var n = new DereferencingMemberAccess(lookahead);
    n.addChild(nameExpr);
    match(Token.Kind.MINUS_GREATER);
    n.addChild(name());
    return n;
  }

  private AstNode memberAccess (AstNode nameExpr) {
    var n = new MemberAccess(lookahead);
    n.addChild(nameExpr);
    match(Token.Kind.PERIOD);
    n.addChild(name());
    return n;
  }

  // Subroutines (or 'routines' for short) may be classified as 'functions',
  // which return a result; or 'procedures', which do not. Furthermore, routines
  // that are members of a class are known as 'methods'. However, we do not
  // distinguish between all these types of routines using different keywords.

  private AstNode routineCall (AstNode nameExpr) {
    var n = new RoutineCall(lookahead);
    n.addChild(nameExpr);
    n.addChild(arguments());
    return n;
  }

  // Todo: Maybe change to routineArguments and add routineArgument

  private AstNode arguments () {
    var n = new Arguments();
    match(Token.Kind.L_PARENTHESIS);
    if (lookahead.getKind() != Token.Kind.R_PARENTHESIS) {
      n.addChild(expression(false));
      while (lookahead.getKind() == Token.Kind.COMMA) {
        match(Token.Kind.COMMA);
        n.addChild(expression(false));
      }
    }
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  private AstNode arraySubscript (AstNode nameExpr) {
    var n = new ArraySubscript();
    n.addChild(nameExpr);
    match(Token.Kind.L_BRACKET);
    n.addChild(expression(false));
    match(Token.Kind.R_BRACKET);
    return n;
  }

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
    else if (lookahead.getKind() == Token.Kind.IDENTIFIER)
      n = name();
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

  // Placeholder
  private AstNode name () {
    return null;
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
