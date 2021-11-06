# Battleship Game - Using Sockets

A Battleship game developed in 2019 using Java and sockets that can be played by two players.
This game was proposed in a college assignment of the subject Networks. The description (portuguese only) of this assignment can be found [here](description-tp2sockets2019.pdf).

**Restriction of this assignment**: do not use different threads, i.e., one player will have the UI blocked when the other player is making his move.

**Requirements:**
- Java 8 or newer versions


# Running the Battleship Server

Go to the directory **./"Client and Server Projects"/ServidorBatalhaNaval/** and execute the following commands in the terminal to compile and execute, respectively:

> javac -encoding UTF8 -cp . src/servidorbatalhanaval/*.java -d src/build

> java -cp "src/build;src" servidorbatalhanaval.ServidorBatalhaNaval


- Configuration screen:

![configuration screen of the server](misc/server_configuration_screen.png "Server")

# Running the Battleship Client

Go to the directory **./"Client and Server Projects"/BatalhaNaval/** and execute the following commands in the terminal to compile and execute, respectively:

> javac -d src/build -encoding UTF8 -cp "lib/batik-bin-1.11/batik-1.11/lib/batik-all-1.11.jar;lib/batik-bin-1.11/batik-1.11/lib/xml-apis-ext-1.3.04.jar;lib/batik-bin-1.11/batik-1.11/lib/xmlgraphics-commons-2.3.jar;." src/batalhanaval/*.java src/batalhanaval/utils/*.java

> java -cp "lib/batik-bin-1.11/batik-1.11/lib/batik-all-1.11.jar;src/build;lib/batik-bin-1.11/batik-1.11/lib/xml-apis-ext-1.3.04.jar;lib/batik-bin-1.11/batik-1.11/lib/xmlgraphics-commons-2.3.jar;src" batalhanaval.BatalhaNaval

After starting the client, press the button "JOGAR" if the server is online. Then it'll wait for a second player to join and start the game. Thus, you need to execute two clients and the server to play the game.

- Initial screen:

![initial screen of client](misc/client_initial_screen.gif "Client")

- Playing:

![client playing](misc/client_playing.png "Client playing")

Copyright © 2021 Daniel Freitas Martins (dnlfm) and Matheus Freitas Martins (mtsfreitas)