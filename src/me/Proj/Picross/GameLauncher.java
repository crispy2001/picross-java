package me.Proj.Picross;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumMap;

public class GameLauncher extends Application
{
	public static void main(String[] args) {
		launch(args);
	}

	private static Stage currentStage;

	private static final EnumMap<GameScene, Scene> sceneMap = new EnumMap<>(GameScene.class);
	private static final EnumMap<GameScene, FXMLLoader> fxmlMap = new EnumMap<>(GameScene.class);

	@Override
	public void start(Stage stage) {
		GameManager.loadNonogram();
		GameManager.readData();

		currentStage = stage;
		currentStage.setTitle("Nonogram");
		currentStage.setOnCloseRequest(e -> GameManager.saveData());

		transitionTo(GameScene.MENU);
		currentStage.setWidth(1200);
		currentStage.setHeight(930);
		currentStage.setResizable(false);
		currentStage.show();
	}

	public static <T> T transitionTo(GameScene sceneType) {
		// lazy load
		if (!sceneMap.containsKey(sceneType)) {
			FXMLLoader loader = new FXMLLoader(Main.getResource(sceneType.getFxmlLocation()));
			try {
				Parent root = loader.load();
				sceneMap.put(sceneType, new Scene(root, 1200, 900));
				fxmlMap.put(sceneType, loader);
			} catch (IOException e) {
				System.out.println("[Launcher] An error occurred while loading FXML...");
				e.printStackTrace();
			}
		}
		Scene scene = sceneMap.get(sceneType);
		scene.getRoot().requestFocus();
		currentStage.setScene(scene);
		return fxmlMap.get(sceneType).getController();
	}

	public static void exit() {
		currentStage.close();
	}

	public enum GameScene
	{
		MENU("/assets/Menu.fxml"),
		DIFFICULTY_SELECTION("/assets/DifficultySelection.fxml"),
		LEVEL_SELECTION("/assets/LevelSelection.fxml"),
		GAME_OVER("/assets/GameOver.fxml"),
		GAME_FINISH("/assets/GameFinish.fxml"),
		GAME("/assets/Game.fxml");

		GameScene(String fxmlLocation) {
			this.fxmlLocation = fxmlLocation;
		}

		private final String fxmlLocation;

		public String getFxmlLocation() {
			return fxmlLocation;
		}
	}
}
