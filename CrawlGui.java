import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.util.Map;
import java.util.Optional;

/**
 * Main Application class which starts the JavaFX application.
 */
public class CrawlGui extends Application {

    /* Message area */
    private TextArea textArea;

    /* The primary stage of the JavaFX application */
    private Stage st;

    /* The map of game */
    private Cartographer map;

    /* Button area */
    private VBox vBox;

    /* Top button area */
    private GridPane grid1;

    /* Bottom button area */
    private GridPane grid2;

    /* All the buttons of game */
    private Button north;
    private Button south;
    private Button west;
    private Button east;
    private Button look;
    private Button examine;
    private Button drop;
    private Button take;
    private Button fight;
    private Button save;

    /* The input dialog to get the short description */
    private TextInputDialog dialog;

    /* The name of a map file to load */
    private String filename;

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Called to start doing application stuff
     *
     * @param stage primary stage
     */
    public void start(Stage stage) {
        stage.setTitle("Crawl - Explore");
        this.st = stage;

        BorderPane border = new BorderPane(); // Layout of the game.

        EventHandler<ActionEvent> eh = new ButtonDoer();

        if (this.getParameters().getRaw().isEmpty()){
            //Exit with status 1.
            System.err.println("Usage: java CrawlGui mapname");
            System.exit(1);
        }else {
            // Gets the arguments from configurations.
            filename = getParameters().getRaw().get(0);
        }

        map = new Cartographer(filename);

        textArea = new TextArea(map.start()); // get the initial information.
        textArea.setPrefHeight(200);
        textArea.setEditable(false);

        dialog = new TextInputDialog();
        dialog.setContentText(null);
        dialog.setHeaderText(null);

        north = new Button("North");
        north.setOnAction(eh);
        south = new Button("South");
        south.setOnAction(eh);
        west = new Button("West");
        west.setOnAction(eh);
        east = new Button("East");
        east.setOnAction(eh);
        look = new Button("Look");
        look.setOnAction(eh);
        examine = new Button("Examine");
        examine.setOnAction(eh);
        drop = new Button("Drop");
        drop.setOnAction(eh);
        take = new Button("Take");
        take.setOnAction(eh);
        fight = new Button("Fight");
        fight.setOnAction(eh);
        save = new Button("Save");
        save.setOnAction(eh);

        vBox = new VBox(); // buttons' layout
        grid2 = new GridPane(); // Bottom buttons' layout
        grid1 = new GridPane(); // Top buttons' layout
        grid1.add(north, 1, 0);
        grid1.add(west, 0, 1);
        grid1.add(east, 2, 1);
        grid1.add(south, 1, 2);
        grid2.add(look, 0, 0);
        grid2.add(examine, 1, 0);
        grid2.add(drop, 0, 1);
        grid2.add(take, 1, 1);
        grid2.add(fight, 0, 2);
        grid2.add(save, 0, 3);
        vBox.getChildren().addAll(grid1,grid2);

        // Set the position of map.
        map.getCanvasContainer().setAlignment(Pos.CENTER);
        border.setCenter(map.getCanvasContainer());

        border.setRight(vBox);
        border.setBottom(textArea);

        Scene scene = new Scene(border);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Adds the event handler for each button.
     */
    private class ButtonDoer implements EventHandler<ActionEvent> {

        /**
         * Sets the functions for each button.
         *
         * @param e Current actionEvent
         */
        public void handle(ActionEvent e) {
            if (e.getSource() == look) {
                // sets look function for look button.
                textArea.appendText(map.look());
            } else if (e.getSource() == examine) {
                // sets examine function for examine button.
                dialog.setTitle("Examine what?");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    textArea.appendText("\n" + map.examine(result.get()));
                }
            } else if (e.getSource() == drop) {
                // sets drop function for drop button.
                dialog.setTitle("Item to drop?");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (map.drop(result.get()).equals("unsuccessful")) {
                        textArea.appendText("\nNothing found with that name");
                    }
                }
            } else if (e.getSource() == north) {
                textArea.appendText("\n" + map.go("North"));
            } else if (e.getSource() == south) {
                textArea.appendText("\n" + map.go("South"));
            } else if (e.getSource() == west) {
                textArea.appendText("\n" + map.go("West"));
            } else if (e.getSource() == east) {
                textArea.appendText("\n" + map.go("East"));
            } else if (e.getSource() == take) {
                // sets take function for take button.
                dialog.setTitle("Take what?");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    map.take(result.get());
                }
            } else if (e.getSource() == fight) {
                // sets fight function for fight button.
                dialog.setTitle("Fight what?");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String fightResult = map.fight(result.get());
                    if (fightResult.equals("You won")) {
                        textArea.appendText("\n" + fightResult);
                    } else if (fightResult.equals("Game over")) {
                        textArea.appendText("\n" + fightResult);
                        // Disable all of the buttons on the GUI.
                        grid1.setDisable(true);
                        grid2.setDisable(true);
                    }
                }
            } else if (e.getSource() == save) {
                // sets save function for save button.
                dialog.setTitle("Save filename?");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    textArea.appendText("\n" + map.save(result.get()));
                }
            }
        }
    }


}
