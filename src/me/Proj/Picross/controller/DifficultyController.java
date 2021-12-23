package me.Proj.Picross.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import me.Proj.Picross.GameLauncher;
import me.Proj.Picross.GameManager;
import me.Proj.Picross.Vector2D;

import java.net.URL;
import java.util.ResourceBundle;

import static me.Proj.Picross.GameLauncher.GameScene;
import static me.Proj.Picross.Nonogram.Difficulty;

public class DifficultyController implements Initializable
{
	private final Vector2D pivot = new Vector2D(580, 570);

	@FXML
	private ImageView arrow;

	@FXML
	private void onMouseMove(MouseEvent e) {
		rotate(e.getSceneX(), e.getSceneY());
		arrow.setLayoutX(pivot.x);
		arrow.setLayoutY(pivot.y);
	}

	private void rotate(double mouseX, double mouseY) {
		// create a new coordinate system that is centered at the pivot
		Vector2D vec = new Vector2D(mouseX, mouseY - 50).subtract(pivot);
		vec.normalize();

		double degree = vec.getAngle();
		Rotate rotation = (Rotate) arrow.getTransforms().get(0);
		if (degree < 175 && degree > 5)
			return;
		rotation.setAngle(degree);
	}

	@FXML
	private void onMouseClicked(MouseEvent e) {
		double mouseX = e.getSceneX(), mouseY = e.getSceneY();
		Vector2D vec = new Vector2D(mouseX, mouseY - 50).subtract(pivot);
		vec.normalize();
		double degree = vec.getAngle();
		if (degree < 175 && degree > 5)
			return;

		LevelController controller = GameLauncher.transitionTo(GameScene.LEVEL_SELECTION);
		controller.resetNodes();

		if (degree > -60 && degree <= 5)
			GameManager.SELECTED_DIFFICULTY = Difficulty.HARD;
		else if (degree <= -60 && degree > -120)
			GameManager.SELECTED_DIFFICULTY = Difficulty.NORMAL;
		else
			GameManager.SELECTED_DIFFICULTY = Difficulty.EASY;

		controller.chooseDifficulty(GameManager.SELECTED_DIFFICULTY);

		System.out.println("[DifficultyController] Switching to level selection...");
	}

	@FXML
	private void onClickBack() {
		GameLauncher.transitionTo(GameScene.MENU);
		System.out.println("[LevelController] Going back to difficulty selection...");
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		arrow.getTransforms().add(new Rotate(0, 0, 50));
	}
}
