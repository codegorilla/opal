package org.opal.error;

// We will need different error classes because each kind of error
// has different kinds of things (e.g. tokens, nodes) to report.

import java.util.List;

public class LexicalError extends Error {

  private final List<String> lines;
  private final String message;

  private final int line;
  private final int column;

  public LexicalError (List<String> lines, String message, int line, int column) {
    super();
    this.lines = lines;
    this.message = message;
    this.line = line;
    this.column = column;
  }

  // We want to construct a summary line followed by some detail lines. The
  // summary line shows the file, line, column, and a brief message. The detail
  // lines show the affected source code line and a marker indicating the
  // specific point where the error occurred.

  @Override
  public String toString () {
    return summary() + '\n' + detail();
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
    var sb = new StringBuilder();
    sb.append("  | ")
      .append(lines.get(line-1))
      .append("\n")
      .append("  | ")
      .repeat(' ', column - 1)
      .append(TermColor.ANSI_RED)
      .append('^')
      .append(TermColor.ANSI_RESET);
    return sb.toString();
  }

}


