package me.Proj.Picross.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import me.Proj.Picross.DragHandler;
import me.Proj.Picross.GameLauncher;
import me.Proj.Picross.GameManager;
import me.Proj.Picross.Main;
import me.Proj.Picross.Nonogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static me.Proj.Picross.GameLauncher.GameScene;

public class GameController
{
	private static final Color UNKNOWN_PANE_COLOR = Color.web("#FFFFFF");
	private static final Color HOVER_PANE_COLOR = Color.web("#8C8973");
	private static final Color WRONG_PANE_COLOR = Color.web("#FA4646");
	private static final Color BORDER_COLOR = Color.web("#222222");
	private static final Color FONT_COLOR = Color.web("#FFFFFF");

	private static final Font NUMBER_FONT = new Font("Cambria", 24);

	@FXML
	private AnchorPane backgroundPane;

	@FXML
	private GridPane grid_5;
	@FXML
	private GridPane grid_10;
	@FXML
	private GridPane grid_15;

	@FXML
	private ImageView image;
	@FXML
	private ImageView lever;

	private boolean isTransitioning = false;
	private final List<Node> addedNodes = new ArrayList<>();

	private static LeverState state = LeverState.O;
	private static Pane[][] paneEntries;
	private static ImageView[][] flags;
	private static final Image FLAG_IMAGE;
	private static final List<Pair<Integer, Integer>> O_TO_X, X_TO_R;
	private static final List<ImageView> HEART_PENS = new ArrayList<>();

	static {
		FLAG_IMAGE = new Image(Main.getResource("/assets/background/flag.png").toString());
		O_TO_X = new ArrayList<>();
		O_TO_X.add(new Pair<>(950, 210));
		O_TO_X.add(new Pair<>(1000, 210));
		O_TO_X.add(new Pair<>(1000, 300));
		O_TO_X.add(new Pair<>(970, 320));

		X_TO_R = new ArrayList<>();
		X_TO_R.add(new Pair<>(970, 320));
		X_TO_R.add(new Pair<>(950, 360));
		X_TO_R.add(new Pair<>(935, 360));
		X_TO_R.add(new Pair<>(935, 390));
		X_TO_R.add(new Pair<>(975, 390));
		X_TO_R.add(new Pair<>(975, 450));
	}

