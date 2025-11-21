package org.opal.error;

// We will need different error classes because each kind of error
// has different kinds of things (e.g. tokens, nodes) to report.

import java.util.List;

public class LexicalError extends Error {

  private final List<String> lines;
  private final String message;
  private final char current;

  private final int line;
  private final int column;

  private final String ANSI_RESET = "\u001B[0m";
  private final String ANSI_RED   = "\u001B[31m";

  public LexicalError (List<String> lines, String message, char current, int line, int column) {
    super();
    this.lines = lines;
    this.message = message;
    this.current = current;
    this.line = line;
    this.column = column;
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
    var sb = new StringBuilder("lexical error (");
    sb.append(line)
      .append(", ")
      .append(column)
      .append("): ")
      .append(message);
    return sb.toString();
  }

  private String detail () {
    var sb = new StringBuffer();
    sb.append("  | ")
      .append(lines.get(line-1))
      .append("\n")
      .append("  | ")
      .repeat(' ', column - 1)
      .append(ANSI_RED)
      .append('^')
//      .repeat('~', lexeme.length() - 1)
      .append(ANSI_RESET);
    return sb.toString();
  }

}


