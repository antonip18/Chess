package org.id_group.chess.model;

import org.id_group.chess.model.move.*;
import org.id_group.chess.model.piece.*;

/** Validates commands against generated moves before making any state change. */
public class MoveMaker {
  private final Board board;
  private final MoveGenerator moveGenerator;
  private final GameHistory gameHistory;
  private final GameStateEvaluator gameStateEvaluator;

  public MoveMaker(
      Board board, MoveGenerator generator, GameHistory history, GameStateEvaluator evaluator) {
    this.board = board;
    this.moveGenerator = generator;
    this.gameHistory = history;
    this.gameStateEvaluator = evaluator;
  }

  public MoveResult move(Piece piece, int row, int col) {
    return move(piece, row, col, null);
  }

  public MoveResult move(Piece piece, int row, int col, PieceType promotedTo) {
    if (gameStateEvaluator.getCurrentGameState() != GameState.NORMAL
        || piece == null
        || !board.isOnBoard(row, col)) return MoveResult.ILLEGAL;
    for (Move candidate : moveGenerator.getPossibleMoves(piece)) {
      if (!candidate.getTargetPosition().equals(new Position(row, col))) continue;
      if (candidate instanceof Promotion promotion) {
        if (promotedTo == null) return MoveResult.PROMOTION_REQUIRED;
        if (promotion.getPromotedTo() != promotedTo) continue;
      } else if (promotedTo != null) continue;
      board.performTheMove(candidate, true);
      gameHistory.move(candidate);
      gameStateEvaluator.calculateGameState();
      return MoveResult.SUCCESS;
    }
    return MoveResult.ILLEGAL;
  }
}
