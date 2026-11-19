package org.id_group.chess.view;

import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.id_group.chess.model.piece.*;
import org.id_group.chess.viewmodel.ViewModel;

public final class ChessView extends Application {
  private final ViewModel viewModel = new ViewModel();
  private final BoardState boardState = new BoardState();
  private final BoardDrawer drawer =
      new BoardDrawer(
          viewModel,
          new SquareColorProvider(boardState),
          boardState,
          new PieceImageProvider(boardState));
  private final BoardHitTester hitTester = new BoardHitTester(boardState, viewModel);
  private final Canvas canvas = new Canvas(800, 800);
  private GameEndOverlay endOverlay;
  private Stage stage;
  private boolean dragCandidate;
  private double pressX, pressY;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    canvas.setId("board");
    canvas.setFocusTraversable(true);
    canvas.setManaged(false);
    endOverlay = new GameEndOverlay(this::newGame);
    StackPane board =
        new StackPane(canvas, endOverlay) {
          @Override
          protected void layoutChildren() {
            double size = Math.max(0, Math.min(getWidth(), getHeight()));
            canvas.setWidth(size);
            canvas.setHeight(size);
            canvas.relocate((getWidth() - size) / 2, (getHeight() - size) / 2);
            endOverlay.resizeRelocate(
                (getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
            redraw();
          }
        };
    board.setMinSize(0, 0);
    board.setPrefSize(800, 800);
    GameControlPanel menu = new GameControlPanel(viewModel);
    menu.getNewGameButton().setOnAction(e -> confirmNewGame());
    menu.getUndoButton().setOnAction(e -> undo());
    menu.getInvertBoardButton().setOnAction(e -> flip());
    menu.getBoardThemes()
        .setOnAction(
            e -> {
              boardState.setBoardTheme(menu.getBoardThemes().getValue());
              redraw();
            });
    menu.getPieceThemes()
        .setOnAction(
            e -> {
              boardState.setPieceTheme(menu.getPieceThemes().getValue());
              redraw();
            });
    menu.getCopyFenButton()
        .setOnAction(
            e -> {
              ClipboardContent content = new ClipboardContent();
              content.putString(viewModel.toFen());
              Clipboard.getSystemClipboard().setContent(content);
            });
    menu.getLoadFenButton().setOnAction(e -> loadFen());
    BorderPane root = new BorderPane(board);
    root.setPadding(new Insets(20));
    root.setRight(menu);
    BorderPane.setMargin(menu, new Insets(0, 0, 0, 20));
    Scene scene = new Scene(root, 1140, 840);
    scene
        .getStylesheets()
        .add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
    scene.addEventFilter(
        KeyEvent.KEY_PRESSED,
        e -> {
          if (e.getTarget() instanceof TextInputControl) return;
          if (e.getCode() == KeyCode.ESCAPE) {
            cancelDrag();
            viewModel.clearSelection();
            endOverlay.hide();
          } else if (e.isShortcutDown() && e.getCode() == KeyCode.Z) undo();
          else if (e.isShortcutDown() && e.getCode() == KeyCode.N) confirmNewGame();
          else if (!e.isControlDown() && !e.isAltDown() && e.getCode() == KeyCode.F) flip();
          else return;
          e.consume();
        });
    configureMouse();
    viewModel.revisionProperty().addListener((o, a, b) -> redraw());
    viewModel
        .gameStateProperty()
        .addListener(
            (o, oldState, state) -> {
              switch (state) {
                case NORMAL -> endOverlay.hide();
                case CHECKMATE_WHITE -> endOverlay.showWin(PieceColor.BLACK_PIECE);
                case CHECKMATE_BLACK -> endOverlay.showWin(PieceColor.WHITE_PIECE);
                default -> endOverlay.showDraw(state);
              }
            });
    stage.setTitle("Szachy · JavaFX");
    stage.setMinWidth(900);
    stage.setMinHeight(740);
    stage.setScene(scene);
    stage.outputScaleXProperty().addListener((o, a, b) -> redraw());
    stage
        .focusedProperty()
        .addListener(
            (o, a, focused) -> {
              if (!focused) {
                cancelDrag();
                redraw();
              }
            });
    stage.show();
  }

  private void configureMouse() {
    canvas.setOnMousePressed(
        e -> {
          if (e.getButton() != MouseButton.PRIMARY) return;
          canvas.requestFocus();
          cancelDrag();
          double x = logical(e.getX()), y = logical(e.getY());
          if (viewModel.isActivePromotion()) {
            PieceType type = hitTester.getPromotionPieceType(x, y);
            if (type != null) viewModel.finishPromotion(type);
            return;
          }
          dragCandidate =
              viewModel.selectSquare(hitTester.getClickedRow(y), hitTester.getClickedCol(x));
          pressX = x;
          pressY = y;
        });
    canvas.setOnMouseDragged(
        e -> {
          if (!dragCandidate || !e.isPrimaryButtonDown()) return;
          double x = logical(e.getX()), y = logical(e.getY());
          if (Math.hypot(x - pressX, y - pressY) > 4) boardState.setDragging(true);
          boardState.setDragX(x);
          boardState.setDragY(y);
          redraw();
        });
    canvas.setOnMouseReleased(
        e -> {
          boolean wasDragging = boardState.isCurrentlyDragging();
          cancelDrag();
          if (wasDragging)
            viewModel.moveSelectedPiece(
                hitTester.getClickedRow(logical(e.getY())),
                hitTester.getClickedCol(logical(e.getX())));
          redraw();
        });
  }

  private double logical(double coordinate) {
    return coordinate * 800 / canvas.getWidth();
  }

  private void redraw() {
    drawer.drawBoard(canvas);
  }

  private void cancelDrag() {
    dragCandidate = false;
    boardState.setDragging(false);
  }

  private void newGame() {
    cancelDrag();
    endOverlay.hide();
    viewModel.newGame();
  }

  private void undo() {
    cancelDrag();
    viewModel.undoLastMove();
  }

  private void flip() {
    cancelDrag();
    boardState.invertBoard();
    redraw();
  }

  private void confirmNewGame() {
    if (viewModel.moveHistory().isEmpty()
        && viewModel.toFen().equals(org.id_group.chess.model.Board.INITIAL_FEN)) {
      newGame();
      return;
    }
    Alert dialog =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "Bieżąca partia zostanie wyczyszczona.",
            ButtonType.CANCEL,
            ButtonType.OK);
    dialog.initOwner(stage);
    dialog.setTitle("Nowa gra");
    dialog.setHeaderText("Rozpocząć nową partię?");
    dialog.showAndWait().filter(ButtonType.OK::equals).ifPresent(button -> newGame());
  }

  private void loadFen() {
    TextInputDialog dialog = new TextInputDialog(viewModel.toFen());
    dialog.initOwner(stage);
    dialog.setTitle("Wczytaj pozycję FEN");
    dialog.setHeaderText("Nowa pozycja zastąpi partię i historię ruchów.");
    dialog.setContentText("FEN:");
    dialog.getDialogPane().setPrefWidth(650);
    dialog
        .showAndWait()
        .ifPresent(
            fen -> {
              try {
                cancelDrag();
                viewModel.loadFen(fen);
              } catch (IllegalArgumentException exception) {
                Alert error =
                    new Alert(
                        Alert.AlertType.ERROR,
                        "Niepoprawna pozycja FEN. " + exception.getMessage(),
                        ButtonType.OK);
                error.initOwner(stage);
                error.setHeaderText("Nie udało się wczytać pozycji");
                error.showAndWait();
              }
            });
  }
}
