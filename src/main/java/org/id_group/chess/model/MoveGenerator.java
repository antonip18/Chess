package org.id_group.chess.model;

import java.util.ArrayList;
import java.util.List;
import org.id_group.chess.model.move.*;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.model.piece.Position;

public class MoveGenerator {
  private final Board board;
  private final CheckValidator checkValidator;
  private final GameHistory gameHistory;

  public MoveGenerator(Board board, CheckValidator checkValidator, GameHistory gameHistory) {
    this.board = board;
    this.checkValidator = checkValidator;
    this.gameHistory = gameHistory;
  }

  private boolean isLegalMove(Piece piece, int row, int col) {
    if (!board.isOnBoard(row, col)) return false;

    Piece p = board.getPieceAtPosition(row, col);
    if (p != null
        && (p.getPieceColor() == piece.getPieceColor() || p.getPieceType() == PieceType.KING))
      return false;

    Move move = new NormalMove(piece, p, new Position(row, col), piece.getPosition());
    board.performTheMove(move, false);

    boolean inCheck = checkValidator.isCheck(board.getKing(piece.getPieceColor()));

    board.reverseLastPerformedMove(move, false);

    return !inCheck;
  }

  private void addMovesInDirection(
      List<Move> possibleMoves, Piece piece, int row, int col, int rowOffset, int colOffset) {
    while (board.isOnBoard(row += rowOffset, col += colOffset)) {
      Piece p = board.getPieceAtPosition(row, col);
      if (p != null && p.getPieceColor() == piece.getPieceColor()) return;

      if (isLegalMove(piece, row, col))
        possibleMoves.add(new NormalMove(piece, p, new Position(row, col), piece.getPosition()));

      if (p != null) return;
    }
  }

  private boolean canPerformShortCastle(Piece king, Piece rook, int row, int col) {
    if (king.hasMoved() || !board.isAtStartingPosition(king)) return false;
    if (rook == null
        || rook.hasMoved()
        || rook.hasBeenCaptured()
        || !board.isAtStartingPosition(rook)) return false;
    if (checkValidator.isCheck(king)) return false;

    for (int cl = col + 1; cl != 7; cl++) {
      Piece p = board.getPieceAtPosition(row, cl);
      if (p != null && p != rook) return false;
    }

    for (int cl = col + 1; cl != 7; cl++) {
      board.movePieceToPosition(king, new Position(row, cl));

      boolean inCheck = checkValidator.isCheck(king);

      board.movePieceToPosition(king, new Position(row, col));

      if (inCheck) return false;
    }

    Move move = new Castle(king, rook, king.getPosition(), rook.getPosition(), true);
    board.performTheMove(move, false);
    boolean inCheck = checkValidator.isCheck(king);
    board.reverseLastPerformedMove(move, false);

    return !inCheck;
  }

  private boolean canPerformLongCastle(Piece king, Piece rook, int row, int col) {
    if (king.hasMoved() || !board.isAtStartingPosition(king)) return false;
    if (rook == null
        || rook.hasMoved()
        || rook.hasBeenCaptured()
        || !board.isAtStartingPosition(rook)) return false;
    if (checkValidator.isCheck(king)) return false;

    for (int cl = col - 1; cl != 0; cl--) {
      Piece p = board.getPieceAtPosition(row, cl);
      if (p != null && p != rook) return false;
    }

    for (int cl = col - 1; cl != 1; cl--) {
      board.movePieceToPosition(king, new Position(row, cl));

      boolean inCheck = checkValidator.isCheck(king);

      board.movePieceToPosition(king, new Position(row, col));

      if (inCheck) return false;
    }

    Move move = new Castle(king, rook, king.getPosition(), rook.getPosition(), false);
    board.performTheMove(move, false);
    boolean inCheck = checkValidator.isCheck(king);
    board.reverseLastPerformedMove(move, false);

    return !inCheck;
  }

