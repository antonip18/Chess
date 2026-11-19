package org.id_group.chess.view;

import javafx.scene.paint.Color;

public class SquareColorProvider {
  private final Color[][] schemes = {
    // Klasyczny
    {Color.web("#F0D9B5"), Color.web("#B58863")},

    // Zielony
    {Color.web("#EEEED2"), Color.web("#769656")},

    // Niebieski
    {Color.web("#D8E9F0"), Color.web("#5B8FA8")},

    // Kingusiowy
    {Color.web("#F4B4D3"), Color.web("#BEAAE6")}
  };

  private final Color[][] selectedSchemes = {
    // Klasyczny
    {Color.web("#F6F669"), Color.web("#BACA44")},

    // Zielony
    {Color.web("#F6F669"), Color.web("#BACA44")},

    // Niebieski
    {Color.web("#F6F669"), Color.web("#BACA44")},

    // Kingusiowy
    {Color.web("#F6F669"), Color.web("#BACA44")}
  };

  private final Color[][] lastMoveSchemes = {
    // Klasyczny
    {Color.web("#D7D96B"), Color.web("#A9A83A")},

    // Zielony
    {Color.web("#D7D96B"), Color.web("#A9A83A")},

    // Niebieski
    {Color.web("#D7D96B"), Color.web("#A9A83A")},

    // Kingusiowy
    {Color.web("#D7D96B"), Color.web("#A9A83A")}
  };

  private final BoardState boardState;

  SquareColorProvider(BoardState boardState) {
    this.boardState = boardState;
  }

  Color getNormalSquareColor(int row, int col) {
    return schemes[boardState.getCurrentBoardTheme().ordinal()][(row + col) % 2];
  }

  Color getSelectedSquareColor(int row, int col) {
    return selectedSchemes[boardState.getCurrentBoardTheme().ordinal()][(row + col) % 2];
  }

  Color getPreviouslyMovedFromOrToSquareColor(int row, int col) {
    return lastMoveSchemes[boardState.getCurrentBoardTheme().ordinal()][(row + col) % 2];
  }
}
