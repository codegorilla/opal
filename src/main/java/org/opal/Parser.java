package org.opal;

import java.util.*;
import java.util.stream.Collectors;

import com.sun.source.tree.ForLoopTree;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.opal.ast.*;
import org.opal.ast.ErrorNode;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

import org.opal.ast.type.ArrayDeclarators;
import org.opal.error.SyntaxError;
import org.opal.symbol.Scope;
import org.opal.symbol.PrimitiveTypeSymbol;

// To do: Implement debug logging

// We wish to implement some form of panic-mode and/or phrase-level
// error recovery discussed in the following references:
// Aho, A., 2007, Sec. 4.4.5
// Louden, K., 1997, Sec. 4.5
// Parr, T., 2012, Sec. 9.3
// Scott, M., 2025, Companion site, Ch. 2
// Topor, R., 1982
// Wirth, N., 1976, Sec. 5.9

public class Parser {

  private final int SLEEP_TIME = 100;

  private final LinkedList<Token> input;
  private final Counter position;

  private Token lookahead;
  private Token.Kind kind;

  // Experimental
  private Token previous;
  private Token mark;

  private final List<String> sourceLines;

  // Used to pass type nodes up and down during tree traversal
  //private final LinkedList<DirectDeclarator> stack;

  // Used to pass nodes up and down during tree traversal
  private final LinkedList<AstNode> nodeStack;

  // Used to collect modifier nodes in preparation for aggregation into
  // specialized modifiers nodes.
  private final LinkedList<AstNode> modifierStack;

  // Used for symbol table operations. Cobalt requires a symbol table during
  // parsing in order to disambiguate a few grammar rules. We cannot wait until
  // the semantic analysis phase to begin constructing symbol tables.
  private final Scope builtinScope;
  private Scope currentScope;

  // Reverse keyword mapping from token-kind to string
  private final HashMap<Token.Kind, String> keywordLookup;

  // Note: Leave this disabled for now so the error recovery code can be built
  // with the most raw output to make it easier to understand behavior and
  // troubleshoot.

  // Tracks whether error recovery mode is enabled to avoid cascading error
  // messages. See [Par12] Sec. 9.3 for details.
  boolean errorRecoveryMode = false;

  // Todo: we may also need a 'null_t' type, for which there is exactly one
  // value, which is 'null'. This is to match the C++ 'nullptr_t' type and its
  // corresponding single 'nullptr' value. I am not sure if this is a primitive
  // type or not. Needs research.

  // Todo: We may decide that 'int', 'short', 'float', etc. should just be
  // typealiases for the various fixed size types.

  private static final Logger LOGGER = LogManager.getLogger();

  // Represents epsilon productions
  private static final AstNode EPSILON = null;

  // Following sets are like FOLLOW sets, but are context aware, so they are
  // generally subsets of FOLLOW sets.

  // Stack for follower sets
  private final LinkedList<EnumSet<Token.Kind>> followerSetStack;

  // Deprecated
  // Stack for sync sets
  //private final LinkedList<EnumSet<Token.Kind>> syncSetStack;

  // Convenience aliases
  private static final Token.Kind ABSTRACT = Token.Kind.ABSTRACT;
  private static final Token.Kind AND = Token.Kind.AND;
  private static final Token.Kind AS = Token.Kind.AS;
  private static final Token.Kind BREAK = Token.Kind.BREAK;
  private static final Token.Kind CASE = Token.Kind.CASE;
  private static final Token.Kind CAST = Token.Kind.CAST;
  private static final Token.Kind CATCH = Token.Kind.CATCH;
  private static final Token.Kind CLASS = Token.Kind.CLASS;
  private static final Token.Kind CONST = Token.Kind.CONST;
  private static final Token.Kind CONSTEVAL = Token.Kind.CONSTEVAL;
  private static final Token.Kind CONSTEXPR = Token.Kind.CONSTEXPR;
  private static final Token.Kind CONTINUE = Token.Kind.CONTINUE;
  private static final Token.Kind DEF = Token.Kind.DEF;
  private static final Token.Kind DEFAULT = Token.Kind.DEFAULT;
  private static final Token.Kind DELETE = Token.Kind.DELETE;
  private static final Token.Kind DIVINE = Token.Kind.DIVINE;
  private static final Token.Kind DO = Token.Kind.DO;
  private static final Token.Kind ELSE = Token.Kind.ELSE;
  private static final Token.Kind ENUM = Token.Kind.ENUM;
  private static final Token.Kind EXTENDS = Token.Kind.EXTENDS;
  private static final Token.Kind FALSE = Token.Kind.FALSE;
  private static final Token.Kind FINAL = Token.Kind.FINAL;
  private static final Token.Kind FOR = Token.Kind.FOR;
  private static final Token.Kind FN = Token.Kind.FN;
  private static final Token.Kind FUN = Token.Kind.FUN;
  private static final Token.Kind GOTO = Token.Kind.GOTO;
  private static final Token.Kind IF = Token.Kind.IF;
  private static final Token.Kind IMPORT = Token.Kind.IMPORT;
  private static final Token.Kind IN = Token.Kind.IN;
  private static final Token.Kind INCLUDE = Token.Kind.INCLUDE;
  private static final Token.Kind LOOP = Token.Kind.LOOP;
  private static final Token.Kind NEW = Token.Kind.NEW;
  private static final Token.Kind NIL = Token.Kind.NIL;
  private static final Token.Kind NOEXCEPT = Token.Kind.NOEXCEPT;
  private static final Token.Kind NULL = Token.Kind.NULL;
  private static final Token.Kind OR = Token.Kind.OR;
  private static final Token.Kind OVERRIDE = Token.Kind.OVERRIDE;
  private static final Token.Kind PACKAGE = Token.Kind.PACKAGE;
  private static final Token.Kind PRIVATE = Token.Kind.PRIVATE;
  private static final Token.Kind PROTECTED = Token.Kind.PROTECTED;
  private static final Token.Kind RETURN = Token.Kind.RETURN;
  private static final Token.Kind STATIC = Token.Kind.STATIC;
  private static final Token.Kind STRUCT = Token.Kind.STRUCT;
  private static final Token.Kind SWITCH = Token.Kind.SWITCH;
  private static final Token.Kind TEMPLATE = Token.Kind.TEMPLATE;
  private static final Token.Kind THIS = Token.Kind.THIS;
  private static final Token.Kind TRAIT = Token.Kind.TRAIT;
  private static final Token.Kind TRANSMUTE = Token.Kind.TRANSMUTE;
  private static final Token.Kind TRUE = Token.Kind.TRUE;
  private static final Token.Kind TRY = Token.Kind.TRY;
  private static final Token.Kind TYPEALIAS = Token.Kind.TYPEALIAS;
  private static final Token.Kind UNION = Token.Kind.UNION;
  private static final Token.Kind UNTIL = Token.Kind.UNTIL;
  private static final Token.Kind USE = Token.Kind.USE;
  private static final Token.Kind VAL = Token.Kind.VAL;
  private static final Token.Kind VAR = Token.Kind.VAR;
  private static final Token.Kind VIRTUAL = Token.Kind.VIRTUAL;
  private static final Token.Kind VOLATILE = Token.Kind.VOLATILE;
  private static final Token.Kind WHEN = Token.Kind.WHEN;
  private static final Token.Kind WHILE = Token.Kind.WHILE;
  private static final Token.Kind WITH = Token.Kind.WITH;
  private static final Token.Kind AMPERSAND = Token.Kind.AMPERSAND;
  private static final Token.Kind AMPERSAND_AMPERSAND = Token.Kind.AMPERSAND_AMPERSAND;
  private static final Token.Kind AMPERSAND_EQUAL = Token.Kind.AMPERSAND_EQUAL;
  private static final Token.Kind ASTERISK = Token.Kind.ASTERISK;
  private static final Token.Kind ASTERISK_EQUAL = Token.Kind.ASTERISK_EQUAL;
  private static final Token.Kind BAR = Token.Kind.BAR;
  private static final Token.Kind BAR_BAR = Token.Kind.BAR_BAR;
  private static final Token.Kind BAR_EQUAL = Token.Kind.BAR_EQUAL;
  private static final Token.Kind CARET = Token.Kind.CARET;
  private static final Token.Kind CARET_EQUAL = Token.Kind.CARET_EQUAL;
  private static final Token.Kind COLON = Token.Kind.COLON;
  private static final Token.Kind COMMA = Token.Kind.COMMA;
  private static final Token.Kind EQUAL = Token.Kind.EQUAL;
  private static final Token.Kind EQUAL_EQUAL = Token.Kind.EQUAL_EQUAL;
  private static final Token.Kind EXCLAMATION = Token.Kind.EXCLAMATION;
  private static final Token.Kind EXCLAMATION_EQUAL = Token.Kind.EXCLAMATION_EQUAL;
  private static final Token.Kind EXCLAMATION_LESS = Token.Kind.EXCLAMATION_LESS;
  private static final Token.Kind GREATER = Token.Kind.GREATER;
  private static final Token.Kind GREATER_EQUAL = Token.Kind.GREATER_EQUAL;
  private static final Token.Kind GREATER_GREATER = Token.Kind.GREATER_GREATER;
  private static final Token.Kind GREATER_GREATER_EQUAL = Token.Kind.GREATER_GREATER_EQUAL;
  private static final Token.Kind L_BRACE = Token.Kind.L_BRACE;
  private static final Token.Kind L_BRACKET = Token.Kind.L_BRACKET;
  private static final Token.Kind L_PARENTHESIS = Token.Kind.L_PARENTHESIS;
  private static final Token.Kind LESS = Token.Kind.LESS;
  private static final Token.Kind LESS_EQUAL = Token.Kind.LESS_EQUAL;
  private static final Token.Kind LESS_LESS = Token.Kind.LESS_LESS;
  private static final Token.Kind LESS_LESS_EQUAL = Token.Kind.LESS_LESS_EQUAL;
  private static final Token.Kind MINUS = Token.Kind.MINUS;
  private static final Token.Kind MINUS_EQUAL = Token.Kind.MINUS_EQUAL;
  private static final Token.Kind MINUS_GREATER = Token.Kind.MINUS_GREATER;
  private static final Token.Kind PERCENT = Token.Kind.PERCENT;
  private static final Token.Kind PERCENT_EQUAL = Token.Kind.PERCENT_EQUAL;
  private static final Token.Kind PERIOD = Token.Kind.PERIOD;
  private static final Token.Kind PERIOD_PERIOD = Token.Kind.PERIOD_PERIOD;
  private static final Token.Kind PLUS = Token.Kind.PLUS;
  private static final Token.Kind PLUS_EQUAL = Token.Kind.PLUS_EQUAL;
  private static final Token.Kind R_BRACE = Token.Kind.R_BRACE;
  private static final Token.Kind R_BRACKET = Token.Kind.R_BRACKET;
  private static final Token.Kind R_PARENTHESIS = Token.Kind.R_PARENTHESIS;
  private static final Token.Kind SEMICOLON = Token.Kind.SEMICOLON;
  private static final Token.Kind SLASH = Token.Kind.SLASH;
  private static final Token.Kind SLASH_EQUAL = Token.Kind.SLASH_EQUAL;
  private static final Token.Kind TILDE = Token.Kind.TILDE;
  private static final Token.Kind TILDE_EQUAL = Token.Kind.TILDE_EQUAL;

