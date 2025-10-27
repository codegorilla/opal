package org.opal;

public class Token {

  private Token.Kind kind;
  private String lexeme;
  private int position;
  private int line;
  private int column;

  public Token (Token.Kind kind, String lexeme, int position, int line, int column) {
    this.kind = kind;
    this.lexeme = lexeme;
    this.position = position;
    this.line = line;
    this.column = column;
  }

  public Token.Kind getKind () {
    return kind;
  }

  public String getLexeme () {
    return lexeme;
  }

  public int getPosition () {
    return position;
  }

  public int getLine () {
    return line;
  }

  public int getColumn () {
    return column;
  }

  public String toString () {
    return new StringBuilder(32)
        .append("Token(")
        .append(kind.toString())
        .append(",'")
        .append(lexeme)
        .append("',")
        .append(position)
        .append(")")
        .toString();
  }

  public enum Kind {

    // Keywords
    ABSTRACT,
    AND,
    AS,
    BREAK,
    CASE,
    CAST,
    CATCH,
    CLASS,
    CONST,
    CONSTEVAL,
    CONSTEXPR,
    CONTINUE,
    DEF,
    DEFAULT,
    DELETE,
    DIVINE,
    DO,
    ELSE,
    ENUM,
    EXTENDS,
    FALSE,
    FINAL,
    FOR,
    FN,
    FUN,
    GOTO,
    IF,
    INCLUDE,
    IN,
    IMPORT,
    LOOP,
    NEW,
    NIL,
    NOEXCEPT,
    NULL,
    OR,
    OVERRIDE,
    PACKAGE,
    PRIVATE,
    PROTECTED,
    RETURN,
    STATIC,
    STRUCT,
    SWITCH,
    TEMPLATE,
    THIS,
    TRAIT,
    TRANSMUTE,
    TRUE,
    TRY,
    TYPEALIAS,
    UNION,
    UNTIL,
    USE,
    USING,
    VAL,
    VAR,
    VIRTUAL,
    VOLATILE,
    WHEN,
    WHILE,
    WITH,
    YIELD,

    // Basic types
    BOOL,
    DOUBLE,
    FLOAT,
    FLOAT32,
    FLOAT64,
    INT,
    INT8,
    INT16,
    INT32,
    INT64,
    LONG,
    NULL_T,
    SHORT,
    UINT,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    VOID,

    // Identifiers
    IDENTIFIER,


    // Integer literals
    BINARY_INT32_LITERAL,
    BINARY_INT64_LITERAL,
    BINARY_UINT32_LITERAL,
    BINARY_UINT64_LITERAL,
    HEXADECIMAL_INT32_LITERAL,
    HEXADECIMAL_INT64_LITERAL,
    HEXADECIMAL_UINT32_LITERAL,
    HEXADECIMAL_UINT64_LITERAL,
    INT32_LITERAL,
    INT64_LITERAL,
    OCTAL_INT32_LITERAL,
    OCTAL_INT64_LITERAL,
    OCTAL_UINT32_LITERAL,
    OCTAL_UINT64_LITERAL,
    UINT32_LITERAL,
    UINT64_LITERAL,

    // Floating-point literals
    FLOAT32_LITERAL,
    FLOAT64_LITERAL,
    HEXADECIMAL_FLOAT32_LITERAL,
    HEXADECIMAL_FLOAT64_LITERAL,

    // Other literals
    CHARACTER_LITERAL,
    STRING_LITERAL,

    // Operators and Punctuation
    AMPERSAND,
    AMPERSAND_AMPERSAND,
    AMPERSAND_EQUAL,
    ASTERISK,
    ASTERISK_EQUAL,
    BAR,
    BAR_BAR,
    BAR_EQUAL,
    CARET,
    CARET_EQUAL,
    COLON,
    COMMA,
    EQUAL,
    EQUAL_EQUAL,
    EXCLAMATION,
    EXCLAMATION_EQUAL,
    EXCLAMATION_LESS,
    GREATER,
    GREATER_EQUAL,
    GREATER_GREATER,
    GREATER_GREATER_EQUAL,
    L_BRACE,
    L_BRACKET,
    L_PARENTHESIS,
    LESS,
    LESS_EQUAL,
    LESS_LESS,
    LESS_LESS_EQUAL,
    MINUS,
    MINUS_EQUAL,
    MINUS_GREATER,
    PERCENT,
    PERCENT_EQUAL,
    PERIOD,
    PERIOD_PERIOD,
    PLUS,
    PLUS_EQUAL,
    R_BRACE,
    R_BRACKET,
    R_PARENTHESIS,
    SEMICOLON,
    SLASH,
    SLASH_EQUAL,
    TILDE,
    TILDE_EQUAL,

    // End of file
    EOF
  }
}
