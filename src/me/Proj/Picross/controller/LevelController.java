package me.Proj.Picross.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import me.Proj.Picross.GameLauncher;
import me.Proj.Picross.GameManager;
import me.Proj.Picross.Main;
import me.Proj.Picross.Nonogram;

import java.util.ArrayList;
import java.util.List;

import static me.Proj.Picross.GameLauncher.GameScene;
import static me.Proj.Picross.Nonogram.Difficulty;

public class LevelController
{
	private final Pair<Integer, Integer>[] LAYOUT_4 = new Pair[] { new Pair<>(275, 275), new Pair<>(275, 600), new Pair<>(700, 275),
			new Pair<>(700, 600) };
	private final Pair<Integer, Integer>[] LAYOUT_2 = new Pair[] { new Pair<>(275, 400), new Pair<>(700, 400) };

	@FXML
	private AnchorPane backgroundPane;

	@FXML
	private ImageView background;

	private final List<Node> addedNodes = new ArrayList<>();

	public void startLevel(String levelID) {
		GameManager.getLevel(levelID).ifPresent(level -> {
			GameManager.startNewGame(level);
			GameController controller = GameLauncher.transitionTo(GameScene.GAME);
			controller.resetNodes();
			controller.startLevel(level);
			System.out.println("[LevelController] Trying to switch to game...");
		});
	}

	public void chooseDifficulty(Difficulty difficulty) {
		background.setImage(new Image(Main.getResource("/assets/background/difficulty_" + difficulty.toString().toLowerCase() + ".png").toString()));

		switch (difficulty) {
			case EASY:
				setupLevels(LAYOUT_4, GameManager.EASY_LEVELS);
				break;
			case NORMAL:
				setupLevels(LAYOUT_4, GameManager.NORMAL_LEVELS);
				break;
			case HARD:
				setupLevels(LAYOUT_2, GameManager.HARD_LEVELS);
				break;
		}
	}

	public void resetNodes() {
		backgroundPane.getChildren().removeAll(addedNodes);
	}

	private void setupLevels(Pair<Integer, Integer>[] levelPos, List<Nonogram> levels) {
		Image unsolved_level = new Image(Main.getResource("/assets/background/unsolved_level.png").toString());
		for (int i = 0; i < levelPos.length; i++) {
			Pair<Integer, Integer> pos = levelPos[i];
			Nonogram level = levels.get(i);

			ImageView levelBlock = new ImageView(unsolved_level);
			levelBlock.setFitHeight(200);
			levelBlock.setFitWidth(200);
			levelBlock.setLayoutX(pos.getKey());
			levelBlock.setLayoutY(pos.getValue());
			levelBlock.setOnMouseClicked(e -> startLevel(level.getID()));

			ImageView levelImage = new ImageView(level.getImage());
			levelImage.setFitHeight(170);
			levelImage.setFitWidth(170);
			levelImage.setLayoutX(pos.getKey() + 15);
			levelImage.setLayoutY(pos.getValue() + 15);
			levelImage.setOnMouseClicked(e -> startLevel(level.getID()));
			levelImage.setVisible(GameManager.hasFinishedLevel(level.getID()));

			backgroundPane.getChildren().add(levelBlock);
			backgroundPane.getChildren().add(levelImage);

			addedNodes.add(levelBlock);
			addedNodes.add(levelImage);
		}
	}

	@FXML
	private void onClickBack() {
		GameLauncher.transitionTo(GameScene.DIFFICULTY_SELECTION);
		System.out.println("[LevelController] Going back to difficulty selection...");
	}
}
