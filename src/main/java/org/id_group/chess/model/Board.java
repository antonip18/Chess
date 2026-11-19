package org.id_group.chess.model;

import org.id_group.chess.model.move.*;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.model.piece.Position;

public class Board {
  private final Piece[][] board = new Piece[8][8];

  private final Piece leftBlackRook;
  private final Piece rightBlackRook;
  private final Piece leftWhiteRook;
  private final Piece rightWhiteRook;

  private final Piece blackKing;
  private final Piece whiteKing;

  private boolean whiteToMove = true;

  public static final String INITIAL_FEN =
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
  private final Position initialEnPassantTarget;
  private final int initialHalfmoveClock;
  private final int initialFullmoveNumber;
  private final boolean initiallyWhiteToMove;

  public Board() {
    this(INITIAL_FEN);
  }

  public Board(String fen) {
    FenPosition position = FenPosition.parse(fen);
    for (int r = 0; r < 8; r++) System.arraycopy(position.pieces()[r], 0, board[r], 0, 8);
    whiteToMove = position.whiteToMove();
    initiallyWhiteToMove = whiteToMove;
    initialEnPassantTarget = position.enPassantTarget();
    initialHalfmoveClock = position.halfmoveClock();
    initialFullmoveNumber = position.fullmoveNumber();
    whiteKing = findKing(PieceColor.WHITE_PIECE);
    blackKing = findKing(PieceColor.BLACK_PIECE);
    leftWhiteRook = originalRook(7, 0, PieceColor.WHITE_PIECE);
    rightWhiteRook = originalRook(7, 7, PieceColor.WHITE_PIECE);
    leftBlackRook = originalRook(0, 0, PieceColor.BLACK_PIECE);
    rightBlackRook = originalRook(0, 7, PieceColor.BLACK_PIECE);
    String rights = position.castlingRights();
    configureCastling(
        whiteKing, leftWhiteRook, rightWhiteRook, rights.contains("Q"), rights.contains("K"));
    configureCastling(
        blackKing, leftBlackRook, rightBlackRook, rights.contains("q"), rights.contains("k"));
    Piece otherKing = getKing(whiteToMove ? PieceColor.BLACK_PIECE : PieceColor.WHITE_PIECE);
    if (new CheckValidator(this).isCheck(otherKing))
      throw new IllegalArgumentException(
          "Król strony, która wykonała ruch, nie może być szachowany.");
  }

  private Piece findKing(PieceColor color) {
    for (Piece[] rank : board)
      for (Piece piece : rank)
        if (piece != null
            && piece.getPieceType() == PieceType.KING
            && piece.getPieceColor() == color) return piece;
    throw new IllegalArgumentException("Brak króla.");
  }

  private Piece originalRook(int row, int col, PieceColor color) {
    Piece p = board[row][col];
    return p != null && p.getPieceType() == PieceType.ROOK && p.getPieceColor() == color ? p : null;
  }

  private void configureCastling(
      Piece king, Piece left, Piece right, boolean longRight, boolean shortRight) {
    if ((longRight || shortRight) && !isAtStartingPosition(king)
        || longRight && left == null
        || shortRight && right == null)
      throw new IllegalArgumentException("Nieprawidłowe prawa do roszady.");
    if (!longRight && !shortRight) king.increaseMoveNumber();
    if (left != null && !longRight) left.increaseMoveNumber();
    if (right != null && !shortRight) right.increaseMoveNumber();
  }

  Position initialEnPassantTarget() {
    return initialEnPassantTarget;
  }

  int initialHalfmoveClock() {
    return initialHalfmoveClock;
  }

  int initialFullmoveNumber() {
    return initialFullmoveNumber;
  }

  boolean initiallyWhiteToMove() {
    return initiallyWhiteToMove;
  }

