package org.id_group.chess.model;

import java.util.*;
import org.id_group.chess.model.move.*;
import org.id_group.chess.model.piece.*;

/** Reversible bookkeeping: evaluating a position never increments its repetition count. */
public class GameHistory {
  private final Board board;
  private final List<Move> moves = new ArrayList<>();
  private final List<Integer> previousHalfmoveClocks = new ArrayList<>();
  private final List<String> positions = new ArrayList<>();
  private final Map<String, Integer> occurrences = new HashMap<>();
  private int movesSinceCaptureOrPawnForward;
  private BoardEncoder encoder;

  public GameHistory(Board board) {
    this.board = board;
    movesSinceCaptureOrPawnForward = board.initialHalfmoveClock();
  }

  void initializePositions(BoardEncoder encoder) {
    this.encoder = encoder;
    recordPosition();
  }

  private void recordPosition() {
    if (encoder == null) return;
    String key = encoder.mapToString();
    positions.add(key);
    occurrences.merge(key, 1, Integer::sum);
  }

  int currentPositionOccurrences() {
    return positions.isEmpty() ? 0 : occurrences.getOrDefault(positions.getLast(), 0);
  }

  int howManyMovesSinceCaptureOrPawnForward() {
    return movesSinceCaptureOrPawnForward;
  }

  int fullmoveNumber() {
    return board.initialFullmoveNumber()
        + (moves.size() + (board.initiallyWhiteToMove() ? 0 : 1)) / 2;
  }

  void move(Move move) {
    previousHalfmoveClocks.add(movesSinceCaptureOrPawnForward);
    if (move instanceof Promotion
        || move.getPiece().getPieceType() == PieceType.PAWN
        || move.getCapturedPiece() != null) movesSinceCaptureOrPawnForward = 0;
    else movesSinceCaptureOrPawnForward++;
    moves.add(move);
    recordPosition();
  }

  public List<Move> moves() {
    return List.copyOf(moves);
  }

  public Move getLastMove() {
    return moves.isEmpty() ? null : moves.getLast();
  }

  public boolean canRevertMove() {
    return !moves.isEmpty();
  }

  public void revertMove() {
    if (!canRevertMove()) return;
    if (encoder != null) {
      String removed = positions.removeLast();
      occurrences.compute(removed, (key, count) -> count == 1 ? null : count - 1);
    }
    Move move = moves.removeLast();
    board.reverseLastPerformedMove(move, true);
    movesSinceCaptureOrPawnForward = previousHalfmoveClocks.removeLast();
  }

  Position enPassantTarget() {
    if (moves.isEmpty()) return board.initialEnPassantTarget();
    Move last = getLastMove();
    if (!(last instanceof NormalMove) || last.getPiece().getPieceType() != PieceType.PAWN)
      return null;
    int from = last.getStartingPosition().getRow(), to = last.getTargetPosition().getRow();
    return Math.abs(from - to) == 2
        ? new Position((from + to) / 2, last.getTargetPosition().getCol())
        : null;
  }

  boolean checkForEnPassant(Piece piece) {
    Position target = enPassantTarget();
    if (target == null
        || piece == null
        || piece.getPieceType() != PieceType.PAWN
        || piece.getPieceColor() == board.getCurrentPlayerColor()) return false;
    int pawnRow = target.getRow() + (board.isWhiteToMove() ? 1 : -1);
    return piece == board.getPieceAtPosition(pawnRow, target.getCol());
  }
}
