package org.id_group.chess.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.id_group.chess.model.GameState;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.model.piece.Position;
import org.id_group.chess.viewmodel.SquareState;
import org.id_group.chess.viewmodel.ViewModel;

public class BoardDrawer {
  private final ViewModel viewModel;
  private final SquareColorProvider squareColorProvider;
  private final BoardState boardState;
  private final PieceImageProvider pieceImageProvider;

  BoardDrawer(
      ViewModel viewModel,
      SquareColorProvider squareColorProvider,
      BoardState boardState,
      PieceImageProvider pieceImageProvider) {
    this.viewModel = viewModel;
    this.squareColorProvider = squareColorProvider;
    this.boardState = boardState;
    this.pieceImageProvider = pieceImageProvider;
  }

  private void drawSquares(GraphicsContext gc) {
    int x = 0, y = 0;

    for (int i = 0; i < 8; i++) {
      x = 0;
      for (int j = 0; j < 8; j++) {
        int ri = boardState.isBoardInverted() ? 7 - i : i;
        int rj = boardState.isBoardInverted() ? 7 - j : j;

        SquareState squareState = viewModel.getSquareState(ri, rj);

        switch (squareState) {
          case SELECTED, SELECTED_IN_PROMOTION ->
              gc.setFill(squareColorProvider.getSelectedSquareColor(ri, rj));
          case PREVIOUS_MOVE_TO, PREVIOUS_MOVE_FROM ->
              gc.setFill(squareColorProvider.getPreviouslyMovedFromOrToSquareColor(ri, rj));
          case NORMAL -> gc.setFill(squareColorProvider.getNormalSquareColor(ri, rj));
        }

        gc.fillRect(x, y, boardState.getSquareWidth(), boardState.getSquareHeight());

        x += boardState.getSquareWidth();
      }
      y += boardState.getSquareHeight();
    }
  }

  private void drawNotDraggedPieces(GraphicsContext gc) {
    int x = 0, y = 0;

    for (int i = 0; i < 8; i++) {
      x = 0;
      for (int j = 0; j < 8; j++) {
        int ri = boardState.isBoardInverted() ? 7 - i : i;
        int rj = boardState.isBoardInverted() ? 7 - j : j;

        PieceType pieceType = viewModel.getPieceTypeAt(ri, rj);

        SquareState squareState = viewModel.getSquareState(ri, rj);

        if (pieceType != null
            && squareState != SquareState.SELECTED_IN_PROMOTION
            && (!boardState.isCurrentlyDragging() || squareState != SquareState.SELECTED)) {
          PieceColor pieceColor = viewModel.getPieceColorAt(ri, rj);

          gc.drawImage(
              pieceImageProvider.getPieceImage(pieceType, pieceColor),
              x + 0.05 * boardState.getSquareWidth(),
              y + 0.05 * boardState.getSquareHeight(),
              90,
              90);
        }

        x += boardState.getSquareWidth();
      }
      y += boardState.getSquareHeight();
    }
  }

  private void drawDraggedPiece(GraphicsContext gc) {
    if (!boardState.isCurrentlyDragging()) return;

    PieceType pieceType = viewModel.getSelectedPieceType();
    PieceColor pieceColor = viewModel.getSelectedPieceColor();

    if (pieceType != null) {
      gc.drawImage(
          pieceImageProvider.getPieceImage(pieceType, pieceColor),
          boardState.getDragX()
              + 0.05 * boardState.getSquareWidth()
              - boardState.getSquareWidth() / 2.0,
          boardState.getDragY()
              + 0.05 * boardState.getSquareHeight()
              - boardState.getSquareHeight() / 2.0,
          90,
          90);
    }
  }

  private void drawPossibleMovesWithFreeSquares(GraphicsContext gc) {
    for (Position pos : viewModel.getPossibleMovesWithFreeSquares()) {
      int row = pos.getRow(), col = pos.getCol();

      if (boardState.isBoardInverted()) {
        row = 7 - row;
        col = 7 - col;
      }

      double radius = 15;
      gc.setFill(Color.rgb(0, 0, 0, 0.25));
      gc.fillOval(
          (0.5 + col) * boardState.getSquareWidth() - radius,
          (0.5 + row) * boardState.getSquareHeight() - radius,
          2 * radius,
          2 * radius);
    }
  }

