package org.opal;

public enum State {

  // Binary integer states
  BIN_START,
  BIN_100,
  BIN_200,
  BIN_300,
  BIN_400,
  BIN_500,
  BIN_600,
  BIN_700,
  BIN_800,
  BIN_ERROR,

  // Hexadecimal number states
  HEX_START,
  HEX_10,
  HEX_20,
  HEX_30,
  HEX_100,
  HEX_200,
  HEX_210,
  HEX_220,
  HEX_230,
  HEX_300,
  HEX_400,
  HEX_500,
  HEX_600,
  HEX_700,
  HEX_800,
  HEX_810,
  HEX_820,
  HEX_ERROR,

  // Decimal number states
  NUM_START,
  NUM_100,
  NUM_200,
  NUM_210,
  NUM_220,
  NUM_230,
  NUM_300,
  NUM_400,
  NUM_500,
  NUM_600,
  NUM_700,
  NUM_800,
  NUM_810,
  NUM_820,
  NUM_ERROR,

  // Octal integer states
  OCT_START,
  OCT_100,
  OCT_200,
  OCT_300,
  OCT_400,
  OCT_500,
  OCT_600,
  OCT_700,
  OCT_800,
  OCT_ERROR
}
