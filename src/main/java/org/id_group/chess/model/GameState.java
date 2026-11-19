package org.id_group.chess.model;

public enum GameState {
  NORMAL,
  /** White is checkmated; Black wins. */
  CHECKMATE_WHITE,
  /** Black is checkmated; White wins. */
  CHECKMATE_BLACK,
  DRAW_STALEMATE,
  DRAW_INSUFFICIENT_MATERIAL,
  DRAW_THREEFOLD_REPETITION,
  DRAW_FIFTY_MOVE_RULE
}
