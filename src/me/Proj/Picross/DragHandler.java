package me.Proj.Picross;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import me.Proj.Picross.controller.GameController;

import java.util.LinkedList;

public class DragHandler
{
	private static final LinkedList<Pair<Integer, Integer>> selectedPanes = new LinkedList<>();
	private static Pane firstSelected = null;

	public static void makeDraggable(Pane pane) {
		pane.setOnMousePressed(e -> handleMousePressed(pane, e));
		pane.setOnDragDetected(e -> handleDragDetected(pane, e));
		pane.setOnMouseDragEntered(e -> handleMouseDragEntered(pane, e));
		pane.setOnMouseReleased(e -> handleMouseReleased(pane, e));
	}

	private static void handleMousePressed(Pane pane, MouseEvent event) {
		firstSelected = pane;
		int x = GridPane.getRowIndex(pane), y = GridPane.getColumnIndex(pane);
		System.out.printf("[Pressed] x: %d, y: %d\n", x, y);

		selectedPanes.add(new Pair<>(x, y));
		event.consume();
	}

	private static void handleDragDetected(Pane pane, MouseEvent event) {
		pane.startFullDrag();
		event.consume();
	}

	private static void handleMouseDragEntered(Pane pane, MouseEvent event) {
		int x = GridPane.getRowIndex(pane), y = GridPane.getColumnIndex(pane);
		System.out.printf("[Dragged] x: %d, y: %d\n", x, y);
		if (firstSelected == null)
			return;

		while (!selectedPanes.isEmpty()) {
			Pair<Integer, Integer> selected = selectedPanes.pop();
			GameController.setNormalColor(selected.getKey(), selected.getValue());
		}

		// if the axis is different, we need to clear all selected panes
		int sx = GridPane.getRowIndex(firstSelected), sy = GridPane.getColumnIndex(firstSelected);
		int deltaX = Math.abs(x - sx), deltaY = Math.abs(y - sy);
		if (deltaX >= deltaY) {
			// select region on x-axis
			for (int i = 0; i <= deltaX; i++) {
				int dx = Math.min(x, sx) + i;
				GameController.setHoverColor(dx, sy);
				selectedPanes.add(new Pair<>(dx, sy));
			}
		} else {
			for (int i = 0; i <= deltaY; i++) {
				int dy = Math.min(y, sy) + i;
				GameController.setHoverColor(sx, dy);
				selectedPanes.add(new Pair<>(sx, dy));
			}
		}

		event.consume();
	}

	private static void handleMouseReleased(Pane pane, MouseEvent event) {
		int x = GridPane.getRowIndex(pane), y = GridPane.getColumnIndex(pane);
		System.out.printf("[Released] x: %d, y: %d\n", x, y);
		GameManager.selectPanes(selectedPanes);
		selectedPanes.clear();

		firstSelected = null;
		event.consume();
	}
}
