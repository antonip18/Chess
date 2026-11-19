package org.id_group.chess.model;

import org.id_group.chess.model.move.EnPassant;
import org.id_group.chess.model.piece.*;

/** Position identity includes turn, castling rights and only legally available en passant. */
public class BoardEncoder {
  private final Board board;
  private final GameHistory history;
  private final MoveGenerator generator;

  BoardEncoder(Board board, GameHistory history, MoveGenerator generator) {
    this.board = board;
    this.history = history;
    this.generator = generator;
  }

  String mapToString() {
    Position ep = history.enPassantTarget();
    boolean legalEp = false;
    if (ep != null) {
      int row = ep.getRow() + (board.isWhiteToMove() ? 1 : -1);
      for (int dc : new int[] {-1, 1}) {
        Piece pawn = board.getPieceAtPosition(row, ep.getCol() + dc);
        if (pawn != null
            && pawn.getPieceType() == PieceType.PAWN
            && generator.getPossibleMoves(pawn).stream().anyMatch(m -> m instanceof EnPassant))
          legalEp = true;
      }
    }
    return placement()
        + " "
        + (board.isWhiteToMove() ? "w" : "b")
        + " "
        + castling()
        + " "
        + (legalEp ? square(ep) : "-");
  }

  String toFen() {
    return placement()
        + " "
        + (board.isWhiteToMove() ? "w" : "b")
        + " "
        + castling()
        + " "
        + (history.enPassantTarget() == null ? "-" : square(history.enPassantTarget()))
        + " "
        + history.howManyMovesSinceCaptureOrPawnForward()
        + " "
        + history.fullmoveNumber();
  }

  private String placement() {
    StringBuilder result = new StringBuilder();
    for (int row = 0; row < 8; row++) {
      if (row > 0) result.append('/');
      int empty = 0;
      for (int col = 0; col < 8; col++) {
        Piece p = board.getPieceAtPosition(row, col);
        if (p == null) {
          empty++;
          continue;
        }
        if (empty > 0) {
          result.append(empty);
          empty = 0;
        }
        char symbol =
            p.getPieceType() == PieceType.KNIGHT
                ? 'n'
                : Character.toLowerCase(p.getPieceType().name().charAt(0));
        result.append(
            p.getPieceColor() == PieceColor.WHITE_PIECE ? Character.toUpperCase(symbol) : symbol);
      }
      if (empty > 0) result.append(empty);
    }
    return result.toString();
  }

  private String castling() {
    StringBuilder result = new StringBuilder();
    if (board.hasCastlingRight(PieceColor.WHITE_PIECE, true)) result.append('K');
    if (board.hasCastlingRight(PieceColor.WHITE_PIECE, false)) result.append('Q');
    if (board.hasCastlingRight(PieceColor.BLACK_PIECE, true)) result.append('k');
    if (board.hasCastlingRight(PieceColor.BLACK_PIECE, false)) result.append('q');
    return result.isEmpty() ? "-" : result.toString();
  }

  public static String square(Position p) {
    return "" + (char) ('a' + p.getCol()) + (8 - p.getRow());
  }
}
