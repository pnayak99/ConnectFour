package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int columns = 7;
    private static final int rows = 6;
    private static final int circle_diameter = 80;
    private static final String diskColor1 = "#24303E";
    private static final String diskColor2 = "#4CAA88";

    private static String Player_One;
    private static String Player_Two;

    private static boolean isPlayerOneTurn = true;

    private Disk[][] insertedDiskArray = new Disk[rows][columns];


    @FXML
    public GridPane rootGridPane;

    @FXML
    public Pane insertedDiskPane;
    
    @FXML
    public Label playerNameLabel;

    @FXML
    public TextField playerOneTextField, playerTwoTextField;

    @FXML
    public Button setNamesButton;

    private boolean isAllowedToInsert = true;

    public void createPlayGround(){

        Shape rectangleWithHoles = createGameStructuralGrid();
        rootGridPane.add(rectangleWithHoles,0,1);

        List<Rectangle> rectangleList = createClickableColumns();

        for (Rectangle rectangle: rectangleList){
            rootGridPane.add(rectangle,0,1);
            }
         setNamesButton.setOnAction(event -> {
             Player_One = playerOneTextField.getText();
             Player_Two = playerTwoTextField.getText();

         });

    }

    public Shape createGameStructuralGrid(){
        Shape rectangleWithHoles = new Rectangle((columns + 1) * circle_diameter, (rows + 1) * circle_diameter);

        for (int row = 0; row < rows; row++){
            for (int col=0; col < columns; col++){
                Circle circle = new Circle();
                circle.setRadius(circle_diameter / 2);
                circle.setCenterX(circle_diameter / 2);
                circle.setCenterY(circle_diameter / 2);
                circle.setSmooth(true);

                circle.setTranslateX(col * (circle_diameter + 1) + circle_diameter / 4);
                circle.setTranslateY(row * (circle_diameter + 1) + circle_diameter / 4);

                rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
            }
        }

        rectangleWithHoles.setFill(Color.WHITE);

        return rectangleWithHoles;
    }

    private List<Rectangle> createClickableColumns(){

        List<Rectangle> rectangleList = new ArrayList<>();

        for (int col = 0; col < columns; col++){
            Rectangle rectangle = new Rectangle(circle_diameter, (rows + 1) * circle_diameter);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col * (circle_diameter + 1) + circle_diameter / 4);

            rectangle.setOnMouseClicked(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

            final int column = col;
            rectangle.setOnMouseClicked(event -> {
                if(isAllowedToInsert) {

                    isAllowedToInsert = false;
                    insertDisk(new Disk(isPlayerOneTurn), column);
                }
            });

            rectangleList.add(rectangle);
        }

        return rectangleList;
    }

    private void insertDisk(Disk disk, int column){

        int row = rows-1;
        while(row >= 0){

            if(getDiskIfPresent(row, column) == null)
                break;

            row--;
        }

        if(row < 0)
            return;;

        insertedDiskArray[row][column] = disk;
        insertedDiskPane.getChildren().add(disk);

        disk.setTranslateX(column * (circle_diameter + 1) + circle_diameter / 4);
        int currentRow = row;
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disk);
        translateTransition.setToY(row * (circle_diameter + 1) + circle_diameter / 4);

        translateTransition.setOnFinished(event -> {
            isAllowedToInsert = true;
            if(gameEnded(currentRow, column)){
                gameOver();
                return;
            }

            isPlayerOneTurn = !isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn? Player_One : Player_Two);
        });
        translateTransition.play();
    }

    private boolean gameEnded(int row, int column){

        List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3)
                                        .mapToObj(r -> new Point2D(r, column))
                                        .collect(Collectors.toList());
        List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
                                            .mapToObj(col -> new Point2D(row, col))
                                            .collect(Collectors.toList());
        Point2D startPoint1 = new Point2D(row - 3, column + 3);
        List<Point2D> diagonal1Points = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> startPoint1.add(i, -i))
                .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row - 3, column - 3);
        List<Point2D> diagonal2Points = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> startPoint2.add(i, i))
                .collect(Collectors.toList());


        boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
                || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> points) {

        int chain = 0;
        for(Point2D point: points){
            int rowIndexForArray = (int) point.getX();
            int columnIndexForArray = (int) point.getY();

            Disk disk = getDiskIfPresent(rowIndexForArray, columnIndexForArray);
            if(disk != null && disk.isPlayerOneMove == isPlayerOneTurn){
                chain++;
                if(chain == 4)
                    return true;
            }else{
                chain = 0;
            }

        }return false;
    }

    private Disk getDiskIfPresent(int row, int column){
        if(row >= rows || row < 0 || column >= columns || column < 0)
            return null;
        return insertedDiskArray[row][column];
    }

    private void gameOver(){
        String winner = isPlayerOneTurn ? Player_One : Player_Two;
        System.out.println("Winner is: " + winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The winner is" + winner);
        alert.setContentText("Want to play again? ");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No");

        alert.getButtonTypes().setAll(yesBtn, noBtn);

        Platform.runLater(() -> {

            Optional<ButtonType> btnClicked = alert.showAndWait();
            if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
                resetgame();
            }else{
                Platform.exit();
                System.exit(0);
            }

        });


    }

    public void resetgame() {
        insertedDiskPane.getChildren().clear();

        for (int row = 0; row < insertedDiskArray.length; row++) {
            for (int col = 0; col < insertedDiskArray[row].length; col++) {
               insertedDiskArray[row][col] = null;
                
            }

        }
        isPlayerOneTurn = true;
        playerNameLabel.setText(Player_One);

        createPlayGround();

    }

    private static class Disk extends Circle{
        private final boolean isPlayerOneMove;

        public Disk(boolean isPlayerOneMove){
            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(circle_diameter / 2);
            setFill(isPlayerOneMove? Color.valueOf(diskColor1): Color.valueOf(diskColor2));
            setCenterX(circle_diameter / 2);
            setCenterY(circle_diameter / 2);

        }
    }
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
