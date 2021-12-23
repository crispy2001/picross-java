package me.Proj.Picross.controller;

import javafx.fxml.FXML;
import me.Proj.Picross.GameLauncher;

import static me.Proj.Picross.GameLauncher.GameScene;

public class MenuController
{
	@FXML
	private void selectLevel() {
		GameLauncher.transitionTo(GameScene.DIFFICULTY_SELECTION);
	}

	@FXML
	private void exit() {
		GameLauncher.exit();
	}
}
