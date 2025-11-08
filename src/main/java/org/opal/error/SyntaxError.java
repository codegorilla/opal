package org.opal.error;

// We will need different error classes because each kind of error
// has different kinds of things (e.g. tokens, nodes) to report.

import org.opal.Token;

import java.util.List;

public class SyntaxError extends Error {

  private final List<String> lines;
  private final String message;
  private final Token token;

  private final String ANSI_RESET = "\u001B[0m";
  private final String ANSI_RED   = "\u001B[31m";

  public SyntaxError (List<String> lines, String message, Token token) {
    super();
    this.lines = lines;
    this.message = message;
    this.token = token;
  }

  // We want to construct a summary line followed by some detail lines. The
  // summary line shows the file, line, column, and a brief message. The detail
  // lines show the affected source code line and a marker indicating the
  // specific point where the error occurred.

  public String complete () {
    var s = new StringBuilder();
    s.append(summary());
    s.append('\n');
    s.append(detail());
    return s.toString();
  }

  private String summary () {
    var sb = new StringBuilder("syntax error (");
    sb.append(token.getLine())
      .append(", ")
      .append(token.getColumn())
      .append("): ")
      .append(message);
    return sb.toString();
  }

  private String detail () {
    var sb = new StringBuffer();
    sb.append("  | ")
      .append(lines.get(token.getLine()-1))
      .append("\n")
      .append("  | ")
      .repeat(' ', token.getColumn() - 1)
      .append(ANSI_RED)
      .append('^')
      .repeat('~', token.getLexeme().length() - 1)
      .append(ANSI_RESET);
    return sb.toString();
  }

}


