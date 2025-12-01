package org.opal;

import java.util.EnumSet;

// Warning: Order of declaration matters!

// To do: We might want to write these from bottom to top then

public class FirstSet {

//  public static final EnumSet<Token.Kind> TRANSLATION_UNIT =
//    FirstSet.PACKAGE_DECLARATION;

  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION =
    EnumSet.of(Token.Kind.PACKAGE);

  public static final EnumSet<Token.Kind> IMPORT_QUALIFIED_NAME =
    EnumSet.of (Token.Kind.IDENTIFIER);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATION =
    EnumSet.of(Token.Kind.IMPORT);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATIONS =
    FirstSet.IMPORT_DECLARATION;

  public static final EnumSet<Token.Kind> USE_DECLARATION =
    EnumSet.of(Token.Kind.USE);

  public static final EnumSet<Token.Kind> USE_DECLARATIONS =
    FirstSet.USE_DECLARATION;

  public static final EnumSet<Token.Kind> USE_QUALIFIED_NAME_TAIL =
    EnumSet.of(Token.Kind.ASTERISK, Token.Kind.L_BRACE, Token.Kind.IDENTIFIER);


  public static final EnumSet<Token.Kind> OTHER_DECLARATION =
    EnumSet.of(Token.Kind.PRIVATE, Token.Kind.CLASS, Token.Kind.DEF, Token.Kind.VAL, Token.Kind.VAR);

  public static final EnumSet<Token.Kind> OTHER_DECLARATIONS =
    FirstSet.OTHER_DECLARATION;

  public static final EnumSet<Token.Kind> DECLARATOR =
    EnumSet.of (
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
      Token.Kind.IDENTIFIER,
      Token.Kind.ASTERISK,
      Token.Kind.CARET,
      Token.Kind.L_BRACKET,
      Token.Kind.L_PARENTHESIS
    );

  //  Might not use these directly
  public static final EnumSet<Token.Kind> ARRAY_DECLARATOR =
    EnumSet.of (Token.Kind.L_BRACKET);

  public static final EnumSet<Token.Kind> POINTER_DECLARATOR =
    EnumSet.of (Token.Kind.ASTERISK);

  public static final EnumSet<Token.Kind> ROUTINE_POINTER_DECLARATOR =
    EnumSet.of (Token.Kind.CARET);

  public static final EnumSet<Token.Kind> COMPOUND_DECLARATOR =
    EnumSet.of (Token.Kind.IDENTIFIER);



  // Reminder: Order of declaration matters!

  // Might not use directly
  public static final EnumSet<Token.Kind> CLASS_DECLARATION =
    EnumSet.of(Token.Kind.CLASS);

  // Might not use directly
  public static final EnumSet<Token.Kind> ROUTINE_DECLARATION =
    EnumSet.of(Token.Kind.DEF);

  // Might not use directly
  public static final EnumSet<Token.Kind> VARIABLE_DECLARATION =
    EnumSet.of(Token.Kind.VAL, Token.Kind.VAR);





  public static final EnumSet<Token.Kind> STANDARD_STATEMENT = EnumSet.of (
    Token.Kind.BREAK,
    Token.Kind.L_BRACE,
    Token.Kind.CONTINUE,
    Token.Kind.DO,
    Token.Kind.FOR,
    Token.Kind.LOOP,
    Token.Kind.SEMICOLON,
    Token.Kind.IF,
    Token.Kind.RETURN,
    Token.Kind.UNTIL,
    Token.Kind.WHILE
  );

  public static final EnumSet<Token.Kind> DECLARATION_STATEMENT = EnumSet.of (
    Token.Kind.TYPEALIAS,
    Token.Kind.VAL,
    Token.Kind.VAR
  );

  public static final EnumSet<Token.Kind> EXPRESSION = EnumSet.of (
    Token.Kind.IDENTIFIER,
    Token.Kind.L_PARENTHESIS,
    Token.Kind.CAST,
    Token.Kind.DIVINE,
    Token.Kind.TRANSMUTE,
    Token.Kind.NEW,
    Token.Kind.DELETE,
    Token.Kind.AMPERSAND,
    Token.Kind.ASTERISK,
    Token.Kind.EXCLAMATION,
    Token.Kind.MINUS,
    Token.Kind.PLUS,
    Token.Kind.TILDE,
    Token.Kind.FALSE,
    Token.Kind.TRUE,
    Token.Kind.CHARACTER_LITERAL,
    Token.Kind.FLOAT32_LITERAL,
    Token.Kind.FLOAT64_LITERAL,
    Token.Kind.INT32_LITERAL,
    Token.Kind.INT64_LITERAL,
    Token.Kind.NULL,
    Token.Kind.STRING_LITERAL,
    Token.Kind.UINT32_LITERAL,
    Token.Kind.UINT64_LITERAL
  );

  public static final EnumSet<Token.Kind> LITERAL = EnumSet.of (
    Token.Kind.FALSE,
    Token.Kind.TRUE,
    Token.Kind.CHARACTER_LITERAL,
    Token.Kind.FLOAT32_LITERAL,
    Token.Kind.FLOAT64_LITERAL,
    Token.Kind.INT32_LITERAL,
    Token.Kind.INT64_LITERAL,
    Token.Kind.NULL,
    Token.Kind.STRING_LITERAL,
    Token.Kind.UINT32_LITERAL,
    Token.Kind.UINT64_LITERAL
  );

  public static final EnumSet<Token.Kind> TYPE = EnumSet.of (
    Token.Kind.BOOL,
    Token.Kind.INT,
    Token.Kind.INT8,
    Token.Kind.INT16,
    Token.Kind.INT32,
    Token.Kind.INT64,
    Token.Kind.UINT,
    Token.Kind.UINT8,
    Token.Kind.UINT16,
    Token.Kind.UINT32,
    Token.Kind.UINT64,
    Token.Kind.FLOAT,
    Token.Kind.DOUBLE,
    Token.Kind.FLOAT32,
    Token.Kind.FLOAT64,
    Token.Kind.VOID,
    Token.Kind.IDENTIFIER,
    Token.Kind.ASTERISK,
    Token.Kind.L_BRACE
  );

}
