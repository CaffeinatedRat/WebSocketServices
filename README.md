WebSocketServices
===========

This plug-in is an ongoing and evolving project with the intent to bring web support, through websockets, to the Minecraft bukkit servers.
This implementation of websockets is designed to be light-weight and simple, reducing overhead to the game server itself.
Clients that support websockets will have the ability to perform simple informational queries, such as who is online, what version the server is running on, etc.
All text base results will be returned in a simple JSON format for consistency.

* Website: [http://www.caffeinatedrat.com](http://www.caffeinatedrat.com)
* Bukkit: [http://dev.bukkit.org/server-mods/websocketservices/] (http://dev.bukkit.org/server-mods/websocketservices/)
* Bugs/Suggestions: CaffeinatedRat@gmail.com

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

Notes
-----------
Some various notes about the plug-in:

* This plug-in can send fragmented frames but cannot receive it at this time.
* The total frame size can only be 2^16 in size, as larger frames will be truncated.  This will not change since we want to keep this lightweight and prevent the minecraft server from being overloaded.
* Other plug-ins may register for websocket events during the onLoad event with the method WebSocketServices.registerApplicationLayer(Plugin plugin, IApplicationLayer applicationLayer).
* A [wiki] (https://github.com/CaffeinatedRat/WebSocketServices/wiki) on how to use the WebSocketServices API is available.

Change Log
-----------

The following contains information about new features, bug fixes, and other version changes.

#### 1.1.8

* Fixed an issue with plug-ins that had double quotes blowing up the json serialization.
* Added a raw time span value for the offlineplayers, who, and whitelist services.  

#### 1.1.7

* Updated the config.yml so that the offlinePlayers service is renamed to offlineplayers due to the case sensitive nature of the services.

#### 1.1.6

* Changed the the response methodology, so that a response can be sent as text or binary regardless of the initial state of the request.
* Added the proper support for double & float values when serialized as JSON.
* Fixed an issue with an exception being thrown for external services created by other plug-ins.

#### 1.1.5

* Added additional configuration validation checks.
* Added the WebSocketService version number to the ping service.
* Added the framework for the support of service arguments.

#### 1.1.4

* Fixed an issue with WebSocketService plug-in not handling Unicode characters correctly. All frames will now be forced into UTF-8.

#### 1.1.3

* Another major fix, this time to the JSON serialization helper class, which had an incorrect method of calculating maximum recursion depth. This more than likely impacted all services, causing the offline, whitelist, who, and plugin services to all cease working after 10 items.

#### 1.1.2

* Fixed a major issue in the plugin service, where missing plugin information could cause the service to break.

#### 1.1.1

* Fixed a small issue where the online time reported for a first time player was incorrectly calculated.
* Updated to use CraftBukkit 1.4.7-R1.0.

#### 1.1.0

* Added an OfflinePlayers service.
* Added the framework for internal JSON serialization so that plug-ins that hook into this plug-in can focus on adding their data to a collection rather than having to deal with JSON formatting.
* Updated to use CraftBukkit 1.4.7.

#### 1.0.2

* The debug configuration value in the config.yml has been changed to logging.
* Fixed the version number in the plugin.yml file.
* The default values in the config.yml have been updated to disable logging by default, as well as the disable fragmentation testing.
* Updated the handshaking response method so that it enforces a Carriage Return-Line Feed (\r\n) newline instead of an OS specific newline when responding to the browser, as per the HTTP specification.

#### 1.0.0

* Initial Release.

To-Do
-----------
* Add support for receiving fragmented frames.
* Add necessary synchronization for threading; however, all services are read-only and the synch penalty is not currently worth it.
* A security layer for possible individual websocket logins (This will have to be some sort of temporary token, as these services work off client-side scripting and all 'password' information is available for all to see).
* Eventually add https support.
* Eventually add support for batched services to reduce the number of threads occupied by individual requests.
* Develop a consistent format for all services data including those generated by a plug-in.

