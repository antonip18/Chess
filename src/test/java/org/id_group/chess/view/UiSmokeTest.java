package org.id_group.chess.view;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.id_group.chess.model.Board;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Real JavaFX window and mouse input. Enable with -Dchess.ui=true on a desktop/Xvfb. */
@EnabledIfSystemProperty(named = "chess.ui", matches = "true")
class UiSmokeTest {
  static Stage stage;
  static Robot robot;

  @BeforeAll
  static void start() throws Exception {
    CountDownLatch ready = new CountDownLatch(1);
    Platform.startup(ready::countDown);
    assertTrue(ready.await(10, TimeUnit.SECONDS));
    fx(
        () -> {
          Platform.setImplicitExit(false);
          stage = new Stage();
          new ChessView().start(stage);
          robot = new Robot();
          return null;
        });
    pulse();
  }

  @AfterAll
  static void close() throws Exception {
    if (stage != null)
      fx(
          () -> {
            stage.close();
            return null;
          });
    Platform.exit();
  }

  @Test
  void playsResizesFlipsPromotesAndRestarts() throws Exception {
    assertEquals(Board.INITIAL_FEN, fen());
    square(6, 4);
    square(4, 4);
    assertTrue(fen().startsWith("rnbqkbnr/pppppppp/8/8/4P3"));
    fire("undo");
    assertEquals(Board.INITIAL_FEN, fen());
    drag(6, 3, 4, 3);
    assertTrue(fen().contains("3P4"));
    fire("undo");
    fire("flip");
    square(1, 3);
    square(3, 3); // e2-e4 seen from Black's side
    assertTrue(fen().contains("4P3"));
    fire("undo");
    fire("flip");
    fx(
        () -> {
          stage.setWidth(940);
          stage.setHeight(760);
          return null;
        });
    pulse();
    square(6, 4);
    square(4, 4);
    assertTrue(fen().contains("4P3"));
    screenshot("ui-small.png");
    fire("undo");
    for (PieceTheme theme : PieceTheme.values()) {
      fx(
          () -> {
            ((ComboBox<PieceTheme>) node("piece-theme")).setValue(theme);
            return null;
          });
      pulse();
    }
    load("7k/P7/8/8/8/8/8/7K w - - 0 1");
    square(1, 0);
    square(0, 0);
    assertTrue(fen().startsWith("7k/P7")); // no mutation before choosing a piece
    square(0, 0);
    assertTrue(fen().startsWith("Q6k/8"));
    fire("undo");
    assertTrue(fen().startsWith("7k/P7"));
    fire("flip");
    square(6, 7);
    square(7, 7);
    square(6, 7); // knight underpromotion
    assertTrue(fen().startsWith("N6k/8"));
    assertTrue(fx(() -> node("game-end").isVisible()));
    fire("restart");
    assertEquals(Board.INITIAL_FEN, fen());
    fire("flip");
    square(6, 5);
    square(5, 5);
    square(1, 4);
    square(3, 4);
    square(6, 6);
    square(4, 6);
    square(0, 3);
    square(4, 7);
    assertEquals("Czarne wygrały", fx(() -> ((Label) node("result")).getText()));
    screenshot("ui-checkmate.png");
    fire("restart");
    assertEquals(Board.INITIAL_FEN, fen());
    square(6, 4);
    square(4, 4);
    Platform.runLater(() -> ((Button) node("new-game")).fire());
    pulse();
    dismissDialog(ButtonType.OK);
    assertEquals(Board.INITIAL_FEN, fen());
    fx(
        () -> {
          stage.setWidth(1140);
          stage.setHeight(880);
          ((ComboBox<PieceTheme>) node("piece-theme")).setValue(PieceTheme.CALIFORNIA);
          return null;
        });
    pulse();
    square(6, 4);
    square(4, 4);
    square(1, 4);
    square(3, 4);
    square(7, 6);
    square(5, 5);
    square(0, 1);
    square(2, 2);
    square(7, 5);
    square(4, 2);
    square(0, 6);
    square(2, 5);
    screenshot("ui-preview.png");
    assertTrue(fx(() -> node("move-history").getBoundsInLocal().getHeight() > 70));
  }

  static Node node(String id) {
    return stage.getScene().lookup("#" + id);
  }

  static void fire(String id) throws Exception {
    fx(
        () -> {
          ((Button) node(id)).fire();
          return null;
        });
    pulse();
  }

  static String fen() throws Exception {
    fire("copy-fen");
    return fx(() -> Clipboard.getSystemClipboard().getString());
  }

  static void square(int row, int col) throws Exception {
    fx(
        () -> {
          Point2D p = point(row, col);
          robot.mouseMove(p);
          robot.mouseClick(MouseButton.PRIMARY);
          return null;
        });
    pulse();
  }

  static Point2D point(int row, int col) {
    Canvas board = (Canvas) node("board");
    return board.localToScreen(
        (col + .5) * board.getWidth() / 8, (row + .5) * board.getHeight() / 8);
  }

  static void drag(int fromRow, int fromCol, int toRow, int toCol) throws Exception {
    fx(
        () -> {
          robot.mouseMove(point(fromRow, fromCol));
          robot.mousePress(MouseButton.PRIMARY);
          return null;
        });
    pulse();
    fx(
        () -> {
          robot.mouseMove(point(toRow, toCol));
          return null;
        });
    pulse();
    fx(
        () -> {
          robot.mouseRelease(MouseButton.PRIMARY);
          return null;
        });
    pulse();
  }

  static void load(String fen) throws Exception {
    Platform.runLater(() -> ((Button) node("load-fen")).fire());
    pulse();
    fx(
        () -> {
          ((TextField) dialog().lookup(".text-field")).setText(fen);
          return null;
        });
    dismissDialog(ButtonType.OK);
  }

  static DialogPane dialog() {
    return (DialogPane)
        Window.getWindows().stream()
            .filter(w -> w != stage && w.isShowing())
            .findFirst()
            .orElseThrow()
            .getScene()
            .getRoot();
  }

  static void dismissDialog(ButtonType type) throws Exception {
    fx(
        () -> {
          ((Button) dialog().lookupButton(type)).fire();
          return null;
        });
    pulse();
  }

  static void screenshot(String name) throws Exception {
    WritableImage image = fx(() -> stage.getScene().snapshot(null));
    BufferedImage out =
        new BufferedImage(
            (int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    int[] pixels = new int[out.getWidth() * out.getHeight()];
    image
        .getPixelReader()
        .getPixels(
            0,
            0,
            out.getWidth(),
            out.getHeight(),
            javafx.scene.image.PixelFormat.getIntArgbInstance(),
            pixels,
            0,
            out.getWidth());
    out.setRGB(0, 0, out.getWidth(), out.getHeight(), pixels, 0, out.getWidth());
    Files.createDirectories(Path.of("target"));
    ImageIO.write(out, "png", Path.of("target", name).toFile());
  }

  static void pulse() throws Exception {
    Thread.sleep(140);
    fx(() -> null);
  }

  static <T> T fx(Supplier<T> action) throws Exception {
    FutureTask<T> task = new FutureTask<>(action::get);
    Platform.runLater(task);
    return task.get(20, TimeUnit.SECONDS);
  }
}
