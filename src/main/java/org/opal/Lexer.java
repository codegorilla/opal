package org.opal;

import java.util.HashMap;
import java.util.LinkedList;

public class Lexer {

  private final char EOF = (char)(-1);

  private String input = null;
  private char current = EOF;

  private final Counter position = new Counter();
  private final Counter line     = new Counter(1);
  private final Counter column   = new Counter(1);

  private final HashMap<String, Token.Kind> keywordLookup = new HashMap<>();

  public Lexer (String input) {
    this.input = input;
    if (!input.isEmpty())
      current = input.charAt(0);
    buildKeywordLookupTable();
  }

  private void consume () {
    position.increment();
    if (position.get() < input.length())
      current = input.charAt(position.get());
    else
      current = EOF;
    column.increment();
  }

  private void backup () {
    position.decrement();
    current = input.charAt(position.get());
    column.decrement();
  }

  private void error (String message) {
    var coords = "(" + line + "," + column.get() + ")";
    System.out.println(coords + ": error:" + message);
  }

  private void buildKeywordLookupTable () {
    // Populate keyword lookup table
    keywordLookup.put("abstract", Token.Kind.ABSTRACT);
    keywordLookup.put("and", Token.Kind.AND);
    keywordLookup.put("as", Token.Kind.AS);
    keywordLookup.put("break", Token.Kind.BREAK);
    keywordLookup.put("case", Token.Kind.CASE);
    keywordLookup.put("cast", Token.Kind.CAST);
    keywordLookup.put("catch", Token.Kind.CATCH);
    keywordLookup.put("class", Token.Kind.CLASS);
    keywordLookup.put("const", Token.Kind.CONST);
    keywordLookup.put("consteval", Token.Kind.CONSTEVAL);
    keywordLookup.put("constexpr", Token.Kind.CONSTEXPR);
    keywordLookup.put("continue", Token.Kind.CONTINUE);
    keywordLookup.put("def", Token.Kind.DEF);
    keywordLookup.put("default", Token.Kind.DEFAULT);
    keywordLookup.put("delete", Token.Kind.DELETE);
    keywordLookup.put("divine", Token.Kind.DIVINE);
    keywordLookup.put("do", Token.Kind.DO);
    keywordLookup.put("else", Token.Kind.ELSE);
    keywordLookup.put("enum", Token.Kind.ENUM);
    keywordLookup.put("extends", Token.Kind.EXTENDS);
    keywordLookup.put("false", Token.Kind.FALSE);
    keywordLookup.put("final", Token.Kind.FINAL);
    keywordLookup.put("for", Token.Kind.FOR);
    keywordLookup.put("fn", Token.Kind.FN);
    keywordLookup.put("fun", Token.Kind.FUN);
    keywordLookup.put("goto", Token.Kind.GOTO);
    keywordLookup.put("if", Token.Kind.IF);
    keywordLookup.put("import", Token.Kind.IMPORT);
    keywordLookup.put("in", Token.Kind.IN);
    keywordLookup.put("include", Token.Kind.INCLUDE);
    keywordLookup.put("loop", Token.Kind.LOOP);
    keywordLookup.put("new", Token.Kind.NEW);
    keywordLookup.put("nil", Token.Kind.NIL);
    keywordLookup.put("noexcept", Token.Kind.NOEXCEPT);
    keywordLookup.put("null", Token.Kind.NULL);
    keywordLookup.put("or", Token.Kind.OR);
    keywordLookup.put("override", Token.Kind.OVERRIDE);
    keywordLookup.put("package", Token.Kind.PACKAGE);
    keywordLookup.put("private", Token.Kind.PRIVATE);
    keywordLookup.put("protected", Token.Kind.PROTECTED);
    keywordLookup.put("return", Token.Kind.RETURN);
    keywordLookup.put("static", Token.Kind.STATIC);
    keywordLookup.put("struct", Token.Kind.STRUCT);
    keywordLookup.put("switch", Token.Kind.SWITCH);
    keywordLookup.put("template", Token.Kind.TEMPLATE);
    keywordLookup.put("this", Token.Kind.THIS);
    keywordLookup.put("trait", Token.Kind.TRAIT);
    keywordLookup.put("transmute", Token.Kind.TRANSMUTE);
    keywordLookup.put("true", Token.Kind.TRUE);
    keywordLookup.put("try", Token.Kind.TRY);
    keywordLookup.put("typealias", Token.Kind.TYPEALIAS);
    keywordLookup.put("union", Token.Kind.UNION);
    keywordLookup.put("until", Token.Kind.UNTIL);
    keywordLookup.put("use", Token.Kind.USE);
    keywordLookup.put("val", Token.Kind.VAL);
    keywordLookup.put("var", Token.Kind.VAR);
    keywordLookup.put("virtual", Token.Kind.VIRTUAL);
    keywordLookup.put("volatile", Token.Kind.VOLATILE);
    keywordLookup.put("when", Token.Kind.WHEN);
    keywordLookup.put("while", Token.Kind.WHILE);
    keywordLookup.put("with", Token.Kind.WITH);
    keywordLookup.put("short", Token.Kind.SHORT);
    keywordLookup.put("int", Token.Kind.INT);
    keywordLookup.put("long", Token.Kind.LONG);
    keywordLookup.put("int8", Token.Kind.INT8);
    keywordLookup.put("int16", Token.Kind.INT16);
    keywordLookup.put("int32", Token.Kind.INT32);
    keywordLookup.put("int64", Token.Kind.INT64);
    keywordLookup.put("uint", Token.Kind.UINT);
    keywordLookup.put("uint8", Token.Kind.UINT8);
    keywordLookup.put("uint16", Token.Kind.UINT16);
    keywordLookup.put("uint32", Token.Kind.UINT32);
    keywordLookup.put("uint64", Token.Kind.UINT64);
    keywordLookup.put("float", Token.Kind.FLOAT);
    keywordLookup.put("double", Token.Kind.DOUBLE);
    keywordLookup.put("float32", Token.Kind.FLOAT32);
    keywordLookup.put("float64", Token.Kind.FLOAT64);
    keywordLookup.put("void", Token.Kind.VOID);
  }

