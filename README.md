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

To-Do
-----------
* Add support for fragmented frames.
* Determine how to manage frames larger than 2^16 in size.
* Figure out how to import the Maven dependency 'Base64' into the jar.
* Add a listening layer for other plug-ins to respond to websocket events.