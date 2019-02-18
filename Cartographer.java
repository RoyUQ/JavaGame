import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.text.DecimalFormat;

/**
 * This class creates the “map” in the GUI.
 * It will draw all reachable rooms starting from a “start room”.
 */
public class Cartographer extends Canvas {

    /* The container of map */
    private GridPane canvasContainer;

    /* To draw all the elements of map */
    private GraphicsContext context;

    /* The name of the file to store the map */
    private String filename;

    /* The start room */
    private Room root;

    /* The player of a map */
    private Player player;

    /* To get the overall map */
    private BoundsMapper mapper;

    /**
     * Creates a map in the GUI.
     *
     * @param filename name of the file which contains the map.
     */
    public Cartographer(String filename) {
        canvasContainer = new GridPane();
        this.filename = filename;
        try {
            // initialize a map.
            Object[] res = MapIO.loadMap(this.filename);
            root = (Room) res[1];
            player = (Player) res[0];
            root.enter(player);
            mapper = new BoundsMapper(root);
            mapper.walk();
            drawMap();
        } catch (Exception e) {
            // exit with status 2.
            System.err.println("Unable to load file");
            System.exit(2);
        }
    }


    /**
     * Get the container.
     *
     * @return The container which contains the map.
     */
    public GridPane getCanvasContainer() {
        return canvasContainer;
    }

    /**
     * Get the initial position of the player.
     *
     * @return initial position
     */
    public String getInitialPosition() {
        return "You find yourself in " + root.getDescription();
    }

    /**
     * Draw all reachable rooms starting from a “start room”.
     */
    private void drawMap() {
        for (Room room : mapper.coords.keySet()) {
            Canvas canvas = new Canvas(50, 50);
            GridPane things = new GridPane(); // container of things
            things.setMaxSize(49, 49);
            things.setPadding(new Insets(1, 1, 1, 1));
            FlowPane players = new FlowPane(); // container of player
            players.setMinSize(24, 24);
            FlowPane critters = new FlowPane(); // container of critters
            critters.setMinSize(24, 24);
            FlowPane treasures = new FlowPane(); // container of treasures
            treasures.setMinSize(24, 24);
            FlowPane deadCritters = new FlowPane(); //container of deadCritters
            deadCritters.setMinSize(24, 24);
            things.add(players, 0, 0);
            things.add(treasures, 1, 0);
            things.add(critters, 0, 1);
            things.add(deadCritters, 1, 1);
            context = canvas.getGraphicsContext2D();
            // Get the coordinates of a room.
            int x = mapper.coords.get(room).x - mapper.xMin;
            int y = mapper.coords.get(room).y - mapper.yMin;
            // Draw rooms
            drawRoom();
            canvasContainer.add(things, x, y);
            canvasContainer.add(canvas, x, y);

            for (String exit : room.getExits().keySet()) {
                // Draw exits
                drawExit(exit);
            }

            for (Thing thing : room.getContents()) {
                // Draw things
                Canvas cv = new Canvas(12, 12);
                context = cv.getGraphicsContext2D();
                if (thing instanceof Treasure) {
                    drawTreasure();
                    treasures.getChildren().add(cv);
                } else if (thing instanceof Player) {
                    drawPlayer();
                    players.getChildren().add(cv);
                } else if (thing instanceof Critter) {
                    if (((Critter) thing).getHealth() > 0) {
                        drawLivedCritter();
                        critters.getChildren().add(cv);
                    } else {
                        drawDeadCritter();
                        deadCritters.getChildren().add(cv);
                    }
                }
            }

        }
    }

    /**
     * Draw different exits for directions.
     *
     * @param direction The direction of the exit.
     */
    private void drawExit(String direction) {
        context.setStroke(Color.BLACK);
        if (direction.equals("North")) {
            context.strokeLine(25, 0, 25, 4);
        } else if (direction.equals("South")) {
            context.strokeLine(25, 50, 25, 46);
        } else if (direction.equals("West")) {
            context.strokeLine(0, 25, 4, 25);
        } else if (direction.equals("East")) {
            context.strokeLine(50, 25, 46, 25);
        }
    }

    /**
     * Draw Player(@)
     */
    private void drawPlayer() {
        context.setFill(Color.BLACK);
        context.fillText("@", 0, 10);
    }

    /**
     * Draw Critter who is alive(M)
     */
    private void drawLivedCritter() {
        context.setFill(Color.BLACK);
        context.fillText("M", 0, 10);
    }

    /**
     * Draw Critter who is dead(m)
     */
    private void drawDeadCritter() {
        context.setFill(Color.BLACK);
        context.fillText("m", 0, 10);
    }

    /**
     * Draw treasure($)
     */
    private void drawTreasure() {
        context.setFill(Color.BLACK);
        context.fillText("$", 0, 10);
    }

