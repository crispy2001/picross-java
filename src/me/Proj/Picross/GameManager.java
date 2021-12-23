package me.Proj.Picross;

import javafx.scene.image.Image;
import javafx.util.Pair;
import me.Proj.Picross.controller.GameController;
import me.Proj.Picross.controller.GameFinishController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static me.Proj.Picross.controller.GameController.LeverState;
import static me.Proj.Picross.GameLauncher.GameScene;
import static me.Proj.Picross.Nonogram.Difficulty;

public class GameManager
{
	public static final int MAX_HEALTH = 5;
	public static Difficulty SELECTED_DIFFICULTY = Difficulty.EASY;

	public static final List<Nonogram> EASY_LEVELS = new ArrayList<>();
	public static final List<Nonogram> NORMAL_LEVELS = new ArrayList<>();
	public static final List<Nonogram> HARD_LEVELS = new ArrayList<>();

	private static final HashMap<String, Nonogram> levelMap = new HashMap<>();
	private static final HashSet<String> finishedLevels = new HashSet<>();
	private static Nonogram currentLevel;
	private static int currentHealth;
	private static int remainingPanes;
	private static PaneState[][] currentPanes;

	private enum PaneState
	{
		AVAILABLE,
		MARKED,
		REVEALED,
		WRONG_PANE
	}

	public static void startNewGame(Nonogram level) {
		currentLevel = level;
		currentHealth = MAX_HEALTH;
		remainingPanes = level.getTotalPanes();

		int size = level.getSize();
		currentPanes = new PaneState[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				currentPanes[i][j] = PaneState.AVAILABLE;
			}
		}
	}

	public static void selectPanes(LinkedList<Pair<Integer, Integer>> selectedPanes) {
		LeverState state = GameController.getLeverState();
		boolean fail = false;
		for (Pair<Integer, Integer> coord : selectedPanes) {
			int x = coord.getKey(), y = coord.getValue();
			switch (state) {
				case O:
					if (!isAvailable(x, y))
						continue;
					if (!currentLevel.isValid(x, y)) {
						// a pane is wrong!
						fail = true;
						currentPanes[x][y] = PaneState.WRONG_PANE;
						GameController.setWrongColor(x, y);
						System.out.println("[Game] Selected wrong pane! HP - 1!");
						currentHealth -= 1;
						GameController.removeHeartPen();
						checkGameOver();
					}
					break;
				case X:
					if (!isAvailable(x, y))
						continue;
					currentPanes[x][y] = PaneState.MARKED;
					GameController.setMarked(x, y);
					GameController.setNormalColor(x, y);
					break;
				case R:
					if (currentPanes[x][y] == PaneState.MARKED || currentPanes[x][y] == PaneState.AVAILABLE) {
						currentPanes[x][y] = PaneState.AVAILABLE;
						GameController.setUnmarked(x, y);
						GameController.setNormalColor(x, y);
					}
					break;
			}
			if (fail)
				break;
		}

		if (state == LeverState.O) {
			for (Pair<Integer, Integer> coord : selectedPanes) {
				int x = coord.getKey(), y = coord.getValue();
				if (currentPanes[x][y] == PaneState.WRONG_PANE)
					GameController.setWrongColor(x, y);
				else
					GameController.setNormalColor(x, y);

				if (!fail && currentPanes[x][y] == PaneState.AVAILABLE) {
					GameController.reveal(x, y);
					currentPanes[x][y] = PaneState.REVEALED;
					remainingPanes--;
				}
			}
		}

		checkGameFinished();
	}

	public static void unmarkFlag(int x, int y) {
		LeverState state = GameController.getLeverState();
		if (state == LeverState.R && currentPanes[x][y] == PaneState.MARKED)
			currentPanes[x][y] = PaneState.AVAILABLE;
	}

	public static boolean isAvailable(int x, int y) {
		return currentPanes[x][y] == PaneState.AVAILABLE;
	}

	public static boolean hasFinishedLevel(String id) {
		return finishedLevels.contains(id);
	}

	public static void readData() {
		System.out.println("[Game] Loading saved data...");
		File saveData = new File(Main.getPath(), "save.dat");
		try {
			if (!saveData.exists()) {
				saveData.createNewFile();
				System.out.println("[Game] Creating new save file...");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(saveData)));
			String levelName;
			while ((levelName = reader.readLine()) != null) {
				finishedLevels.add(levelName);
				System.out.println("[Game] Read finished level: " + levelName);
			}
			reader.close();
			System.out.println("[Game] Successfully loaded saved data...");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveData() {
		System.out.println("[Game] Trying to save data...");
		File saveData = new File(Main.getPath(), "save.dat");
		try {
			if (!saveData.exists()) {
				saveData.createNewFile();
				System.out.println("[Game] Creating new save file...");
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveData)));
			for (String level : finishedLevels) {
				writer.write(level);
				writer.write('\n');
			}
			writer.close();
			System.out.println("[Game] Successfully saved data.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void checkGameFinished() {
		if (remainingPanes == 0) {
			GameFinishController controller = GameLauncher.transitionTo(GameScene.GAME_FINISH);
			controller.setFinishedLevel(currentLevel);
			finishedLevels.add(currentLevel.getID());
			System.out.println("[Game] Congratulations! You've finished the puzzle!");
		} else if (remainingPanes < 5) {
			System.out.printf("[Game] %d pieces remaining.\n", remainingPanes);
		}
	}

	public static Optional<Nonogram> getLevel(String id) {
		return Optional.ofNullable(levelMap.get(id));
	}

	public static int getCurrentHealth() {
		return currentHealth;
	}

	private static void checkGameOver() {
		if (currentHealth <= 0) {
			GameLauncher.transitionTo(GameScene.GAME_OVER);
			System.out.println("[Game] Game over...");
		}
	}

	public static void loadNonogram() {
		try {
			InputStream listOfLevels = Main.getResourceAsStream("/assets/level/levels.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(listOfLevels));
			String levelName;
			while ((levelName = reader.readLine()) != null) {
				Nonogram nonogram = readNonogramFromStream(levelName, Main.getResourceAsStream("/assets/level/" + levelName + ".txt"));
				switch (nonogram.getSize()) {
					case 5:
						EASY_LEVELS.add(nonogram);
						break;
					case 10:
						NORMAL_LEVELS.add(nonogram);
						break;
					case 15:
						HARD_LEVELS.add(nonogram);
						break;
				}
				Image image = new Image(Main.getResource("/assets/level/" + levelName + ".png").toString());
				nonogram.setImage(image);
				levelMap.put(levelName, nonogram);
				System.out.println("[LevelController] Successfully read nonogram: " + levelName);
			}
			reader.close();
		}
		catch (IOException e) {
			System.out.println("[LevelController] An error occurred while reading nonogram data...");
			e.printStackTrace();
		}
	}

	private static Nonogram readNonogramFromStream(String levelName, InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		// the first line is the size
		int size = Integer.parseInt(reader.readLine());
		int total = 0;
		boolean[][] map = new boolean[size][size];
		for (int i = 0; i < size; i++) {
			String[] row = reader.readLine().split(" ");
			for (int j = 0; j < size; j++) {
				if (row[j].equals("1")) {
					map[i][j] = true;
					total++;
				}
			}
		}
		return new Nonogram(levelName, size, total, map);
	}
}
