package Networking;

/***
 *
 * Public classes are Server and Client
 *
 *
 * use Server initially to create an environment and connect all players to it, including the one whose PC is running the
 * Server object itself, through a Client object
 *
 * Public classes are {@link Networking.Client} and {@link Networking.Server}.
 *
 * A {@link Networking.Server} should first be initialized;
 *      -assign it a {@link javax.sound.sampled.Port} & {@link Networking.GameServer}
 *
 * A {@link Networking.Client} should then be initialized, order of connection test make a difference
 *      it requires a Port, a host and a Listener
 *      Port is the port which the Clients server will usse for running the game
 *      host is the public IP of the Server
 *      Listener is a functional interface whom, by over-writing its functions during new object creation (AnonymousInnerClass)
 *      explains to the Clients server how to handle each of the cases it may encounter
 */