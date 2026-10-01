package activation;

import model.Actor;
import model.Card;
import model.Player;
import model.Room;
import model.Weapon;

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
    }

    // CREAR LA BARAJA

    private void createDeck() {
        deck.clear();

        for (Room room : rooms) {
            deck.add(room);
        }

        for (Actor actor : actors) {
            deck.add(actor);
        }

        for (Weapon weapon : weapons) {
            deck.add(weapon);
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

    private void playGame() {
        while (!gameIsOver) {
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

    // ENVIAR TURNO

    private void sendTurn(Player player) {
        /*
        El turno se tiene que enviar mediante el networking después
         */
    }

    // MOVER JUGADOR

    private  void movePlayer (Player player, int totalMv) {
        /*
        El movimiento en el tablero se implementa después
         */
    }

    // COMPROBAR SI EL JUGADOR SE ENCUENTRA EN LA HABITACIÓN PARA LA ACUSACIÓN FINAL

    private boolean playerIsInRoom (Player player) {
        /*
        Este método chequea si el jugador puede hacer la acusación final o no
         */

        return false;
    }

    // PREGUNTAR SI EL JUGADOR QUIERE HACER LA ACUSACIÓN O NO

    private boolean playerWantsToAccuse (Player player) {

        /*
        El método pregunta si el jugador quiere hacer la acusación o no
        Se conectará a la GUI
         */

        return false;
    }

    // ACTUALIZAR LOS JUGADORES

    private void updatePlayers() {
        /*
        Actualiza la información del jugador
         */
    }

    // ACTUALIZAR TABLERO

    private void updateBoard() {
        // Actualiza el tablero y deja a los jugadores en la posición inicial de partida
    }

    // SIGUIENTE JUGADOR

   private Player getNextPlayer(Player currentPlayer, List <Player> players) {
        int position = players.indexOf(currentPlayer);
        position++;

        if (position >= players.size()) {
            position = 0;
        }

        return players.get(position);
   }

   // MOSTRAR EL RESULTADO FINAL

    private void showFinalResult() {
        // Se muestra a través de la GUI el resultado final
    }

    // CERRAR PARTIDA

    private void closeGame() {
        // Se cierra la partida
    }

    // AÑADIR JUGADOR

    private void addPlayer(Player player) {
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