  boolean hasCastlingRight(PieceColor color, boolean shortCastle) {
    Piece king = getKing(color), rook = shortCastle ? getRightRook(color) : getLeftRook(color);
    return !king.hasMoved()
        && isAtStartingPosition(king)
        && rook != null
        && !rook.hasMoved()
        && !rook.hasBeenCaptured()
        && isAtStartingPosition(rook)
        && getPieceAtPosition(rook.getRow(), rook.getCol()) == rook;
  }

  public boolean isWhiteToMove() {
    return whiteToMove;
  }

  public PieceColor getCurrentPlayerColor() {
    return whiteToMove ? PieceColor.WHITE_PIECE : PieceColor.BLACK_PIECE;
  }

  boolean isAtStartingPosition(Piece piece) {
    switch (piece.getPieceType()) {
      case PAWN:
        return piece.getPieceColor() == PieceColor.WHITE_PIECE
            ? piece.getRow() == 6
            : piece.getRow() == 1;
      case KING:
        return piece.getCol() == 4
            && (piece.getPieceColor() == PieceColor.WHITE_PIECE
                ? piece.getRow() == 7
                : piece.getRow() == 0);
      case ROOK:
        return (piece.getCol() == 0 || piece.getCol() == 7)
            && (piece.getPieceColor() == PieceColor.WHITE_PIECE
                ? piece.getRow() == 7
                : piece.getRow() == 0);
      default:
        return false;
    }
  }

  public Piece getPieceAtPosition(int row, int column) {
    if (!isOnBoard(row, column)) return null;
    return board[row][column];
  }

  boolean isOnBoard(int row, int col) {
    return row >= 0 && col >= 0 && row <= 7 && col <= 7;
  }

  public Piece getKing(PieceColor color) {
    return color == PieceColor.BLACK_PIECE ? blackKing : whiteKing;
  }

  public Piece getLeftRook(PieceColor color) {
    return color == PieceColor.BLACK_PIECE ? leftBlackRook : leftWhiteRook;
  }

  public Piece getRightRook(PieceColor color) {
    return color == PieceColor.BLACK_PIECE ? rightBlackRook : rightWhiteRook;
  }

  private void performCastle(Castle move) {
    Piece king = move.getKing();
    Piece rook = move.getRook();

    int row = king.getRow();
    int kingCol = move.wasShortCastle() ? 6 : 2;
    int rookCol = move.wasShortCastle() ? 5 : 3;

    board[king.getRow()][king.getCol()] = null;
    board[rook.getRow()][rook.getCol()] = null;

    board[row][kingCol] = king;
    board[row][rookCol] = rook;

    king.move(row, kingCol);
    rook.move(row, rookCol);
  }

  private void performPromotion(Promotion move) {
    board[move.getStartingPosition().getRow()][move.getStartingPosition().getCol()] = null;
    board[move.getTargetPosition().getRow()][move.getTargetPosition().getCol()] = move.getPiece();
    move.getPiece().move(move.getTargetPosition());
    move.getPiece().promote(move.getPromotedTo());
  }

  private void performNormalMove(NormalMove move) {
    board[move.getStartingPosition().getRow()][move.getStartingPosition().getCol()] = null;
    board[move.getTargetPosition().getRow()][move.getTargetPosition().getCol()] = move.getPiece();
    move.getPiece().move(move.getTargetPosition());
  }

  private void performEnPassant(EnPassant move) {
    board[move.getStartingPosition().getRow()][move.getStartingPosition().getCol()] = null;
    board[move.getStartingPosition().getRow()][move.getTargetPosition().getCol()] = null;
    board[move.getTargetPosition().getRow()][move.getTargetPosition().getCol()] = move.getPiece();
    move.getPiece().move(move.getTargetPosition());
  }

