package game;

import model.Actor;
import model.Card;
import model.Player;
import model.Room;
import model.Weapon;
import model.Piece;
import ui.match.BoardLayout;

import javax.swing.*;
import java.util.ArrayList;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import java.io.IOException;
import java.nio.file.Path;

public class Game {

    // Estadísticas e historial

    private GameHistory gameHistory;
    private StatisticsManager statisticsManager;
    private boolean statisticsUpdated;

    // Listas de cartas

    private List <Room> rooms;
    private List <Actor> actors;
    private List <Weapon> weapons;

    // Cartas del juego

    private List <Card> solutionList;
    private List <Card> deck;
    private List <Card> clues;

    // Jugadores

    private List <Player> players;
    private Player currentPlayer;

    // Game state

    private boolean gameIsOver;
    private BoardLayout board;
    private GameState gameState;
    private RoundManager roundManager;
    private MoveValidator moveValidator;
    private AccusationValidator accusationValidator;


    // MÉTODO CONSTRUCTOR DE LA CLASE

    public Game () {
        rooms = new ArrayList<Room>();
        actors = new ArrayList<Actor>();
        weapons = new ArrayList<Weapon>();

        solutionList = new ArrayList<Card>();
        deck = new ArrayList<Card>();
        clues = new ArrayList<Card>();

        players = new ArrayList<Player>();

        gameIsOver = false;
    }

    // MÉTODO PARA INICIALIZAR LA PARTIDA

    public List<Piece> getPieces() {
        List<Piece> pieces = new ArrayList<>();

        for (Player player : players) {
            if (player.getPiece() != null) {
                pieces.add(player.getPiece());
            }
        }

        return pieces;
    }

    public void setBoard(BoardLayout board) {
        this.board = board;
    }

