package org.opal;

public class Token {

  // Kind of token (e.g. IF, PLUS, SEMICOLON)
  private final Token.Kind kind;

  // Text contents of token
  private final String lexeme;

  // Position in input stream where first character occurs, counting from zero
  private final int index;

  // Line number of token, counting from one
  private final int line;

  // Column number where first character occurs, counting from one
  private final int column;

  // Tokens may be flagged as erroneous by parser's match method
  private boolean error = false;

  public Token (Token.Kind kind, String lexeme, int index, int line, int column) {
    this.kind = kind;
    this.lexeme = lexeme;
    this.index = index;
    this.line = line;
    this.column = column;
  }

  public Token.Kind getKind () {
    return kind;
  }

  public String getLexeme () {
    return lexeme;
  }

  public int getIndex () {
    return index;
  }

  public int getLine () {
    return line;
  }

  public int getColumn () {
    return column;
  }

  public boolean getError () {
    return error;
  }

  public void setError () {
    error = true;
  }

  public String toString () {
    return new StringBuilder(32)
        .append("Token(")
        .append(kind.toString())
        .append(",'")
        .append(lexeme)
        .append("',")
        .append(index)
        .append(",")
        .append(line)
        .append(",")
        .append(column)
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

    // Error
    ERROR,

    // End of file
    EOF
  }
}
