package org.id_group.chess.view;

public enum BoardTheme {
  CLASSIC("Klasyczny"),
  GREEN("Zielony"),
  BLUE("Niebieski"),
  PASTEL("Pastelowy");
  private final String label;

  BoardTheme(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
