WebSocketServices
===========

This plug-in is an ongoing and evolving project with the intent to bring web support, through websockets, to the Minecraft bukkit servers.
This implementation of websockets is designed to be light-weight and simple, reducing overhead to the game server itself.
Clients that support websockets will have the ability to perform simple informational queries, such as who is online, what version the server is running on, etc.
All text base results will be returned in a simple JSON format for consistency.

Website: [http://www.caffeinatedrat.com](http://www.caffeinatedrat.com)  
Bugs/Suggestions: CaffeinatedRat@gmail.com

Compilation
-----------

Maven is required for this project

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and: `mvn clean package`

Coding and Pull Request Conventions
-----------

This project follows a lot of the standards that craftbukkit follows:

* This project follows the [Sun/Oracle coding standards] (http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html).
* No tabs; use 4 spaces instead.
* No trailing whitespaces.
* No CRLF line endings, LF only, put your gits 'core.autocrlf' on 'true'.
* No 80 column limit or 'weird' midstatement newlines.
* The pull request must contain code that builds without errors.
* The pull request must contain code that has been united tested to some degree as to not fail on runtime.
* The description of your pull request should provide detailed information on the pull along with justification of the changes where applicable.

To-Do
-----------
* Add support for receiving fragmented frames (sending fragmented frames is now supported).
* Determine how to manage frames larger than 2^16 in size.
* Figure out how to import the Maven dependency 'Base64' into the jar.
* Add a listening layer for other plug-ins to respond to websocket events (in progress...)
* Create a wiki entry on how to construct the jar for this plugin, as well as how to install the base64-3.8.1.jar.
* A security layer for possible individual websocket logins (This will have to be some sort of temporary token, as these services work off client-side scripting and all 'password' information is available for all to see).
* Eventually add https support.
