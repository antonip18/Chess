package org.id_group.chess.model.move;

import java.util.Objects;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.Position;

public class EnPassant extends Move {
  private final Piece pawn;
  private final Piece capturedPawn;
  private final Position startingPosition;
  private final Position targetPosition;

  public EnPassant(
      Piece pawn, Piece capturedPawn, Position targetPosition, Position startingPosition) {
    this.pawn = pawn;
    this.capturedPawn = capturedPawn;
    this.startingPosition = startingPosition;
    this.targetPosition = targetPosition;
  }

  public Piece getPawn() {
    return pawn;
  }

  public Piece getCapturedPawn() {
    return capturedPawn;
  }

  @Override
  public Position getStartingPosition() {
    return startingPosition;
  }

  @Override
  public Position getTargetPosition() {
    return targetPosition;
  }

  @Override
  public Piece getPiece() {
    return pawn;
  }

  @Override
  public Piece getCapturedPiece() {
    return capturedPawn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pawn, targetPosition);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EnPassant mov)) return false;
    return mov.pawn == pawn && targetPosition.equals(mov.targetPosition);
  }
}
