package org.id_group.chess.view;

public class BoardState {
  private final int squareHeight = 100;
  private final int squareWidth = 100;
  private boolean currentlyDragging = false;

  private boolean boardInverted = false;

  void invertBoard() {
    boardInverted = !boardInverted;
  }

  boolean isBoardInverted() {
    return boardInverted;
  }

  boolean isCurrentlyDragging() {
    return currentlyDragging;
  }

  void setDragging(boolean dragging) {
    currentlyDragging = dragging;
  }

  int getSquareWidth() {
    return squareWidth;
  }

  int getSquareHeight() {
    return squareHeight;
  }

  private PieceTheme currentPieceTheme = PieceTheme.CALIFORNIA;
  private BoardTheme currentBoardTheme = BoardTheme.CLASSIC;

  PieceTheme getCurrentPieceTheme() {
    return currentPieceTheme;
  }

  BoardTheme getCurrentBoardTheme() {
    return currentBoardTheme;
  }

  void setBoardTheme(BoardTheme theme) {
    currentBoardTheme = theme;
  }

  void setPieceTheme(PieceTheme theme) {
    currentPieceTheme = theme;
  }

  double dragX, dragY;

  double getDragX() {
    return dragX;
  }

  double getDragY() {
    return dragY;
  }

  void setDragX(double dragX) {
    this.dragX = dragX;
  }

  void setDragY(double dragY) {
    this.dragY = dragY;
  }
}
