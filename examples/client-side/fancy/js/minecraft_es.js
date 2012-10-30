//Add your server address here, make sure the port number matches that of the port number in the websocketservices/config.yml.
var websocketAddress = "ws://192.168.1.100:25564";

//Adjust this time interval to the number of milliseconds you want the website to ping your server.
var pingInterval = 15000;
var pingTimerPID = 0;

//Ajust this time invertal to the number of milliseconds you want the website to poll information about who is online.
var onlineInterval = 15000;
var onlineTimerPID = 0;

var images = [];

function drawPlayersFace(id, playersName)
{
	/// <summary>
	/// The canvas is used to stretch our tiny 8x8 faces into something more visible.
	/// </summary>
		
	//Determine if we've loaded and cached the image.
	if (images[playersName] === undefined) {

		//Get the player's skin...if only we could get the case-sensitive name so we can pull the skins for players that do not have a completely lowercase name.
		var img = new Image()
		
		img.setAttribute("data-canvasId", id);
		img.onload = function() {
		
			var canvas = document.getElementById(this.getAttribute("data-canvasId"));
			if(canvas !== undefined) {
				var context = canvas.getContext("2d");
				
				if(context !== undefined) {
					context.drawImage(this, 8, 8, 8, 8, 0, 0, canvas.width, canvas.height);
				}
			}
			
			//Cache the images so we don't attempt to pull again...although the browser should be handling this.
			images[playersName] = this;
		};
		img.onerror = function() {
			this.src = "media/char.png";
			
			//Cache the images so we don't attempt to pull again...although the browser should be handling this.
			images[playersName] = this;
		};
		
		//Cool it on the calls to Amazon for now, uncomment out when testing is done.
		img.src = 'http://s3.amazonaws.com/MinecraftSkins/' + playersName + '.png';			
		//img.src = "media/char.png";
	}
	else {

		var img = images[playersName];
	
		var canvas = document.getElementById(id);
		if(canvas !== undefined) {
			var context = canvas.getContext("2d");
				
			if(context !== undefined) {
				context.drawImage(img, 8, 8, 8, 8, 0, 0, canvas.width, canvas.height);
			}
		}
	}
	//END OF if (images[playersName] === undefined) {...
}

function ping()
{
	var connectionOpened = false;
	var ws = new WebSocket(websocketAddress);
	ws.onopen = function() {
		connectionOpened = true;
		ws.send('ping');
	};
	ws.onmessage = function(msg) {
		var json = jQuery.parseJSON(msg.data);
		if(json.Status == "SUCCESSFUL") {
		
			if (json.serverTime % 23000 >= 13000) {
				$("body").addClass("imgNight").removeClass("imgOffline").removeClass("imgDay");
			}
			else {
				$("body").addClass("imgDay").removeClass("imgOffline").removeClass("imgNight");
			}
		}
		//END OF if(json.Status == "SUCCESSFUL") {...
	}
	ws.onclose = function() {
	
		if(connectionOpened) {
			$('#serverStatus').text('ONLINE');
			$('#serverStatus').addClass('online');
			$('#serverStatus').removeClass('offline');
			
			$('#websocketSupported').show();
			$('#offlineDiv').hide();
		}
		else {
			$("body").addClass("imgOffline").removeClass("imgDay").removeClass("imgNight");

			$('#serverStatus').text('OFFLINE');
			$('#serverStatus').addClass('offline');
			$('#serverStatus').removeClass('online');
			
			$('#websocketSupported').hide();
			$('#offlineDiv').show();
		}
		//END OF if(connectionOpened) {...
		
		//We only want to continue this timer when the socket closes.
		pingTimerPID = setTimeout(ping, pingInterval);
	};
	ws.onerror = function(error) {
		console.log('WebSocket Error ' + error);
	};
}

