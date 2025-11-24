package org.opal;

import java.util.*;
import java.util.stream.Collectors;

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

  // Experimental
  private Token previous;
  private Token mark;

  private final List<String> sourceLines;

  // Used to pass type nodes up and down during tree traversal
  private final LinkedList<Type> stack;

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
  // subsets of FOLLOW sets.

  // Stack for following sets
  private final LinkedList<Set<Token.Kind>> followingSetStack;

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
    previous = null;
    mark = null;
    this.sourceLines = sourceLines;
    stack = new LinkedList<>();
    nodeStack = new LinkedList<>();
    modifierStack = new LinkedList<>();
    builtinScope = new Scope(Scope.Kind.BUILT_IN);
    currentScope = builtinScope;

    followingSetStack = new LinkedList<>();

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

  // In The Definitive ANTLR4 Reference, Parr describes ANTLR's error recovery
  // in detail. His algorithm attempts phrase-level recovery through
  // single-token deletion or insertion, followed by context-informed
  // panic-mode recovery.

  // We might want to implement Damerau–Levenshtein distance algorithm to
  // auto-correct spellings (e.g. packge > package, esle > else). This can be
  // accomplished with the Apache Commons Text library or other means.

  // We want to implement error recovery in the following order of priority:
  // (1) single-token deletion if possible, (2) single-token insertion if
  // possible, (3) panic-mode recovery.

  // To do: We might want to also implement single-token replacement. If
  // deletion or insertion aren't possible, and the expected token is a
  // specific keyword, we can check if the token is just mis-spelled.

  // Although single-token deletion might seem redundant with panic-mode (since
  // a panicking parser will start off by deleting the current lookahead token)
  // this is not the case, because we want to try single-token insertion before
  // resorting to full panic-mode.

  // Neither single-token deletion nor single-token insertion make sense if the
  // current token is EOF. The former requires that we look at the next token,
  // which would not exist. The latter requires consideration of what kind of
  // token would follow, which also would not exist.

  private void delete (Token.Kind expectedKind) {
    if (!errorRecoveryMode)
      extraneousError(expectedKind);
    LOGGER.info("Match: deleted " + lookahead);
    consume();
    LOGGER.info("Match: matched " + lookahead);
    mark = lookahead;
    consume();
  }

  private void insert (Token.Kind expectedKind) {
    if (!errorRecoveryMode)
      missingError(expectedKind);
    mark = new Token(expectedKind, "<MISSING>", lookahead.getIndex(), lookahead.getLine(), lookahead.getColumn());
    previous = mark; // DEPRECATED
    LOGGER.info("Match: inserted " + mark);
  }

  // Note: Instead of an ERROR kind, we could just mark whatever the lookahead
  // is and then annotate the token with an error flag.

  private void sync () {
    mark = new Token(Token.Kind.ERROR, lookahead.getLexeme(), lookahead.getIndex(), lookahead.getLine(), lookahead.getColumn());
    LOGGER.info("Match: created " + mark);
    LOGGER.info("Match: synchronization started");
    // Combine all following sets into a sync set
    var syncSet = EnumSet.noneOf(Token.Kind.class);
    for (var followingSet : followingSetStack)
      syncSet.addAll(followingSet);
    var kind = lookahead.getKind();
    // Scan forward until we hit something in the sync set
    while (!syncSet.contains(kind)) {
      LOGGER.info("Match: skipped {}", lookahead);
      consume();
      kind = lookahead.getKind();
    }
    LOGGER.info("Match: synchronization complete");
  }

  private void matchX (Token.Kind expectedKind, EnumSet<Token.Kind> followerSet) {
    if (lookahead.getKind() == expectedKind) {
      // Happy path :)
      LOGGER.info("Match: matched " + lookahead);
      mark = lookahead;
      consume();
      errorRecoveryMode = false;
    } else {
      // Sad path :(
      LOGGER.info("Match: entering sad path");
      if (expectedKind == Token.Kind.EOF) {
        // Try single-token deletion
        var peek = input.get(position.get() + 1);
        if (peek.getKind() == Token.Kind.EOF)
          delete(Token.Kind.EOF);
        // Otherwise, done
        else
          generalError(Token.Kind.EOF);
      } else {
        if (lookahead.getKind() == Token.Kind.EOF) {
          // Try single-token insertion
          if (followerSet != null && followerSet.contains(lookahead.getKind()))
            insert(expectedKind);
          // Otherwise, done
          else {
            if (!errorRecoveryMode)
              generalError(expectedKind);
          }
        } else {
          // Try single-token deletion
          var peek = input.get(position.get() + 1);
          if (peek.getKind() == expectedKind)
            delete(expectedKind);
          // Otherwise, try single-token insertion
          else if (followerSet != null && followerSet.contains(lookahead.getKind()))
            insert(expectedKind);
          // Otherwise, fall back to panic-mode
          else {
            if (!errorRecoveryMode)
              generalError(expectedKind);
            sync();
          }
        }
        // Should be true, but can set to false for development
        errorRecoveryMode = false;
      }
    }
  }

  private void matchX (Token.Kind expectedKind) {
    matchX(expectedKind, null);
  }

  // DEPRECATED

  @Deprecated
  private boolean match (Token.Kind expectedKind) {
    return match(expectedKind, null);
  }

  @Deprecated
  private boolean match (Token.Kind expectedKind, EnumSet<Token.Kind> followingSet) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("Match: MATCHED " + lookahead);
      errorRecoveryMode = false;
      consume();
      return true;
    }
    else {
      LOGGER.info("Match: FOUND " + lookahead);
      LOGGER.info("Match: No action taken, synchronization required");
      if (!errorRecoveryMode) {
        generalError(expectedKind);
        errorRecoveryMode = false;
      }
      return false;
    }
  }

  // These error methods pertain to the match method. The specific error method
  // called depends on how an error was corrected. Single-token deletion
  // generates an "extraneous token" error, while single-token insertion
  // generates a "missing token" error. Otherwise, the "general error" is used.

  private void extraneousError (Token.Kind expectedKind) {
    var expectedKindString = keywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "expected " + expectedKindString + ", got extraneous " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error);
  }

  private void missingError (Token.Kind expectedKind) {
    var expectedKindString = keywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "missing " + expectedKindString + ", got unexpected " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error);
  }

  private void generalError (Token.Kind expectedKind) {
    var expectedKindString = keywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "expected " + expectedKindString + ", got " + actualKindString;
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
    var actualKind = lookahead.getKind();
    var actualKindString = keywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var messageSome = "expected one of " + expectedKindsString + "; got " + actualKindString;
    var messageOne  = "expected "        + expectedKindsString + ", got " + actualKindString;
    var message = expectedKinds.size() > 1 ? messageSome : messageOne;
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
    } else {
      var expectedKindFriendly = friendlyKind(expectedKind);
      var actualKindFriendly = friendlyKind(lookahead.getKind());
      var message = "expected " + expectedKindFriendly + ", got " + actualKindFriendly;
      throw new IllegalArgumentException("internal error: " + message);
    }
  }

  private void consume () {
    position.increment();
    if (position.get() < input.size())
      lookahead = input.get(position.get());
  }

  public AstNode process () {
    buildReverseKeywordLookupTable();
    definePrimitiveTypes();
    LOGGER.info("*** Parsing started... ***");
    var node = translationUnit(EnumSet.of(Token.Kind.EOF));
    // EOF is the only token in the follow set of translationUnit. Must match
    // it to ensure there is no garbage left over.
    matchX(Token.Kind.EOF);

    LOGGER.info("*** Parsing complete! ***");
    // Inspect builtin scope
//    var s = builtinScope.getSymbolTable().getData;
//    System.out.println(s);
    return node;
  }

  private void buildReverseKeywordLookupTable () {
    // Experimental, showing that this is a reverse token lookup, not
    // necessarily a reverse keyword lookup
    keywordLookup.put(Token.Kind.MINUS, "'-'");
    keywordLookup.put(Token.Kind.PLUS, "'+'");
    keywordLookup.put(Token.Kind.L_BRACE, "'{'");
    keywordLookup.put(Token.Kind.R_BRACE, "'}'");
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

  private AstNode translationUnit (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new TranslationUnit();
    var fsp = EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, USE, IMPORT);
    var fsi = EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, USE);
    var fsu = EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR);
    n.addChild(packageDeclaration(fsp));
    n.addChild(lookahead.getKind() == IMPORT ? importDeclarations(fsi) : EPSILON);
    n.addChild(lookahead.getKind() == USE ? useDeclarations(fsu) : EPSILON);
    var kind = lookahead.getKind();
    if (
      kind == PRIVATE ||
      kind == CLASS   ||
      kind == DEF     ||
      kind == VAL     ||
      kind == VAR
    ) {
      n.addChild(otherDeclarations());
    }
    //var scope = new Scope(Scope.Kind.GLOBAL);
    //scope.setEnclosingScope(currentScope);
    //currentScope = scope;
    //n.setScope(currentScope);
    followingSetStack.pop();
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

  private AstNode packageDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    matchX(PACKAGE, FollowerSet.IDENTIFIER);
    var n = new PackageDeclaration(mark);
    matchX(Token.Kind.IDENTIFIER, FollowerSet.SEMICOLON);
    n.addChild(new PackageName(mark));
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, USE, IMPORT, Token.Kind.EOF));
    followingSetStack.pop();
    return n;
  }

  private AstNode importDeclarations (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new ImportDeclarations();
    n.addChild(importDeclaration(FollowingSet.IMPORT));
    while (lookahead.getKind() == IMPORT)
      n.addChild(importDeclaration(FollowingSet.IMPORT));
    followingSetStack.pop();
    return n;
  }

  // We could implement this several ways. First, we could use a binary tree
  // with dots as internal nodes and names as leaf nodes. Second, we could
  // simply have a chain of names, with each child names being under its
  // respective parent. Lastly, we can have a list of names under the import
  // declaration. We choose to go with the latter case because that is the
  // easiest implementation and the others hold no advantages for our
  // particular use case.

  private AstNode importDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    confirm(IMPORT);
    var n = new ImportDeclaration(mark);
    n.addChild(importQualifiedName(EnumSet.of(SEMICOLON, AS)));
    n.addChild(lookahead.getKind() == AS ? importAsClause(FollowingSet.SEMICOLON) : EPSILON);
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, USE, IMPORT, Token.Kind.EOF));
    followingSetStack.pop();
    return n;
  }

  // This might be a candidate for error recovery of epsilon production.
  // Input "import opal-lang;" gives an error but the resulting ERROR token
  // does not get captured in the AST even though it is not able to be
  // corrected. This demonstrates that if an error occurs at all, we cannot
  // rely on it being apparent from looking at the AST. Also, the error given
  // is not a great one (due to epsilon production). It shows that there might
  // be room for improvement.

  private AstNode importQualifiedName (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new ImportQualifiedName();
    var followerSet = EnumSet.of(AS, PERIOD, SEMICOLON);
    matchX(Token.Kind.IDENTIFIER, followerSet);
    n.addChild(new ImportName(mark));
    while (lookahead.getKind() == PERIOD) {
      confirm(PERIOD);
      matchX(Token.Kind.IDENTIFIER, followerSet);
      n.addChild(new ImportName(mark));
    }
    followingSetStack.pop();
    return n;
  }

  private AstNode importAsClause (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    confirm(AS);
    matchX(Token.Kind.IDENTIFIER, FollowerSet.SEMICOLON);
    var n = new ImportName(mark);
    followingSetStack.pop();
    return n;
  }

  private AstNode useDeclarations (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new UseDeclarations();
    n.addChild(useDeclaration(FollowingSet.USE));
    while (lookahead.getKind() == USE)
      n.addChild(useDeclaration(FollowingSet.USE));
    followingSetStack.pop();
    return n;
  }

  private AstNode useDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    confirm(USE);
    var n = new UseDeclaration(mark);
    n.addChild(useQualifiedName(FollowingSet.SEMICOLON));
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, USE, Token.Kind.EOF));
    followingSetStack.pop();
    return n;
  }

  // Follower sets are used in two different ways, depending on whether or not
  // the token needs to be captured in an AST node or not. If so, it is passed
  // into a "terminal" production method, where it gets used in a match method
  // call. Otherwise, it is used directly in a match method call within the
  // non-terminal production method.

  private AstNode useQualifiedName (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode n = new UseQualifiedName();
    matchX(Token.Kind.IDENTIFIER, FollowerSet.PERIOD);
    var p = new UseName(mark);
    n.addChild(p);
    match(PERIOD, FollowerSet.IDENTIFIER);
    p.addChild(useQualifiedNameTail());
    followingSetStack.pop();
    return n;
  }

  private AstNode useQualifiedNameTail () {
    AstNode n;
    var kind = lookahead.getKind();
    if (kind == ASTERISK) {
      confirm(ASTERISK);
      n = new UseNameWildcard(mark);
    } else if (kind == L_BRACE) {
      n = useNameGroup();
    } else if (kind == Token.Kind.IDENTIFIER) {
      confirm(Token.Kind.IDENTIFIER);
      n = new UseName(mark);
      if (lookahead.getKind() == PERIOD) {
        confirm(PERIOD);
        n.addChild(useQualifiedNameTail());
      }
    } else {
      // No viable alternative. We should be able to improve upon this.
      checkError(EnumSet.of(ASTERISK, L_BRACE, Token.Kind.IDENTIFIER));
      mark = lookahead;
      sync();
      // We don't know which kind of node this is so we need to make a generic
      // error node, perhaps NoViableAlt, like ANTLR?
      n = new ErrorNode(mark);
    }
    return n;
  }

  private AstNode useNameGroup () {
    confirm(L_BRACE);
    var n = new UseNameGroup(mark);
    matchX(Token.Kind.IDENTIFIER, EnumSet.of(COMMA, R_BRACE));
    n.addChild(new UseName(mark));
    while (lookahead.getKind() == COMMA) {
      confirm(COMMA);
      matchX(Token.Kind.IDENTIFIER, EnumSet.of(COMMA, R_BRACE));
      n.addChild(new UseName(mark));
    }
    matchX(R_BRACE, FollowerSet.SEMICOLON);
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
    var n = new OtherDeclarations();
    var kind = lookahead.getKind();
    var fso = EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR);
    while (
      kind == PRIVATE ||
      kind == CLASS   ||
      kind == DEF     ||
      kind == VAL     ||
      kind == VAR
    ) {
      n.addChild(otherDeclaration(fso));
      kind = lookahead.getKind();
    }
    return n;
  }

  // Entities may be declared as private, indicating that they are not
  // exported. Otherwise, they are considered public, i.e. exported.

  private AstNode otherDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode p;
    if (lookahead.getKind() == PRIVATE) {
      confirm(PRIVATE);
      p = new ExportSpecifier(mark);
    } else {
      p = EPSILON;
    }
    AstNode n = null;
    if (lookahead.getKind() == TEMPLATE) {
//      n = templateDeclaration();
    } else {
      modifiers();
      var kind = lookahead.getKind();
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
        sync();
        modifierStack.clear();
        n = new ErrorNode(mark);
      }
    }
    followingSetStack.pop();
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
      kind == ABSTRACT  ||
      kind == CONST     ||
      kind == CONSTEXPR ||
      kind == FINAL     ||
      kind == VOLATILE
    ) {
      confirm(kind);
      modifierStack.push(new Modifier(mark));
      kind = lookahead.getKind();
    }
  }

  // CLASS DECLARATIONS

  private AstNode classDeclaration (AstNode exportSpecifier) {
    confirm(CLASS);
    var n = new ClassDeclaration(mark);
    n.addChild(exportSpecifier);
    n.addChild(classModifiers());
    matchX(Token.Kind.IDENTIFIER, EnumSet.of(EXTENDS, L_BRACE));
    n.addChild(new ClassName(mark));
    if (lookahead.getKind() == EXTENDS)
      n.addChild(baseClasses(FollowingSet.L_BRACE));
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

  private AstNode baseClasses (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    confirm(EXTENDS);
    var n = new BaseClasses();
    matchX(Token.Kind.IDENTIFIER, EnumSet.of(COMMA, L_BRACE));
    n.addChild(new BaseClass(mark));
    while (lookahead.getKind() == COMMA) {
      confirm(COMMA);
      matchX(Token.Kind.IDENTIFIER, EnumSet.of(COMMA, L_BRACE));
      n.addChild(new BaseClass(mark));
    }
    followingSetStack.pop();
    return n;
  }

  // ClassBody is essentially equivalent to memberDeclarations

  private AstNode classBody () {
    matchX(L_BRACE, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, R_BRACE));
    var n = new ClassBody();
    var kind = lookahead.getKind();
    while (
      kind == PRIVATE ||
      kind == CLASS   ||
      kind == DEF     ||
      kind == VAL     ||
      kind == VAR
    ) {
      n.addChild(memberDeclaration(EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR, R_BRACE)));
      kind = lookahead.getKind();
    }
    matchX(R_BRACE, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR));
    return n;
  }

  // MEMBER DECLARATIONS

  private AstNode memberDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode accessSpecifier;
    var kind = lookahead.getKind();
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
    kind = lookahead.getKind();
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
    followingSetStack.pop();
    return n;
  }

  private void memberModifiers () {
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
      confirm(kind);
      modifierStack.push(new Modifier(mark));
      kind = lookahead.getKind();
    }
  }

  // To do: Finish follower set

  // What if there are modifiers on typealias? Is that a syntax error or
  // semantic error?

  private AstNode memberTypealiasDeclaration (AstNode accessSpecifier) {
    confirm(TYPEALIAS);
    var n = new MemberTypealiasDeclaration(mark);
    n.addChild(accessSpecifier);
    matchX(Token.Kind.IDENTIFIER, FollowerSet.EQUAL);
    n.addChild(new TypealiasName(mark));
    // Follower set is whatever can start a type
    matchX(EQUAL, EnumSet.of(ASTERISK, COMMA, Token.Kind.BOOL, Token.Kind.IDENTIFIER));
    n.addChild(type());
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR));
    return n;
  }

  private AstNode memberRoutineDeclaration (AstNode accessSpecifier) {
    confirm(DEF);
    var n = new MemberRoutineDeclaration(mark);
    n.addChild(accessSpecifier);
    n.addChild(memberRoutineModifiers());
    matchX(Token.Kind.IDENTIFIER, FollowerSet.L_PARENTHESIS);
    n.addChild(new RoutineName(mark));
    n.addChild(routineParameters());
    // No following set required here because these are completely optional
    n.addChild(cvQualifiers());
    var kind = lookahead.getKind();
    if (kind == AMPERSAND) {
      confirm(AMPERSAND);
      n.addChild(new RefQualifier(mark));
    } else if (kind == AMPERSAND_AMPERSAND) {
      confirm(AMPERSAND_AMPERSAND);
      n.addChild(new RefQualifier(mark));
    } else {
      n.addChild(EPSILON);
    }
    if (lookahead.getKind() == NOEXCEPT) {
      confirm(NOEXCEPT);
      n.addChild(new NoexceptSpecifier(mark));
    } else {
      n.addChild(EPSILON);
    }
    // To do: finish error recovery
    n.addChild((lookahead.getKind() == Token.Kind.MINUS_GREATER) ? routineReturnType() : null);
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
    var kind = lookahead.getKind();
    if (kind == CONST) {
      confirm(CONST);
      n.addChild(new CVQualifier(mark));
      if (lookahead.getKind() == VOLATILE) {
        confirm(VOLATILE);
        n.addChild(new CVQualifier(mark));
      }
    } else if (kind == VOLATILE) {
      confirm(VOLATILE);
      n.addChild(new CVQualifier(mark));
      if (lookahead.getKind() == CONST) {
        confirm(CONST);
        n.addChild(new CVQualifier(mark));
      }
    }
    return n;
  }

  private AstNode memberVariableDeclaration (AstNode accessSpecifier) {
    confirm(lookahead.getKind() == VAL ? VAL : VAR);
    var n = new MemberVariableDeclaration(mark);
    n.addChild(accessSpecifier);
    n.addChild(variableModifiers());
    matchX(Token.Kind.IDENTIFIER, EnumSet.of(COLON, EQUAL));
    n.addChild(new VariableName(mark));
    if (lookahead.getKind() == COLON) {
      n.addChild(variableTypeSpecifier(FollowingSet.EQUAL));
      if (lookahead.getKind() == EQUAL)
        n.addChild(variableInitializer());
      else
        n.addChild(EPSILON);
    } else {
      n.addChild(EPSILON);
      n.addChild(variableInitializer());
    }
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR));
    return n;
  }

  // TYPEALIAS DECLARATION

  private AstNode typealiasDeclaration (AstNode exportSpecifier) {
    confirm(TYPEALIAS);
    var n = new TypealiasDeclaration(mark);
    n.addChild(exportSpecifier);
    matchX(Token.Kind.IDENTIFIER, FollowerSet.EQUAL);
    n.addChild(new TypealiasName(mark));
    // To do: Follower set is whatever can start a type
    matchX(EQUAL, EnumSet.of(ASTERISK, COMMA, Token.Kind.BOOL, Token.Kind.IDENTIFIER));
    n.addChild(type());
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR));
    return n;
  }

  private AstNode localTypealiasDeclaration () {
    confirm(TYPEALIAS);
    var n = new LocalTypealiasDeclaration(lookahead);
    matchX(Token.Kind.IDENTIFIER, FollowerSet.EQUAL);
    n.addChild(new TypealiasName(mark));
    // To do: Follower set is whatever can start a type
    matchX(EQUAL, EnumSet.of(ASTERISK, COMMA, Token.Kind.BOOL, Token.Kind.IDENTIFIER));
    n.addChild(type());
    matchX(SEMICOLON, EnumSet.of(VAL, VAR));
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
    matchX(Token.Kind.IDENTIFIER, FollowerSet.L_PARENTHESIS);
    n.addChild(new RoutineName(mark));
    n.addChild(routineParameters());
    if (lookahead.getKind() == NOEXCEPT) {
      confirm(NOEXCEPT);
      n.addChild(new NoexceptSpecifier(mark));
    } else {
      n.addChild(EPSILON);
    }
    n.addChild((lookahead.getKind() == Token.Kind.MINUS_GREATER) ? routineReturnType() : null);
    n.addChild(routineBody());
//    currentScope = scope.getEnclosingScope();
    return n;
  }

  @Deprecated
  private AstNode routineModifiers () {
    var n = new RoutineModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  private AstNode routineParameters () {
    // To do: Add in parameter modifiers as required
    matchX(L_PARENTHESIS, EnumSet.of(Token.Kind.IDENTIFIER, R_PARENTHESIS));
    var n = new RoutineParameters();
    if (lookahead.getKind() == Token.Kind.IDENTIFIER)
      n.addChild(routineParameter(EnumSet.of(R_PARENTHESIS)));
    while (lookahead.getKind() == COMMA) {
      match(COMMA);
      n.addChild(routineParameter(EnumSet.of(R_PARENTHESIS)));
    }
    // FS = left brace, arrow, noexcept, etc.
    matchX(Token.Kind.R_PARENTHESIS);
    return n;
  }

  // Routine parameters are for all intents and purposes local variables

  private AstNode routineParameter (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new RoutineParameter();
    matchX(Token.Kind.IDENTIFIER, FollowerSet.COLON);
    n.addChild(new RoutineParameterName(mark));
    n.addChild(routineParameterTypeSpecifier());
    followingSetStack.pop();
    return n;
  }

  private AstNode routineParameterTypeSpecifier () {
    // To do: Add type follower set
    matchX(COLON);
    var n = new RoutineParameterTypeSpecifier(mark);
    n.addChild(type());
    return n;
  }

  // Some languages us a colon for the return type, while others use an arrow.
  // Opal uses an arrow. Apart from the fact that and Opal is a C++ derivative,
  // (which uses an arrow), the arrow stands out more when there are CV and ref
  // qualifiers.

  // We can either treat this like a type specifier or use it as a passthrough
  // to a type specifier.

  private AstNode routineReturnType () {
    var n = new RoutineReturnType();
    if (lookahead.getKind() == Token.Kind.MINUS_GREATER) {
      match(Token.Kind.MINUS_GREATER);
      n.addChild(type());
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

  // To do: Local variables don't have access specifiers. Because children can
  // be accessed by name, we need a separate local variable node type.

  // We put null values into the list of children to ensure a constant node
  // count and node order.

  // To do: variable initializer is being arrived at via a certain path and an
  // uncertain path. So do we match or do we confirm? I think we need to match,
  // which is a more fail-safe option.

  private AstNode variableDeclaration (AstNode exportSpecifier) {
    confirm(lookahead.getKind() == VAL ? VAL : VAR);
    var n = new VariableDeclaration(mark);
    n.addChild(exportSpecifier);
    n.addChild(variableModifiers());
    matchX(Token.Kind.IDENTIFIER, EnumSet.of(COLON, EQUAL));
    n.addChild(new VariableName(mark));
    if (lookahead.getKind() == COLON) {
      n.addChild(variableTypeSpecifier(FollowingSet.EQUAL));
      if (lookahead.getKind() == EQUAL)
        n.addChild(variableInitializer());
      else
        n.addChild(EPSILON);
    } else {
      n.addChild(EPSILON);
      n.addChild(variableInitializer());
    }
    matchX(SEMICOLON, EnumSet.of(PRIVATE, CLASS, DEF, VAL, VAR));
    return n;
  }

  private AstNode variableModifiers () {
    var n = new VariableModifiers();
    while (!modifierStack.isEmpty())
      n.addChild(modifierStack.pop());
    return n;
  }

  private final EnumSet<Token.Kind> fsv1 = EnumSet.of (
    Token.Kind.IDENTIFIER,
    Token.Kind.BOOL,
    Token.Kind.DOUBLE,
    Token.Kind.FLOAT,
    Token.Kind.FLOAT32,
    Token.Kind.FLOAT64,
    Token.Kind.INT,
    Token.Kind.INT8,
    Token.Kind.INT16,
    Token.Kind.INT32,
    Token.Kind.INT64,
    Token.Kind.LONG,
    Token.Kind.NULL_T,
    Token.Kind.SHORT,
    Token.Kind.UINT,
    Token.Kind.UINT8,
    Token.Kind.UINT16,
    Token.Kind.UINT32,
    Token.Kind.UINT64,
    Token.Kind.VOID,
    ASTERISK,
    CARET,
    L_PARENTHESIS
  );

  // Is this only ever arrived at on a sure path? If so, we can replace the
  // match method with confirm.

  private AstNode variableTypeSpecifier (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    matchX(Token.Kind.COLON, fsv1);
    var n = new VariableTypeSpecifier(mark);
    n.addChild(type());
    followingSetStack.pop();
    return n;
  }

  private final EnumSet<Token.Kind> fsv2 = EnumSet.of (
    Token.Kind.IDENTIFIER,
    INT32_LITERAL,
    INT64_LITERAL,
    UINT32_LITERAL,
    UINT64_LITERAL,
    FLOAT32_LITERAL,
    FLOAT64_LITERAL,
    CHARACTER_LITERAL,
    STRING_LITERAL,
    PLUS,
    MINUS,
    TILDE,
    EXCLAMATION,
    AMPERSAND,
    ASTERISK,
    L_PARENTHESIS,
    PERIOD
  );

  private AstNode variableInitializer () {
    matchX(EQUAL, fsv2);
    var n = new VariableInitializer(mark);
    n.addChild(expression(true));
    return n;
  }

  private AstNode localVariableDeclaration () {
    confirm(lookahead.getKind() == VAL ? VAL : VAR);
    var n = new LocalVariableDeclaration(mark);
    n.addChild(variableModifiers());
    matchX(Token.Kind.IDENTIFIER, EnumSet.of(COLON, EQUAL));
    n.addChild(new VariableName(mark));
    if (lookahead.getKind() == COLON) {
      n.addChild(variableTypeSpecifier(FollowingSet.EQUAL));
      if (lookahead.getKind() == EQUAL)
        n.addChild(variableInitializer());
      else
        n.addChild(EPSILON);
    } else {
      n.addChild(EPSILON);
      n.addChild(variableInitializer());
    }
    // Local classes and nested routines are not supported
    // To do: Add expression first set items
    matchX(SEMICOLON, EnumSet.of(VAL, VAR));
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
      kind == Token.Kind.CLASS     ||
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
    var n = new BreakStatement(lookahead);
    match(Token.Kind.BREAK);
    match(SEMICOLON);
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
    var kind = lookahead.getKind();
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
    var kind = lookahead.getKind();
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
      kind = lookahead.getKind();
    }
    return n;
  }

  private AstNode logicalOrExpression () {
    var n = logicalAndExpression();
    while (lookahead.getKind() == OR) {
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
    while (lookahead.getKind() == AND) {
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
    while (lookahead.getKind() == BAR) {
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
    while (lookahead.getKind() == CARET) {
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
    while (lookahead.getKind() == AMPERSAND) {
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
    var kind = lookahead.getKind();
    while (kind == EQUAL_EQUAL || kind == EXCLAMATION_EQUAL) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(relationalExpression());
      n = p;
      kind = lookahead.getKind();
    }
    return n;
  }

  private AstNode relationalExpression () {
    var n = shiftExpression();
    var kind = lookahead.getKind();
    while (kind == GREATER || kind == LESS || kind == GREATER_EQUAL || kind == LESS_EQUAL) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(shiftExpression());
      n = p;
      kind = lookahead.getKind();
    }
    return n;
  }

  private AstNode shiftExpression () {
    var n = additiveExpression();
    var kind = lookahead.getKind();
    while (kind == GREATER_GREATER || kind == LESS_LESS) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(additiveExpression());
      n = p;
      kind = lookahead.getKind();
    }
    return n;
  }

  // All of the things that can follow a binary expression.

  EnumSet<Token.Kind> exprSet = EnumSet.of (
    Token.Kind.IDENTIFIER,
    PLUS,
    MINUS,
    ASTERISK,
    FALSE,
    TRUE,
    CHARACTER_LITERAL,
    FLOAT32_LITERAL,
    FLOAT64_LITERAL,
    INT32_LITERAL,
    INT64_LITERAL,
    NULL,
    STRING_LITERAL,
    UINT32_LITERAL,
    UINT64_LITERAL
  );

  private AstNode additiveExpression () {
    var n = multiplicativeExpression();
    var kind = lookahead.getKind();
    while (kind == PLUS || kind == MINUS) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(multiplicativeExpression());
      n = p;
      kind = lookahead.getKind();
    }
    return n;
  }

  private AstNode multiplicativeExpression () {
    var n = unaryExpression();
    var kind = lookahead.getKind();
    while (kind == ASTERISK || kind == SLASH || kind == PERCENT) {
      confirm(kind);
      var p = new BinaryExpression(mark);
      p.addChild(n);
      p.addChild(unaryExpression());
      n = p;
      kind = lookahead.getKind();
    }
    return n;
  }

  // C++ formulation might be slightly different with mutual recursion between
  // unaryExpression and castExpression methods. What effect might that have?
  // (See p. 54, Ellis & Stroustrup, 1990.)

  private AstNode unaryExpression () {
    AstNode n = null;
    var kind = lookahead.getKind();
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
      n = postfixExpression();
    }
    return n;
  }

  private AstNode castExpression () {
    var n = new CastExpression(lookahead);
    match(lookahead.getKind());
    match(Token.Kind.LESS);
    n.addChild(type());
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
    if (lookahead.getKind() == Token.Kind.L_BRACKET) {
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
    n.addChild(lookahead.getKind() == Token.Kind.L_BRACKET ? newPlacement() : null);
    n.addChild(type());
    n.addChild(lookahead.getKind() == Token.Kind.L_PARENTHESIS ? newInitializer() : null);
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
    while (lookahead.getKind() == Token.Kind.COMMA) {
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

  // To do: This is not erroring out on bad input to new expression. Needs
  // investigation.

  private AstNode primaryExpression () {
    AstNode n = null;
    var kind = lookahead.getKind();
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
    } else if (lookahead.getKind() == Token.Kind.THIS)
      n = this_();
    else if (lookahead.getKind() == Token.Kind.IDENTIFIER) {
      // Test this -- is this not working?
      n = name();
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
    AstNode n;
    var kind = lookahead.getKind();
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
      sync();
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
    // If program crashes, check here, this is just for testing.
//    System.out.println("***");
//    System.out.println(n);
//    System.out.println("***");
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