  private List<Move> getPossibleKingMoves(Piece king) {
    List<Move> possibleMoves = new ArrayList<>();

    int row = king.getRow(), col = king.getCol();

    for (int i : new int[] {-1, 0, 1}) {
      for (int j : new int[] {-1, 0, 1}) {
        if (i == 0 && j == 0) continue;
        Piece p = board.getPieceAtPosition(row + i, col + j);
        if (isLegalMove(king, row + i, col + j))
          possibleMoves.add(
              new NormalMove(king, p, new Position(row + i, col + j), king.getPosition()));
      }
    }

    // Short Castle
    Piece rook = board.getRightRook(king.getPieceColor());
    if (canPerformShortCastle(king, rook, row, col))
      possibleMoves.add(new Castle(king, rook, king.getPosition(), rook.getPosition(), true));

    // Long Castle
    rook = board.getLeftRook(king.getPieceColor());
    if (canPerformLongCastle(king, rook, row, col))
      possibleMoves.add(new Castle(king, rook, king.getPosition(), rook.getPosition(), false));

    return possibleMoves;
  }

  private List<Move> getPossibleKnightMoves(Piece piece) {
    List<Move> possibleMoves = new ArrayList<>();

    int row = piece.getRow(), col = piece.getCol();

    for (int i : new int[] {-2, -1, 1, 2}) {
      for (int j : new int[] {-2, -1, 1, 2}) {
        if (Math.abs(i) == Math.abs(j)) continue;
        Piece p = board.getPieceAtPosition(row + i, col + j);
        if (isLegalMove(piece, row + i, col + j))
          possibleMoves.add(
              new NormalMove(piece, p, new Position(row + i, col + j), piece.getPosition()));
      }
    }

    return possibleMoves;
  }

  private List<Move> getPossibleQueenRookAndBishopMoves(Piece piece) {
    List<Move> possibleMoves = new ArrayList<>();

    PieceType type = piece.getPieceType();
    int row = piece.getRow(), col = piece.getCol();

    if (type == PieceType.QUEEN || type == PieceType.ROOK) {
      addMovesInDirection(possibleMoves, piece, row, col, 0, -1);
      addMovesInDirection(possibleMoves, piece, row, col, 0, 1);
      addMovesInDirection(possibleMoves, piece, row, col, 1, 0);
      addMovesInDirection(possibleMoves, piece, row, col, -1, 0);
    }

    if (type == PieceType.QUEEN || type == PieceType.BISHOP) {
      addMovesInDirection(possibleMoves, piece, row, col, -1, -1);
      addMovesInDirection(possibleMoves, piece, row, col, 1, 1);
      addMovesInDirection(possibleMoves, piece, row, col, 1, -1);
      addMovesInDirection(possibleMoves, piece, row, col, -1, 1);
    }

    return possibleMoves;
  }