function getServerInfo()
{
	var ws = new WebSocket(websocketAddress);
	ws.onopen = function() {
		ws.send('info');
	};
	ws.onmessage = function(msg) {
		if(msg !== undefined) {
		
			var json = jQuery.parseJSON(msg.data);
						
			if(json.Status == "SUCCESSFUL") {
				$('#minecraftServerName').text(json.serverName);
				$('#minecraftName').text(json.name);
				$('#minecraftVersion').text(json.version);
				$('#bukkitVersion').text(json.bukkitVersion);
				$('#motd').text(json.motd);
				
				var worldType = 'Unknown';
				if (json.worldType.toUpperCase() == "DEFAULT") {
					worldType = "Por defecto";
				}
				else if (json.worldType.toUpperCase() == "FLAT") {
					worldType = "Plano";
				}
				else if (json.worldType.toUpperCase() == "LARGEBIOMES") {
					worldType = "Biomes Grandes";
				}
				
				$('#worldType').text(worldType);
				
				var gameMode = '';
				if (json.gameMode.toUpperCase() == "CREATIVE") {
					gameMode = "Creativo"
				}
				else if (json.gameMode.toUpperCase() == "SURVIVAL") {
					gameMode = "Supervivencia"
				}

				$('#gameMode').text(gameMode);
				$('#isWhiteListed').text(json.isWhiteListed ? 'Sí' : 'No');
				$('#allowsNether').text(json.allowsNether ? 'Sí' : 'No');
				$('#allowsEnd').text(json.allowsEnd ? 'Sí' : 'No');
				$('#allowsFlight').text(json.allowsFlight ? 'Sí' : 'No');
				$('#port').text(json.port);
				$('#ipAddress').text(json.ipAddress);
				$('#time').text( (json.serverTime % 23000 >= 13000) ? "Night" : "Day" );
			}
			//END OF if(json.Status == "SUCCESSFUL") {...
		}
		//END OF if(msg !== undefined) {...
	}
	ws.onerror = function(error) {
		console.log('WebSocket Error ' + error);
	};
}

function getPlayerInfo()
{
	var ws = new WebSocket(websocketAddress);
	ws.onopen = function() {
		ws.send('who');
	};
	ws.onmessage = function(msg) {
		if(msg !== undefined) {
			var json = jQuery.parseJSON(msg.data);
			
			if(json.Status == "SUCCESSFUL") {
				$('#maxNumberOfPlayers').text(json.MaxPlayers);
				$('#totalPlayersOnline').text(json.Players.length);
				
				if(json.Players.length > 0) {
					$('#playerList').text('');
					for(i = 0; i < json.Players.length; i++) {
						//Open the player element.
						var element = '<li><div class="playerElement">'
						
						//Show the mod icon if the user is a moderator.
						if(json.Players[i].isOperator) {
							element += '<div class="inline"><div title="Moderator" class="tiles modTile"></div></div>';
						}
						else {
							element += '<div class="inline"><div title="Player" class="tiles playerTile"></div></div>';
						}
						
						//Show the player's face.
						element += '<div id="test" class="inline"><canvas class="playersFace" id="can';
						element += json.Players[i].name; 
						element += '"></canvas></div>';
						
						//Show the player's name with profile link.
						var environment = json.Players[i].environment.replace('_', '').toLowerCase();
						
						element += '<div class="inline">';
						
						element += '<strong><a class="playerName" href="#" data-name="';
						element += json.Players[i].name;
						element += '" data-environment="';
						element += environment;
						element += '" click="javascript:return false;">';
						element += json.Players[i].name;
						element += '</a></strong>';
						
						element += '</div>';
						
						//Show the total time the player has been online.
						element += '<div class="inline">(Online: ' + json.Players[i].onlineTime + ')</div>';
						
						//Display the environment the player is currently in.
						element += '<div class="inline" style="padding-left: 5px;">';
						element += '<div title="';
						element += json.Players[i].environment.replace('_', ' ');
						element += '" class="tiles ';
						element += environment;
						element += 'Tile"></div>';
						
						//Close the player element.
						element += '</div></li>';				
						
						$('#playerList').append(element);
						
						//Resize the player's face.
						$('#playerList').append('<script>drawPlayersFace("can' + json.Players[i].name + '", "' + json.Players[i].name + '");</script>');
					}
					//END OF for(i = 0; i < json.Players.length; i++) {...
				}
				else  {
					$('#playerList').text('').append('<li>No hay jugadores Online.</li>');
				}
				//END OF if(json.Players.length > 0) {...
			}
			//END OF if(json.Status == "SUCCESSFUL") {...
		}
		//END OF if(msg !== undefined) {...
	};
	ws.onerror = function(error) {
		console.log('WebSocket Error ' + error);
	};	
}

