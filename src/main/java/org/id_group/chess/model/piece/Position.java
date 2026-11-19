package org.id_group.chess.model.piece;

public class Position {
  private final int row;
  private final int col;

  public Position(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public boolean isOnBoard() {
    return col >= 0 && col <= 7 && row >= 0 && row <= 7;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  @Override
  public int hashCode() {
    return 8 * row + col;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Position pos)) return false;
    return pos.row == row && pos.col == col;
  }
}