    public void startGame() {

        // LISTA DE MÉTODOS AUXILIARES NECESARIOS PARA INICILIZAR LA PARTIDA

        createRooms();
        createActors();
        createWeapons();
        createSolution();
        createDeck();
        shuffleDeck();
        dealCards();
        getRemainingCards();
        gameIsOver = false;

        if (players.isEmpty()) {
            JOptionPane.showMessageDialog(null, "There's not enough players to start the game", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // INICIALIZAR LOS COMPONENTES DE LA PARTIDA
        gameState = new GameState(players);
        roundManager = new RoundManager(players);
        moveValidator = new MoveValidator(board, roundManager);
        accusationValidator = new AccusationValidator();

        // INICIAR EL ESTADO Y LOS TURNOS
        gameState.start();
        roundManager.startTurns();

        currentPlayer = roundManager.getCurrentPlayer();

        // ACTUALIZAR TABLERO

        updateBoard();

        // COMENZAR LA LÓGICA ACTUAL DEL JUEGO

        playGame();
    }

    // CREAR HABITACIONES

    private void createRooms() {
        rooms.clear();

        createRoom("CRAI", 0, 0, 7, 4);

        createRoom ("Baños", 9, 0, 5, 6);

        createRoom ("Cafetería", 16, 0, 7 , 5);

        Room maquinas = new Room ("Maquinas").occupyRectangle(0, 6,7 ,4);

        maquinas.free(6, 6);
        maquinas.free (6, 9);

        rooms.add(maquinas);

        createRoom ("Laboratorio", 9, 7, 5, 6);

        Room deustoTech = new Room ("DeustoTech").occupyRectangle(16, 7, 7, 7);

        deustoTech.free(16, 13);
        deustoTech.free(17,13);

        rooms.add(deustoTech);

        createRoom ("Aula 1", 0, 11, 6, 4);

        Room aula2 = new Room ("Aula 2").occupyRectangle(0, 17, 6, 5);

        aula2.free(0, 17);
        aula2.free(5, 17);

        rooms.add(aula2);

        Room room9 = new Room ("Room 9").occupyRectangle(8, 15, 7,7);
        room9.free(8,21);
        room9.free(14, 21);

        rooms.add(room9);

        createRoom ("Room 10", 17, 16, 6, 6);
    }

    private void createRoom(String name, int column, int row, int width, int height){
        Room room = new Room(name).occupyRectangle(column, row, width, height);

        rooms.add(room);
    }

    // CREAR ACTORES

    private void createActors() {
    List <String> actorNames = List.of(
            "Garaizar",
            "Luka",
            "David",
            "Andrada",
            "Antal",
            "Bringas"
    );

    List <Color> actorColors = List.of(
            Color.PINK,
            Color.MAGENTA,
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW
    );

    actors.clear();

    for (int i = 0; i < actorNames.size(); i++) {
        actors.add(new Actor(actorNames.get(i), actorColors.get(i)));
    }

    }

    // CREAR ARMAS

    private void createWeapons() {
        String [] weaponNames =  {
                "Machete",
                "Tiza",
                "Teclado",
                "Cable",
                "Silla",
                "Destornillador"
        };

        weapons.clear();

        for (String name : weaponNames) {
            weapons.add(new Weapon(name));
        }
    }

    // CREAR SOLUCIÓN

    private void createSolution() {
        Collections.shuffle(rooms);
        Collections.shuffle(actors);
        Collections.shuffle(weapons);

        Room solutionRoom = rooms.removeFirst();

        Actor solutionActor = actors.removeFirst();

        Weapon solutionWeapon = weapons.removeFirst();

        solutionList.clear();

        solutionList.add(solutionRoom);
        solutionList.add(solutionActor);
        solutionList.add(solutionWeapon);
    }

    // CREAR LA BARAJA

    private void createDeck() {
        deck.clear();

        for (Room room : rooms) {
            if (!solutionList.contains(room)) {
                deck.add(room);
            }
        }

        for (Actor actor : actors) {
            if (!solutionList.contains(actor)) {
                deck.add(actor);
            }
        }

        for (Weapon weapon : weapons) {
            if (!solutionList.contains(weapon)) {
                deck.add(weapon);
            }
        }
    }

    // BARAJAR LA BARAJA
    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    // REPARTIR CARTAS

    private void dealCards() {
        for (Player player : players) {
            for (int i = 0; i <4; i++) {
                if (!deck.isEmpty()) {
                    Card card = deck.remove(0);

                    player.addCard(card);
                }
            }
        }
    }

    // GUARDAR CARTAS RESTANTES COMO PISTAS

    private void getRemainingCards() {
        clues.clear();

        while (!deck.isEmpty()) {
            Card card = deck.remove(0);
            clues.add(card);
        }
    }

    // EMPEZAR LA PARTIDA

    public void playGame() {
        currentPlayer = getCurrentPlayer();

        sendTurn(currentPlayer);

        Dice diceRoll = new Dice();

        int totalMv = diceRoll.roll();
    }
    // MÉTODO PARA CONSEGUIR EL JUGADOR ACTUAL
    private Player getCurrentPlayer() {

        return currentPlayer;
    }


    // PARA ESTABLECER EL JUGADOR ACTUAL
    private void setCurrentPlayer (Player player) {
        currentPlayer = player;
    }



    // ENVIAR TURNO

    private void sendTurn(Player player) {
        if (player == null) {
            return; // si el jugador es nulo return
        }

        currentPlayer = player;
    }

    // MOVER JUGADOR

    private void movePlayer (Player player, int totalMv) {

        if (player == null || player.getPiece() == null){
            return;
        }

        Piece piece = player.getPiece();

        for (int i = 0; i < totalMv; i++) {
            int newX = piece.getX() + 1;
            int newY = piece.getY();

            if (board != null && moveValidator != null) {
                if (!moveValidator.isValidMove(player, piece, newX, newY)) {
                    break;
                }

                board.movePiece(piece, piece.getX(), piece.getY(), newX, newY);
            } else {
                if (!canMoveTo(piece, newX, newY)) {
                    break;
                }

                piece.setPosition(newX, newY);
            }
        }
    }

    private boolean canMoveTo(Piece piece, int x, int y) {
        if (x < 0 || x >= BoardLayout.GRID_COLUMNS || y < 0 || y >= BoardLayout.GRID_ROWS) {
            return false;
        }

        for (Piece other : getPieces()) {
            if (other != piece && other.getX() == x && other.getY() == y) {
                return false;
            }
        }

        return true;
    }

    // MÉTODO EN EL QUE EL JUGADOR QUIERE ACUSAR

    private boolean askForAccusation() {
        JComboBox<Room> roomBox = new JComboBox<>(rooms.toArray(new Room[0]));

        JComboBox<Actor> actorBox = new JComboBox<>(actors.toArray(new Actor[0]));

        JComboBox<Weapon> weaponBox = new JComboBox<>(weapons.toArray(new Weapon[0]));

        JPanel panel = new JPanel(new GridLayout(0,2, 5, 5));

        panel.add (new JLabel("Room: "));
        panel.add(roomBox);

        panel.add (new JLabel ("Actor: "));
        panel.add(actorBox);

        panel.add (new JLabel ("Weapon: "));
        panel.add (weaponBox);

        int option = JOptionPane.showConfirmDialog(null, panel, "Final accusation", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) {
            return false;
        }

        return checkAccusation((Room) roomBox.getSelectedItem(),
                (Actor) actorBox.getSelectedItem(),
                (Weapon) weaponBox.getSelectedItem());
    }

    // JUGAR TURNO

    public void playTurn() {
        if (gameIsOver || roundManager == null) {
            return;
        }

        currentPlayer = roundManager.getCurrentPlayer();

        if (currentPlayer == null) {
            return;
        }

        sendTurn(currentPlayer);

        Dice diceRoll = new Dice();
        int totalMv = diceRoll.roll();

        JOptionPane.showMessageDialog(null, currentPlayer.getName() + "ha sacado" + totalMv, "Tirada de dados", JOptionPane.INFORMATION_MESSAGE);

        // MOVER AL JUGADOR
        movePlayer(currentPlayer, totalMv);

        // ACTUALIZAR EL TABLERO
        updateBoard();

        // SI EL JUGADOR ESTÁ EN LA HABITACIÓN PUEDE ACUSAR

        if (playerIsInRoom(currentPlayer) && playerWantsToAccuse(currentPlayer)) {
            boolean correct = askForAccusation();

            if (correct) {
                gameIsOver = true;
                showFinalResult();
                closeGame();
                return;
            }

            // FINALIZAR EL TURNO Y PASAR AL SIGUIENTE JUGADOR

            roundManager.nextTurn();
            currentPlayer = roundManager.getCurrentPlayer();
        }

    }

    // COMPROBAR SI EL JUGADOR SE ENCUENTRA EN LA HABITACIÓN PARA LA ACUSACIÓN FINAL

    private boolean playerIsInRoom (Player player) {
       if (player == null || player.getPiece() == null) {
           return false;
       }

       Piece piece = player.getPiece();

       int x = piece.getX();
       int y = piece.getY();

       for (Room room : rooms) {
           if (room.occupies (x,y)) {
               return true;
           }
       }

        return false;
    }

    // PREGUNTAR SI EL JUGADOR QUIERE HACER LA ACUSACIÓN O NO

    private boolean playerWantsToAccuse (Player player) {

        if (player == null) {
            return false; 
        }
        
        int option = JOptionPane.showConfirmDialog(
                null, player.getName() + ", Do you want to make the final accusation?",
                "Final accusation",
                JOptionPane.YES_NO_OPTION
        );

        return option == JOptionPane.YES_NO_OPTION;
    }

    // COMPROBAR ACUSACIÓN

    public boolean checkAccusation (Card room, Card actor, Card weapon) {
        if (gameState == null || !gameState.canContinue()) {
            return false;
        }

        return accusationValidator.isCorrectAccusation(room, actor, weapon, solutionList);
    }

    // ACTUALIZAR LOS JUGADORES

    private void updatePlayers() {
        for (Player player : players) {
            if (player.getPiece() == null) {
                continue;
            }
        }
    }

    // ACTUALIZAR TABLERO

    private void updateBoard() {
        for (Player player : players) {
            if (player.getPiece() == null) {
                continue;
            }
            
            Piece piece = player.getPiece();

            System.out.printf(player.getName() + "esta en (" + piece.getX() + ", " + piece.getY() + ")");
        }
    }

    // SIGUIENTE JUGADOR

   private Player getNextPlayer(Player currentPlayer, List <Player> players) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        int position = players.indexOf(currentPlayer);

        if (position == -1) {
            return players.get(0);
        }

        position++;

        if (position >= players.size()){
            position = 0;
        }

        return players.get(position);
   }

   // MOSTRAR EL RESULTADO FINAL

    private void showFinalResult() {
        JOptionPane.showMessageDialog(
                null, "La partida ha terminado.",
                "Fin de la partida",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // CERRAR PARTIDA

    public void closeGame() {
        gameIsOver = true;
        currentPlayer = null;

        if (gameState != null) {
            gameState.finish();
        }

        if (roundManager != null) {
            roundManager.stopTurns();
        }

        if (board != null) {
            board.repaint();
        }
    }

    // AÑADIR JUGADOR

    public void addPlayer(Player player) {
        if (player == null) {
            return;
        }

        players.add(player);
    }

    // GETTERS

    public List <Room> getRooms() {
        return rooms;
    }

    public List <Actor> getActors() {
        return actors;
    }

    public List <Weapon> getWeapons() {
        return weapons;
    }

    public List <Card> getSolutionList() {
        return solutionList;
    }

    public List <Card> getDeck() {
        return deck;
    }

    public List <Card> getClues() {
        return clues;
    }

    public List <Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayerPublic() {
        return currentPlayer;
    }

    public boolean gaveIsOver() {
        return gameIsOver;
    }

    public void setGameIsOver(boolean gameIsOver) {
        this.gameIsOver = gameIsOver;
    }

    public GameState getGameState () {
        return gameState;
    }

    public RoundManager getRoundManager () {
        return roundManager;
    }

    public MoveValidator getMoveValidator() {
        return moveValidator;
    }

}