function getWhiteListing()
{
	var ws = new WebSocket(websocketAddress);
	ws.onopen = function() {
		ws.send('whitelist');
	};
	ws.onmessage = function(msg) {
		if(msg !== undefined) {
			var json = jQuery.parseJSON(msg.data);
			
			if(json.Status == "SUCCESSFUL") {
				$('#totalWhitelistedPlayers').text(json.Whitelist.length);
				
				if(json.Whitelist.length > 0) {
					$('#whiteList').text('');
					for(i = 0; i < json.Whitelist.length; i++) {
						
						var onlineTime = 'ahora';
						if(!json.Whitelist[i].isOnline) {
							
							var lastPlayedTime =  'Nunca';
							if(json.Whitelist[i].lastPlayed.toUpperCase() != 'NEVER') {
								lastPlayedTime = json.Whitelist[i].lastPlayed;
							}
							
							onlineTime = 'Last Played: ' + lastPlayedTime + '' 	
						}
					
						//Open the player element.
						var element = '<li><div class="playerElement">';

						//Show the mod icon if the user is a moderator.
						if(json.Whitelist[i].isOperator) {
							element += '<div class="inline"><div title="Op" class="tiles modTile"></div></div>';
						}
						else {
							element += '<div class="inline"><div title="Jugador" class="tiles playerTile"></div></div>';
						}
						
						//Show the player's face.
						element += '<div id="test" class="inline"><canvas class="playersFace" id="canWH' + json.Whitelist[i].name + '"></canvas></div>';		
						
						//Show the player's name with profile link.
						element += '<div class="inline">';
						
						element += '<strong><a class="playerName" href="#" data-name="';
						element += json.Whitelist[i].name;
						element += '" data-environment="';
						element += "normal"
						element += '" click="javascript:return false;">';
						element += json.Whitelist[i].name;
						element += '</a></strong>';
						
						element += '</div>';
						
						//Show the amount of time it has been since the player was online.
						element += '<div class="inline">(' + onlineTime + ')</div>';
						
						//Close the player element.
						element += '</div></li>';
						
						$('#whiteList').append(element);
						
						//Resize the player's face.
						$('#whiteList').append('<script>drawPlayersFace("canWH' + json.Whitelist[i].name + '", "' + json.Whitelist[i].name + '");</script>');						
					}
					//END OF for(i = 0; i < json.Whitelist.length; i++) {...
				}
				else  {
					$('#whiteList').text('').append('<li>No hay informacion sobre la Whitelist.</li>');
				}
				//END OF if(json.Whitelist.length > 0) {...
			}
			//END OF if(json.Status == "SUCCESSFUL") {...
		}
		//END OF if(msg !== undefined) {...
	};
	ws.onerror = function(error) {
		console.log('WebSocket Error ' + error);
	};
}

function getPluginInfo()
{
	var ws = new WebSocket(websocketAddress);
	ws.onopen = function() {
		ws.send('plugins');
	};
	ws.onmessage = function(msg) {
		if(msg !== undefined) {
			var json = jQuery.parseJSON(msg.data);
			
			if(json.Status == "SUCCESSFUL") {
				
				if(msg !== undefined)
				{
					var json = jQuery.parseJSON(msg.data);
					
					if(json.Status == "SUCCESSFUL") {

					$('#pluginList').html('<tr><th>Name</th><th>Version</th><th>Author(s)</th><th>Descriptions</th></tr>');
					
						if(json.Plugins.length > 0) {
							for(i = 0; i < json.Plugins.length; i++) {
							
								//For readability.
								var element = '<tr>';
								element += '<td>' + json.Plugins[i].name + '</td>';
								element += '<td>' + json.Plugins[i].version + '</td>';
								element += '<td>' + json.Plugins[i].author + '</td>';
								element += '<td>' + json.Plugins[i].description + '</td>';
								element += '</tr>';
							
								$('#pluginList').append(element);
							}
							//END OF for(i = 0; i < json.Plugins.length; i++) {...
						}
						else
						{
							$('#pluginList').append('<tr><td colspan="4">No plugins.</td></tr>');
						}
						//END OF if(json.Plugins.length > 0) {...
					}
				}
			}
			//END OF if(json.Status == "SUCCESSFUL") {...
		}
		//END OF if(msg !== undefined) {...
	};
	ws.onerror = function(error) {
		console.log('WebSocket Error ' + error);
	};
}

function Init()
{
	if (("WebSocket" in window)) {
		$('#tabs').tabs( {} );
		ping();
		getServerInfo();
		
		$( "#skinProfile-modal" ).dialog({
            height: 600,
			width: 600,
			resizable: false,
            modal: true,
			autoOpen: false
        });
	}
	else {
		// The browser doesn't support WebSockets
		$('#websocketNotSupported').show();
	}
}

$(document).ready(function(){

	Init();
		
	$('#serverTab').click(function() {
		getServerInfo();
		clearInterval(onlineTimerPID);
	});
	
	$('#playerTab').click(function() {
		getPlayerInfo();
		onlineTimerPID = setInterval(getPlayerInfo, onlineInterval);
	});
	
	$('#whitelistTab').click(function() {
		clearInterval(onlineTimerPID);
		getWhiteListing();
	});
	
	$('#pluginsTab').click(function() {
		clearInterval(onlineTimerPID);
		getPluginInfo();
	});
	
	$('.playerName').live('click', function(e) {
		
		$('#skinProfile-modal').dialog('open');
		$('#skinProfile-modal').dialog('option', 'title', $(this).data('name') + '\'s profile');
		$('#skinWrapper').data('name', $(this).data('name'));
		$('#skinWrapper').removeClass();
		$('#skinWrapper').addClass('profileBackground_' + $(this).data('environment'));
		
		init_profile();
		animate();
		
		e.preventDefault();
	});
});