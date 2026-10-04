package activation;

import model.Actor;
import model.Card;
import model.Player;
import model.Room;
import model.Weapon;
import model.Figure;
import model.PlayerActivity;

import javax.swing.*;
import java.util.ArrayList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Game {

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

    public List <Figure> getFigures () {
        List <Figure> figures = new ArrayList<>();

        for (Player player : players) {
            if (player.getFigure() != null) {
                figures.add(player.getFigure());
            }
        }

        return figures;
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

        if (!players.isEmpty()) {
            currentPlayer = players.get(0);
        }

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

        Room solutionRoom = rooms.remove(0);

        Actor solutionActor = actors.remove(0);

        Weapon solutionWeapon = weapons.remove(0);

        solutionList.clear();

        solutionList.add(solutionRoom);
        solutionList.add(solutionActor);
        solutionList.add(solutionWeapon);
    }

    // CREAR LA BARAJA

    private void createDeck() {
        deck.clear();
        deck.addAll(rooms);
        deck.addAll(actors);
        deck.addAll(weapons);
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
                    Card card = deck.removeFirst();

                    player.addCard(card);
                }
            }
        }
    }

    // GUARDAR CARTAS RESTANTES COMO PISTAS

    private void getRemainingCards() {
        clues.clear();

        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            clues.add(card);
        }
    }

    // EMPEZAR LA PARTIDA

    private void playGame() {
        while (!gameIsOver && !players.isEmpty()) {
            currentPlayer = getCurrentPlayer();

            sendTurn(currentPlayer);

            // TIRAR DADOS

            dice diceRoll = new dice();

            int dice1 = diceRoll.roll(); // Dado 1
            int dice2 = diceRoll.roll(); // Dado 2

            int totalMv = dice1 + dice2; // Suma de los dos dados
            // MOVER JUGADOR
            movePlayer (currentPlayer, totalMv);
        }

         // falta por implementar este método


        // HACER SOSPECHA

        if (playerIsInRoom(currentPlayer)) {
            // Se hace la sospecha
        }

        // HACER ACUSACIÓN FINAL

        if (playerIsInRoom(currentPlayer)) {
            if (playerWantsToAccuse(currentPlayer)) {
                // Se hace la acusación
            }
        }

        // ACTUALIZAR EL ESTADO

        updatePlayers();
        updateBoard();

        // PASAMOS AL SIGUIENTE JUGADOR

        if (!gameIsOver) {
            currentPlayer = getNextPlayer(currentPlayer, players);
        }

        // FIN DE LA PARTIDA

        showFinalResult();
        closeGame();

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
        if (player == null) {
            return;
        }

        Figure figure = player.getFigure();

        if (figure == null) {
            return;
        }

        for (int i = 0; i < totalMv; i++) {
            int newX = figure.getX() + 1;
            int newY = figure.getY();

            if (figure.canMoveTo(newX, newY, getFigures())){
                figure.moveTo (newX, newY);
            } else {
                break;
            }
        }
    }

    private void registerInactiveTurn (Player player) {
        if (player == null) {
            return;
        }

        player.getActivity().registerInactiveTurn();

        if (player.getActivity().hasTooManyInactiveTurns()) {
            removePlayer(player);
        }
    }

    private void removePlayer (Player player) {
        if (player == null) {
            return;
        }

        int position = players.indexOf(player);

        players.remove(player);
        System.out.println(player.getName() + "has been removed for inactivity");

        if (players.isEmpty()) {
            gameIsOver = true;
            currentPlayer = null;
            return;
        }

        if (position >= players.size()) position = 0;

        if (player == currentPlayer) {
            currentPlayer = getNextPlayer(player, players);
        }
    }

    // COMPROBAR SI EL JUGADOR SE ENCUENTRA EN LA HABITACIÓN PARA LA ACUSACIÓN FINAL

    private boolean playerIsInRoom (Player player) {
       if (player == null || player.getFigure() == null) {
           return false;
       }

       Figure figure = player.getFigure();

       int x = figure.getX();
       int y = figure.getY();

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
                null, player.getName() + ", ¿quieres realizar una acusación final?", 
                "Acusación final",
                JOptionPane.YES_NO_OPTION
        );

        return option == JOptionPane.YES_NO_OPTION;
    }

    // ACTUALIZAR LOS JUGADORES

    private void updatePlayers() {
        for (Player player : players) {
            if (player.getFigure() == null) {
                continue;
            }
        }
    }

    // ACTUALIZAR TABLERO

    private void updateBoard() {
        for (Player player : players) {
            if (player.getFigure() == null) {
                continue;
            }
            
            Figure figure = player.getFigure();

            System.out.printf(player.getName() + "esta en (" + figure.getX() + ", " + figure.getY() + ")");
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

    private void closeGame() {
        gameIsOver = true;
        currentPlayer = null;
    }

    // AÑADIR JUGADOR

    private void addPlayer(Player player) {
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

}