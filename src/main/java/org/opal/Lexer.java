package org.opal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.opal.error.LexicalError;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Lexer {

  private final int SLEEP_TIME = 100;

  private static final Logger LOGGER = LogManager.getLogger();

  private final List<String> sourceLines;

  private final char EOF = (char)(-1);

  private String input = null;
  private char current = EOF;

  private final Counter position = new Counter();
  private final Counter line     = new Counter(1);
  private final Counter column   = new Counter(1);

  private final Marker markPosition = new Marker();
  private final Marker markColumn   = new Marker();

  private final HashMap<String, Token.Kind> keywordLookup;

  public Lexer (String input, List<String> sourceLines) {
    this.input = input;
    if (!input.isEmpty())
      current = input.charAt(0);
    this.sourceLines = sourceLines;
    var keywordTable = new KeywordTable();
    keywordLookup = keywordTable.getForwardLookupTable();

    // Set up logging
    var level = Level.INFO;
    Configurator.setRootLevel(level);
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

  private void mark () {
    markPosition.set(position.get());
    markColumn.set(column.get());
  }

  private void error (String message) {
    System.out.println(new LexicalError(sourceLines, message, line.get(), column.get()));
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

  private Token getToken () {
    Token.Kind kind = null;
    String lexeme = "";

    while (current != EOF) {

//      System.out.println("Sleeping for " + SLEEP_TIME + " seconds in declarations...");
//      try {
//        Thread.sleep(SLEEP_TIME);
//      } catch (InterruptedException e) {
//        throw new RuntimeException(e);
//      }

      if (current == '=') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.EQUAL_EQUAL;
          lexeme = "==";
        } else {
          kind = Token.Kind.EQUAL;
          lexeme = "=";
        }
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '|') {
        mark();
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
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '^') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.CARET_EQUAL;
          lexeme = "^=";
        } else {
          kind = Token.Kind.CARET;
          lexeme = "^";
        }
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '&') {
        mark();
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
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '>') {
        mark();
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
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '<') {
        mark();
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
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '+') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.PLUS_EQUAL;
          lexeme = "+=";
        } else {
          kind = Token.Kind.PLUS;
          lexeme = "+";
        }
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '-') {
        mark();
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
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '*') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.ASTERISK_EQUAL;
          lexeme = "*=";
        } else {
          kind = Token.Kind.ASTERISK;
          lexeme = "*";
        }
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      // To do: Need to account for comments
      else if (current == '/') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.SLASH_EQUAL;
          lexeme = "/=";
          return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
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
          return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
        }
      }

      else if (current == '%') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.PERCENT_EQUAL;
          lexeme = "%=";
        } else {
          kind = Token.Kind.PERCENT;
          lexeme = "%";
        }
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '!') {
        mark();
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
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '~') {
        mark();
        consume();
        if (current == '=') {
          consume();
          kind = Token.Kind.TILDE_EQUAL;
          lexeme = "~=";
        } else {
          kind = Token.Kind.TILDE;
          lexeme = "~";
        }
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
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
        mark();
        consume();
        return new Token(Token.Kind.COLON, ":", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == ';') {
        mark();
        consume();
        return new Token(Token.Kind.SEMICOLON, ";", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '.') {
        mark();
        consume();
        if (current == '.') {
          consume();
          return new Token(Token.Kind.PERIOD_PERIOD, "..", markPosition.get(), line.get(), markColumn.get());
        } else if (Character.isDigit(current)) {
          return number();
        } else {
          return new Token(Token.Kind.PERIOD, ".", markPosition.get(), line.get(), markColumn.get());
        }
      }

      else if (current == ',') {
        mark();
        consume();
        return new Token(Token.Kind.COMMA, ",", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '{') {
        mark();
        consume();
        return new Token(Token.Kind.L_BRACE, "{", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '}') {
        mark();
        consume();
        return new Token(Token.Kind.R_BRACE, "}", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '[') {
        mark();
        consume();
        return new Token(Token.Kind.L_BRACKET, "[", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == ']') {
        mark();
        consume();
        return new Token(Token.Kind.R_BRACKET, "]", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == '(') {
        mark();
        consume();
        return new Token(Token.Kind.L_PARENTHESIS, "(", markPosition.get(), line.get(), markColumn.get());
      }

      else if (current == ')') {
        mark();
        consume();
        return new Token(Token.Kind.R_PARENTHESIS, ")", markPosition.get(), line.get(), markColumn.get());
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
        mark();
        do {
          consume();
        } while ((position.get() < input.length()) && (Character.isLetter(current) || Character.isDigit(current) || current == '_'));
        // End index of slice is excluded from result
        lexeme = input.substring(markPosition.get(), position.get());
        if (keywordLookup.containsKey(lexeme))
          kind = keywordLookup.get(lexeme);
        else
          kind = Token.Kind.IDENTIFIER;
        return new Token(kind, lexeme, markPosition.get(), line.get(), markColumn.get());
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

//      System.out.println("Sleeping for " + SLEEP_TIME + " seconds in declarations...");
//      try {
//        Thread.sleep(SLEEP_TIME);
//      } catch (InterruptedException e) {
//        throw new RuntimeException(e);
//      }

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
            error("invalid number: expected decimal digit, got '" + current + "'");
            consume();
            state = State.NUM_400;
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