  private static final Token.Kind CHARACTER_LITERAL = Token.Kind.CHARACTER_LITERAL;
  private static final Token.Kind FLOAT32_LITERAL = Token.Kind.FLOAT32_LITERAL;
  private static final Token.Kind FLOAT64_LITERAL = Token.Kind.FLOAT64_LITERAL;
  private static final Token.Kind INT32_LITERAL = Token.Kind.INT32_LITERAL;
  private static final Token.Kind INT64_LITERAL = Token.Kind.INT64_LITERAL;
  private static final Token.Kind STRING_LITERAL = Token.Kind.STRING_LITERAL;
  private static final Token.Kind UINT32_LITERAL = Token.Kind.UINT32_LITERAL;
  private static final Token.Kind UINT64_LITERAL = Token.Kind.UINT64_LITERAL;

  public Parser (LinkedList<Token> input, List<String> sourceLines) {
    this.input = input;
    position = new Counter();
    lookahead = input.get(position.get());
    kind = lookahead.getKind();
    previous = null;
    mark = null;
    this.sourceLines = sourceLines;
    //stack = new LinkedList<>();
    nodeStack = new LinkedList<>();
    modifierStack = new LinkedList<>();
    builtinScope = new Scope(Scope.Kind.BUILT_IN);
    currentScope = builtinScope;

    followerSetStack = new LinkedList<>();
    // Deprecated
//    syncSetStack = new LinkedList<>();
//    syncSetStack.push(EnumSet.noneOf(Token.Kind.class));

    var keywordTable = new KeywordTable();
    keywordLookup = keywordTable.getReverseLookupTable();
    buildReverseKeywordLookupTable();

    // Set up logging
    var level = Level.INFO;
    Configurator.setRootLevel(level);
  }

  // Maybe replace this with a map that maps kinds back to strings
  private String friendlyKind (Token.Kind kind) {
    return kind.toString().toLowerCase().replace("_", " ");
  }

  // Note: Instead of an ERROR kind, we could just mark whatever the lookahead
  // is and then annotate the token with an error flag.

  private void sync (EnumSet<Token.Kind> syncSet) {
//    mark = new Token(Token.Kind.ERROR, lookahead.getLexeme(), lookahead.getIndex(), lookahead.getLine(), lookahead.getColumn());
//    LOGGER.info("Sync: created " + mark);
    LOGGER.info("Sync: synchronization started");
    // Combine all sync sets
    // Scan forward until we hit something in the sync set
    while (!syncSet.contains(kind)) {
      LOGGER.info("Sync: skipped {}", lookahead);
      consume();
    }
    LOGGER.info("Sync: synchronization complete");
  }

  private boolean match (Token.Kind expectedKind) {
    if (lookahead.getKind() == expectedKind) {
      // Happy path :)
      LOGGER.info("Match: matched " + lookahead);
      mark = lookahead;
      consume();
      errorRecoveryMode = false;
      return true;
    } else {
      // Sad path :(
      LOGGER.info("Match: entering sad path");
      mark = lookahead;
      if (!errorRecoveryMode)
        generalError(expectedKind);
      // Should we at least advance the input stream? If we do, then we
      // effectively delete the bad token. Different sources say yes or no,
      // but several seem to indicate that we should NOT consume.
      errorRecoveryMode = true;
      return false;
    }
  }

  // Kind of like match method, but don't consume?

