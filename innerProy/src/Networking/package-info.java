
/***
 *
 * Public classes are Server and Client<br>
 *<br>
 * use Server initially to create an environment and connect all players to it, including the one whose PC is running the<br>
 * Server object itself, through a Client object<br>
 *<br>
 * Public classes are {@link Networking.Client} and {@link Networking.Server}.<br>
 *<br>
 * A {@link Networking.Server} should first be initialized;<br>
 *      -assign it a {@link javax.sound.sampled.Port} & <br> Networking.server.GameServer<br>
 *<br>
 * A {@link Networking.Client} should then be initialized, order of connection test make a difference<br>
 *      it requires a Port, a host and a Listener<br>
 *      Port is the port which the Clients server will usse for running the game<br>
 *      host is the public IP of the Server<br>
 *      Listener is a functional interface whom, by over-writing its functions during new object creation (AnonymousInnerClass)<br>
 *      explains to the Clients server how to handle each of the cases it may encounter<br>
 */
package Networking;