  public LinkedList<Token> process () {
    var tokens = new LinkedList<Token>();
    var token = getToken();
    tokens.addLast(token);
    while (token.getKind() != Token.Kind.EOF) {
      token = getToken();
      tokens.addLast(token);
    }
    return tokens;
  }

  // Todo: For multi-character lexemes, we need to ensure that the column is
  // calculated as the start of the lexeme, not the end.

  private Token getToken () {
    Token.Kind kind = null;
    String lexeme = "";

    while (current != EOF) {

      if (current == '=') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.EQUAL_EQUAL;
          lexeme = "==";
        } else {
          kind = Token.Kind.EQUAL;
          lexeme = "=";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '|') {
        consume();
        if (current == '|') {
          consume();
          kind = Token.Kind.BAR_BAR;
          lexeme = "||";
        } else if (current == '=') {
          consume();
          kind = Token.Kind.BAR_EQUAL;
          lexeme = "|=";
        } else {
          kind = Token.Kind.BAR;
          lexeme = "|";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '^') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.CARET_EQUAL;
          lexeme = "^=";
        } else {
          kind = Token.Kind.CARET;
          lexeme = "^";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '&') {
        consume();
        if (current == '&') {
          consume();
          kind = Token.Kind.AMPERSAND_AMPERSAND;
          lexeme = "&&";
        } else if (current == '=') {
          consume();
          kind = Token.Kind.AMPERSAND_EQUAL;
          lexeme = "&=";
        } else {
          kind = Token.Kind.AMPERSAND;
          lexeme = "&";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '>') {
        consume();
        if (current == '>') {
          consume();
          if (current == '=') {
            consume();
            kind = Token.Kind.GREATER_GREATER_EQUAL;
            lexeme = ">>=";
          } else {
            kind = Token.Kind.GREATER_GREATER;
            lexeme = ">>";
          }
        } else if (current == '=') {
          consume();
          kind = Token.Kind.GREATER_EQUAL;
          lexeme = ">=";
        } else {
          kind = Token.Kind.GREATER;
          lexeme = ">";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '<') {
        consume();
        if (current == '<') {
          consume();
          if (current == '=') {
            consume();
            kind = Token.Kind.LESS_LESS_EQUAL;
            lexeme = "<<=";
          } else {
            kind = Token.Kind.LESS_LESS;
            lexeme = "<<";
          }
        } else if (current == '=') {
          consume();
          kind = Token.Kind.LESS_EQUAL;
          lexeme = "<=";
        } else {
          kind = Token.Kind.LESS;
          lexeme = "<";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '+') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.PLUS_EQUAL;
          lexeme = "+=";
        } else {
          kind = Token.Kind.PLUS;
          lexeme = "+";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '-') {
        consume();
        if (current == '>') {
          consume();
          kind = Token.Kind.MINUS_GREATER;
          lexeme = "->";
        } else if (current == '=') {
          consume();
          kind = Token.Kind.MINUS_EQUAL;
          lexeme = "-=";
        } else {
          kind = Token.Kind.MINUS;
          lexeme = "-";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '*') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.ASTERISK_EQUAL;
          lexeme = "*=";
        } else {
          kind = Token.Kind.ASTERISK;
          lexeme = "*";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      // To do: Need to account for comments
      else if (current == '/') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.SLASH_EQUAL;
          lexeme = "/=";
          return new Token(kind, lexeme, position.get(), line.get(), column.get());
        } else if (current == '*') {
          // Block comment
          consume();
          var commentDone = false;
          while (!commentDone) {
            while (current != '*' && current != EOF) {
              if (current == '\n') {
                // Skip line feeds(LF)
                consume();
                line.increment();
                column.reset();
              } else if (current =='\r') {
                // Skip carriage return +line feed(CR + LF) pairs
                consume();
                if (current == '\n') {
                  consume();
                  line.increment();
                  column.reset();
                } else {
                  // Found carriage return (CR) by itself, which is invalid
                  System.out.println("error: invalid line ending");
                }
              } else
                consume();
            }
            while (current == '*')
              consume();
            if (current == '/') {
              consume();
              commentDone = true;
            } else if (current == EOF) {
              // Error - comment not closed
              System.out.println("error: comment not closed");
              commentDone = true;
            }
          }
        } else if (current == '/') {
          // Line comment
          do {
            consume();
          } while (current != '\n' && current != '\r' && current != EOF);
        } else {
          kind = Token.Kind.SLASH;
          lexeme = "/";
          return new Token(kind, lexeme, position.get(), line.get(), column.get());
        }
      }

      else if (current == '%') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.PERCENT_EQUAL;
          lexeme = "%=";
        } else {
          kind = Token.Kind.PERCENT;
          lexeme = "%";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '!') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.EXCLAMATION_EQUAL;
          lexeme = "!=";
        } else if (current == '<') {
          consume();
          kind = Token.Kind.EXCLAMATION_LESS;
          lexeme = "!<";
        } else {
          kind = Token.Kind.EXCLAMATION;
          lexeme = "!";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '~') {
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.TILDE_EQUAL;
          lexeme = "~=";
        } else {
          kind = Token.Kind.TILDE;
          lexeme = "~";
        }
        return new Token(kind, lexeme, position.get(), line.get(), column.get());
      }

      else if (current == '"') {
        // String
        final var beginPosition = position.get();
        consume();
        while (current != '"' && current != EOF) {
          // Might need to put some logic in here to increment line
          // number and reset position if (a newline is encountered
          consume();
        }
        if (current == '"') {
          consume();
          final var value = input.substring(beginPosition, position.get());
          return new Token(Token.Kind.STRING_LITERAL, value, position.get(), line.get(), column.get());
        } else if (current == EOF) {
          // To do: probably should pretend terminator is there and return token
          System.out.println("error: missing string terminator");
        }
      }

      else if (current == '\'') {
        // Character
        final var beginPosition = position.get();
        consume();
        while (current != '\'' && current != EOF) {
          // Might need to put some logic in here to increment line
          // number and reset position if (a newline is encountered
          consume();
        }
        if (current == '\'') {
          consume();
          final var value = input.substring(beginPosition, position.get());
          return new Token(Token.Kind.CHARACTER_LITERAL, value, position.get(), line.get(), column.get());
        } else if (current == EOF) {
          // To) {: probably should pretend terminator is there and return token
          System.out.println("error: missing character terminator");
        }
      }

      else if (current == ':') {
        consume();
        return new Token(Token.Kind.COLON, ":", position.get(), line.get(), column.get());
      }

      else if (current == ';') {
        consume();
        return new Token(Token.Kind.SEMICOLON, ";", position.get(), line.get(), column.get());
      }

      else if (current == '.') {
        consume();
        if (current == '.') {
          consume();
          return new Token(Token.Kind.PERIOD_PERIOD, "..", position.get(), line.get(), column.get());
        } else if (Character.isDigit(current)) {
          return number();
        } else
          return new Token(Token.Kind.PERIOD, ".", position.get(), line.get(), column.get());
      }

      else if (current == ',') {
        consume();
        return new Token(Token.Kind.COMMA, ",", position.get(), line.get(), column.get());
      }

      else if (current == '{') {
        consume();
        return new Token(Token.Kind.L_BRACE, "{", position.get(), line.get(), column.get());
      }

      else if (current == '}') {
        consume();
        return new Token(Token.Kind.R_BRACE, "}", position.get(), line.get(), column.get());
      }

      else if (current == '[') {
        consume();
        return new Token(Token.Kind.L_BRACKET, "[", position.get(), line.get(), column.get());
      }

      else if (current == ']') {
        consume();
        return new Token(Token.Kind.R_BRACKET, "]", position.get(), line.get(), column.get());
      }

      else if (current == '(') {
        consume();
        return new Token(Token.Kind.L_PARENTHESIS, "(", position.get(), line.get(), column.get());
      }

      else if (current == ')') {
        consume();
        return new Token(Token.Kind.R_PARENTHESIS, ")", position.get(), line.get(), column.get());
      }

      else if (current == '0') {
        consume();
        if (current == 'b') {
          backup();
          return binaryInteger();
        } else if (current == 'o') {
          backup();
          return octalInteger();
        } else if (current == 'x') {
          backup();
          return hexadecimalNumber();
        } else {
          backup();
          return number();
        }
      }

      else if (current == ' ' || current == '\t') {
        // Skip spaces and tabs
        while (current == ' ' || current == '\t') {
          consume();
        }
      }

      else if (current == '\n') {
        // Skip line feed (LF) characters
        while (current == '\n') {
          consume();
          line.increment();
          column.reset();
        }
      }

      else if (current == '\r') {
        // skip carriage return + line feed (CR+LF) pairs
        while (current == '\r') {
          consume();
          if (current == '\n') {
            consume();
            line.increment();
            column.reset();
          } else {
            // Should return error token here maybe
            // Found carriage return by itself, which is invalid (except on mac?)
            System.out.println("error: invalid line ending");
          }
        }
      }

      else if (Character.isLetter(current) || current == '_') {
        final var beginPosition = position.get();
        final var beginColumn = column.get();
        do {
          consume();
        } while ((position.get() < input.length()) && (Character.isLetter(current) || Character.isDigit(current) || current == '_'));
        // End index of slice is excluded from result
        lexeme = input.substring(beginPosition, position.get());
        if (keywordLookup.containsKey(lexeme))
          kind = keywordLookup.get(lexeme);
        else
          kind = Token.Kind.IDENTIFIER;
        return new Token(kind, lexeme, beginPosition, line.get(), beginColumn);
      }

      else if (Character.isDigit(current))
        return number();

      else
        System.out.println("ERROR!");
    }

    // Placeholder to avoid error
    return new Token(Token.Kind.EOF, "<EOF>", position.get(), line.get(), column.get());
  }

  private boolean isBinaryDigit (char ch) {
    return ch == '0' || ch == '1';
  }

  private boolean isOctalDigit (char ch) {
    return ch >= '0' && ch <= '7';
  }

  private boolean isDecimalDigit (char ch) {
    return ch >= '0' && ch <= '9';
  }

  private boolean isHexadecimalDigit (char ch) {
    return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
  }

  private Token binaryInteger () {
    // Note: We arrive at this function after lookahead or
    // backtracking, so we really should never fail to match the '0b'
    // portion, unless there is a bug in this program.
    final var beginPosition = position.get();
    var state = State.BIN_START;
    Token token = null;
    while (token == null) {

      switch (state) {
        case State.BIN_START:
          if (current == '0') {
            consume();
            state = State.BIN_100;
          } else
            state = State.BIN_ERROR;
          break;
        case State.BIN_100:
          if (current == 'b') {
            consume();
            state = State.BIN_200;
          } else
            state = State.BIN_ERROR;
          break;
        case State.BIN_200:
          consume();
          if (isBinaryDigit(current)) {
            consume();
            state = State.BIN_400;
          } else if (current == '_') {
            consume();
            state = State.BIN_300;
          } else {
            // Pretend we got a digit or underscore for error recovery purposes
            error("invalid number: found '" + current + "', expected binary digit or underscore");
            consume();
            state = State.BIN_400;
          }
          break;
        case State.BIN_300:
          if (isBinaryDigit(current)) {
            consume();
            state = State.BIN_400;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current +"', expected binary digit");
            consume();
            state = State.BIN_400;
          }
          break;
        case State.BIN_400:
          if (isBinaryDigit(current)) {
            consume();
          } else if (current == '_') {
            consume();
            state = State.BIN_500;
          } else if (current == 'L') {
            consume();
            state = State.BIN_600;
          } else if (current == 'u') {
            consume();
            state = State.BIN_700;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.BINARY_INT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.BIN_500:
          if (isBinaryDigit(current)) {
            consume();
            state = State.BIN_400;
          } else if (current == 'L') {
            consume();
            state = State.BIN_600;
          } else if (current == 'u') {
            consume();
            state = State.BIN_700;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected binary digit");
            consume();
            state = State.BIN_400;
          }
          break;
        case State.BIN_600:
          if (current == 'u') {
            consume();
            state = State.BIN_800;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.BINARY_INT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.BIN_700:
          if (current == 'L') {
            consume();
            state = State.BIN_800;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.BINARY_UINT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.BIN_800:
          // Accept
          final var lexeme = input.substring(beginPosition, position.get());
          token = new Token(Token.Kind.BINARY_UINT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          break;
        default:
          // Invalid state. Can only be reached through a lexer bug.
          System.out.println("error: Invalid state.");
          break;
      }
    }
    return token;
  }

  private Token octalInteger () {
    // Note: We arrive at this function after lookahead or
    // backtracking, so we really should never fail to match the '0o'
    // portion, unless there is a bug in this program.
    final var beginPosition = position.get();
    var state = State.OCT_START;
    Token token = null;
    while (token == null) {
      switch (state) {
        case State.OCT_START:
          if (current == '0') {
            consume();
            state = State.OCT_100;
          } else
            state = State.OCT_ERROR;
          break;
        case State.OCT_100:
          if (current == 'o') {
            consume();
            state = State.OCT_200;
          } else
            state = State.OCT_ERROR;
          break;
        case State.OCT_200:
          if (isOctalDigit(current)) {
            consume();
            state = State.OCT_400;
          } else if (current == '_') {
            consume();
            state = State.OCT_300;
          } else {
            // Pretend we got a digit or underscore for error recovery purposes
            error("invalid number: found '" + current + "', expected octal digit or underscore");
            consume();
            state = State.OCT_400;
          }
          break;
        case State.OCT_300:
          if (isOctalDigit(current)) {
            consume();
            state = State.OCT_400;
          }
          break;
        case State.OCT_400:
          if (isOctalDigit(current)) {
            consume();
          } else if (current == '_') {
            consume();
            state = State.OCT_500;
          } else if (current == 'L') {
            consume();
            state = State.OCT_600;
          } else if (current == 'u') {
            consume();
            state = State.OCT_700;
          } else {
            // Accept
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.OCTAL_INT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.OCT_500:
          if (isOctalDigit(current)) {
            consume();
            state = State.OCT_400;
          } else if (current == 'L') {
            consume();
            state = State.OCT_600;
          } else if (current == 'u') {
            consume();
            state = State.OCT_700;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected octal digit");
            consume();
            state = State.OCT_400;
          }
          break;
        case State.OCT_600:
          if (current == 'u') {
            consume();
            state = State.OCT_800;
          } else {
            // Accept
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.OCTAL_INT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.OCT_700:
          if (current == 'L') {
            consume();
            state = State.OCT_800;
          } else {
            // Accept
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.OCTAL_UINT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.OCT_800:
          // Accept
          final var lexeme = input.substring(beginPosition, position.get());
          token = new Token(Token.Kind.OCTAL_UINT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          break;
        default:
          // Invalid state. Can only be reached through a lexer bug.
          System.out.println("error: Invalid state.");
          break;
      }
    }
    return token;
  }

  private Token hexadecimalNumber () {
    // This scans for a hexadecimal integer or floating point number.
    final var beginPosition = position.get();
    var state = State.HEX_START;
    Token token = null;
    while (token == null) {
      switch (state) {
        case State.HEX_START:
          if (current == '0') {
            consume();
            state = State.HEX_10;
          } else
            state = State.HEX_ERROR;
          break;
        case State.HEX_10:
          if (current == 'x') {
            consume();
            state = State.HEX_20;
          } else {
            state = State.HEX_ERROR;
          }
          break;
        case State.HEX_20:
          if (isHexadecimalDigit(current)) {
            consume();
            state = State.HEX_100;
          } else if (current == '_') {
            consume();
            state = State.HEX_30;
          } else if (current == '.') {
            consume();
            state = State.HEX_300;
          } else {
            // Pretend we got a digit, dot, or underscore for error recovery purposes
            error("invalid number: found '" + current + "', expected hexadecimal digit, dot, or underscore");
            consume();
            state = State.HEX_100;
          }
          break;
        case State.HEX_30:
          if (isHexadecimalDigit(current)) {
            consume();
            state = State.HEX_100;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected hexadecimal digit");
            consume();
            state = State.HEX_100;
          }
          break;
        case State.HEX_100:
          if (isHexadecimalDigit(current)) {
            consume();
          } else if (current == '_') {
            consume();
            state = State.HEX_200;
          } else if (current == 'L') {
            consume();
            state = State.HEX_210;
          } else if (current == 'u') {
            consume();
            state = State.HEX_220;
          } else if (current == '.') {
            consume();
            state = State.HEX_300;
          } else if (current == 'p') {
            consume();
            state = State.HEX_600;
          } else {
            // Accept
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_INT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.HEX_200:
          if (isHexadecimalDigit(current)) {
            consume();
            state = State.HEX_100;
          } else if (current == 'L') {
            consume();
            state = State.HEX_210;
          } else if (current == 'u') {
            consume();
            state = State.HEX_220;
          } else if (current == 'p') {
            consume();
            state = State.HEX_600;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected 'L', 'u', 'p', or hexadecimal digit");
            consume();
            state = State.HEX_100;
          }
          break;
        case State.HEX_210:
          if (current == 'u') {
            consume();
            state = State.HEX_230;
          } else {
            // Accept
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_INT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.HEX_220:
          if (current == 'L') {
            consume();
            state = State.HEX_230;
          } else {
            // Accept
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_UINT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.HEX_230: {
          // Accept
          final var lexeme = input.substring(beginPosition, position.get());
          token = new Token(Token.Kind.HEXADECIMAL_UINT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
        case State.HEX_300:
          if (isHexadecimalDigit(current)) {
            consume();
            state = State.HEX_400;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected hexadecimal digit");
            consume();
            // Do we need to change states here?
          }
          break;
        case State.HEX_400:
          if (isHexadecimalDigit(current)) {
            consume();
          } else if (current == '_') {
            consume();
            state = State.HEX_500;
          } else if (current == 'p') {
            consume();
            state = State.HEX_600;
          } else {
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_FLOAT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.HEX_500:
          if (isHexadecimalDigit(current)) {
            consume();
            state = State.HEX_400;
          } else if (current == 'p') {
            consume();
            state = State.HEX_600;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected 'p' or hexadecimal digit");
            consume();
            state = State.HEX_400;
          }
          break;
        case State.HEX_600:
          if (isDecimalDigit(current)) {
            consume();
            state = State.HEX_800;
          } else if (current == '+' || current == '-') {
            consume();
            state = State.HEX_700;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected '+', '-', or decimal digit");
            consume();
            state = State.HEX_800;
          }
          break;
        case State.HEX_700:
          if (isDecimalDigit(current)) {
            consume();
            state = State.HEX_800;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected decimal digit");
            consume();
            state = State.HEX_800;
          }
          break;
        case State.HEX_800:
          if (isDecimalDigit(current)) {
            consume();
          } else if (current == 'd') {
            consume();
            state = State.HEX_810;
          } else if (current == 'f') {
            consume();
            state = State.HEX_820;
          } else {
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_FLOAT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.HEX_810:
          {
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_FLOAT64_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        case State.HEX_820:
          {
              final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.HEXADECIMAL_FLOAT32_LITERAL, lexeme, position.get(), line.get(), column.get());
          }
          break;
        default:
          // Invalid state. Can only be reached through a lexer bug.
          System.out.println("error: Invalid state.");
      }
    }
    return token;
  }

  private Token number () {
    // This scans for an integer or floating point number.
    final var beginPosition = position.get();
    final var beginColumn = column.get();
    var state = State.NUM_START;
    Token token = null;
    while (token == null) {
      switch (state) {
        case State.NUM_START:
          // We are guaranteed to get a digit here unless the lexer has a bug in it.
          if (isDecimalDigit(current)) {
            consume();
            state = State.NUM_100;
          } else if (current == '.') {
            consume();
            state = State.NUM_300;
          } else {
            state = State.NUM_ERROR;
          }
          break;
        case State.NUM_100:
          if (isDecimalDigit(current)) {
            consume();
          } else if (current == '_') {
            consume();
            state = State.NUM_200;
          } else if (current == 'L') {
            consume();
            state = State.NUM_210;
          } else if (current == 'u') {
            consume();
            state = State.NUM_220;
          } else if (current == '.') {
            consume();
            state = State.NUM_300;
          } else if (current == 'e') {
            consume();
            state = State.NUM_600;
          } else if (current == 'd') {
            consume();
            state = State.NUM_810;
          } else if (current == 'f') {
            consume();
            state = State.NUM_820;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.INT32_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        case State.NUM_200:
          if (isDecimalDigit(current)) {
            consume();
            state = State.NUM_100;
          } else if (current == 'e') {
            consume();
            state = State.NUM_600;
          } else if (current == 'd') {
            consume();
            state = State.NUM_810;
          } else if (current == 'f') {
            consume();
            state = State.NUM_820;
          } else if (current == 'L') {
            consume();
            state = State.NUM_210;
          } else if (current == 'u') {
            consume();
            state = State.NUM_220;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected 'd', 'f', 'e', 'L', 'u', or decimal digit");
            consume();
            state = State.NUM_100;
          }
          break;
        case State.NUM_210:
          if (current == 'u') {
            consume();
            state = State.NUM_230;
          } else {
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.INT64_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        case State.NUM_220:
          if (current == 'L') {
            consume();
            state = State.NUM_230;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.UINT32_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        case State.NUM_230:
          {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.UINT64_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
            break;
          }
        case State.NUM_300:
          if (isDecimalDigit(current)) {
            consume();
            state = State.NUM_400;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected decimal digit");
            consume();
          }
          break;
        case State.NUM_400:
          if (isDecimalDigit(current)) {
            consume();
          } else if (current == '_') {
            consume();
            state = State.NUM_500;
          } else if (current == 'e') {
            consume();
            state = State.NUM_600;
          } else if (current == 'd') {
            consume();
            state = State.NUM_810;
          } else if (current == 'f') {
            consume();
            state = State.NUM_820;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.FLOAT64_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        case State.NUM_500:
          if (isDecimalDigit(current)) {
            consume();
            state = State.NUM_400;
          } else if (current == 'e') {
            consume();
            state = State.NUM_600;
          } else if (current == 'd') {
            consume();
            state = State.NUM_810;
          } else if (current == 'f') {
            consume();
            state = State.NUM_820;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected decimal digit");
            consume();
            state = State.NUM_400;
          }
          break;
        case State.NUM_600:
          if (isDecimalDigit(current)) {
            consume();
            state = State.NUM_800;
          } else if (current == '+' || current == '-') {
            consume();
            state = State.NUM_700;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected '+', '-', or decimal digit");
            consume();
            state = State.NUM_800;
          }
          break;
        case State.NUM_700:
          if (isDecimalDigit(current)) {
            consume();
            state = State.NUM_800;
          } else {
            // Pretend we got a digit for error recovery purposes
            error("invalid number: found '" + current + "', expected decimal digit");
            consume();
            state = State.NUM_800;
          }
          break;
        case State.NUM_800:
          if (isDecimalDigit(current)) {
            consume();
          } else if (current == 'd') {
            consume();
            state = State.NUM_810;
          } else if (current == 'f') {
            consume();
            state = State.NUM_820;
          } else {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.FLOAT64_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        case State.NUM_810:
          {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.FLOAT64_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        case State.NUM_820:
          {
            // Accept
            final var lexeme = input.substring(beginPosition, position.get());
            token = new Token(Token.Kind.FLOAT32_LITERAL, lexeme, beginPosition, line.get(), beginColumn);
          }
          break;
        default:
           // Invalid state. Can only be reached through a lexer bug.
           System.out.println("error: Invalid state.");
           break;
      }
    }
    return token;
  }
}
