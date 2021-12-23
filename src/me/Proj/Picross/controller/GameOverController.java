package me.Proj.Picross.controller;

import javafx.fxml.FXML;
import me.Proj.Picross.GameLauncher;
import me.Proj.Picross.GameManager;

import static me.Proj.Picross.GameLauncher.GameScene;

public class GameOverController
{
	@FXML
	private void onClickBack() {
		LevelController controller = GameLauncher.transitionTo(GameScene.LEVEL_SELECTION);
		controller.resetNodes();
		controller.chooseDifficulty(GameManager.SELECTED_DIFFICULTY);
	}
}
