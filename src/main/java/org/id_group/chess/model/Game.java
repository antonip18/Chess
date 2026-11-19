package org.id_group.chess.model;

import java.util.List;
import org.id_group.chess.model.move.Move;
import org.id_group.chess.model.move.Promotion;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.model.piece.Position;

public class Game {
  private final Board board;
  private final GameHistory gameHistory;
  private final CheckValidator checkValidator;
  private final MoveGenerator moveGenerator;
  private final GameStateEvaluator gameStateEvaluator;
  private final MoveMaker moveMaker;

  public Game() {
    this(Board.INITIAL_FEN);
  }

  public Game(String fen) {
    board = new Board(fen);
    gameHistory = new GameHistory(board);
    checkValidator = new CheckValidator(board);
    moveGenerator = new MoveGenerator(board, checkValidator, gameHistory);
    gameStateEvaluator = new GameStateEvaluator(board, gameHistory, checkValidator, moveGenerator);
    moveMaker = new MoveMaker(board, moveGenerator, gameHistory, gameStateEvaluator);
    gameStateEvaluator.calculateGameState();
  }

  public boolean isCheck() {
    return gameStateEvaluator.isCheck();
  }

  public String toFen() {
    return new BoardEncoder(board, gameHistory, moveGenerator).toFen();
  }

  public List<String> getMoveHistory() {
    return gameHistory.moves().stream()
        .map(
            move -> {
              String text =
                  BoardEncoder.square(move.getStartingPosition())
                      + "–"
                      + BoardEncoder.square(move.getTargetPosition());
              if (move instanceof Promotion promotion)
                text +=
                    "="
                        + (promotion.getPromotedTo() == PieceType.KNIGHT
                            ? "N"
                            : promotion.getPromotedTo().name().substring(0, 1));
              return text;
            })
        .toList();
  }

  public PieceColor getCurrentPlayerColor() {
    return board.getCurrentPlayerColor();
  }

  public GameState getGameState() {
    return gameStateEvaluator.getCurrentGameState();
  }

  public Piece getPieceAt(int row, int col) {
    return board.getPieceAtPosition(row, col);
  }

  public Position getKingPosition(PieceColor color) {
    return board.getKing(color).getPosition();
  }

  public Position getLastMoveStartingPosition() {
    Move move = gameHistory.getLastMove();
    return move == null ? null : move.getStartingPosition();
  }

  public Position getLastMoveTargetPosition() {
    Move move = gameHistory.getLastMove();
    return move == null ? null : move.getTargetPosition();
  }

  public MoveResult move(Piece piece, int row, int col, PieceType promotedTo) {
    return moveMaker.move(piece, row, col, promotedTo);
  }

  public MoveResult move(Piece piece, int row, int col) {
    return moveMaker.move(piece, row, col);
  }

  public List<Move> getPossibleMoves(Piece piece) {
    return getGameState() == GameState.NORMAL ? moveGenerator.getPossibleMoves(piece) : List.of();
  }

  public boolean canRevertLastMove() {
    return gameHistory.canRevertMove();
  }

  public void reverseLastMove() {
    gameHistory.revertMove();
    gameStateEvaluator.calculateGameState();
  }
}
