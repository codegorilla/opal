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

  private final int SLEEP_TIME = 10;

  private final LinkedList<Token> input;
  private final Counter position;
  private Token lookahead;

  // Experimental
  private Token previous;

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

  private final HashMap<Token.Kind, String> reverseKeywordLookup = new HashMap<>();

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

  public Parser (LinkedList<Token> input, List<String> sourceLines) {
    this.input = input;
    position = new Counter();
    lookahead = input.get(position.get());
    previous = null;
    this.sourceLines = sourceLines;
    stack = new LinkedList<>();
    nodeStack = new LinkedList<>();
    modifierStack = new LinkedList<>();
    builtinScope = new Scope(Scope.Kind.BUILT_IN);
    currentScope = builtinScope;

    followingSetStack = new LinkedList<>();

    var level = Level.INFO;
    Configurator.setRootLevel(level);

  }

  // The check-in and check-out methods are based on panic-mode error recovery
  // discussed in [Wir76], [Top82], [Aho07], and others (see above).

  private void checkIn (Set<Token.Kind> firstSet, Set<Token.Kind> followSet) {
    var kind = lookahead.getKind();
    if (!firstSet.contains(kind)) {
      if (!errorRecoveryMode) {
        checkError(firstSet);
        errorRecoveryMode = false;
      }
      // Scan forward until we hit something in the FIRST or FOLLOW sets
      while (!firstSet.contains(kind) && !followSet.contains(kind) && kind != Token.Kind.EOF) {
        LOGGER.info("Skipping {}", lookahead);
        consume();
        kind = lookahead.getKind();
      }
    }
  }

  private void checkOut (Set<Token.Kind> followSet) {
    var kind = lookahead.getKind();
    if (!followSet.contains(kind)) {
      if (!errorRecoveryMode) {
        checkError(followSet);
        errorRecoveryMode = false;
      }
      // Scan forward until we hit something in the FOLLOW set
      while (!followSet.contains(kind) && kind != Token.Kind.EOF) {
        LOGGER.info("Skipping {}", lookahead);
        consume();
        kind = lookahead.getKind();
      }
    }
  }

  private void checkError (Set<Token.Kind> expectedKinds) {
    var expectedKindsString = expectedKinds.stream()
      .map(kind -> reverseKeywordLookup.getOrDefault(kind, friendlyKind(kind)))
      .sorted()
      .collect(Collectors.joining(", "));
    var actualKind = lookahead.getKind();
    var actualKindString = reverseKeywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var messageSome = "expected one of " + expectedKindsString + "; got " + actualKindString;
    var messageOne  = "expected "        + expectedKindsString + ", got " + actualKindString;
    var message = expectedKinds.size() > 1 ? messageSome : messageOne;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error.complete());
  }

  // Maybe replace this with a map that maps kinds back to strings
  private String friendlyKind (Token.Kind kind) {
    return kind.toString().toLowerCase().replace("_", " ");
  }

  // *** EXPERIMENTAL ***

  private void sync () {
    LOGGER.info("sync: synchronization started");
    // Combine all following sets on stack into a sync set
    var syncSet = EnumSet.noneOf(Token.Kind.class);
    for (var followingSet : followingSetStack)
      syncSet.addAll(followingSet);
    var kind = lookahead.getKind();
    // To do: Nested while inside if is probably redundant
    if (!syncSet.contains(kind)) {
      // Scan forward until we hit something in the sync set
      // To do: We probably don't need the EOF test anymore because it is
      // already included in the translation unit's following set.
      while (!syncSet.contains(kind) && kind != Token.Kind.EOF) {
        LOGGER.info("sync: skipped {}", lookahead);
        consume();
        kind = lookahead.getKind();
      }
      LOGGER.info("sync: stopped at {}", lookahead);
    }
    LOGGER.info("sync: synchronization complete");
  }

  // *** END EXPERIMENT ***

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

  private boolean match (Token.Kind expectedKind) {
    return match(expectedKind, (Token.Kind)null);
  }

  // A valid match will reset the errorRecoveryMode flag

  // For SINGLE following kind

  private boolean match (Token.Kind expectedKind, Token.Kind followingKind) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("Match: Matched " + lookahead);
      errorRecoveryMode = false;
      consume();
      return true;
    }
    else {
      if (lookahead.getKind() != Token.Kind.EOF) {
        // Perform single-token deletion if possible
        var peek = input.get(position.get() + 1);
        if (peek.getKind() == expectedKind) {
          LOGGER.info("Match: Performing single-token deletion");
          if (!errorRecoveryMode) {
            extraneousError(expectedKind);
            errorRecoveryMode = false;
          }
          consume();
          consume();
          return true;
        }
        // Otherwise, perform single-token insertion if possible
        else if (lookahead.getKind() == followingKind) {
          LOGGER.info("Match: Performing single-token insertion");
          if (!errorRecoveryMode) {
            missingError(expectedKind);
            errorRecoveryMode = false;
          }
          previous = new Token(expectedKind, "<MISSING>", lookahead.getIndex(), lookahead.getLine(), lookahead.getColumn());
          return true;
        } else {
          LOGGER.info("Match: No action taken, synchronization required");
          // Do we fall back to sync-and-return here? If so, then we'll need a
          // FOLLOW set passed in. Or we can just leave that to a separate sync
          // method. I don't think we want to sync here because then we lose
          // the reference to the "previous" token that needs to be put into an
          // error node. So that means sync need to be external to this method.
          if (!errorRecoveryMode) {
            generalError(expectedKind);
            errorRecoveryMode = false;
          }
          return false;
        }
      }
      return false;
    }
  }

  // For MULTIPLE following kinds

  private boolean match (Token.Kind expectedKind, EnumSet<Token.Kind> followingSet) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("Match: Matched " + lookahead);
      errorRecoveryMode = false;
      consume();
      return true;
    }
    else {
      if (lookahead.getKind() != Token.Kind.EOF) {
        // Perform single-token deletion if possible
        var peek = input.get(position.get() + 1);
        if (peek.getKind() == expectedKind) {
          LOGGER.info("Match: Performing single-token deletion");
          if (!errorRecoveryMode) {
            extraneousError(expectedKind);
            errorRecoveryMode = false;
          }
          consume();
          consume();
          return true;
        }
        // Otherwise, perform single-token insertion if possible
        if (followingSet.contains(lookahead.getKind())) {
          LOGGER.info("Match: Performing single-token insertion");
          if (!errorRecoveryMode) {
            missingError(expectedKind);
            errorRecoveryMode = false;
          }
          previous = new Token(expectedKind, "<MISSING>", lookahead.getIndex(), lookahead.getLine(), lookahead.getColumn());
          return true;
        } else {
          LOGGER.info("Match: No action taken, synchronization required");
          // Do we fall back to sync-and-return here? If so, then we'll need a
          // FOLLOW set passed in. Or we can just leave that to a separate sync
          // method. I don't think we want to sync here because then we lose
          // the reference to the "previous" token that needs to be put into an
          // error node. So that means sync need to be external to this method.
          if (!errorRecoveryMode) {
            generalError(expectedKind);
            errorRecoveryMode = false;
          }
          return false;
        }
      }
      return false;
    }
  }

  // These error methods pertain to the match method. The specific error method
  // called depends on how an error was corrected. Single-token deletion
  // generates an "extraneous token" error, while single-token insertion
  // generates a "missing token" error. Otherwise, the "general error" is used.

  private void extraneousError (Token.Kind expectedKind) {
    var expectedKindString = reverseKeywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = reverseKeywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "expected " + expectedKindString + ", got extraneous " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error.complete());
  }

  private void missingError (Token.Kind expectedKind) {
    var expectedKindString = reverseKeywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = reverseKeywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "missing " + expectedKindString + ", got unexpected " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error.complete());
  }

  private void generalError (Token.Kind expectedKind) {
    var expectedKindString = reverseKeywordLookup.getOrDefault(expectedKind, friendlyKind(expectedKind));
    var actualKind = lookahead.getKind();
    var actualKindString = reverseKeywordLookup.getOrDefault(actualKind, friendlyKind(actualKind));
    var message = "expected " + expectedKindString + ", got " + actualKindString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error.complete());
  }

  // Confirm is similar to match, but it does not perform any error recovery
  // and does not return a result. Instead, it throws an exception. This can
  // only fail if there is a bug in the compiler.

  private void confirm (Token.Kind expectedKind) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("Confirm: Confirmed " + lookahead);
      consume();
    } else {
      var expectedKindFriendly = friendlyKind(expectedKind);
      var actualKindFriendly = friendlyKind(lookahead.getKind());
      var message = "expected " + expectedKindFriendly + ", got " + actualKindFriendly;
      throw new IllegalArgumentException("internal error: " + message);
    }
  }

  private void consume () {
    previous = lookahead;
    position.increment();
    lookahead = input.get(position.get());
  }

  public AstNode process () {
    buildReverseKeywordLookupTable();
    definePrimitiveTypes();
    LOGGER.info("*** Parsing started... ***");
    var node = translationUnit(EnumSet.of(Token.Kind.EOF));
    LOGGER.info("*** Parsing complete! ***");
    // Inspect builtin scope
//    var s = builtinScope.getSymbolTable().getData;
//    System.out.println(s);
    return node;
  }

  private void buildReverseKeywordLookupTable () {
    reverseKeywordLookup.put(Token.Kind.ABSTRACT, "abstract");
    reverseKeywordLookup.put(Token.Kind.AND, "and");
    reverseKeywordLookup.put(Token.Kind.AS, "as");
    reverseKeywordLookup.put(Token.Kind.BREAK, "break");
    reverseKeywordLookup.put(Token.Kind.CASE, "case");
    reverseKeywordLookup.put(Token.Kind.CAST, "cast");
    reverseKeywordLookup.put(Token.Kind.CATCH, "catch");
    reverseKeywordLookup.put(Token.Kind.CLASS, "class");
    reverseKeywordLookup.put(Token.Kind.CONST, "const");
    reverseKeywordLookup.put(Token.Kind.CONSTEVAL, "consteval");
    reverseKeywordLookup.put(Token.Kind.CONSTEXPR, "constexpr");
    reverseKeywordLookup.put(Token.Kind.CONTINUE, "continue");
    reverseKeywordLookup.put(Token.Kind.DEF, "def");
    reverseKeywordLookup.put(Token.Kind.DEFAULT, "default");
    reverseKeywordLookup.put(Token.Kind.DELETE, "delete");
    reverseKeywordLookup.put(Token.Kind.DIVINE, "divine");
    reverseKeywordLookup.put(Token.Kind.DO, "do");
    reverseKeywordLookup.put(Token.Kind.ELSE, "else");
    reverseKeywordLookup.put(Token.Kind.ENUM, "enum");
    reverseKeywordLookup.put(Token.Kind.EXTENDS, "extends");
    reverseKeywordLookup.put(Token.Kind.FALSE, "false");
    reverseKeywordLookup.put(Token.Kind.FINAL, "final");
    reverseKeywordLookup.put(Token.Kind.FOR, "for");
    reverseKeywordLookup.put(Token.Kind.FN, "fn");
    reverseKeywordLookup.put(Token.Kind.FUN, "fun");
    reverseKeywordLookup.put(Token.Kind.GOTO, "goto");
    reverseKeywordLookup.put(Token.Kind.IF, "if");
    reverseKeywordLookup.put(Token.Kind.IMPORT, "import");
    reverseKeywordLookup.put(Token.Kind.IN, "in");
    reverseKeywordLookup.put(Token.Kind.INCLUDE, "include");
    reverseKeywordLookup.put(Token.Kind.LOOP, "loop");
    reverseKeywordLookup.put(Token.Kind.NEW, "new");
    reverseKeywordLookup.put(Token.Kind.NIL, "nil");
    reverseKeywordLookup.put(Token.Kind.NOEXCEPT, "noexcept");
    reverseKeywordLookup.put(Token.Kind.NULL, "null");
    reverseKeywordLookup.put(Token.Kind.OR, "or");
    reverseKeywordLookup.put(Token.Kind.OVERRIDE, "override");
    reverseKeywordLookup.put(Token.Kind.PACKAGE, "package");
    reverseKeywordLookup.put(Token.Kind.PRIVATE, "private");
    reverseKeywordLookup.put(Token.Kind.PROTECTED, "protected");
    reverseKeywordLookup.put(Token.Kind.RETURN, "return");
    reverseKeywordLookup.put(Token.Kind.STATIC, "static");
    reverseKeywordLookup.put(Token.Kind.STRUCT, "struct");
    reverseKeywordLookup.put(Token.Kind.SWITCH, "switch");
    reverseKeywordLookup.put(Token.Kind.TEMPLATE, "template");
    reverseKeywordLookup.put(Token.Kind.THIS, "this");
    reverseKeywordLookup.put(Token.Kind.TRAIT, "trait");
    reverseKeywordLookup.put(Token.Kind.TRANSMUTE, "transmute");
    reverseKeywordLookup.put(Token.Kind.TRUE, "true");
    reverseKeywordLookup.put(Token.Kind.TRY, "try");
    reverseKeywordLookup.put(Token.Kind.TYPEALIAS, "typealias");
    reverseKeywordLookup.put(Token.Kind.UNION, "union");
    reverseKeywordLookup.put(Token.Kind.UNTIL, "until");
    reverseKeywordLookup.put(Token.Kind.USE, "use");
    reverseKeywordLookup.put(Token.Kind.VAL, "val");
    reverseKeywordLookup.put(Token.Kind.VAR, "var");
    reverseKeywordLookup.put(Token.Kind.VIRTUAL, "virtual");
    reverseKeywordLookup.put(Token.Kind.VOLATILE, "volatile");
    reverseKeywordLookup.put(Token.Kind.WHEN, "when");
    reverseKeywordLookup.put(Token.Kind.WHILE, "while");
    reverseKeywordLookup.put(Token.Kind.WITH, "with");
    reverseKeywordLookup.put(Token.Kind.SHORT, "short");
    reverseKeywordLookup.put(Token.Kind.INT, "int");
    reverseKeywordLookup.put(Token.Kind.LONG, "long");
    reverseKeywordLookup.put(Token.Kind.INT8, "int8");
    reverseKeywordLookup.put(Token.Kind.INT16, "int16");
    reverseKeywordLookup.put(Token.Kind.INT32, "int32");
    reverseKeywordLookup.put(Token.Kind.INT64, "int64");
    reverseKeywordLookup.put(Token.Kind.UINT, "uint");
    reverseKeywordLookup.put(Token.Kind.UINT8, "uint8");
    reverseKeywordLookup.put(Token.Kind.UINT16, "uint16");
    reverseKeywordLookup.put(Token.Kind.UINT32, "uint32");
    reverseKeywordLookup.put(Token.Kind.UINT64, "uint64");
    reverseKeywordLookup.put(Token.Kind.FLOAT, "float");
    reverseKeywordLookup.put(Token.Kind.DOUBLE, "double");
    reverseKeywordLookup.put(Token.Kind.FLOAT32, "float32");
    reverseKeywordLookup.put(Token.Kind.FLOAT64, "float64");
    reverseKeywordLookup.put(Token.Kind.VOID, "void");
    // Experimental, showing that this is a reverse token lookup, not
    // necessarily a reverse keyword lookup
    reverseKeywordLookup.put(Token.Kind.MINUS, "'-'");
    reverseKeywordLookup.put(Token.Kind.PLUS, "'+'");
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

  // Declarations following set is empty so we don't need to define one, let alone
  // push and pop it in the declarations production.

  private AstNode translationUnit (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new TranslationUnit();
    n.addChild(declarations());
    //var scope = new Scope(Scope.Kind.GLOBAL);
    //scope.setEnclosingScope(currentScope);
    //currentScope = scope;
    //n.setScope(currentScope);
    followingSetStack.pop();
    return n;
  }

  // DECLARATIONS **************************************************

  // Package, import, and use declarations must appear (in that order) before
  // any other declarations in the translation unit.

  // In The Definitive ANTLR4 Reference, Parr describes ANTLR's error recovery
  // in detail. His algorithm attempts phrase-level recovery through
  // single-token deletion or insertion, followed by context-informed
  // panic-mode recovery.

  // We might want to implement Damerau–Levenshtein distance algorithm to
  // auto-correct spellings (e.g. packge > package, esle > else). This can be
  // accomplished with the Apache Commons Text library or other means.

  // Package declaration is normally followed by import and use declarations,
  // but it doesn't have to be. It is possible for translation unit to contain
  // no import or use declarations.

  private AstNode declarations () {
    var n = new Declarations();
    var fsp = EnumSet.of(PRIVATE, VAL, VAR, DEF, CLASS, USE, IMPORT);
    var fsi = EnumSet.of(PRIVATE, VAL, VAR, DEF, CLASS, USE);
    var fsu = EnumSet.of(PRIVATE, VAL, VAR, DEF, CLASS);
    n.addChild(packageDeclaration(fsp));
    n.addChild(lookahead.getKind() == IMPORT ? importDeclarations(fsi) : EPSILON);
    n.addChild(lookahead.getKind() == USE ? useDeclarations(fsu) : EPSILON);
    if (FirstSet.OTHER_DECLARATIONS.contains(lookahead.getKind()))
      n.addChild(otherDeclarations());
    return n;
  }

  // The package declaration is special in that there is only one per
  // translation unit, and it must appear at the very top. A package is
  // basically a direct 1:1 translation to a C++ module and namespace of the
  // same name.

  private AstNode packageDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode n;
    if (match(PACKAGE, Token.Kind.IDENTIFIER)) {
      n = new PackageDeclaration(previous);
      var ss = EnumSet.of(PERIOD, SEMICOLON);
      n.addChild(packageName(ss));
      while (lookahead.getKind() == PERIOD) {
        confirm(PERIOD);
        n.addChild(packageName(ss));
      }
      match(SEMICOLON);
    } else {
      // Not sure it even makes sense to put a token in the error node
      n = new ErrorNode(lookahead);
      sync();
    }
    followingSetStack.pop();
    return n;
  }

  private AstNode packageName (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode n;
    if (match(Token.Kind.IDENTIFIER, SEMICOLON))
      n = new PackageName(previous);
    else {
      n = new ErrorNode(lookahead);
      sync();
    }
    followingSetStack.pop();
    return n;
  }

  private AstNode importDeclarations (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new ImportDeclarations();
    var ss = EnumSet.of(IMPORT);
    while (lookahead.getKind() == IMPORT)
      n.addChild(importDeclaration(ss));
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
    var n = new ImportDeclaration(previous);
    var ss1 = EnumSet.of(SEMICOLON, AS);
    n.addChild(importQualifiedName(ss1));
    var ss2 = EnumSet.of(SEMICOLON);
    n.addChild(lookahead.getKind() == AS ? importAsClause(ss2) : EPSILON);
    match(SEMICOLON);
    followingSetStack.pop();
    return n;
  }

  // This might be a candidate for error recovery of epsilon production.
  // Input "import opal-lang;" doesn't provide a great error message.

  // Qualified names are very similar to aggregation productions

  // To do: I don't think we want to do an identifier check here. This is
  // basically just an aggregation production.

  private AstNode importQualifiedName (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode n;
    if (lookahead.getKind() == Token.Kind.IDENTIFIER) {
      n = new ImportQualifiedName();
      var ss = EnumSet.of(PERIOD);
      n.addChild(importName(ss));
      while (lookahead.getKind() == PERIOD) {
        confirm(PERIOD);
        n.addChild(importName(ss));
      }
    } else {
      n = new ErrorNode(previous);
      sync();
    }
    followingSetStack.pop();
    return n;
  }

  private AstNode importName (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    AstNode n;
    if (match(Token.Kind.IDENTIFIER, EnumSet.of(AS, PERIOD, SEMICOLON)))
      n = new ImportName(previous);
    else {
      n = new ErrorNode(lookahead);
      sync();
    }
    followingSetStack.pop();
    return n;
  }

  private AstNode importAsClause (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    confirm(AS);
    var n = importAsName();
    followingSetStack.pop();
    return n;
  }

  private AstNode importAsName () {
    AstNode n;
    if (match(Token.Kind.IDENTIFIER, SEMICOLON))
      n = new ImportAsName(previous);
    else {
      n = new ErrorNode(lookahead);
      sync();
    }
    return n;
  }

  private AstNode useDeclarations (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    var n = new UseDeclarations();
    var ss = EnumSet.of(USE);
    while (lookahead.getKind() == USE)
      n.addChild(useDeclaration(ss));
    followingSetStack.pop();
    return n;
  }

  private AstNode useDeclaration (EnumSet<Token.Kind> followingSet) {
    followingSetStack.push(followingSet);
    confirm(USE);
    var n = new UseDeclaration(previous);
    n.addChild(useQualifiedName(FollowingSet.SEMICOLON));
    var ms = EnumSet.of(PRIVATE, VAL, VAR, DEF, CLASS, USE);
    if (!match(SEMICOLON, ms))
      sync();
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
    var p = useName(FollowerSet.PERIOD);
    n.addChild(p);
    match(PERIOD, FollowerSet.IDENTIFIER);
    // To do: We should be able to do single-token deletion here prior to
    // jumping into the next method since we know the next thing should be '*',
    // '{', or ID. See [Par12] pg. 164. We cannot do single-token insertion
    // however, because it would depending on knowing which alternative is
    // being taken, since some alternatives might have the same follower
    // tokens. (It might actually be possible in some limited situations, but
    // would be too much trouble to implement).
    p.addChild(useQualifiedNameTail());
    followingSetStack.pop();
    return n;
  }

  private AstNode useQualifiedNameTail () {
    AstNode n;
    var kind = lookahead.getKind();
    if (kind == ASTERISK)
      n = useNameWildcard();
    else if (kind == L_BRACE)
      n = useNameGroup();
    else if (kind == Token.Kind.IDENTIFIER) {
      n = useName();
      if (lookahead.getKind() == PERIOD) {
        confirm(PERIOD);
        n.addChild(useQualifiedNameTail());
      }
    } else {
      // Maybe print error sync message?
      n = new ErrorNode(lookahead);
      sync();
    }
    return n;
  }

  @Terminal
  private AstNode useNameWildcard () {
    confirm(ASTERISK);
    var n = new UseNameWildcard(previous);
    return n;
  }

  private AstNode useNameGroup () {
    confirm(L_BRACE);
    var n = new UseNameGroup(previous);
    n.addChild(useName(FollowerSet.COMMA_R_BRACE));
    while (lookahead.getKind() == COMMA) {
      confirm(COMMA);
      n.addChild(useName(FollowerSet.COMMA_R_BRACE));
    }
    // To do: Do we check and sync on punctuation?
    match(R_BRACE);
    return n;
  }

  // The @Terminal annotation doesn't do anything, but serves as an indicator
  // that this production wraps a terminal. The literature on recursive descent
  // parsing usually states that terminals do not need their own productions.
  // However, this doesn't take into account the need for AST construction and
  // especially error recovery code. Once those are added, wrapping certain
  // terminals (namely those that produce AST nodes) with a terminal production
  // helps to reduce code duplication.

  @Terminal
  private AstNode useName () {
    AstNode n;
    if (match(Token.Kind.IDENTIFIER))
      n = new UseName(previous);
    else {
      n = new ErrorNode(lookahead);
      sync();
    }
    return n;
  }

  @Terminal
  private AstNode useName (EnumSet<Token.Kind> matchSet) {
    AstNode n;
    if (match(Token.Kind.IDENTIFIER, matchSet))
      n = new UseName(previous);
    else {
      n = new ErrorNode(lookahead);
      sync();
    }
    return n;
  }

  // OTHER DECLARATIONS

  // To do: Other declarations needs to be optional

  private AstNode otherDeclarations () {
    var n = new OtherDeclarations();
    while (lookahead.getKind() != Token.Kind.EOF) {
      // Infinite loop, need to consume
      System.out.println("Sleeping for " + SLEEP_TIME + " seconds in declarations...");
      try {
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      n.addChild(otherDeclaration());
    }
    return n;
  }

  private static final EnumSet<Token.Kind> FIRST_OTHER_DECLARATION  = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR
  );
  private static final EnumSet<Token.Kind> FOLLOW_OTHER_DECLARATION = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR
  );

  private AstNode otherDeclaration () {
    System.out.println("OTHER DECL");
    checkIn(FIRST_OTHER_DECLARATION, FOLLOW_OTHER_DECLARATION);
    AstNode n = null;
    var spec = (lookahead.getKind() == Token.Kind.PRIVATE) ? exportSpecifier() : null;
    if (lookahead.getKind() == Token.Kind.TEMPLATE) {
//      n = templateDeclaration();
    } else {
      modifiers();
      n = switch (lookahead.getKind()) {
        case Token.Kind.CLASS -> classDeclaration(spec);
        case Token.Kind.TYPEALIAS -> typealiasDeclaration(spec);
        case Token.Kind.DEF -> routineDeclaration(spec);
        case Token.Kind.VAL, Token.Kind.VAR -> variableDeclaration(spec);
        default -> null;
      };
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
    AstNode n;
    var kind = lookahead.getKind();
    var spec = (kind == Token.Kind.PRIVATE || kind == Token.Kind.PROTECTED) ? memberAccessSpecifier() : null;
    memberModifiers();
    n = switch (lookahead.getKind()) {
      case Token.Kind.TYPEALIAS -> memberTypealiasDeclaration(spec);
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
      modifier();
      kind = lookahead.getKind();
    }
  }

  private AstNode memberTypealiasDeclaration (AstNode accessSpecifier) {
    var n = new MemberTypealiasDeclaration(lookahead);
    match(Token.Kind.TYPEALIAS);
    n.addChild(accessSpecifier);
    n.addChild(typealiasName());
    match(Token.Kind.EQUAL);
    n.addChild(type());
    match(SEMICOLON);
    return n;
  }

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

  private AstNode memberVariableDeclaration (MemberAccessSpecifier accessSpecifier) {
    var n = new MemberVariableDeclaration(lookahead);
    match(Token.Kind.VAR);
    n.addChild(accessSpecifier);
    n.addChild(variableModifiers());
    n.addChild(variableName());
    n.addChild((lookahead.getKind() == Token.Kind.COLON) ? variableTypeSpecifier() : null);
    n.addChild((lookahead.getKind() == Token.Kind.EQUAL) ? variableInitializer() : null);
    match(SEMICOLON);
    return n;
  }

  // TYPEALIAS DECLARATION

  private AstNode typealiasDeclaration (AstNode exportSpecifier) {
    var n = new TypealiasDeclaration(lookahead);
    match(Token.Kind.TYPEALIAS);
    n.addChild(exportSpecifier);
    n.addChild(typealiasName());
    match(Token.Kind.EQUAL);
    n.addChild(type());
    match(SEMICOLON);
    return n;
  }

  private AstNode typealiasName () {
    var n = new TypealiasName(lookahead);
    match(Token.Kind.IDENTIFIER);
    return n;
  }

  private AstNode localTypealiasDeclaration () {
    var n = new LocalTypealiasDeclaration(lookahead);
    match(Token.Kind.TYPEALIAS);
    n.addChild(typealiasName());
    match(Token.Kind.EQUAL);
    n.addChild(type());
    match(SEMICOLON);
    return n;
  }

  // ROUTINE DECLARATIONS

  // Todo: We need to push another scope onto the scope stack. Keep in mind that
  // the routine parameters may be in the same exact scope as the routine body
  // (or top-most block of the routine).

  // For now, there are no local routines, so no need to distinguish between
  // global and local routines.

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
    n.addChild(type());
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
    match(SEMICOLON);
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
    n.addChild(type());
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
    match(SEMICOLON);
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
      kind == SEMICOLON ||
      kind == Token.Kind.IF        ||
      kind == Token.Kind.RETURN    ||
      kind == Token.Kind.UNTIL     ||
      kind == Token.Kind.WHILE
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
    var kind = lookahead.getKind();
    while (
      kind == Token.Kind.EQUAL ||
      kind == Token.Kind.ASTERISK_EQUAL ||
      kind == Token.Kind.SLASH_EQUAL ||
      kind == Token.Kind.PERCENT_EQUAL ||
      kind == Token.Kind.PLUS_EQUAL ||
      kind == Token.Kind.MINUS_EQUAL ||
      kind == Token.Kind.LESS_LESS_EQUAL ||
      kind == Token.Kind.GREATER_GREATER_EQUAL ||
      kind == Token.Kind.AMPERSAND_EQUAL ||
      kind == Token.Kind.CARET_EQUAL ||
      kind == Token.Kind.BAR_EQUAL
    ) {
      var p = n;
      n = new BinaryExpression(lookahead);
      n.addChild(p);
      match(lookahead.getKind());
      p = logicalOrExpression();
      n.addChild(p);
      kind = lookahead.getKind();
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

  // C++ formulation might be slightly different with mutual recursion between
  // unaryExpression and castExpression methods. What effect might that have?
  // (See p. 54, Ellis & Stroustrup, 1990.)

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
    else if (kind == Token.Kind.DELETE) {
      n = deleteExpression();
    }
    else if (kind == Token.Kind.NEW) {
      n = newExpression();
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