    /**
     * Draw room
     */
    private void drawRoom() {
        context.setStroke(Color.BLACK);
        context.strokeRect(0, 0, 50, 50);
    }

    /**
     * Find the room which contains the player.
     *
     * @return the current room.
     */
    private Room findCurrentRoom() {
        for (Room r : mapper.coords.keySet()) {
            // Find the room which contains the player.
            if (r.getContents().contains(player)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Move to another room.
     *
     * @param direction The direction of movement.
     * @return The result of this movement.
     */
    public String go(String direction) {
        Room currentRoom = findCurrentRoom();
        if (currentRoom.getExits().keySet().contains(direction)) {
            if (currentRoom.leave(player)) {
                Room nextRoom = currentRoom.getExits().get(direction);
                nextRoom.enter(player);
                clear();
                drawMap();
                return "You enter " + nextRoom.getDescription();
            } else {
                return "Something prevents you from leaving";
            }
        } else {
            return "No door that way";
        }
    }

    /**
     * Get all the information of a room.
     *
     * @return The information of all elements in a room.
     */
    public String look() {
        Room currentRoom = findCurrentRoom();
        String things = "";
        String carring = "";
        double totalValue = 0.0;
        DecimalFormat df = new DecimalFormat("0.0");
        for (Thing thing : currentRoom.getContents()) {
            things = things + " " + thing.getShortDescription() + "\n";
        }
        for (Thing thing : player.getContents()) {
            carring = carring + " " + thing.getShortDescription() + "\n";
            Lootable loot = (Lootable) thing;
            totalValue += loot.getValue();
        }

        return "\n" + currentRoom.getDescription() + " - " + "you see:" + "\n"
                + things + "You are carrying:" + "\n" + carring + "worth "
                + df.format(totalValue) + " in total";

    }

    /**
     * Get the initial information of a game.
     *
     * @return initial information
     */
    public String start() {
        return "You find yourself in " + root.getDescription();
    }

    /**
     * Get the long description of a thing you want to check.
     *
     * @param shortDescription short description of the thing you want to check
     * @return long description
     */
    public String examine(String shortDescription) {
        for (Thing thing : player.getContents()) {
            if (shortDescription.equals(thing.getShortDescription())) {
                return thing.getDescription();
            }
        }
        Room currentRoom = findCurrentRoom();
        for (Thing thing : currentRoom.getContents()) {
            if (shortDescription.equals(thing.getShortDescription())) {
                return thing.getDescription();
            }
        }
        return "Nothing found with that name";
    }

    /**
     * Drop a thing to the floor of current room.
     *
     * @param shortDescription short description of the thing you want to drop
     * @return the result of this action
     */
    public String drop(String shortDescription) {
        for (Thing thing : player.getContents()) {
            if (shortDescription.equals(thing.getShortDescription())) {
                Room currentRoom = findCurrentRoom();
                player.drop(thing);
                currentRoom.enter(thing);
                clear();
                drawMap();
                return "successful";
            }
        }

        return "unsuccessful";
    }

    /**
     * Take a thing on the floor of current room.
     *
     * @param shortDescription short description of the thing you want to take.
     */
    public void take(String shortDescription) {
        Room currentRoom = findCurrentRoom();
        for (Thing thing : currentRoom.getContents()) {
            if (!(thing instanceof Player)) {
                if (shortDescription.equals(thing.getShortDescription())) {
                    if (thing instanceof Critter) {
                        if (((Critter) thing).getHealth() > 0) {
                            break;
                        } else {
                            if (currentRoom.leave(thing)) {
                                player.add(thing);
                                break;
                            } else {
                                break;
                            }
                        }
                    } else {
                        if (currentRoom.leave(thing)) {
                            player.add(thing);
                            break;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        clear();
        drawMap();
    }

    /**
     * Fight with a alive critter in the current room.
     *
     * @param shortDescription short description of the critter
     *                         you want to fight.
     * @return the result of this fight.
     */
    public String fight(String shortDescription) {
        Room currentRoom = findCurrentRoom();
        for (Thing critter : currentRoom.getContents()) {
            if (critter instanceof Critter) {
                if (shortDescription.equals(critter.getShortDescription())) {
                    if (((Critter) critter).getHealth() > 0) {
                        player.fight((Critter) critter);
                        if (player.isAlive()) {
                            clear();
                            drawMap();
                            return "You won";
                        } else {
                            clear();
                            drawMap();
                            return "Game over";
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Save the game to a file.
     *
     * @param file the name of file
     * @return the result of this action.
     */
    public String save(String file) {
        if (MapIO.saveMap(root, file)) {
            return "Saved";
        } else {
            return "Unable to save";
        }
    }

    /**
     * Clear the current map.
     */
    public void clear() {
        canvasContainer.getChildren().clear();
    }
}