  private void option (Token.Kind expectedKind, Token.Kind followingKind) {
    if (kind == expectedKind || kind == followingKind) {
      // Happy path :)
      LOGGER.info("Option: matched " + lookahead);
    } else {
      // Sad path :(
      LOGGER.info("Option: entering sad path");
      // To do: Maybe we should have our own error method for this
      if (!errorRecoveryMode)
        checkError(EnumSet.of(expectedKind, followingKind));
      // Should we at least advance the input stream? If we do, then we
      // effectively delete the bad token. Different sources say yes or no,
      // but several seem to indicate that we should NOT consume.
      errorRecoveryMode = true;
    }
  }


  private void generalError (Token.Kind expectedKind) {
    var expectedKindString = keywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "expected " + expectedKindString + ", found " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error);
  }

  // This probably only pertains when there are a finite set of multiple
  // options. Thus expected kinds will always be greater than one. But this has
  // not been proven yet.

  private void checkError (EnumSet<Token.Kind> expectedKinds) {
    var expectedKindsString = expectedKinds.stream()
      .map(kind -> keywordLookup.getOrDefault(kind, friendlyKind(kind)))
      .sorted()
      .collect(Collectors.joining(", "));
    var actualKind = kind;
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var messageSome = "expected one of " + expectedKindsString + "; found " + actualKindString;
    var messageOne  = "expected "        + expectedKindsString + ", found " + actualKindString;
    var message = expectedKinds.size() > 1 ? messageSome : messageOne;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error);
  }

  // Experiment with only two items

  private void checkError2 (EnumSet<Token.Kind> expectedKinds) {
    var expectedKindsString = expectedKinds.stream()
      .map(kind -> keywordLookup.getOrDefault(kind, friendlyKind(kind)))
      .sorted()
      .toList();
    var actualKind = kind;
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "expected " + expectedKindsString.get(0) +
                  " or "      + expectedKindsString.get(1) +
                  ", found " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error);
  }

  // Confirm is similar to match, but it does not perform any error recovery
  // and does not return a result. Instead, it throws an exception. This can
  // only fail if there is a bug in the compiler.

  private void confirm (Token.Kind expectedKind) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("Confirm: confirmed " + lookahead);
      mark = lookahead;
      consume();
      errorRecoveryMode = false;
    } else {
      var expectedKindFriendly = friendlyKind(expectedKind);
      var actualKindFriendly = friendlyKind(lookahead.getKind());
      var message = "expected " + expectedKindFriendly + ", found " + actualKindFriendly;
      throw new IllegalArgumentException("internal error: " + message);
    }
  }

  private void consume () {
    position.increment();
    if (position.get() < input.size()) {
      lookahead = input.get(position.get());
      kind = lookahead.getKind();
    }
  }

  public AstNode process () {
    buildReverseKeywordLookupTable();
    definePrimitiveTypes();
    LOGGER.info("*** Parsing started... ***");
    var node = translationUnit();
    // EOF is the only token in the follow set of translationUnit. Must match
    // it to ensure there is no garbage left over.
    match(Token.Kind.EOF);

    LOGGER.info("*** Parsing complete! ***");
    // Inspect builtin scope
//    var s = builtinScope.getSymbolTable().getData;
//    System.out.println(s);
    return node;
  }

  private void buildReverseKeywordLookupTable () {
    // Experimental, showing that this is a reverse token lookup, not
    // necessarily a reverse keyword lookup
    keywordLookup.put(Token.Kind.COLON, "':'");
    keywordLookup.put(Token.Kind.COMMA, "','");
    keywordLookup.put(Token.Kind.SEMICOLON, "';'");
    keywordLookup.put(Token.Kind.EQUAL, "'='");
    keywordLookup.put(Token.Kind.MINUS, "'-'");
    keywordLookup.put(Token.Kind.PLUS, "'+'");
    keywordLookup.put(Token.Kind.ASTERISK, "'*'");
    keywordLookup.put(Token.Kind.L_BRACE, "'{'");
    keywordLookup.put(Token.Kind.R_BRACE, "'}'");
    keywordLookup.put(Token.Kind.AS, "'as'");
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

  // TRANSLATION UNIT *********************************************************

  // In parsing theory lingo, the top-most production is known as the "start
  // symbol". Thus, the translation unit is our start symbol.

  // Maybe we need a check-in here

  private AstNode translationUnit () {
    followerSetStack.push(EnumSet.of(Token.Kind.EOF));
    var n = new TranslationUnit();
    n.addChild(packageDeclaration());

    checkIn(FirstSet.REMAINING_DECLARATIONS_1);
    if (kind == IMPORT) {
      n.addChild(importDeclarations());
    }
    else {
      n.addChild(EPSILON);
    }

    checkIn(FirstSet.REMAINING_DECLARATIONS_2);
    if (kind == USE)
      n.addChild(useDeclarations());
    else
      n.addChild(EPSILON);

    checkIn(FirstSet.REMAINING_DECLARATIONS_3);
    if (
      kind == PRIVATE ||
      kind == CLASS   ||
      kind == DEF     ||
      kind == VAL     ||
      kind == VAR
    ) {
      n.addChild(otherDeclarations());
    }

    System.out.println("HERE");

//    else if (kind == Token.Kind.EOF) {
//    } else {
//      // Assume error and that we don't want epsilon production
//      n.addChild(otherDeclarations());
//      //checkError(EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR));
//      //sync(EnumSet.of(Token.Kind.EOF)); // Why doesn't this sync to EOF?
//    }

    //var scope = new Scope(Scope.Kind.GLOBAL);
    //scope.setEnclosingScope(currentScope);
    //currentScope = scope;
    //n.setScope(currentScope);
    followerSetStack.pop();
    return n;
  }

  // DECLARATIONS *************************************************************

  // Package, import, and use declarations must appear (in that order) before
  // any other declarations in the translation unit.

  // Package declaration is normally followed by import and use declarations,
  // but it doesn't have to be. It is possible for translation unit to contain
  // no import or use declarations.

  // The package declaration is special in that there is only one per
  // translation unit, and it must appear at the very top. A package is
  // basically a direct 1:1 translation to a C++ module and namespace of the
  // same name.

  // To do: Support qualified names for packages

  // FOLLOW set is statically determined, whereas FOLLOWING set is sometimes
  // not known at compile time. There can be multiple versions of the FOLLOWING
  // set for a given production.

  private EnumSet<Token.Kind> combine () {
    var combined = EnumSet.noneOf(Token.Kind.class);
    for (var followerSet : followerSetStack)
      combined.addAll(followerSet);
    return combined;
  }

  private void recover () {
    var combined = combine();
    sync(combined);
  }

  private void checkIn (EnumSet<Token.Kind> firstSet) {
    LOGGER.info("Check-in: check-in started");
    // Might need to set mark in process() not here
    mark = lookahead;
    if (!firstSet.contains(kind)) {
      if (!errorRecoveryMode)
        checkError(firstSet);
      mark = new Token(Token.Kind.ERROR, lookahead.getLexeme(), lookahead.getIndex(), lookahead.getLine(), lookahead.getColumn());
      LOGGER.info("Check-in: created " + mark);
      // Combine first set and all follower sets
      var combined = EnumSet.copyOf(firstSet);
      for (var followerSet : followerSetStack)
        combined.addAll(followerSet);
      sync(combined);
    }
    LOGGER.info("Check-in: check-in complete");
  }

  private void checkOut () {
    LOGGER.info("Check-out: check-out started");
    if (errorRecoveryMode) {
      // Combine all follower sets
      var combined = combine();
      if (!combined.contains(kind)) {
        sync(combined);
      }
      // Not sure if we need to reset this here or if we can wait until next
      // match. Should be equivalent since check-out takes you to next match
      // or confirm.
      //errorRecoveryMode = false;
    }
    LOGGER.info("Check-out: check-out complete");
  }

  // SET_SEMICOLON is a special add-on to the FOLLOWER set stack. It allows
  // synchronization to halt at semicolons that are part of the current
  // production.
  private final EnumSet<Token.Kind> SET_SEMICOLON = EnumSet.of(SEMICOLON);
  private final EnumSet<Token.Kind> SET_R_BRACE = EnumSet.of(R_BRACE);

  private AstNode packageDeclaration () {
    followerSetStack.push(FollowerSet.PACKAGE_DECLARATION);
    // Hypothesis: Check-ins occur when the parser must choose one of several
    // paths, and the epsilon production is not one of the options. This will
    // force the parser to sync up to any item in the FIRST or FOLLOWER sets.
    checkIn(FirstSet.PACKAGE_DECLARATION);
    var n = new PackageDeclaration(mark);
    if (kind == PACKAGE) {
      confirm(PACKAGE);
      match(Token.Kind.IDENTIFIER);
      n.addChild(new PackageName(mark));
      match(SEMICOLON);
    }
    checkOut();
    if (errorRecoveryMode && kind == SEMICOLON)
      confirm(SEMICOLON);
    followerSetStack.pop();
    return n;
  }

  // No check-in is required because we only arrive at this production through
  // an explicit check for 'import' keyword. Thus, we can only get here if the
  // current lookahead token is known for sure to be 'import'.

  private AstNode importDeclarations () {
    followerSetStack.push(FollowerSet.IMPORT_DECLARATIONS);
    // No check-in required
    var n = new ImportDeclarations();
    n.addChild(importDeclaration());
    while (kind == IMPORT)
      n.addChild(importDeclaration());
    followerSetStack.pop();
    return n;
  }

  // We could implement this several ways. First, we could use a binary tree
  // with dots as internal nodes and names as leaf nodes. Second, we could
  // simply have a chain of names, with each child names being under its
  // respective parent. Lastly, we can have a list of names under the import
  // declaration. We choose to go with the latter case because that is the
  // easiest implementation and the others hold no advantages for our
  // particular use case.

  private AstNode importDeclaration () {
    followerSetStack.push(FollowerSet.IMPORT_DECLARATION);
    // No check-in required
    confirm(IMPORT);
    var n = new ImportDeclaration(mark);
    n.addChild(importQualifiedName());
    checkIn(EnumSet.of(AS, SEMICOLON));
    if (kind == AS)
      n.addChild(importAsClause());
    else if (kind == SEMICOLON) {
      // Might not need epsilon because nothing comes after this
      System.out.println("GOT HERE!");
      n.addChild(EPSILON);
    }
    match(SEMICOLON);
    checkOut();
    followerSetStack.pop();
    return n;
  }

  // This might be a candidate for error recovery of epsilon production.
  // Input "import opal-lang;" gives an error but the resulting ERROR token
  // does not get captured in the AST even though it is not able to be
  // corrected. This demonstrates that if an error occurs at all, we cannot
  // rely on it being apparent from looking at the AST. Also, the error given
  // is not a great one (due to epsilon production). It shows that there might
  // be room for improvement.

  private AstNode importQualifiedName () {
    followerSetStack.push(FollowerSet.IMPORT_QUALIFIED_NAME);
    checkIn(FirstSet.IMPORT_QUALIFIED_NAME);
    var n = new ImportQualifiedName();
    match(Token.Kind.IDENTIFIER);
    n.addChild(new ImportName(mark));
    while (kind == PERIOD) {
      confirm(PERIOD);
      match(Token.Kind.IDENTIFIER);
      n.addChild(new ImportName(mark));
    }
    checkOut();
    followerSetStack.pop();
    return n;
  }

  private AstNode importAsClause () {
    // No check-in required
    confirm(AS);
    match(Token.Kind.IDENTIFIER);
    var n = new ImportName(mark);
    return n;
  }

  private AstNode useDeclarations () {
    followerSetStack.push(FollowerSet.USE_DECLARATIONS);
    // No check-in required
    var n = new UseDeclarations();
    n.addChild(useDeclaration());
    while (kind == USE)
      n.addChild(useDeclaration());
    followerSetStack.pop();
    return n;
  }

  private AstNode useDeclaration () {
    followerSetStack.push(FollowerSet.USE_DECLARATION);
    // No check-in required
    confirm(USE);
    var n = new UseDeclaration(mark);
    n.addChild(useQualifiedName());
    match(SEMICOLON);
    checkOut();
    followerSetStack.pop();
    return n;
  }

  // Follower sets are used in two different ways, depending on whether or not
  // the token needs to be captured in an AST node or not. If so, it is passed
  // into a "terminal" production method, where it gets used in a match method
  // call. Otherwise, it is used directly in a match method call within the
  // non-terminal production method.

  private AstNode useQualifiedName () {
    AstNode n = new UseQualifiedName();
    match(Token.Kind.IDENTIFIER);
    var p = new UseName(mark);
    n.addChild(p);
    match(PERIOD);
    p.addChild(useQualifiedNameTail());
    return n;
  }

  // This is consistent with the hypothesis on check-ins that they appear when
  // there are several options to choose from, none of which are the epsilon
  // production.

  private AstNode useQualifiedNameTail () {
    AstNode n;
    checkIn(FirstSet.USE_QUALIFIED_NAME_TAIL);
    if (kind == ASTERISK) {
      confirm(ASTERISK);
      n = new UseNameWildcard(mark);
    } else if (kind == L_BRACE) {
      n = useNameGroup();
    } else if (kind == Token.Kind.IDENTIFIER) {
      confirm(Token.Kind.IDENTIFIER);
      n = new UseName(mark);
      if (kind == PERIOD) {
        confirm(PERIOD);
        n.addChild(useQualifiedNameTail());
      }
    } else {
      n = new ErrorNode(mark);
    }
    return n;
  }

  // This production has an optional construct that requires proper handling of
  // epsilon productions.

  private AstNode useNameGroup () {
    followerSetStack.push(SET_R_BRACE);
    confirm(L_BRACE);
    var n = new UseNameGroup(mark);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new UseName(mark));
//    checkIn(EnumSet.of(COMMA, R_BRACE));
    option(COMMA, R_BRACE);
    while (kind == COMMA) {
      confirm(COMMA);
      match(Token.Kind.IDENTIFIER);
      n.addChild(new UseName(mark));
      option(COMMA, R_BRACE);
//      checkIn(EnumSet.of(COMMA, R_BRACE));
    }
    match(R_BRACE);
    checkOut();
    if (errorRecoveryMode && kind == R_BRACE)
      confirm(R_BRACE);
    followerSetStack.pop();
    return n;
  }

  // OTHER DECLARATIONS