  private List<Move> getPossiblePawnMoves(Piece piece) {
    List<Move> possibleMoves = new ArrayList<>();

    int dir = piece.getPieceColor() == PieceColor.WHITE_PIECE ? -1 : 1;
    int row = piece.getRow(), col = piece.getCol();

    // Move one square forward
    Piece p = board.getPieceAtPosition(row + dir, col);
    if (p == null && isLegalMove(piece, row + dir, col)) {
      if (row + dir == 0 || row + dir == 7) {
        possibleMoves.add(
            new Promotion(
                piece, null, new Position(row + dir, col), piece.getPosition(), PieceType.QUEEN));
        possibleMoves.add(
            new Promotion(
                piece, null, new Position(row + dir, col), piece.getPosition(), PieceType.ROOK));
        possibleMoves.add(
            new Promotion(
                piece, null, new Position(row + dir, col), piece.getPosition(), PieceType.KNIGHT));
        possibleMoves.add(
            new Promotion(
                piece, null, new Position(row + dir, col), piece.getPosition(), PieceType.BISHOP));
      } else {
        possibleMoves.add(
            new NormalMove(piece, null, new Position(row + dir, col), piece.getPosition()));
      }
    }
    // The destination must resolve check; the intermediate square only needs to be empty.
    if (board.isAtStartingPosition(piece)
        && board.getPieceAtPosition(row + dir, col) == null
        && board.getPieceAtPosition(row + 2 * dir, col) == null
        && isLegalMove(piece, row + 2 * dir, col)) {
      possibleMoves.add(
          new NormalMove(piece, null, new Position(row + 2 * dir, col), piece.getPosition()));
    }

    // Capture diagonally
    p = board.getPieceAtPosition(row + dir, col - 1);
    if (p != null
        && p.getPieceColor() != piece.getPieceColor()
        && isLegalMove(piece, row + dir, col - 1)) {
      if (row + dir == 0 || row + dir == 7) {
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col - 1), piece.getPosition(), PieceType.QUEEN));
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col - 1), piece.getPosition(), PieceType.ROOK));
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col - 1), piece.getPosition(), PieceType.KNIGHT));
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col - 1), piece.getPosition(), PieceType.BISHOP));
      } else {
        possibleMoves.add(
            new NormalMove(piece, p, new Position(row + dir, col - 1), piece.getPosition()));
      }
    }

    p = board.getPieceAtPosition(row + dir, col + 1);
    if (p != null
        && p.getPieceColor() != piece.getPieceColor()
        && isLegalMove(piece, row + dir, col + 1)) {
      if (row + dir == 0 || row + dir == 7) {
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col + 1), piece.getPosition(), PieceType.QUEEN));
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col + 1), piece.getPosition(), PieceType.ROOK));
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col + 1), piece.getPosition(), PieceType.KNIGHT));
        possibleMoves.add(
            new Promotion(
                piece, p, new Position(row + dir, col + 1), piece.getPosition(), PieceType.BISHOP));
      } else {
        possibleMoves.add(
            new NormalMove(piece, p, new Position(row + dir, col + 1), piece.getPosition()));
      }
    }

    // En Passant
    p = board.getPieceAtPosition(row, col - 1);
    if (p != null
        && p.getPieceColor() != piece.getPieceColor()
        && gameHistory.checkForEnPassant(p)) {
      Move move = new EnPassant(piece, p, new Position(row + dir, col - 1), piece.getPosition());
      board.performTheMove(move, false);

      boolean notInCheck = !checkValidator.isCheck(board.getKing(piece.getPieceColor()));

      board.reverseLastPerformedMove(move, false);

      if (notInCheck) possibleMoves.add(move);
    }

    p = board.getPieceAtPosition(row, col + 1);
    if (p != null
        && p.getPieceColor() != piece.getPieceColor()
        && gameHistory.checkForEnPassant(p)) {
      Move move = new EnPassant(piece, p, new Position(row + dir, col + 1), piece.getPosition());
      board.performTheMove(move, false);

      boolean notInCheck = !checkValidator.isCheck(board.getKing(piece.getPieceColor()));

      board.reverseLastPerformedMove(move, false);

      if (notInCheck) possibleMoves.add(move);
    }

    return possibleMoves;
  }

  public List<Move> getPossibleMoves(Piece piece) {
    if (piece == null
        || piece.hasBeenCaptured()
        || board.getPieceAtPosition(piece.getRow(), piece.getCol()) != piece) return List.of();
    if (piece.getPieceColor() == PieceColor.WHITE_PIECE && !board.isWhiteToMove())
      return new ArrayList<>();
    if (piece.getPieceColor() == PieceColor.BLACK_PIECE && board.isWhiteToMove())
      return new ArrayList<>();

    PieceType type = piece.getPieceType();

    return type == PieceType.KING
        ? getPossibleKingMoves(piece)
        : type == PieceType.KNIGHT
            ? getPossibleKnightMoves(piece)
            : type == PieceType.PAWN
                ? getPossiblePawnMoves(piece)
                : getPossibleQueenRookAndBishopMoves(piece);
  }
}