  private void drawPossibleMovesWithNotFreeSquares(GraphicsContext gc) {
    for (Position pos : viewModel.getPossibleMovesWithNotFreeSquares()) {
      int row = pos.getRow(), col = pos.getCol();

      if (boardState.isBoardInverted()) {
        row = 7 - row;
        col = 7 - col;
      }

      double radius = boardState.getSquareHeight() / 2.0 - 6;
      gc.setStroke(Color.rgb(0, 0, 0, 0.35));
      gc.setLineWidth(8);
      gc.strokeOval(
          (0.5 + col) * boardState.getSquareWidth() - radius,
          (0.5 + row) * boardState.getSquareHeight() - radius,
          2 * radius,
          2 * radius);
    }
  }

  private void drawPromotingPieceTypes(GraphicsContext gc) {
    if (!viewModel.isActivePromotion()) return;

    PieceColor color = viewModel.getCurrentPlayerColor();
    int col = viewModel.getPromotingColumn();

    if (boardState.isBoardInverted()) col = 7 - col;

    double xPos = boardState.getSquareWidth() * col + 0.05 * boardState.getSquareWidth();
    double yPos, offset;

    boolean topAligned =
        (color == PieceColor.WHITE_PIECE && !boardState.isBoardInverted())
            || (color == PieceColor.BLACK_PIECE && boardState.isBoardInverted());

    if (topAligned) {
      yPos = 0;
      offset = boardState.getSquareHeight();
    } else {
      yPos = 7 * boardState.getSquareHeight();
      offset = -boardState.getSquareHeight();
    }

    double overlayX = boardState.getSquareWidth() * col;
    double overlayY = topAligned ? 0 : 4 * boardState.getSquareHeight();
    double overlayWidth = boardState.getSquareWidth();
    double overlayHeight = 4 * boardState.getSquareHeight();

    DropShadow shadow = new DropShadow();
    shadow.setRadius(10);
    shadow.setSpread(0.4);
    shadow.setColor(Color.rgb(0, 0, 0, 0.4));

    gc.save();
    gc.setEffect(shadow);
    gc.setFill(Color.WHITE);
    gc.fillRect(overlayX, overlayY, overlayWidth, overlayHeight);
    gc.restore();

    yPos += 0.05 * boardState.getSquareHeight();

    for (PieceType type :
        new PieceType[] {PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP}) {
      gc.drawImage(pieceImageProvider.getPieceImage(type, color), xPos, yPos, 90, 90);
      yPos += offset;
    }
  }

  private void drawEndGameSquareOverlay(GraphicsContext gc, Position p, Color color) {
    p = visualPosition(p);
    double x = p.getCol() * boardState.getSquareWidth();
    double y = p.getRow() * boardState.getSquareHeight();

    gc.setFill(color);
    gc.fillRect(x, y, boardState.getSquareWidth(), boardState.getSquareHeight());
  }

  private void drawEndGameDetails(GraphicsContext gc) {
    GameState state = viewModel.getGameState();
    if (state == GameState.NORMAL) return;
    if (state == GameState.CHECKMATE_WHITE || state == GameState.CHECKMATE_BLACK) {
      PieceColor winner =
          state == GameState.CHECKMATE_WHITE ? PieceColor.BLACK_PIECE : PieceColor.WHITE_PIECE;
      PieceColor loser =
          winner == PieceColor.WHITE_PIECE ? PieceColor.BLACK_PIECE : PieceColor.WHITE_PIECE;
      drawWinningKingCircle(gc, viewModel.getKingPosition(winner));
      drawLosingKingCircle(gc, viewModel.getKingPosition(loser), loser);
    } else {
      drawDrawingKingCircle(gc, viewModel.getKingPosition(PieceColor.WHITE_PIECE));
      drawDrawingKingCircle(gc, viewModel.getKingPosition(PieceColor.BLACK_PIECE));
    }
  }

  private void drawWinningKingCircle(GraphicsContext gc, Position p) {
    p = visualPosition(p);
    drawCircle(gc, p, Color.rgb(60, 160, 70));

    double x = (p.getCol() + 1) * boardState.getSquareWidth();
    double y = p.getRow() * boardState.getSquareHeight();
    double radius = 32;

    x -= radius / 2;
    y += 14;

    double cx = x;
    double cy = y;
    double size = 12;

    double baseY = cy + size * 0.45;
    double dipY = cy;
    double tipLowY = cy - size * 0.25;
    double tipHighY = cy - size * 0.65;

    double[] xPoints = {
      cx - size, cx - size, cx - size * 0.5, cx, cx + size * 0.5, cx + size, cx + size
    };
    double[] yPoints = {baseY, tipLowY, dipY, tipHighY, dipY, tipLowY, baseY};

    gc.setFill(Color.WHITE);
    gc.fillPolygon(xPoints, yPoints, xPoints.length);
    gc.fillRect(cx - size, baseY, 2 * size, size * 0.22);

    double jewelRadius = size * 0.09;
    gc.setFill(Color.rgb(60, 160, 70));
    gc.fillOval(cx - size - jewelRadius / 2, tipLowY - jewelRadius / 2, jewelRadius, jewelRadius);
    gc.fillOval(cx - jewelRadius / 2, tipHighY - jewelRadius / 2, jewelRadius, jewelRadius);
    gc.fillOval(cx + size - jewelRadius / 2, tipLowY - jewelRadius / 2, jewelRadius, jewelRadius);
  }