//      System.out.println("Sleeping for " + SLEEP_TIME + " seconds in declarations...");
//      try {
//        Thread.sleep(SLEEP_TIME);
//      } catch (InterruptedException e) {
//        throw new RuntimeException(e);
//      }

  private AstNode otherDeclarations () {
    followerSetStack.push(FollowerSet.OTHER_DECLARATIONS);
    checkIn(FirstSet.OTHER_DECLARATIONS);
    var n = new OtherDeclarations();
    while (FirstSet.OTHER_DECLARATION.contains(kind)) {
      n.addChild(otherDeclaration());
    }
    followerSetStack.pop();
    return n;
  }

  // Entities may be declared as private, indicating that they are not
  // exported. Otherwise, they are considered public, i.e. exported.

  private AstNode otherDeclaration () {
    followerSetStack.push(FirstSet.OTHER_DECLARATION);
    AstNode p;
    if (kind == PRIVATE) {
      confirm(PRIVATE);
      p = new ExportSpecifier(mark);
    } else {
      p = EPSILON;
    }
    AstNode n = null;
    if (kind == TEMPLATE) {
//      n = templateDeclaration();
    } else {
      modifiers();
      if (kind == CLASS)
        n = classDeclaration(p);
      else if (kind == TYPEALIAS)
        n = typealiasDeclaration(p);
      else if (kind == DEF)
        n = routineDeclaration(p);
      else if (kind == VAL || kind == VAR)
        n = variableDeclaration(p);
      else {
        checkError(EnumSet.of(CLASS, TYPEALIAS, DEF, VAL, VAR));
        mark = lookahead;
        var combined = combine();
        sync(combined);
        modifierStack.clear();
        n = new ErrorNode(mark);
      }
    }
    followerSetStack.pop();
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
    while (
      kind == ABSTRACT  ||
      kind == CONST     ||
      kind == CONSTEXPR ||
      kind == FINAL     ||
      kind == VOLATILE
    ) {
      confirm(kind);
      modifierStack.push(new Modifier(mark));
    }
  }

  // CLASS DECLARATIONS

  private AstNode classDeclaration (AstNode exportSpecifier) {
    confirm(CLASS);
    var n = new ClassDeclaration(mark);
    n.addChild(exportSpecifier);
    n.addChild(classModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new ClassName(mark));
    if (kind == EXTENDS)
      n.addChild(baseClasses(EnumSet.of(L_BRACE)));
    else
      n.addChild(EPSILON);
    n.addChild(classBody());
    return n;
  }

  private AstNode classModifiers () {
    var n = new ClassModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  // For now, we only support public (i.e. "is-a") inheritance. Private (i.e.
  // "is-implemented-in-terms-of") inheritance is NOT supported. Most use cases
  // of private inheritance are better met by composition instead.

  private AstNode baseClasses (EnumSet<Token.Kind> syncSet) {
    followerSetStack.push(syncSet);
    confirm(EXTENDS);
    var n = new BaseClasses();
    match(Token.Kind.IDENTIFIER);
    n.addChild(new BaseClass(mark));
    while (kind == COMMA) {
      confirm(COMMA);
      match(Token.Kind.IDENTIFIER);
      n.addChild(new BaseClass(mark));
    }
    followerSetStack.pop();
    return n;
  }

  // ClassBody is essentially equivalent to memberDeclarations

  private AstNode classBody () {
    match(L_BRACE);
    var n = new ClassBody();
    while (
      kind == PRIVATE ||
      kind == CLASS   ||
      kind == DEF     ||
      kind == VAL     ||
      kind == VAR
    ) {
      n.addChild(memberDeclaration(EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, R_BRACE)));
    }
    match(R_BRACE);
    return n;
  }

  // MEMBER DECLARATIONS

  private AstNode memberDeclaration (EnumSet<Token.Kind> syncSet) {
    followerSetStack.push(syncSet);
    AstNode accessSpecifier;
    if (kind == PRIVATE) {
      confirm(PRIVATE);
      accessSpecifier = new MemberAccessSpecifier(mark);
    } else if (kind == PROTECTED) {
      confirm(PROTECTED);
      accessSpecifier = new MemberAccessSpecifier(mark);
    } else {
      accessSpecifier = EPSILON;
    }
    memberModifiers();
    AstNode n;
    if (kind == TYPEALIAS) {
      n = memberTypealiasDeclaration(accessSpecifier);
    } else if (kind == DEF) {
      n = memberRoutineDeclaration(accessSpecifier);
    } else if (kind == VAL || kind == VAR) {
      n = memberVariableDeclaration(accessSpecifier);
    } else {
      // Error - need to sync?
      n = null;
    }
    followerSetStack.pop();
    return n;
  }

  private void memberModifiers () {
    while (
      kind == ABSTRACT  ||
      kind == CONST     ||
      kind == CONSTEXPR ||
      kind == FINAL     ||
      kind == OVERRIDE  ||
      kind == STATIC    ||
      kind == VIRTUAL   ||
      kind == VOLATILE
    ) {
      confirm(kind);
      modifierStack.push(new Modifier(mark));
    }
  }

  // To do: Finish follower set

  // What if there are modifiers on typealias? Is that a syntax error or
  // semantic error?

  private AstNode memberTypealiasDeclaration (AstNode accessSpecifier) {
    confirm(TYPEALIAS);
    var n = new MemberTypealiasDeclaration(mark);
    n.addChild(accessSpecifier);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new TypealiasName(mark));
    // Follower set is whatever can start a type
    match(EQUAL);
    n.addChild(declarator(null));
    match(SEMICOLON);
    return n;
  }

  private AstNode memberRoutineDeclaration (AstNode accessSpecifier) {
    confirm(DEF);
    var n = new MemberRoutineDeclaration(mark);
    n.addChild(accessSpecifier);
    n.addChild(memberRoutineModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new RoutineName(mark));
    n.addChild(routineParameters());
    // No following set required here because these are completely optional
    n.addChild(cvQualifiers());
    if (kind == AMPERSAND) {
      confirm(AMPERSAND);
      n.addChild(new RefQualifier(mark));
    } else if (kind == AMPERSAND_AMPERSAND) {
      confirm(AMPERSAND_AMPERSAND);
      n.addChild(new RefQualifier(mark));
    } else {
      n.addChild(EPSILON);
    }
    if (kind == NOEXCEPT) {
      confirm(NOEXCEPT);
      n.addChild(new NoexceptSpecifier(mark));
    } else {
      n.addChild(EPSILON);
    }
    if (kind == MINUS_GREATER) {
      n.addChild(routineReturnTypeSpecifier());
    } else {
      n.addChild(EPSILON);
    }
    n.addChild(routineBody());
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
    if (kind == CONST) {
      confirm(CONST);
      n.addChild(new CVQualifier(mark));
      if (kind == VOLATILE) {
        confirm(VOLATILE);
        n.addChild(new CVQualifier(mark));
      }
    } else if (kind == VOLATILE) {
      confirm(VOLATILE);
      n.addChild(new CVQualifier(mark));
      if (kind == CONST) {
        confirm(CONST);
        n.addChild(new CVQualifier(mark));
      }
    }
    return n;
  }

  private AstNode memberVariableDeclaration (AstNode accessSpecifier) {
    confirm(kind == VAL ? VAL : VAR);
    var n = new MemberVariableDeclaration(mark);
    n.addChild(accessSpecifier);
    n.addChild(variableModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new VariableName(mark));
    if (kind == COLON) {
      n.addChild(variableTypeSpecifier());
      if (kind == EQUAL)
        n.addChild(variableInitializer());
      else
        n.addChild(EPSILON);
    } else {
      n.addChild(EPSILON);
      n.addChild(variableInitializer());
    }
    match(SEMICOLON);
    return n;
  }

  // TYPEALIAS DECLARATION

  private AstNode typealiasDeclaration (AstNode exportSpecifier) {
    confirm(TYPEALIAS);
    var n = new TypealiasDeclaration(mark);
    n.addChild(exportSpecifier);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new TypealiasName(mark));
    // To do: Follower set is whatever can start a type
    match(EQUAL);
    n.addChild(declarator(null));
    match(SEMICOLON);
    return n;
  }

  private AstNode localTypealiasDeclaration () {
    confirm(TYPEALIAS);
    var n = new LocalTypealiasDeclaration(lookahead);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new TypealiasName(mark));
    // To do: Follower set is whatever can start a type
    match(EQUAL);
    n.addChild(declarator(null));
    match(SEMICOLON);
    return n;
  }

  // ROUTINE DECLARATIONS

  // Todo: We need to push another scope onto the scope stack. Keep in mind that
  // the routine parameters may be in the same exact scope as the routine body
  // (or top-most block of the routine).

  // For now, there are no local routines, so no need to distinguish between
  // global and local routines.

  // To do: Finish error recovery

  private AstNode routineDeclaration (AstNode exportSpecifier) {
    confirm(DEF);
    var n = new RoutineDeclaration(mark);
    n.addChild(exportSpecifier);
    n.addChild(routineModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new RoutineName(mark));
    n.addChild(routineParameters());
    if (kind == NOEXCEPT) {
      confirm(NOEXCEPT);
      n.addChild(new NoexceptSpecifier(mark));
    } else {
      n.addChild(EPSILON);
    }
    if (kind == MINUS_GREATER) {
      n.addChild(routineReturnTypeSpecifier());
    } else {
      n.addChild(EPSILON);
    }
    n.addChild(routineBody());
    return n;
  }

  // Is this really deprecated?

  @Deprecated
  private AstNode routineModifiers () {
    var n = new RoutineModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  private AstNode routineParameters () {
    // To do: Add in parameter modifiers as required
    match(L_PARENTHESIS);
    var n = new RoutineParameters();
    if (kind == Token.Kind.IDENTIFIER)
      n.addChild(routineParameter(EnumSet.of(COMMA, R_PARENTHESIS)));
    while (kind == COMMA) {
      confirm(COMMA);
      n.addChild(routineParameter(EnumSet.of(COMMA, R_PARENTHESIS)));
    }
    match(R_PARENTHESIS);
    return n;
  }

  // Routine parameters are for all intents and purposes local variables

  private AstNode routineParameter (EnumSet<Token.Kind> syncSet) {
    followerSetStack.push(syncSet);
    var n = new RoutineParameter();
    match(Token.Kind.IDENTIFIER);
    n.addChild(new RoutineParameterName(mark));
    n.addChild(routineParameterTypeSpecifier(EnumSet.of(COMMA, R_PARENTHESIS)));
    followerSetStack.pop();
    return n;
  }

  private AstNode routineParameterTypeSpecifier (EnumSet<Token.Kind> syncSet) {
    followerSetStack.push(syncSet);
    match(COLON);
    var n = new RoutineParameterTypeSpecifier(mark);
    n.addChild(declarator(null));
    followerSetStack.pop();
    return n;
  }

  // Some languages us a colon for the return type, while others use an arrow.
  // Opal uses an arrow. Apart from the fact that and Opal is a C++ derivative,
  // (which uses an arrow), the arrow stands out more when there are CV and ref
  // qualifiers.

  // We can either treat this like a type specifier or use it as a passthrough
  // to a type specifier.

  private AstNode routineReturnTypeSpecifier () {
    followerSetStack.push(EnumSet.of(L_BRACE));
    confirm(MINUS_GREATER);
    var n = new RoutineReturnTypeSpecifier();
    n.addChild(declarator(null));
    followerSetStack.pop();
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

  // To do: Local variables don't have access specifiers. Because children can
  // be accessed by name, we need a separate local variable node type.

  // We put null values into the list of children to ensure a constant node
  // count and node order.

  // To do: variable initializer is being arrived at via a certain path and an
  // uncertain path. So do we match or do we confirm? I think we need to match,
  // which is a more fail-safe option.

  // To do: Why doesn't sync stop at semicolon? Is there an issue with the
  // following set? Here, we will experiment with putting semicolons into the
  // following set. This is not a pure approach, as we previously assumed that
  // all members of the following set had to at least be in the FOLLOW set.

  private AstNode variableDeclaration (AstNode exportSpecifier) {
    followerSetStack.push(EnumSet.of(SEMICOLON));
    confirm(kind == VAL ? VAL : VAR);
    var n = new VariableDeclaration(mark);
    n.addChild(exportSpecifier);
    n.addChild(variableModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new VariableName(mark));
    if (kind == COLON) {
      n.addChild(variableTypeSpecifier());
      if (kind == EQUAL)
        n.addChild(variableInitializer());
      else
        n.addChild(EPSILON);
    } else if (kind == EQUAL) {
      n.addChild(EPSILON);
      n.addChild(variableInitializer());
    } else {
      checkError(EnumSet.of(COLON, EQUAL));
      // Might not need to mark
      mark = lookahead;
      var combined = combine();
      sync(combined);
    }
    followerSetStack.pop();
    match(SEMICOLON);
    return n;
  }

  private AstNode variableModifiers () {
    var n = new VariableModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  // Is this only ever arrived at on a sure path? If so, we can replace the
  // match method with confirm.

  private AstNode variableTypeSpecifier () {
    followerSetStack.push(EnumSet.of(EQUAL));
    match(Token.Kind.COLON);
    var n = new VariableTypeSpecifier(mark);
    n.addChild(declarator(null));
    followerSetStack.pop();
    return n;
  }

  private AstNode variableInitializer () {
    followerSetStack.push(EnumSet.of(SEMICOLON));
    match(EQUAL);
    var n = new VariableInitializer(mark);
    n.addChild(expression(true));
    followerSetStack.pop();
    return n;
  }

  private AstNode localVariableDeclaration () {
    confirm(kind == VAL ? VAL : VAR);
    var n = new LocalVariableDeclaration(mark);
    n.addChild(variableModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new VariableName(mark));
    if (kind == COLON) {
      n.addChild(variableTypeSpecifier());
      if (kind == EQUAL)
        n.addChild(variableInitializer());
      else
        n.addChild(EPSILON);
    } else {
      n.addChild(EPSILON);
      n.addChild(variableInitializer());
    }
    // Local classes and nested routines are not supported
    match(SEMICOLON);
    return n;
  }

  // STATEMENTS **************************************************

  // Notice that we don't include 'if' in the first set for expression
  // statements. This is because we want send the parser towards the statement
  // version of 'if'.

  private AstNode statement () {
    AstNode n = null;
    if (
      kind == BREAK     ||
      kind == L_BRACE   ||
      kind == CONTINUE  ||
      kind == DO        ||
      kind == FOR       ||
      kind == LOOP      ||
      kind == SEMICOLON ||
      kind == IF        ||
      kind == RETURN    ||
      kind == UNTIL     ||
      kind == WHILE
    ) {
      n = standardStatement();
    } else if (
      kind == Token.Kind.TYPEALIAS ||
      kind == Token.Kind.VAL       ||
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
      kind == Token.Kind.IDENTIFIER        ||
      kind == Token.Kind.NEW               ||
      kind == Token.Kind.DELETE
    ) {
      n = expressionStatement();
    } else {
      System.out.println("Error: invalid statement");
    }
    return n;
  }

  private AstNode standardStatement () {
    AstNode n = null;
    switch (kind) {
      case Token.Kind.BREAK ->
        n = breakStatement();
      case Token.Kind.L_BRACE ->
        n = compoundStatement();
      case Token.Kind.CONTINUE ->
        n = continueStatement();
      case Token.Kind.DO ->
        n = doStatement();
      case SEMICOLON ->
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
    confirm(BREAK);
    var n = new BreakStatement(mark);
    match(SEMICOLON);
    return n;
  }

  private AstNode compoundStatement () {
    var n = new CompoundStatement(lookahead);
    match(Token.Kind.L_BRACE);
    while (kind != Token.Kind.R_BRACE) {
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
    match(SEMICOLON);
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
    if (kind == Token.Kind.TYPEALIAS)
      n = localTypealiasDeclaration();
    else if (kind == Token.Kind.VAL || kind == Token.Kind.VAR)
      n = localVariableDeclaration();
    // To do: Need error checking here
    return n;
  }

  // The do statement is flexible and can either be a "do while" or a "do until"
  // statement, depending on what follows the 'do' keyword.

  private AstNode doStatement () {
    AstNode n = null;
    match(Token.Kind.DO);
    switch (kind) {
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
    match(SEMICOLON);
    return n;
  }

  // The expression statement is primarily just a passthrough, but we want a
  // dedicated AST node at the root of all statement sub-trees.
  // NOTE: IS ABOVE TRUE?

  private AstNode expressionStatement () {
    var n = new ExpressionStatement();
    n.addChild(expression(true));
    match(SEMICOLON);
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
    if (kind == Token.Kind.ELSE)
      n.addChild(elseClause());
    return n;
  }

  private AstNode elseClause () {
    var n = new ElseClause(lookahead);
    match(Token.Kind.ELSE);
    if (kind == Token.Kind.IF)
      n.addChild(ifStatement());
    else
      n.addChild(statementBody());
    return n;
  }

  private AstNode loopStatement () {
    var n = new LoopStatement(lookahead);
    match(Token.Kind.LOOP);
    if (kind == Token.Kind.L_PARENTHESIS)
      n.addChild(loopControl());
    else
      n.addChild(null);
    n.addChild(loopBody());
    return n;
  }

  private AstNode loopControl () {
    var n = new LoopControl(lookahead);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(lookahead.getKind() != SEMICOLON ? loopInitializer() : null);
    match(SEMICOLON);
    n.addChild(lookahead.getKind() != SEMICOLON ? loopCondition() : null);
    match(SEMICOLON);
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
    if (kind == Token.Kind.L_BRACE)
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
    if (lookahead.getKind() != SEMICOLON) {
      n.addChild(expression(true));
      match(SEMICOLON);
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
    if (kind == Token.Kind.L_BRACE)
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
      var p = new Expression();
      p.addChild(n);
      n = p;
    }
    return n;
  }

  // We may wish to add a 'walrus' operator (:=), which can be used inside a
  // conditional statement to indicate that the developer truly intends to have
  // an assignment rather than an equality check.

  private AstNode assignmentExpression () {
    var n = logicalOrExpression();
    while (
      kind == EQUAL ||
      kind == ASTERISK_EQUAL ||
      kind == SLASH_EQUAL ||
      kind == PERCENT_EQUAL ||
      kind == PLUS_EQUAL ||
      kind == MINUS_EQUAL ||
      kind == LESS_LESS_EQUAL ||
      kind == GREATER_GREATER_EQUAL ||
      kind == AMPERSAND_EQUAL ||
      kind == CARET_EQUAL ||
      kind == BAR_EQUAL
    ) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(logicalOrExpression());
      n = p;
    }
    return n;
  }

  private AstNode logicalOrExpression () {
    var n = logicalAndExpression();
    while (kind == OR) {
      confirm(OR);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(logicalAndExpression());
      n = p;
    }
    return n;
  }

  private AstNode logicalAndExpression () {
    var n = inclusiveOrExpression();
    while (kind == AND) {
      confirm(AND);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(inclusiveOrExpression());
      n = p;
    }
    return n;
  }

  private AstNode inclusiveOrExpression () {
    var n = exclusiveOrExpression();
    while (kind == BAR) {
      confirm(BAR);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(exclusiveOrExpression());
      n = p;
    }
    return n;
  }

  private AstNode exclusiveOrExpression () {
    var n = andExpression();
    while (kind == CARET) {
      confirm(CARET);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(andExpression());
      n = p;
    }
    return n;
  }

  private AstNode andExpression () {
    var n = equalityExpression();
    while (kind == AMPERSAND) {
      confirm(AMPERSAND);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(equalityExpression());
      n = p;
    }
    return n;
  }

  private AstNode equalityExpression () {
    var n = relationalExpression();
    while (kind == EQUAL_EQUAL || kind == EXCLAMATION_EQUAL) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(relationalExpression());
      n = p;
    }
    return n;
  }

  private AstNode relationalExpression () {
    var n = shiftExpression();
    while (kind == GREATER || kind == LESS || kind == GREATER_EQUAL || kind == LESS_EQUAL) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(shiftExpression());
      n = p;
    }
    return n;
  }

  private AstNode shiftExpression () {
    var n = additiveExpression();
    while (kind == GREATER_GREATER || kind == LESS_LESS) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(additiveExpression());
      n = p;
    }
    return n;
  }

  private AstNode additiveExpression () {
    var n = multiplicativeExpression();
    while (kind == PLUS || kind == MINUS) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(multiplicativeExpression());
      n = p;
    }
    return n;
  }

  private AstNode multiplicativeExpression () {
    var n = unaryExpression();
    while (kind == ASTERISK || kind == SLASH || kind == PERCENT) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(unaryExpression());
      n = p;
    }
    return n;
  }

  // C++ formulation might be slightly different with mutual recursion between
  // unaryExpression and castExpression methods. What effect might that have?
  // (See p. 54, Ellis & Stroustrup, 1990.)

  private AstNode unaryExpression () {
    AstNode n = null;
    if (kind == ASTERISK || kind == MINUS || kind == PLUS || kind == EXCLAMATION || kind == TILDE) {
      confirm(kind);
      n = new UnaryExpression(mark);
      n.addChild(unaryExpression());
    } else if (kind == CAST || kind == DIVINE || kind == TRANSMUTE) {
      n = castExpression();
    } else if (kind == DELETE) {
      n = deleteExpression();
    } else if (kind == NEW) {
      n = newExpression();
    } else {
      // I think I need to check first set above
      n = postfixExpression();
    }
    return n;
  }

  private AstNode castExpression () {
    var n = new CastExpression(lookahead);
    match(lookahead.getKind());
    match(Token.Kind.LESS);
    n.addChild(declarator(EnumSet.of(GREATER)));
    match(Token.Kind.GREATER);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(expression(true));
    match(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // In C++, the delete keyword is followed by a cast expression. I believe
  // this is to limit the possible kind of expressions that can be used.
  // However, C++ has a different arrangement between the cast an unary
  // expressions compared to this language. We should investigate if limiting
  // the delete expression is the reason why, and if so, adjust the grammar to
  // match.

  private AstNode deleteExpression () {
    var n = new DeleteExpression(lookahead);
    match(Token.Kind.DELETE);
    if (kind == Token.Kind.L_BRACKET) {
      n.setArrayFlag();
      match(Token.Kind.L_BRACKET);
      match(Token.Kind.R_BRACKET);
    }
    n.addChild(expression(true));
    return n;
  }

  private AstNode newExpression () {
    var n = new NewExpression(lookahead);
    match(Token.Kind.NEW);
    n.addChild(kind == Token.Kind.L_BRACKET ? newPlacement() : null);
    n.addChild(declarator(null));
    n.addChild(kind == Token.Kind.L_PARENTHESIS ? newInitializer() : null);
    return n;
  }

  private AstNode newPlacement () {
    match(Token.Kind.L_BRACKET);
    var n = expression(true);
    match(Token.Kind.R_BRACKET);
    return n;
  }

  private AstNode newInitializer () {
    var n = new NewInitializer(lookahead);
    match(Token.Kind.L_PARENTHESIS);
    n.addChild(expression(true));
    while (kind == Token.Kind.COMMA) {
      match(Token.Kind.COMMA);
      n.addChild(expression(true));
    }
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
      kind == Token.Kind.L_BRACKET ||
      kind == Token.Kind.MINUS_GREATER ||
      kind == Token.Kind.PERIOD ||
      kind == Token.Kind.L_PARENTHESIS
    ) {
      switch (kind) {
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
      while (kind == Token.Kind.COMMA) {
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

  // To do: This is not erroring out on bad input to new expression. Needs
  // investigation.

  private AstNode primaryExpression () {
    AstNode n = null;
    if (
      kind == FALSE ||
      kind == TRUE ||
      kind == CHARACTER_LITERAL ||
      kind == FLOAT32_LITERAL ||
      kind == FLOAT64_LITERAL ||
      kind == INT32_LITERAL ||
      kind == INT64_LITERAL ||
      kind == NULL ||
      kind == STRING_LITERAL ||
      kind == UINT32_LITERAL ||
      kind == UINT64_LITERAL
    ) {
      n = literal();
    } else if (kind == Token.Kind.THIS)
      n = this_();
    else if (kind == Token.Kind.IDENTIFIER) {
      // Test this -- is this not working?
      n = name();
    }
    // Defer implementing if expressions
//    else if (kind == Token.Kind.IF)
//      n = ifExpression();
    else if (kind == Token.Kind.L_PARENTHESIS) {
      n = parenthesizedExpression();
    } else {
      System.out.println("ERROR - INVALID PRIMARY EXPRESSION");
    }
    return n;
  }

  private AstNode literal () {
    AstNode n;
    if (kind == FALSE) {
      confirm(FALSE);
      n = new BooleanLiteral(mark);
    } else if (kind == TRUE) {
      confirm(TRUE);
      n = new BooleanLiteral(mark);
    } else if (kind == CHARACTER_LITERAL) {
      confirm(CHARACTER_LITERAL);
      n = new CharacterLiteral(mark);
    } else if (kind == FLOAT32_LITERAL) {
      confirm(FLOAT32_LITERAL);
      n = new FloatingPointLiteral(mark);
    } else if (kind == FLOAT64_LITERAL) {
      confirm(FLOAT64_LITERAL);
      n = new FloatingPointLiteral(mark);
    } else if (kind == INT32_LITERAL) {
      confirm(INT32_LITERAL);
      n = new IntegerLiteral(mark);
    } else if (kind == INT64_LITERAL) {
      confirm(INT64_LITERAL);
      n = new IntegerLiteral(mark);
    } else if (kind == NULL) {
      confirm(NULL);
      n = new NullLiteral(mark);
    } else if (kind == STRING_LITERAL) {
      confirm(STRING_LITERAL);
      n = new StringLiteral(mark);
    } else if (kind == UINT32_LITERAL) {
      confirm(UINT32_LITERAL);
      n = new UnsignedIntegerLiteral(mark);
    } else if (kind == UINT64_LITERAL) {
      confirm(UINT64_LITERAL);
      n = new UnsignedIntegerLiteral(mark);
    } else {
      checkError(FirstSet.LITERAL);
      mark = lookahead;
      var combined = combine();
      sync(combined);
      n = new ErrorNode(mark);
    }
    return n;
  }

  // Note: In C++, 'this' is a pointer, but in cppfront, it is not. Its unclear
  // if we can achieve the same thing in cobalt. For now, just assume it is a
  // pointer.

  private AstNode this_ () {
    var n = new This(lookahead);
    match(THIS);
    return n;
  }

  private AstNode parenthesizedExpression () {
    match(L_PARENTHESIS);
    var n = expression(false);
    match(R_PARENTHESIS);
    return n;
  }

  private AstNode name () {
    var n = new Name(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  // TYPES **************************************************

  // Type processing is interesting because Opal uses a form of the
  // C-declaration style, so parsing types directly is challenging due to the
  // order in which tokens appear and the way they are nested. Instead of
  // try to parse them into types immediately, we just create an AST that
  // resembles the input. Then, during semantic analysis, the actual types are
  // built by walking this tree in the appropriate order.

  private AstNode declarator (EnumSet<Token.Kind> syncSet) {
    if (syncSet != null)
      followerSetStack.push(syncSet);
    var n = new Declarator();
    if (kind == ASTERISK)
      n.addChild(pointerDeclarators());
    n.addChild(directDeclarator());
    if (kind == L_BRACKET)
      n.addChild(arrayDeclarators());
    if (syncSet != null)
      followerSetStack.pop();
    return n;
  }

  private AstNode directDeclarator () {
    AstNode n;
    if (
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
      confirm(kind);
      // Should be simple declarator
      n = new PrimitiveType(mark);
    } else if (kind == Token.Kind.IDENTIFIER) {
      confirm(Token.Kind.IDENTIFIER);
      n = new NominalType(mark);
    } else if (kind == CARET) {
      n = routinePointerDeclarator();
    } else if (kind == L_PARENTHESIS) {
      confirm(L_PARENTHESIS);
      n = declarator(null);
      match(R_PARENTHESIS);
    } else {
      // Error - Needs sync
      n = null;
    }
    return n;
  }

  // For now, assume all routine pointers must have a return type specified,
  // in which case, the last child of the AST node is the return type.

  private AstNode routinePointerDeclarator () {
    confirm(CARET);
    var n = new RoutinePointerDeclarator(mark);
    match(L_PARENTHESIS);
    if (FirstSet.TYPE.contains(kind)) {
      n.addChild(declarator(EnumSet.of(COMMA, R_PARENTHESIS)));
    }
    while (kind == COMMA) {
      confirm(COMMA);
      n.addChild(declarator(EnumSet.of(COMMA, R_PARENTHESIS)));
    }
    match(R_PARENTHESIS);
    match(MINUS_GREATER);
    n.addChild(declarator(EnumSet.of(COMMA, R_PARENTHESIS)));
    return n;
  }

  private AstNode pointerDeclarators () {
    var n = new PointerDeclarators();
    while (kind == ASTERISK) {
      confirm(ASTERISK);
      n.addChild(new PointerDeclarator(mark));
    }
    return n;
  }

  private AstNode arrayDeclarators () {
    var n = new ArrayDeclarators();
    while (kind == L_BRACKET) {
      n.addChild(arrayDeclarator());
    }
    return n;
  }

  // Check that expression is const during semantic analysis

  private AstNode arrayDeclarator () {
    confirm(L_BRACKET);
    var n = new ArrayDeclarator(mark);
    if (FirstSet.EXPRESSION.contains(kind))
      n.addChild(expression(true));
    match(R_BRACKET);
    return n;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, Token.Kind b) {
    var combined = EnumSet.copyOf(a);
    combined.add(b);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, Token.Kind b, Token.Kind c) {
    var combined = EnumSet.copyOf(a);
    combined.add(b);
    combined.add(c);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b, EnumSet<Token.Kind> c) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.addAll(c);
    return combined;
  }

  /*
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
    while (kind == Token.Kind.COMMA) {
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
  */

}