  public void performTheMove(Move move, boolean shouldIncreaseMoveNumber) {
    whiteToMove = !whiteToMove;

    if (move.getCapturedPiece() != null) move.getCapturedPiece().capturePiece();

    switch (move) {
      case NormalMove normalMove:
        performNormalMove(normalMove);
        if (shouldIncreaseMoveNumber) normalMove.getPiece().increaseMoveNumber();
        return;
      case Castle castle:
        performCastle(castle);
        if (shouldIncreaseMoveNumber) {
          castle.getKing().increaseMoveNumber();
          castle.getRook().increaseMoveNumber();
        }
        return;
      case Promotion promotion:
        performPromotion(promotion);
        if (shouldIncreaseMoveNumber) promotion.getPiece().increaseMoveNumber();
        return;
      case EnPassant enPassant:
        performEnPassant(enPassant);
        if (shouldIncreaseMoveNumber) enPassant.getPiece().increaseMoveNumber();
        return;
      default:
        throw new IllegalStateException("Unexpected value: " + move);
    }
  }

  private void reverseLastPerformedCastle(Castle move) {
    Piece king = move.getKing();
    Piece rook = move.getRook();

    board[king.getRow()][king.getCol()] = null;
    board[rook.getRow()][rook.getCol()] = null;

    Position kingStartingPosition = move.getKingStartingPosition();
    Position rookStartingPosition = move.getRookStartingPosition();

    board[kingStartingPosition.getRow()][kingStartingPosition.getCol()] = king;
    board[rookStartingPosition.getRow()][rookStartingPosition.getCol()] = rook;

    king.move(kingStartingPosition.getRow(), kingStartingPosition.getCol());
    rook.move(rookStartingPosition.getRow(), rookStartingPosition.getCol());
  }

  private void reverseLastPerformedPromotion(Promotion move) {
    board[move.getTargetPosition().getRow()][move.getTargetPosition().getCol()] =
        move.getCapturedPiece();
    board[move.getStartingPosition().getRow()][move.getStartingPosition().getCol()] =
        move.getPiece();
    move.getPiece().move(move.getStartingPosition());
    move.getPiece().revertPromotion();
  }

  private void reverseLastPerformedNormalMove(NormalMove move) {
    board[move.getTargetPosition().getRow()][move.getTargetPosition().getCol()] =
        move.getCapturedPiece();
    board[move.getStartingPosition().getRow()][move.getStartingPosition().getCol()] =
        move.getPiece();
    move.getPiece().move(move.getStartingPosition());
  }

  private void reverseLastPerformedEnPassant(EnPassant move) {
    board[move.getStartingPosition().getRow()][move.getStartingPosition().getCol()] =
        move.getPiece();
    board[move.getStartingPosition().getRow()][move.getTargetPosition().getCol()] =
        move.getCapturedPawn();
    board[move.getTargetPosition().getRow()][move.getTargetPosition().getCol()] = null;
    move.getPiece().move(move.getStartingPosition());
  }

  public void reverseLastPerformedMove(Move move, boolean shouldDecreaseMoveNumber) {
    whiteToMove = !whiteToMove;

    if (move.getCapturedPiece() != null) move.getCapturedPiece().uncapturePiece();

    switch (move) {
      case NormalMove normalMove:
        reverseLastPerformedNormalMove(normalMove);
        if (shouldDecreaseMoveNumber) normalMove.getPiece().decreaseMoveNumber();
        return;
      case Castle castle:
        reverseLastPerformedCastle(castle);
        if (shouldDecreaseMoveNumber) {
          castle.getKing().decreaseMoveNumber();
          castle.getRook().decreaseMoveNumber();
        }
        return;
      case Promotion promotion:
        reverseLastPerformedPromotion(promotion);
        if (shouldDecreaseMoveNumber) promotion.getPiece().decreaseMoveNumber();
        return;
      case EnPassant enPassant:
        reverseLastPerformedEnPassant(enPassant);
        if (shouldDecreaseMoveNumber) enPassant.getPiece().decreaseMoveNumber();
        return;
      default:
        throw new IllegalStateException("Unexpected value: " + move);
    }
  }

  void movePieceToPosition(Piece piece, Position position) {
    board[piece.getRow()][piece.getCol()] = null;
    board[position.getRow()][position.getCol()] = piece;
    piece.move(position);
  }
}
