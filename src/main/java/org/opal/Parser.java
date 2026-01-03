package org.opal;

import java.util.*;

import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.opal.ast.*;
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
  @Deprecated
  private Token mark2 = new Token(Token.Kind.IDENTIFIER, "DEPRECATED_MARK", 1, 1, 1);

  private final List<String> sourceLines;

  // Used to pass type nodes up and down during tree traversal
  //private final LinkedList<DirectDeclarator> stack;

  // Used to pass nodes up and down during tree traversal
  private final LinkedList<AstNode> nodeStack;

  // Used to collect modifier nodes in preparation for aggregation into
  // specialized modifiers nodes.
  private final LinkedList<Modifier> modifierStack;

  // Used for symbol table operations. Cobalt requires a symbol table during
  // parsing in order to disambiguate a few grammar rules. We cannot wait until
  // the semantic analysis phase to begin constructing symbol tables.
  // NOTE: We will probably just use a separate type table for parsing purposes
  // and then discard the information. So the full symbol table won't exist until
  // semantic analysis phases.
//  private final Scope builtinScope;
//  private Scope currentScope;

  // Reverse mapping from token-kind to string
  private final HashMap<Token.Kind, String> reverseLookup;

  // Note: Leave this disabled for now so the error recovery code can be built
  // with the most raw output to make it easier to understand behavior and
  // troubleshoot.

  // Tracks whether error recovery mode is enabled to avoid cascading error
  // messages. See [Par12] Sec. 9.3 for details.
  boolean errorRecoveryMode = false;

  // Used to keep track of whether or not we are in a sub-expression for better
  // error message.
  private boolean inSubExpression = false;

  // Todo: we may also need a 'null_t' type, for which there is exactly one
  // value, which is 'null'. This is to match the C++ 'nullptr_t' type and its
  // corresponding single 'nullptr' value. I am not sure if this is a primitive
  // type or not. Needs research.

  // Todo: We may decide that 'int', 'short', 'float', etc. should just be
  // typealiases for the various fixed size types.

  private static final Logger LOGGER = LogManager.getLogger();

  // Represents epsilon productions
  private static final AstNode EPSILON = null;

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

  private static final EnumSet<Token.Kind> SYNC_DECL = EnumSet.of(SEMICOLON, R_BRACE, Token.Kind.EOF);
  private static final EnumSet<Token.Kind> SYNC_STMT = EnumSet.of(SEMICOLON, R_BRACE, Token.Kind.EOF);



  public Parser (LinkedList<Token> input, List<String> sourceLines) {
    this.input = input;
    position = new Counter();
    lookahead = input.get(position.get());
    kind = lookahead.getKind();
    this.sourceLines = sourceLines;
    //stack = new LinkedList<>();
    nodeStack = new LinkedList<>();
    modifierStack = new LinkedList<>();

    var lookupTable = new LookupTable();
    reverseLookup = lookupTable.getReverseLookupTable();

    // Set up logging
    var level = Level.INFO;
    Configurator.setRootLevel(level);
  }

  // We might need multiple versions of check-in and check-out because the
  // error messages may differ.

  private void checkIn (EnumSet<Token.Kind> firstSet, EnumSet<Token.Kind> followSet, Token.Kind expectedKind) {
    LOGGER.info("check-in started");
    if (!firstSet.contains(kind)) {
      panic(expectedKind);
      recover(union(firstSet, followSet));
    }
    LOGGER.info("check-in complete");
  }

  // Check-out only occurs if we are in error recovery mode (i.e. a panic
  // occurred).

  private void checkOut (EnumSet<Token.Kind> followSet, String expectedKindString) {
    LOGGER.info("check-out started");
    if (!followSet.contains(kind) && errorRecoveryMode) {
      recover(followSet);
      cleanup();
    }
    LOGGER.info("check-out complete");
  }

  private static final EnumSet<Token.Kind> SYNC_GLOBAL = EnumSet.of(SEMICOLON, R_BRACE, Token.Kind.EOF);

  // Why can't we exit error recovery mode once recover is complete? I think we
  // should be able to. Do we need to wait for cleanup() to run?

  private void recover (EnumSet<Token.Kind> recoverSet) {
    LOGGER.info("recovery started");
    var syncSet = union(recoverSet, SYNC_GLOBAL);
    while (!syncSet.contains(kind)) {
      LOGGER.info("skipped {}", lookahead);
      consume();
    }
    errorRecoveryMode = false;
    LOGGER.info("recovery complete");
  }

  // When can we clear error recovery mode? I believe it is on match, confirm,
  // and cleanup. Note: For this reason, we probably cannot just consume on
  // entering an if body, but should confirm instead.

  // Update: I don't think we can clear error recovery mode on match. This can
  // lead to pre-mature exit from error recovery mode.

  // Update: Can we exit error recovery mode once recovery has run? If so, we
  // don't need to exit ERM from cleanup.

  private void cleanup () {
    if (kind == SEMICOLON || kind == R_BRACE) {
      LOGGER.info("cleaned {}", lookahead);
      consume();
    }
  }

  // I don't think we want to reset error recovery mode on match. This should
  // only be done after some explicit acknowledgement that error recovery is
  // complete.

  private Token match (Token.Kind expectedKind) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("matched " + lookahead);
      var mark = lookahead;
      consume();
      return mark;
    } else {
      LOGGER.info("mis-matched " + lookahead);
      lookahead.setError();
      if (!errorRecoveryMode)
        matchError(expectedKind);
      // Should we at least advance the input stream? If we do, then we
      // effectively delete the bad token. Different sources say yes or no,
      // but several seem to indicate that we should NOT consume.
      errorRecoveryMode = true;
      return lookahead;
    }
  }

  private void matchError (Token.Kind expectedKind) {
    var expectedKindString = reverseLookup.get(expectedKind);
    var expectedString =
      expectedKindString == null ? friendlyKind(expectedKind) : quote(expectedKindString);
    var foundString =
      kind == Token.Kind.IDENTIFIER ? quote(lookahead.getLexeme()) : quote(reverseLookup.get(kind));
    var message = "expected " + expectedString + ", but found " + foundString;
    var error = new SyntaxError(sourceLines, message, lookahead);
    System.out.println(error);
  }

  private String friendlyKind (Token.Kind kind) {
    return kind.toString().toLowerCase().replace("_", " ");
  }

  // Confirm is similar to match, but it throws an exception instead of
  // printing an error message and triggering error recovery. This can only
  // fail if there is a bug in the compiler.

  private Token confirm (Token.Kind expectedKind) {
    if (lookahead.getKind() == expectedKind) {
      LOGGER.info("confirmed " + lookahead);
      var mark = lookahead;
      consume();
      return mark;
    } else {
      var expectedKindFriendly = friendlyKind(expectedKind);
      var actualKindFriendly = friendlyKind(lookahead.getKind());
      var expectedMessage = "expected " + expectedKindFriendly;
      var foundMessage = ", but found " + actualKindFriendly;
      var message = expectedMessage + foundMessage;
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

  // These should probably just be called "error" or something like that

  private void panic (Token.Kind expectedKind) {
    var expectedString = quote(reverseLookup.get(expectedKind));
    panic(expectedString);
  }

  private void panic (Token.Kind expectedKind1, Token.Kind expectedKind2) {
    var expectedString =
      quote(reverseLookup.get(expectedKind1)) + " or " +
      quote(reverseLookup.get(expectedKind2));
    panic(expectedString);
  }

  private void panic (Token.Kind expectedKind1, Token.Kind expectedKind2, Token.Kind expectedKind3) {
    var expectedString =
      quote(reverseLookup.get(expectedKind1)) + ", " +
      quote(reverseLookup.get(expectedKind2)) + ", or " +
      quote(reverseLookup.get(expectedKind3));
    panic(expectedString);
  }

  private void panic (Token.Kind... expectedKinds) {
    var first  = quote(reverseLookup.get(expectedKinds[0]));
    var last   = quote(reverseLookup.get(expectedKinds[expectedKinds.length-1]));
    var middle = new StringBuilder();
    for (var i=1; i<expectedKinds.length-1; i++)
      middle.append(", ").append(quote(reverseLookup.get(expectedKinds[i])));
    middle.append(", or ");
    var expectedString = first + middle + last;
    panic(expectedString);
  }

  private void panic (String expectedString) {
    LOGGER.info("panic triggered");
    if (!errorRecoveryMode) {
      var foundString =
        kind == Token.Kind.IDENTIFIER ? quote(lookahead.getLexeme()) : quote(reverseLookup.get(kind));
      var message = "expected " + expectedString + ", but found " + foundString;
      var error = new SyntaxError(sourceLines, message, lookahead);
      System.out.println("GOT HERE IN PANIC");
      System.out.println(error);
    }
    errorRecoveryMode = true;
  }

  private String quote (String input) {
    return "'" + input + "'";
  }

  public AstNode process () {
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

  // TRANSLATION UNIT *********************************************************

  // In parsing theory lingo, the top-most production is known as the "start
  // symbol". Thus, the translation unit is our start symbol.

  // Maybe we need a check-in here

  // TBD: Are check-ins in the middle of a production proper? I think the
  // purpose of these is to ensure that error message say, "expected X or Y",
  // not just "expected Y". Should we just be looking at follow sets?

  private AstNode translationUnit () {
    var n = new TranslationUnit();
    n.setPackageDeclaration(packageDeclaration());
    n.setImportDeclarations(importDeclarations());
    n.setUseDeclarations(useDeclarations());
    n.setOtherDeclarations(otherDeclarations());

    //var scope = new Scope(Scope.Kind.GLOBAL);
    //scope.setEnclosingScope(currentScope);
    //currentScope = scope;
    //n.setScope(currentScope);
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

  private PackageDeclaration packageDeclaration () {
    checkIn(FirstSet.PACKAGE_DECLARATION, FollowSet.PACKAGE_DECLARATION, PACKAGE);
    if (kind == PACKAGE) {
      var token = confirm(PACKAGE);
      var node = new PackageDeclaration(token);
      node.setPackageName(packageName());
      match(SEMICOLON);
      checkOut(FollowSet.PACKAGE_DECLARATION, "'import', 'use', or start of other declaration");
      return node;
    }
    cleanup();
    return null;
  }

  // To do: Support qualified names for packages

  private PackageName packageName () {
    var token = match(Token.Kind.IDENTIFIER);
    return new PackageName(token);
  }

  private ImportDeclarations importDeclarations () {
    var n = new ImportDeclarations();
    while (!FollowSet.IMPORT_DECLARATIONS.contains(kind)) {
      if (kind == IMPORT)
        n.addImportDeclaration(importDeclaration());
      else {
        panic("'import', 'use', or start of other declaration");
        recover(union(FirstSet.IMPORT_DECLARATION, FollowSet.IMPORT_DECLARATIONS));
        cleanup();
      }
    }
    return n;
  }

  // We could implement this several ways. First, we could use a binary tree
  // with dots as internal nodes and names as leaf nodes. Second, we could
  // simply have a chain of names, with each child names being under its
  // respective parent. Lastly, we can have a list of names under the import
  // declaration. We choose to go with the latter case because that is the
  // easiest implementation and the others hold no advantages for our
  // particular use case.

  // We don't need a check-in for importDeclaration because it is optional, so
  // the check-in logic is in the caller. If we enter this production, then we
  // already know that the current token is in the FIRST set.

  private ImportDeclaration importDeclaration () {
    var token = confirm(IMPORT);
    var node = new ImportDeclaration(token);
    node.setQualifiedName(importQualifiedName());
    if (kind != SEMICOLON) {
      if (kind == AS)
        node.setAsName(importAsClause());
      else
        panic(AS, SEMICOLON);
    }
    match(SEMICOLON);
    checkOut(FollowSet.IMPORT_DECLARATION, "'import', 'use', or start of other declaration");
    return node;
  }

  private ImportQualifiedName importQualifiedName () {
    var node = new ImportQualifiedName();
    node.addImportName(importName());
    while (kind != AS && kind != SEMICOLON) {
      if (kind == PERIOD) {
        confirm(PERIOD);
        node.addImportName(importName());
      } else {
        panic(PERIOD, AS, SEMICOLON);
        break;
      }
    }
    return node;
  }

  private ImportName importName () {
    var token = match(Token.Kind.IDENTIFIER);
    return new ImportName(token);
  }

  private ImportAsName importAsClause () {
    confirm(AS);
    var token = match(Token.Kind.IDENTIFIER);
    return new ImportAsName(token);
  }

  private UseDeclarations useDeclarations () {
    var n = new UseDeclarations();
    while (!FollowSet.USE_DECLARATIONS.contains(kind)) {
      if (kind == USE)
        n.addUseDeclaration(useDeclaration());
      else {
        panic("'use' or start of other declaration");
        recover(union(FirstSet.USE_DECLARATION, FollowSet.USE_DECLARATIONS));
        cleanup();
      }
    }
    return n;
  }

  // We don't need a check-in for useDeclaration because it is optional, so the
  // check-in logic is in the caller. If we enter this production, then we
  // already know that the current token is in the FIRST set.

  private UseDeclaration useDeclaration () {
    var token = confirm(USE);
    var node = new UseDeclaration(token);
    node.setQualifiedName(useQualifiedName());
    match(SEMICOLON);
    checkOut(FollowSet.USE_DECLARATION, "'use' or start of other declaration");
    return node;
  }

  private UseQualifiedName useQualifiedName () {
    UseQualifiedName n = new UseQualifiedName();
    var p = useName();
    n.setUseName(p);
    match(PERIOD);
    p.setChild(useQualifiedNameTail());
    return n;
  }

  private AstNode useQualifiedNameTail () {
    if (kind == ASTERISK) {
      return useNameWildcard();
    } else if (kind == L_BRACE) {
      return useNameGroup();
    } else if (kind == Token.Kind.IDENTIFIER) {
      var n = useName();
      if (kind == PERIOD) {
        confirm(PERIOD);
        n.setChild(useQualifiedNameTail());
      }
      return n;
    } else {
      // For now, assume a use name was intended. Perhaps later we can try
      // phrase-level recovery to ascertain the intent more accurately.
      panic("identifier, '{', or '*'");
      return new UseName(lookahead);
    }
  }

  private UseNameWildcard useNameWildcard () {
    var token = confirm(ASTERISK);
    return new UseNameWildcard(token);
  }

  private AstNode useNameGroup () {
    confirm(L_BRACE);
    var n = new UseNameGroup();
    n.addUseName(useName());
    while (kind != R_BRACE) {
      if (kind == COMMA) {
        confirm(COMMA);
        n.addUseName(useName());
      } else {
        panic(COMMA, R_BRACE);
        break;
      }
    }
    match(R_BRACE);
    return n;
  }

  private UseName useName () {
    var token = match(Token.Kind.IDENTIFIER);
    return new UseName(token);
  }

  // OTHER DECLARATIONS

  private OtherDeclarations otherDeclarations () {
    var n = new OtherDeclarations();
    while (!FollowSet.OTHER_DECLARATIONS.contains(kind)) {
      if (
        kind == PRIVATE ||
        kind == CONST   ||
        kind == CLASS   ||
        kind == DEF     ||
        kind == VAL     ||
        kind == VAR
      ) {
        n.addOtherDeclaration(otherDeclaration());
      } else {
        panic("start of other declaration");
        recover(union(FirstSet.OTHER_DECLARATION, FollowSet.OTHER_DECLARATIONS));
        cleanup();
      }
    }
    return n;
  }

  // We don't need a check-in for otherDeclaration because it is optional, so
  // the check-in logic is in the caller. If we enter this production, then we
  // already know that the current token is in the FIRST set.

  // Entities may be declared as private, indicating that they are not
  // exported. Otherwise, they are considered public, i.e. exported.

  private AstNode otherDeclaration () {
    ExportSpecifier p;
    if (kind == PRIVATE) {
      var token = confirm(PRIVATE);
      p = new ExportSpecifier(token);
    } else {
      // Maybe change to EPSILON later
      p = null;
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
        // Probably should panic with custom "expected start of declaration"
        // message
        panic(CLASS, TYPEALIAS, DEF, VAL, VAR);
        modifierStack.clear();
      }
    }
    checkOut(FollowSet.OTHER_DECLARATION, "start of other declaration");
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
      var token = confirm(kind);
      modifierStack.push(new Modifier(token));
    }
  }

  // CLASS DECLARATIONS

  private AstNode classDeclaration (AstNode exportSpecifier) {
    var token = confirm(CLASS);
    var n = new ClassDeclaration(token);
    n.addChild(exportSpecifier);
    n.addChild(classModifiers());
    n.addChild(className());
    if (kind == EXTENDS)
      n.addChild(baseClasses(EnumSet.of(L_BRACE)));
    else
      n.addChild(EPSILON);
    n.addChild(classBody());
    return n;
  }

  private ClassName className () {
    var token = match(Token.Kind.IDENTIFIER);
    return new ClassName(token);
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
    confirm(EXTENDS);
    var n = new BaseClasses();
    var token = match(Token.Kind.IDENTIFIER);
    n.addChild(new BaseClass(token));
    while (kind == COMMA) {
      confirm(COMMA);
      token = match(Token.Kind.IDENTIFIER);
      n.addChild(new BaseClass(token));
    }
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
    AstNode accessSpecifier;
    if (kind == PRIVATE) {
      confirm(PRIVATE);
      accessSpecifier = new MemberAccessSpecifier(mark2);
    } else if (kind == PROTECTED) {
      confirm(PROTECTED);
      accessSpecifier = new MemberAccessSpecifier(mark2);
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
      var token = confirm(kind);
      modifierStack.push(new Modifier(token));
    }
  }

  // What if there are modifiers on typealias? Is that a syntax error or
  // semantic error?

  private AstNode memberTypealiasDeclaration (AstNode accessSpecifier) {
    confirm(TYPEALIAS);
    var n = new MemberTypealiasDeclaration(mark2);
    n.addChild(accessSpecifier);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new TypealiasName(mark2));
    match(EQUAL);
    n.addChild(declarator(Parser.Context.TYPEALIAS_DECLARATION));
    match(SEMICOLON);
    return n;
  }

  private AstNode memberRoutineDeclaration (AstNode accessSpecifier) {
    confirm(DEF);
    var n = new MemberRoutineDeclaration(mark2);
    n.addChild(accessSpecifier);
    n.addChild(memberRoutineModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new RoutineName(mark2));
    n.addChild(routineParameters());
    // No following set required here because these are completely optional
    n.addChild(cvQualifiers());
    if (kind == AMPERSAND) {
      confirm(AMPERSAND);
      n.addChild(new RefQualifier(mark2));
    } else if (kind == AMPERSAND_AMPERSAND) {
      confirm(AMPERSAND_AMPERSAND);
      n.addChild(new RefQualifier(mark2));
    } else {
      n.addChild(EPSILON);
    }
    if (kind == NOEXCEPT) {
      confirm(NOEXCEPT);
      n.addChild(new NoexceptSpecifier(mark2));
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
      n.addChild(new CVQualifier(mark2));
      if (kind == VOLATILE) {
        confirm(VOLATILE);
        n.addChild(new CVQualifier(mark2));
      }
    } else if (kind == VOLATILE) {
      confirm(VOLATILE);
      n.addChild(new CVQualifier(mark2));
      if (kind == CONST) {
        confirm(CONST);
        n.addChild(new CVQualifier(mark2));
      }
    }
    return n;
  }

  private AstNode memberVariableDeclaration (AstNode accessSpecifier) {
    confirm(kind == VAL ? VAL : VAR);
    var n = new MemberVariableDeclaration(mark2);
    n.addChild(accessSpecifier);
    n.addChild(variableModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new VariableName(mark2));
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
    var n = new TypealiasDeclaration(mark2);
    n.addChild(exportSpecifier);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new TypealiasName(mark2));
    match(EQUAL);
    n.addChild(declarator(Parser.Context.TYPEALIAS_DECLARATION));
    match(SEMICOLON);
    return n;
  }

  private AstNode localTypealiasDeclaration () {
    confirm(TYPEALIAS);
    var n = new LocalTypealiasDeclaration(lookahead);
    match(Token.Kind.IDENTIFIER);
    n.addChild(new TypealiasName(mark2));
    match(EQUAL);
    n.addChild(declarator(Parser.Context.TYPEALIAS_DECLARATION));
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

  private AstNode routineDeclaration (ExportSpecifier exportSpecifier) {
    var token = confirm(DEF);
    var n = new RoutineDeclaration(token);
    n.setExportSpecifier(exportSpecifier);
    n.setModifiers(routineModifiers());
    n.setName(routineName());
    n.addChild(routineParameters());
    if (kind == NOEXCEPT) {
      confirm(NOEXCEPT);
      n.addChild(new NoexceptSpecifier(mark2));
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

  private RoutineName routineName () {
    var token = match(Token.Kind.IDENTIFIER);
    return new RoutineName(token);
  }

  private RoutineModifiers routineModifiers () {
    var n = new RoutineModifiers();
    while (!modifierStack.isEmpty())
      n.addModifier(modifierStack.pop());
    return n;
  }

  private AstNode routineParameters () {
    // To do: Add in parameter modifiers as required
    match(L_PARENTHESIS);
    var n = new RoutineParameters();
    if (kind == Token.Kind.IDENTIFIER)
      n.addChild(routineParameter());
    while (kind == COMMA) {
      confirm(COMMA);
      n.addChild(routineParameter());
    }
    match(R_PARENTHESIS);
    return n;
  }

  // Routine parameters are for all intents and purposes local variables

  private AstNode routineParameter () {
    var n = new RoutineParameter();
    match(Token.Kind.IDENTIFIER);
    n.addChild(new RoutineParameterName(mark2));
    n.addChild(routineParameterTypeSpecifier());
    return n;
  }

  private AstNode routineParameterTypeSpecifier () {
    match(COLON);
    var n = new RoutineParameterTypeSpecifier(mark2);
    n.addChild(declarator(Parser.Context.ROUTINE_PARAMETER_TYPE_SPECIFIER));
    return n;
  }

  // Some languages us a colon for the return type, while others use an arrow.
  // Opal uses an arrow. Apart from the fact that and Opal is a C++ derivative,
  // (which uses an arrow), the arrow stands out more when there are CV and ref
  // qualifiers.

  // We can either treat this like a type specifier or use it as a passthrough
  // to a type specifier.

  private AstNode routineReturnTypeSpecifier () {
    confirm(MINUS_GREATER);
    var n = new RoutineReturnTypeSpecifier();
    n.addChild(declarator(Context.ROUTINE_RETURN_TYPE_SPECIFIER));
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

  // Check-out?

  private AstNode variableDeclaration (ExportSpecifier exportSpecifier) {
    var token = confirm(kind == VAL ? VAL : VAR);
    var n = new VariableDeclaration(token);
    n.setExportSpecifier(exportSpecifier);
    n.setModifiers(variableModifiers());
    n.setName(variableName());
    if (kind != SEMICOLON) {
      if (kind == COLON) {
        n.setTypeSpecifier(variableTypeSpecifier());
        if (kind == EQUAL)
          n.setInitializer(variableInitializer());
      } else if (kind == EQUAL) {
        n.setInitializer(variableInitializer());
      } else {
        panic(COLON, EQUAL, SEMICOLON);
      }
    }
    match(SEMICOLON);
    return n;
  }

  private VariableName variableName () {
    var token = match(Token.Kind.IDENTIFIER);
    return new VariableName(token);
  }

  private VariableModifiers variableModifiers () {
    var n = new VariableModifiers();
    while (!modifierStack.isEmpty())
      n.addModifier(modifierStack.pop());
    return n;
  }

  private VariableTypeSpecifier variableTypeSpecifier () {
    confirm(COLON);
    var n = new VariableTypeSpecifier();
    n.setDeclarator(declarator(Parser.Context.VARIABLE_TYPE_SPECIFIER));
    return n;
  }

  private VariableInitializer variableInitializer () {
    confirm(EQUAL);
    var n = new VariableInitializer();
    n.setExpression(expression(true));
    return n;
  }

  private AstNode localVariableDeclaration () {
    confirm(kind == VAL ? VAL : VAR);
    var n = new LocalVariableDeclaration(mark2);
    n.addChild(variableModifiers());
    match(Token.Kind.IDENTIFIER);
    n.addChild(new VariableName(mark2));
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
    var n = new BreakStatement(mark2);
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
  // root expression node or not. UPDATE: I think I want to stop using an
  // explicit root expression node.

  // Top-level categories such as declaration, statement, expression tend to
  // have large FIRST sets. In this case, it is ok to use the "contains"
  // method. Otherwise, we prefer to use chains of "if" statements.

  private Expression expression (Parser.Context context) {
    Expression n;
    if (FirstSet.EXPRESSION.contains(kind)) {
      n = assignmentExpression();
    } else {
      // Yes, we need this! If the sync never hits anything in the first set
      // then we need to return a bogus expression!
      n = new BogusExpression(mark2);
    }
    inSubExpression = false;
    return n;
  }

  private Expression subExpression () {
    return assignmentExpression();
  }

  @Deprecated
  private Expression expression (boolean root) {
    var n = assignmentExpression();
    if (root) {
      var p = new Expression();
      p.setSubExpression(n);
      n = p;
    }
    return n;
  }

  // We may wish to add a 'walrus' operator (:=), which can be used inside a
  // conditional statement to indicate that the developer truly intends to have
  // an assignment rather than an equality check.

  private Expression assignmentExpression () {
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
      var token = confirm(kind);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(logicalOrExpression());
      n = p;
    }
    return n;
  }

  private Expression logicalOrExpression () {
    var n = logicalAndExpression();
    while (kind == OR) {
      var token = confirm(OR);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(logicalAndExpression());
      n = p;
    }
    return n;
  }

  private Expression logicalAndExpression () {
    var n = inclusiveOrExpression();
    while (kind == AND) {
      var token = confirm(AND);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(inclusiveOrExpression());
      n = p;
    }
    return n;
  }

  private Expression inclusiveOrExpression () {
    var n = exclusiveOrExpression();
    while (kind == BAR) {
      var token = confirm(BAR);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(exclusiveOrExpression());
      n = p;
    }
    return n;
  }

  private Expression exclusiveOrExpression () {
    var n = andExpression();
    while (kind == CARET) {
      var token = confirm(CARET);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(andExpression());
      n = p;
    }
    return n;
  }

  private Expression andExpression () {
    var n = equalityExpression();
    while (kind == AMPERSAND) {
      var token = confirm(AMPERSAND);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(equalityExpression());
      n = p;
    }
    return n;
  }

  private Expression equalityExpression () {
    var n = relationalExpression();
    while (kind == EQUAL_EQUAL || kind == EXCLAMATION_EQUAL) {
      var token = confirm(kind);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(relationalExpression());
      n = p;
    }
    return n;
  }

  private Expression relationalExpression () {
    var n = shiftExpression();
    while (kind == GREATER || kind == LESS || kind == GREATER_EQUAL || kind == LESS_EQUAL) {
      var token = confirm(kind);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(shiftExpression());
      n = p;
    }
    return n;
  }

  private Expression shiftExpression () {
    var n = additiveExpression();
    while (kind == GREATER_GREATER || kind == LESS_LESS) {
      var token = confirm(kind);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(additiveExpression());
      n = p;
    }
    return n;
  }

  private Expression additiveExpression () {
    var n = multiplicativeExpression();
    while (kind == PLUS || kind == MINUS) {
      var token = confirm(kind);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(multiplicativeExpression());
      n = p;
    }
    return n;
  }

  private Expression multiplicativeExpression () {
    var n = unaryExpression();
    inSubExpression = true;
    while (kind == ASTERISK || kind == SLASH || kind == PERCENT) {
      var token = confirm(kind);
      var p = new BinaryExpression(token);
      p.setLeft(n);
      p.setRight(unaryExpression());
      n = p;
    }
    return n;
  }

  // C++ formulation might be slightly different with mutual recursion between
  // unaryExpression and castExpression methods. What effect might that have?
  // (See p. 54, Ellis & Stroustrup, 1990.)

  // I forgot why we are setting a subExpression flag. What is the reason?

  private Expression unaryExpression () {
    Expression n = null;
    if (kind == ASTERISK || kind == MINUS || kind == PLUS || kind == EXCLAMATION || kind == TILDE) {
      var token = confirm(kind);
      n = new UnaryExpression(token);
      n.setSubExpression(unaryExpression());
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

  private Expression castExpression () {
    var n = new CastExpression(lookahead);
    match(lookahead.getKind());
    match(Token.Kind.LESS);
    n.addChild(declarator(Parser.Context.CAST_EXPRESSION));
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

  private Expression deleteExpression () {
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

  private Expression newExpression () {
    var n = new NewExpression(lookahead);
    match(Token.Kind.NEW);
    n.addChild(kind == Token.Kind.L_BRACKET ? newPlacement() : null);
    n.addChild(declarator(Parser.Context.NEW_EXPRESSION));
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

  private Expression postfixExpression () {
    var node = primaryExpression();
    /*
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
    */
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

  private Expression dereferencingMemberAccess (AstNode nameExpr) {
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

  private Expression primaryExpression () {
    Expression n = null;
    if (kind == Token.Kind.THIS) {
      n = this_();
    } else if (kind == Token.Kind.IDENTIFIER) {
      n = name();
    } else if (kind == Token.Kind.L_PARENTHESIS) {
      n = parenthesizedExpression();
    } else {
      n = literal();
    }
    return n;
  }

  private Expression literal () {
    Expression n;
    if (kind == FALSE)
      n = booleanLiteral();
    else if (kind == TRUE)
      n = booleanLiteral();
    else if (kind == CHARACTER_LITERAL)
      n = characterLiteral();
    else if (kind == FLOAT32_LITERAL)
      n = floatingPointLiteral();
    else if (kind == FLOAT64_LITERAL)
      n = floatingPointLiteral();
    else if (kind == INT32_LITERAL)
      n = integerLiteral();
    else if (kind == INT64_LITERAL)
      n = integerLiteral();
    else if (kind == NULL)
      n = nullLiteral();
    else if (kind == STRING_LITERAL)
      n = stringLiteral();
    else if (kind == UINT32_LITERAL)
      n = unsignedIntegerLiteral();
    else if (kind == UINT64_LITERAL)
      n = unsignedIntegerLiteral();
    else {
      if (inSubExpression)
        panic("start of sub-expression");
      else
        panic("start of expression");
      n = null;
    }
    return n;
  }

  private BooleanLiteral booleanLiteral () {
    var token = confirm(kind);
    return new BooleanLiteral(token);
  }

  private CharacterLiteral characterLiteral () {
    var token = confirm(CHARACTER_LITERAL);
    return new CharacterLiteral(token);
  }

  private FloatingPointLiteral floatingPointLiteral () {
    var token = confirm(kind);
    return new FloatingPointLiteral(token);
  }

  private IntegerLiteral integerLiteral () {
    var token = confirm(kind);
    return new IntegerLiteral(token);
  }

  private NullLiteral nullLiteral () {
    var token = confirm(NULL);
    return new NullLiteral(token);
  }

  private StringLiteral stringLiteral () {
    var token = confirm(STRING_LITERAL);
    return new StringLiteral(token);
  }

  private UnsignedIntegerLiteral unsignedIntegerLiteral () {
    var token = confirm(kind);
    return new UnsignedIntegerLiteral(token);
  }

  // Note: In C++, 'this' is a pointer, but in cppfront, it is not. Its unclear
  // if we can achieve the same thing in cobalt. For now, just assume it is a
  // pointer.

  private Expression this_ () {
    var n = new This(lookahead);
    match(THIS);
    return n;
  }

  private Expression parenthesizedExpression () {
    match(L_PARENTHESIS);
    var n = expression(false);
    match(R_PARENTHESIS);
    return n;
  }

  private Expression name () {
    var token = match(Token.Kind.IDENTIFIER);
    return new Name(token);
  }

  // TYPES **************************************************

  // Type processing is interesting because Opal uses a form of the
  // C-declaration style, so parsing types directly is challenging due to the
  // order in which tokens appear and the way they are nested. Instead of
  // try to parse them into types immediately, we just create an AST that
  // resembles the input. Then, during semantic analysis, the actual types are
  // built by walking this tree in the appropriate order.

  // Instead of passing in a sync set in order to account for dynamic context,
  // we hard-code different sync-sets and pass in an associated context
  // enumeration.

  private Declarator declarator (Parser.Context context) {
    var n = new Declarator();
    n.setPointerDeclarators(pointerDeclarators());
    n.setDirectDeclarator(directDeclarator());
    n.setArrayDeclarators(arrayDeclarators(context));
    return n;
  }

  private Declarator directDeclarator () {
    Declarator n;
    if (kind == Token.Kind.IDENTIFIER) {
      n = nominalType();
    } else if (kind == L_PARENTHESIS) {
      n = parenthesizedType();
    } else if (
      kind == Token.Kind.BOOL    ||
      kind == Token.Kind.SHORT   ||
      kind == Token.Kind.LONG    ||
      kind == Token.Kind.INT     ||
      kind == Token.Kind.INT8    ||
      kind == Token.Kind.INT16   ||
      kind == Token.Kind.INT32   ||
      kind == Token.Kind.INT64   ||
      kind == Token.Kind.NULL_T  ||
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
      n = primitiveType();
    } else if (kind == CARET) {
      n = routinePointerType();
    } else {
      panic("IN PANIC WITHIN DIRECT DECLARATOR!");
      n = new Declarator();
      n.setError();
    }
    return n;
  }

  private NominalDeclarator nominalType () {
    var token = confirm(Token.Kind.IDENTIFIER);
    return new NominalDeclarator(token);
  }

  private Declarator parenthesizedType () {
    confirm(L_PARENTHESIS);
    var n = declarator(Parser.Context.PARENTHESIZED_DECLARATOR);
    match(R_PARENTHESIS);
    return n;
  }

  private PrimitiveDeclarator primitiveType () {
    var token = confirm(kind);
    return new PrimitiveDeclarator(token);
  }

  // For now, assume all routine pointers must have a return type specified,
  // in which case, the last child of the AST node is the return type.

  private Declarator routinePointerType () {
    var token = confirm(CARET);
    var n = new RoutinePointerType(token);
    n.setRoutinePointerTypeParameters(routinePointerTypeParameters());
    match(MINUS_GREATER);
    // Now need to match return type declarator
    return n;
  }

  private RoutinePointerTypeParameters routinePointerTypeParameters () {
    var token = match(L_PARENTHESIS);
    var n = new RoutinePointerTypeParameters(token);
    if (kind != R_PARENTHESIS) {
      // Need if declarator first set vs. error cond
      n.addRoutinePointerTypeParameter(routinePointerTypeParameter());
    }
    while (kind != R_PARENTHESIS) {
      match(COMMA);
      // Need if declarator first set vs. error cond
      n.addRoutinePointerTypeParameter(routinePointerTypeParameter());
    }
    match(R_PARENTHESIS);
    return n;
  }

  private RoutinePointerTypeParameter routinePointerTypeParameter () {
    var n = new RoutinePointerTypeParameter();
    // We need a special context to handle ')' and ','
    n.setDeclarator(declarator((Parser.Context)null));
    return n;
  }

  private PointerDeclarators pointerDeclarators () {
    var n = new PointerDeclarators();
    while (
      kind != CARET                 &&
      kind != L_PARENTHESIS         &&
      kind != Token.Kind.IDENTIFIER &&
      kind != Token.Kind.BOOL       &&
      kind != Token.Kind.SHORT      &&
      kind != Token.Kind.INT        &&
      kind != Token.Kind.LONG       &&
      kind != Token.Kind.INT8       &&
      kind != Token.Kind.INT16      &&
      kind != Token.Kind.INT32      &&
      kind != Token.Kind.INT64      &&
      kind != Token.Kind.NULL_T     &&
      kind != Token.Kind.UINT       &&
      kind != Token.Kind.UINT8      &&
      kind != Token.Kind.UINT16     &&
      kind != Token.Kind.UINT32     &&
      kind != Token.Kind.UINT64     &&
      kind != Token.Kind.FLOAT      &&
      kind != Token.Kind.DOUBLE     &&
      kind != Token.Kind.FLOAT32    &&
      kind != Token.Kind.FLOAT64    &&
      kind != Token.Kind.VOID
    ) {
      if (kind == ASTERISK) {
        n.addPointerDeclarator(pointerDeclarator());
      } else {
        panic("'*' or direct declarator");
        break;
      }
    }
    return n;
  }

  private PointerDeclarator pointerDeclarator () {
    var token = confirm(ASTERISK);
    return new PointerDeclarator(token);
  }

  private ArrayDeclarators arrayDeclarators (Parser.Context context) {
    var n = new ArrayDeclarators();
    if (context == Context.TYPEALIAS_DECLARATION) {
      while (kind != SEMICOLON) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, SEMICOLON);
          break;
        }
      }
    } else if (context == Parser.Context.VARIABLE_TYPE_SPECIFIER) {
      while (kind != EQUAL && kind != SEMICOLON) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, EQUAL, SEMICOLON);
          break;
        }
      }
    } else if (context == Parser.Context.ROUTINE_RETURN_TYPE_SPECIFIER) {
      while (kind != L_BRACE) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, L_BRACE);
          break;
        }
      }
    } else if (context == Parser.Context.ROUTINE_PARAMETER_TYPE_SPECIFIER) {
      while (kind != Token.Kind.R_PARENTHESIS) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, R_PARENTHESIS);
          break;
        }
      }
    } else if (context == Parser.Context.PARENTHESIZED_DECLARATOR ) {
      while (kind != R_PARENTHESIS) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, Token.Kind.R_PARENTHESIS);
          break;
        }
      }
    } else if (context == Parser.Context.CAST_EXPRESSION) {
      while (kind != GREATER) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, Token.Kind.GREATER);
          break;
        }
      }
    } else if (context == Parser.Context.NEW_EXPRESSION) {
      while (kind != L_PARENTHESIS) {
        if (kind == L_BRACKET)
          n.addArrayDeclarator(arrayDeclarator());
        else {
          panic(L_BRACKET, Token.Kind.GREATER);
          break;
        }
      }
    }
    return n;
  }

  // Need to check that expression is const during semantic analysis

  private ArrayDeclarator arrayDeclarator () {
    var token = confirm(L_BRACKET);
    var n = new ArrayDeclarator(token);
    if (kind != R_BRACKET)
      n.setExpression(expression(Parser.Context.ARRAY_DECLARATOR));
    match(R_BRACKET);
    return n;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
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

  public enum Context {
    TYPEALIAS_DECLARATION,
    VARIABLE_TYPE_SPECIFIER,
    ROUTINE_PARAMETER_TYPE_SPECIFIER,
    ROUTINE_RETURN_TYPE_SPECIFIER,
    ARRAY_DECLARATOR,
    PARENTHESIZED_DECLARATOR,
    CAST_EXPRESSION,
    NEW_EXPRESSION
  }


}