  private void drawLosingKingCircle(GraphicsContext gc, Position p, PieceColor color) {
    p = visualPosition(p);
    drawCircle(gc, p, Color.rgb(200, 45, 45));

    double x = (p.getCol() + 1) * boardState.getSquareWidth();
    double y = p.getRow() * boardState.getSquareHeight();
    double radius = 32;

    x -= radius / 2;
    y += 17;

    Image kingImage = pieceImageProvider.getPieceImage(PieceType.KING, color);

    double imgSize = radius * 0.8;

    gc.save();
    gc.translate(x, y);
    gc.rotate(-90);
    gc.drawImage(kingImage, -imgSize / 2, -imgSize / 2, imgSize, imgSize);
    gc.restore();
  }

  private void drawDrawingKingCircle(GraphicsContext gc, Position p) {
    p = visualPosition(p);
    drawCircle(gc, p, Color.rgb(110, 110, 110));

    double x = (p.getCol() + 1) * boardState.getSquareWidth();
    double y = p.getRow() * boardState.getSquareHeight();
    double radius = 32;

    x -= radius / 2;
    y += 14;

    gc.save();
    gc.translate(x, y);
    gc.setFill(Color.WHITE);
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setTextBaseline(VPos.CENTER);
    gc.setFont(Font.font(radius * 0.8));
    gc.fillText("½", 0, 0);
    gc.restore();
  }

  private void drawCircle(GraphicsContext gc, Position p, Color color) {
    double x = (p.getCol() + 1) * boardState.getSquareWidth();
    double y = p.getRow() * boardState.getSquareHeight();
    double radius = 32;

    gc.setFill(color);
    gc.fillOval(x - radius, y, radius, radius);
  }

  private Position visualPosition(Position position) {
    return boardState.isBoardInverted()
        ? new Position(7 - position.getRow(), 7 - position.getCol())
        : position;
  }

  private void drawCoordinates(GraphicsContext gc) {
    gc.setFont(Font.font("System", 13));
    gc.setTextAlign(TextAlignment.LEFT);
    gc.setTextBaseline(VPos.TOP);
    for (int i = 0; i < 8; i++) {
      gc.setFill(squareColorProvider.getNormalSquareColor(i, 1));
      gc.fillText(Integer.toString(boardState.isBoardInverted() ? i + 1 : 8 - i), 4, i * 100 + 3);
      gc.setFill(squareColorProvider.getNormalSquareColor(6, i));
      gc.fillText(
          String.valueOf((char) ('a' + (boardState.isBoardInverted() ? 7 - i : i))),
          i * 100 + 86,
          782);
    }
  }

  void drawBoard(Canvas canvas) {
    if (canvas.getWidth() <= 0) return;
    GraphicsContext gc = canvas.getGraphicsContext2D();
    double scale = canvas.getWidth() / 800;
    double outputScale =
        canvas.getScene() == null || canvas.getScene().getWindow() == null
            ? 1
            : canvas.getScene().getWindow().getOutputScaleX();
    pieceImageProvider.setPixelSize(90 * scale * outputScale);
    gc.save();
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    gc.scale(scale, scale);
    drawSquares(gc);
    if (viewModel.isCheck())
      drawEndGameSquareOverlay(
          gc,
          viewModel.getKingPosition(viewModel.getCurrentPlayerColor()),
          Color.rgb(210, 60, 60, 0.65));
    drawCoordinates(gc);
    drawNotDraggedPieces(gc);
    if (!viewModel.isActivePromotion()) {
      drawPossibleMovesWithFreeSquares(gc);
      drawPossibleMovesWithNotFreeSquares(gc);
    }
    drawDraggedPiece(gc);
    drawPromotingPieceTypes(gc);
    drawEndGameDetails(gc);
    gc.restore();
  }
}