	private void initializeGrid(GridPane grid, int size) {
		paneEntries = new Pane[size][size];

		grid.setVisible(true);
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				Pane pane = new Pane();
				pane.setBackground(new Background(new BackgroundFill(UNKNOWN_PANE_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
				pane.setOnMouseEntered(e -> onMouseEntered(pane));
				pane.setOnMouseExited(e -> onMouseExited(pane));

				DragHandler.makeDraggable(pane);
				grid.add(pane, col, row);
				paneEntries[row][col] = pane;
			}
		}
	}

	private void initializeFlag(GridPane grid, int size) {
		flags = new ImageView[size][size];
		double posX = grid.getLayoutX(), posY = grid.getLayoutY();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				ImageView view = new ImageView(FLAG_IMAGE);
				int flagSize = (int) (grid.getPrefWidth() / size);
				view.setFitWidth(flagSize);
				view.setFitHeight(flagSize);
				view.setLayoutX(posX + flagSize * y);
				view.setLayoutY(posY + flagSize * x);
				view.setVisible(false);

				int finalX = x, finalY = y;
				view.setOnMouseClicked(e -> {
					if (state == LeverState.R) {
						view.setVisible(false);
						GameManager.unmarkFlag(finalX, finalY);
					}
				});

				flags[x][y] = view;
				backgroundPane.getChildren().add(view);
				addedNodes.add(view);
			}
		}
	}

	private void spawnHeartPens() {
		int posX = 760, posY = 700;
		Image heartPen =  new Image(Main.getResource("/assets/background/heartpen.png").toString());
		for (int i = 0; i < GameManager.MAX_HEALTH; i++) {
			ImageView heart = new ImageView(heartPen);
			heart.setLayoutX(posX + i * 80);
			heart.setLayoutY(posY);
			heart.setFitWidth(100);
			heart.setFitHeight(150);
			backgroundPane.getChildren().add(heart);
			HEART_PENS.add(heart);
		}
	}

	public static void removeHeartPen() {
		int index = GameManager.getCurrentHealth();
		HEART_PENS.get(index).setVisible(false);
	}

	private void drawLines(GridPane grid, int size) {
		double layoutX = grid.getLayoutX(), layoutY = grid.getLayoutY();
		int paneSize = (int) (grid.getPrefWidth() / size);
		// spawn vertical lines
		for (int i = 0; i <= size; i++) {
			Line line = new Line(0, -160, 0, grid.getPrefHeight());
			line.setLayoutX(layoutX + i * paneSize);
			line.setLayoutY(layoutY);
			line.setStroke(BORDER_COLOR);
			backgroundPane.getChildren().add(line);
			addedNodes.add(line);
		}

		// spawn horizontal lines
		for (int i = 0; i <= size; i++) {
			Line line = new Line(-160, 0, grid.getPrefWidth(), 0);
			line.setLayoutX(layoutX);
			line.setLayoutY(layoutY + i * paneSize);
			line.setStroke(BORDER_COLOR);
			backgroundPane.getChildren().add(line);
			addedNodes.add(line);
		}
	}

	private void generateText(Nonogram level, GridPane grid, int size) {
		double layoutX = grid.getLayoutX(), layoutY = grid.getLayoutY();
		int paneSize = (int) (grid.getPrefWidth() / size);
		ArrayList<ArrayList<Integer>> rowPanes = level.getRowPanes(), colPanes = level.getColPanes();

		int MAGIC_X, MAGIC_Y;
		switch (size) {
			case 5:
				MAGIC_X = 55;
				MAGIC_Y = 70;
				break;
			case 10:
				MAGIC_X = 25;
				MAGIC_Y = 40;
				break;
			case 15:
				MAGIC_X = 15;
				MAGIC_Y = 30;
				break;
			default:
				System.out.println("[GridController] Invalid input size.");
				return;
		}

		// generate text for rows
		for (int i = 0; i < size; i++) {
			ArrayList<Integer> rowCount = rowPanes.get(i);
			ListIterator<Integer> it = rowCount.listIterator(rowCount.size());
			int offsetX = 0;
			while (it.hasPrevious()) {
				int c = it.previous();
				Text text = new Text(Integer.toString(c));
				if (c >= 10)
					offsetX -= 15;
				text.setLayoutX(layoutX + offsetX - 20);
				text.setLayoutY(layoutY + i * paneSize + MAGIC_Y);
				text.setFont(NUMBER_FONT);
				text.setFill(FONT_COLOR);
				backgroundPane.getChildren().add(text);
				addedNodes.add(text);
				offsetX -= 20;
			}
		}

		// generate text for columns
		for (int i = 0; i < size; i++) {
			ArrayList<Integer> colCount = colPanes.get(i);
			ListIterator<Integer> it = colCount.listIterator(colCount.size());
			int offsetX = 0, offsetY = 0;
			while (it.hasPrevious()) {
				int c = it.previous();
				Text text = new Text(Integer.toString(c));
				if (c >= 10)
					offsetX -= 10;
				text.setLayoutX(layoutX + i * paneSize + MAGIC_X + offsetX);
				text.setLayoutY(layoutY + offsetY - 10);
				text.setFont(NUMBER_FONT);
				text.setFill(FONT_COLOR);
				backgroundPane.getChildren().add(text);
				addedNodes.add(text);
				offsetY -= 25;
			}
		}
	}

	public void resetNodes() {
		backgroundPane.getChildren().removeAll(addedNodes);
		backgroundPane.getChildren().removeAll(HEART_PENS);
		HEART_PENS.clear();

		grid_5.setVisible(false);
		grid_5.getChildren().clear();

		grid_10.setVisible(false);
		grid_10.getChildren().clear();

		grid_15.setVisible(false);
		grid_15.getChildren().clear();
	}

	public enum LeverState
	{
		O,
		X,
		R
	}

	public static LeverState getLeverState() {
		return state;
	}

	@FXML
	private void handleClickLever() {
		if (isTransitioning)
			return;
		isTransitioning = true;
		List<Pair<Integer, Integer>> seq = new ArrayList<>();
		switch (state) {
			case O:
				seq.addAll(O_TO_X);
				state = LeverState.X;
				break;
			case X:
				seq.addAll(X_TO_R);
				state = LeverState.R;
				break;
			case R:
				seq.addAll(O_TO_X);
				seq.addAll(X_TO_R);
				Collections.reverse(seq);
				state = LeverState.O;
				break;
		}
		Timeline animation = new Timeline();
		double duration = 1000, interval = duration / (seq.size() - 1);
		for (int i = 0; i < seq.size(); i++) {
			Pair<Integer, Integer> loc = seq.get(i);
			int x = loc.getKey(), y = loc.getValue();
			KeyFrame frame = new KeyFrame(new Duration(i * interval), new KeyValue(lever.layoutXProperty(), x), new KeyValue(lever.layoutYProperty(), y));
			animation.getKeyFrames().add(frame);
		}
		animation.getKeyFrames().add(new KeyFrame(new Duration(duration), e -> isTransitioning = false));
		animation.play();
	}

	@FXML
	private void onClickBack() {
		LevelController controller = GameLauncher.transitionTo(GameScene.LEVEL_SELECTION);
		controller.resetNodes();
		controller.chooseDifficulty(GameManager.SELECTED_DIFFICULTY);
	}

	public static void reveal(int x, int y) {
		paneEntries[x][y].setVisible(false);
		flags[x][y].setVisible(false);
	}

	public void startLevel(Nonogram level) {
		int size = level.getSize();
		GridPane grid;
		switch (size) {
			case 5:
				grid = grid_5;
				break;
			case 10:
				grid = grid_10;
				break;
			case 15:
				grid = grid_15;
				break;
			default:
				// invalid size
				return;
		}
		initializeGrid(grid, size);
		initializeFlag(grid, size);
		drawLines(grid, size);
		generateText(level, grid, size);
		spawnHeartPens();

		image.setImage(level.getImage());
	}

	private static void onMouseEntered(Pane pane) {
		int x = GridPane.getRowIndex(pane), y = GridPane.getColumnIndex(pane);
		if (GameManager.isAvailable(x, y))
			setHoverColor(x, y);
	}

	private static void onMouseExited(Pane pane) {
		int x = GridPane.getRowIndex(pane), y = GridPane.getColumnIndex(pane);
		if (GameManager.isAvailable(x, y))
			setNormalColor(x, y);
	}

	private static void setHoverColor(Pane pane) {
		pane.setBackground(new Background(new BackgroundFill(HOVER_PANE_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	private static void setNormalColor(Pane pane) {
		pane.setBackground(new Background(new BackgroundFill(UNKNOWN_PANE_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	private static void setWrongColor(Pane pane) {
		pane.setBackground(new Background(new BackgroundFill(WRONG_PANE_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	public static void setMarked(int x, int y) {
		flags[x][y].setVisible(true);
	}

	public static void setUnmarked(int x, int y) {
		flags[x][y].setVisible(false);

	}

	public static void setHoverColor(int x, int y) {
		setHoverColor(paneEntries[x][y]);
	}

	public static void setNormalColor(int x, int y) {
		setNormalColor(paneEntries[x][y]);
	}

	public static void setWrongColor(int x, int y) {
		setWrongColor(paneEntries[x][y]);
	}

}
