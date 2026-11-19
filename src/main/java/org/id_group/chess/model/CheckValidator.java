package org.id_group.chess.model;

import java.util.Arrays;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;

public class CheckValidator {
  Board board;

  public CheckValidator(Board board) {
    this.board = board;
  }

  private boolean isOppositeKingChecking(Piece king) {
    int row = king.getRow();
    int col = king.getCol();

    for (int i : new int[] {-1, 0, 1}) {
      for (int j : new int[] {-1, 0, 1}) {
        Piece piece = board.getPieceAtPosition(row + i, col + j);

        if (piece != null
            && piece.getPieceType() == PieceType.KING
            && piece.getPieceColor() != king.getPieceColor()) return true;
      }
    }

    return false;
  }

  private boolean checkForChecksInDirection(
      Piece king, int offsetX, int offsetY, PieceType... pieceTypes) {
    int row = king.getRow() + offsetY;
    int col = king.getCol() + offsetX;

    while (board.isOnBoard(row, col)) {
      Piece piece = board.getPieceAtPosition(row, col);

      col += offsetX;
      row += offsetY;

      if (piece == null) continue;
      return piece.getPieceColor() != king.getPieceColor()
          && Arrays.asList(pieceTypes).contains(piece.getPieceType());
    }

    return false;
  }

  private boolean isPawnChecking(Piece king, int direction) {
    int row = king.getRow();
    int col = king.getCol();

    Piece piece = board.getPieceAtPosition(row + direction, col - 1);
    if (piece != null
        && piece.getPieceType() == PieceType.PAWN
        && piece.getPieceColor() != king.getPieceColor()) return true;

    piece = board.getPieceAtPosition(row + direction, col + 1);
    return piece != null
        && piece.getPieceType() == PieceType.PAWN
        && piece.getPieceColor() != king.getPieceColor();
  }

  private boolean isKnightChecking(Piece king) {
    int row = king.getRow();
    int col = king.getCol();
    for (int i : new int[] {-2, -1, 1, 2}) {
      for (int j : new int[] {-2, -1, 1, 2}) {
        if (Math.abs(i) == Math.abs(j)) continue;
        Piece piece = board.getPieceAtPosition(i + row, j + col);
        if (piece != null
            && piece.getPieceType() == PieceType.KNIGHT
            && piece.getPieceColor() != king.getPieceColor()) return true;
      }
    }
    return false;
  }

  public boolean isCheck(Piece king) {
    if (king.getPieceType() != PieceType.KING)
      throw new IllegalArgumentException("Nie podano króla!");

    return isOppositeKingChecking(king)
        || isKnightChecking(king)
        || isPawnChecking(king, king.getPieceColor() == PieceColor.WHITE_PIECE ? -1 : 1)
        || checkForChecksInDirection(king, 1, 0, PieceType.QUEEN, PieceType.ROOK)
        || checkForChecksInDirection(king, -1, 0, PieceType.QUEEN, PieceType.ROOK)
        || checkForChecksInDirection(king, 0, 1, PieceType.QUEEN, PieceType.ROOK)
        || checkForChecksInDirection(king, 0, -1, PieceType.QUEEN, PieceType.ROOK)
        || checkForChecksInDirection(king, 1, 1, PieceType.QUEEN, PieceType.BISHOP)
        || checkForChecksInDirection(king, -1, 1, PieceType.QUEEN, PieceType.BISHOP)
        || checkForChecksInDirection(king, 1, -1, PieceType.QUEEN, PieceType.BISHOP)
        || checkForChecksInDirection(king, -1, -1, PieceType.QUEEN, PieceType.BISHOP);
  }
}
