package me.Proj.Picross.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import me.Proj.Picross.GameLauncher;
import me.Proj.Picross.GameManager;
import me.Proj.Picross.Nonogram;

import static me.Proj.Picross.GameLauncher.GameScene;

public class GameFinishController
{
	@FXML
	private ImageView image;

	@FXML
	private void onClickBack() {
		LevelController controller = GameLauncher.transitionTo(GameScene.LEVEL_SELECTION);
		controller.resetNodes();
		controller.chooseDifficulty(GameManager.SELECTED_DIFFICULTY);
	}

	public void setFinishedLevel(Nonogram level) {
		image.setImage(level.getImage());
	}
}
