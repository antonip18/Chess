package org.id_group.chess.model;

import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;

public class GameStateEvaluator {
  private final Board board;
  private final MoveGenerator moveGenerator;
  private final GameHistory gameHistory;
  private final CheckValidator checkValidator;

  private GameState currentGameState = GameState.NORMAL;

  public GameStateEvaluator(
      Board board,
      GameHistory gameHistory,
      CheckValidator checkValidator,
      MoveGenerator moveGenerator) {
    this.board = board;
    this.gameHistory = gameHistory;
    this.checkValidator = checkValidator;
    this.moveGenerator = moveGenerator;
    gameHistory.initializePositions(new BoardEncoder(board, gameHistory, moveGenerator));
  }

  public boolean isMoveAvailable() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = board.getPieceAtPosition(i, j);
        if (piece != null
            && piece.getPieceColor() == board.getCurrentPlayerColor()
            && !moveGenerator.getPossibleMoves(piece).isEmpty()) return true;
      }
    }

    return false;
  }

  private boolean insufficientMaterial() {
    int wWBishop = 0, wBBishop = 0;
    int bWBishop = 0, bBBishop = 0;
    int bN = 0, wN = 0;

    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = board.getPieceAtPosition(i, j);
        if (piece == null) continue;

        PieceType type = piece.getPieceType();
        if (type == PieceType.PAWN || type == PieceType.QUEEN || type == PieceType.ROOK)
          return false;

        switch (piece.getPieceColor()) {
          case BLACK_PIECE -> {
            if (type == PieceType.KNIGHT) bN++;
            if (type == PieceType.BISHOP) {
              if ((i + j) % 2 == 0) bWBishop++;
              else bBBishop++;
            }
          }
          case WHITE_PIECE -> {
            if (type == PieceType.KNIGHT) wN++;
            if (type == PieceType.BISHOP) {
              if ((i + j) % 2 == 0) wWBishop++;
              else wBBishop++;
            }
          }
        }
      }
    }

    if (wN + wBBishop + wWBishop + bN + bBBishop + bWBishop <= 1) return true;
    if (wN != 0 || bN != 0) return false;
    if (wWBishop == 0 && bWBishop == 0) return true;
    if (wBBishop == 0 && bBBishop == 0) return true;

    return false;
  }

  private GameState returnCurrentGameState() {
    if (isMoveAvailable()) {
      if (insufficientMaterial()) return GameState.DRAW_INSUFFICIENT_MATERIAL;
      if (gameHistory.howManyMovesSinceCaptureOrPawnForward() >= 100)
        return GameState.DRAW_FIFTY_MOVE_RULE;
      if (gameHistory.currentPositionOccurrences() >= 3) return GameState.DRAW_THREEFOLD_REPETITION;
      return GameState.NORMAL;
    }

    return !isCheck()
        ? GameState.DRAW_STALEMATE
        : board.getCurrentPlayerColor() == PieceColor.WHITE_PIECE
            ? GameState.CHECKMATE_WHITE
            : GameState.CHECKMATE_BLACK;
  }

  public void calculateGameState() {
    currentGameState = returnCurrentGameState();
  }

  public GameState getCurrentGameState() {
    return currentGameState;
  }

  boolean isCheck() {
    return checkValidator.isCheck(
        board.isWhiteToMove()
            ? board.getKing(PieceColor.WHITE_PIECE)
            : board.getKing(PieceColor.BLACK_PIECE));
  }
}
