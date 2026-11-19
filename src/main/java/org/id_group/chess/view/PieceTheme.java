package org.id_group.chess.view;

public enum PieceTheme {
  CALIFORNIA("California"),
  CBURNETT("Cburnett"),
  MERIDA("Merida");
  private final String label;

  PieceTheme(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
