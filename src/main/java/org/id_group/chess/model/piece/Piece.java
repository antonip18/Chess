package org.id_group.chess.model.piece;

public class Piece {
  private Position position;
  private final PieceColor color;
  private PieceType type;
  private int numMoves = 0;

  public Piece(PieceType type, PieceColor color, int row, int col) {
    this.type = type;
    this.color = color;
    this.position = new Position(row, col);
  }

  public Piece(PieceType type, PieceColor color, Position position) {
    this.type = type;
    this.color = color;
    this.position = position;
  }

  public int getCol() {
    return position.getCol();
  }

  public int getRow() {
    return position.getRow();
  }

  public PieceType getPieceType() {
    return type;
  }

  public Position getPosition() {
    return position;
  }

  public PieceColor getPieceColor() {
    return color;
  }

  public boolean hasMoved() {
    return numMoves > 0;
  }

  public void decreaseMoveNumber() {
    numMoves--;
  }

  public void increaseMoveNumber() {
    numMoves++;
  }

  public void move(int row, int col) {
    this.position = new Position(row, col);
  }

  public void move(Position position) {
    this.position = position;
  }

  public void promote(PieceType type) {
    if (type == PieceType.PAWN || type == PieceType.KING) throw new IllegalArgumentException();
    this.type = type;
  }

  public void revertPromotion() {
    this.type = PieceType.PAWN;
  }

  private boolean captured = false;

  public boolean hasBeenCaptured() {
    return captured;
  }

  public void capturePiece() {
    captured = true;
  }

  public void uncapturePiece() {
    captured = false;
  }
}
