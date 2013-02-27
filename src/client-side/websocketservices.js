/**
* Copyright (c) 2012, Ken Anderson <caffeinatedrat at gmail dot com>
* All rights reserved.
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE AUTHOR AND CONTRIBUTORS BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

//-----------------------------------------------------------------
// Namespace
//-----------------------------------------------------------------
var CaffeinatedRat = CaffeinatedRat || {};
CaffeinatedRat.Minecraft = CaffeinatedRat.Minecraft || { };

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices = function (parameters) {

	//-----------------------------------------------------------------
	// Versioning
	//-----------------------------------------------------------------
	CaffeinatedRat.Minecraft.WebSocketServices.VERSION = '1.1.5';
	CaffeinatedRat.Minecraft.WebSocketServices.REVISION = '1';

	console.log('CaffeinatedRat.Minecraft.WebSocketServices.Version: ' + CaffeinatedRat.Minecraft.WebSocketServices.VERSION + '-R.' + CaffeinatedRat.Minecraft.WebSocketServices.REVISION);

	//-----------------------------------------------------------------
	// Member vars
	//-----------------------------------------------------------------

	this._pingTimerPID = 0;
	this._onlineTimerPID = 0;

	this._images = [];
	this._defaultSkin = null;

	var _canvasId = 0;

	//Callbacks
	this._pingConnectedCallback = null;
	this._pingDisconnectedCallback = null;
	this._pingserverTimeCallback = null;
	this._serverInfoCallback = null;
	this._playerInfoCallback = null;
	this._whiteListInfoCallback = null;
	this._offlineInfoCallback = null;
	this._pluginInfoCallback = null;

	//Templates
	this._templateWho = null;
	this._templateWhiteList = null;
	this._templateOfflinePlayerList = null;
	this._templatePluginList = null;

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	//By default, enable the websocket check.
	parameters.websocketCheck = (parameters.websocketCheck === undefined) ? true : parameters.websocketCheck;

	//If the websocket check is enabled throw an exception for browsers that do not support it.
	if ((!("WebSocket" in window)) && (parameters.websocketCheck)) {

		throw new CaffeinatedRat.Minecraft.WebSocketServices.NotSupportedException("constructor");

	}

	//We need the websocket Address, otherwise what's the purpose.
	if (parameters.websocketAddress !== undefined) {

		this._websocketAddress = parameters.websocketAddress;

	}
	else {

		throw new CaffeinatedRat.Minecraft.WebSocketServices.InvalidAddressException("constructor");

	}

	//Determines the ping rate in milliseconds.
	//By default the interval is 15 seconds.
	if (parameters.pingInterval !== undefined) {

		//Adjust this time interval to the number of milliseconds you want the website to ping your server.
		this._pingInterval = parameters.pingInterval;

	}
	else {

		this._pingInterval = 15000;

	}

	//Temporary: Determines rate in milliseconds of getting the player's online.
	//By default the interval is 15 seconds.
	if (parameters.onlineInterval !== undefined) {

		//Adjust this time invertal to the number of milliseconds you want the website to poll information about who is online.
		this._onlineInterval = parameters.onlineInterval;

	}
	else {

		this._onlineInterval = 15000;

	}

	//Determines if the JSON data is dumped to the console.
	//By default is disabled.
	if (parameters.debug !== undefined) {

		this._debug = parameters.debug;

	}
	else {

		this._debug = false;

	}

	//Determines if there is an alternative image server to grab the skins from.
	//By default uses the Amazon server.
	if (parameters.imageServerURL !== undefined) {

		this._imageServerURL = parameters.imageServerURL;

	}
	else {

		//Default to the amazon server if an alternative image server is not provided
		this._imageServerURL = 'http://s3.amazonaws.com/MinecraftSkins/';

	}

	//Determines if all skins should use the default skin.
	//By default is disabled.
	if (parameters.forceDefaultSkin !== undefined) {

		this._forceDefaultSkin = parameters.forceDefaultSkin;

	}
	else {

		this._forceDefaultSkin = false;

	}


	//Assigns the default skin.  If one is not passed then the default steve skin is used.
	if (parameters.defaultSkin !== undefined) {

		this._defaultSkin = parameters.defaultSkin;

	}
	else {

		this._defaultSkin = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAMAAACVQ462AAAABGdBTUEAALGPC/xhBQAAAwBQTFRFAAAAHxALIxcJJBgIJBgKJhgLJhoKJxsLJhoMKBsKKBsLKBoNKBwLKRwMKh0NKx4NKx4OLR0OLB4OLx8PLB4RLyANLSAQLyIRMiMQMyQRNCUSOigUPyoVKCgoPz8/JiFbMChyAFtbAGBgAGhoAH9/Qh0KQSEMRSIOQioSUigmUTElYkMvbUMqb0UsakAwdUcvdEgvek4za2trOjGJUj2JRjqlVknMAJmZAJ6eAKioAK+vAMzMikw9gFM0hFIxhlM0gVM5g1U7h1U7h1g6ilk7iFo5j14+kF5Dll9All9BmmNEnGNFnGNGmmRKnGdIn2hJnGlMnWpPlm9bnHJcompHrHZaqn1ms3titXtnrYBttIRttolsvohst4Jyu4lyvYtyvY5yvY50xpaA////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPSUN6AAAAQB0Uk5T////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////AFP3ByUAAAAYdEVYdFNvZnR3YXJlAFBhaW50Lk5FVCB2My4zNqnn4iUAAAKjSURBVEhLpZSLVtNAEIYLpSlLSUITLCBaGhNBQRM01M2mSCoXNUURIkZFxQvv/wz6724Wij2HCM7J6UyS/b+dmZ208rsww6jiqo4FhannZb5yDqjaNgDVwE/8JAmCMqF6fwGwbU0CKjD/+oAq9jcM27gxAFpNQxU3Bwi9Ajy8fgmGZuvaGAcIuwFA12CGce1jJESr6/Ot1i3Tnq5qptFqzet1jRA1F2XHWQFAs3RzwTTNhQd3rOkFU7c0DijmohRg1TR9ZmpCN7/8+PX954fb+sTUjK7VLKOYi1IAaTQtUrfm8pP88/vTw8M5q06sZoOouSgHEDI5vrO/eHK28el04yxf3N8ZnyQooZiLfwA0arNb6d6bj998/+vx8710a7bW4E2Uc1EKsEhz7WiQBK9eL29urrzsB8ngaK1JLDUXpYAkGSQH6e7640fL91dWXjxZ33138PZggA+Sz0WQlAL4gmewuzC1uCenqXevMPWc9XrMX/VXh6Hicx4ByHEeAfRg/wtgSMAvz+CKEkYAnc5SpwuD4z70PM+hUf+4348ixF7EGItjxmQcCx/Dzv/SOkuXAF3PdT3GIujjGLELNYwxhF7M4oi//wsgdlYZdMXCmEUUSsSu0OOBACMoBTiu62BdRPEjYxozXFyIpK7IAE0IYa7jOBRqGlOK0BFq3Kdpup3DthFwP9QDlBCGKEECoHEBEDLAXHAQMQnI8jwFYRQw3AMOQAJoOADoAVcDAh0HZAKQZUMZdC43kdeqAPwUBEsC+M4cIEq5KEEBCl90mR8CVR3nxwCdBBS9OAe020UGnXb7KcxzPY9SXoEEIBZtgE7UDgBKyLMhgBS2YdzjMJb4XHRDAPiQhSGjNOxKQIZTgC8BiMECgarxprjjO0OXiV4MAf4A/x0nbcyiS5EAAAAASUVORK5CYII=";

	}

	//Determines if image smoothing is enabled.
	//By default is enabled.
	if (parameters.imageSmoothing !== undefined) {

		this._imageSmoothing = parameters.imageSmoothing;

	}
	else {

		this._imageSmoothing = true;

	}

	//-----------------------------------------------------------------
	// Private methods
	//-----------------------------------------------------------------

	this.getCanvasId = function () {

		return _canvasId++;

	}

	//Hide the templates until they are fully constructed.
	$('.wssMinecraftPlayerList').hide();
	$('.wssMinecraftWhiteList').hide();
	$('.wssMinecraftOfflineList').hide();
	$('.wssMinecraftPluginList').hide();

}

//-----------------------------------------------------------------
// Internal methods
//-----------------------------------------------------------------

CaffeinatedRat.Minecraft.WebSocketServices.prototype.getImage = function(key) {

	if (this._images !== undefined) {
	
		return this._images[key];
	
	}
	
	return undefined;

}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.mapToTemplate = function (template, parentContainer, collection, mapFunction) {

	/// <summary>
	/// Maps the data for the specific mapping function to the template and parent container.
	/// </summary>

	if (template != null) {

		//Attempt to get the item template.
		var listItemTemplate = template.find('.wssListItemTemplate');
		if (listItemTemplate.length > 0) {

			if (collection.length > 0) {

				//Remove the templates only, do not remove any one else's content.
				parentContainer.find('.wssListItemTemplate').remove();
				parentContainer.find('.wssEmptyListTemplate').remove();
				//parentContainer.empty();

				//Now purge the container.
				var listItemContainer = listItemTemplate.clone();
				var itemTemplate = listItemContainer.children(':first').detach();
				listItemContainer.appendTo(parentContainer);

				//And attach all processed player elements to our container.
				for (i = 0; i < collection.length; i++) {

					if (mapFunction !== undefined) {

						this[mapFunction](collection[i], listItemContainer, itemTemplate);

					}
					else {

						console.log('CaffeinatedRat.Minecraft.WebSocketServices.mapToTemplate: No mapping function was defined.');
						parentContainer.text('No mapping function was defined.');

					}

				}
				//END OF for (i = 0; i < collection.length; i++) {...

			}
			else {

				//Remove the templates only, do not remove any one else's content.
				parentContainer.find('.wssListItemTemplate').remove();
				parentContainer.find('.wssEmptyListTemplate').remove();
				//parentContainer.empty();

				var emptyListItemContainer = template.find('.wssEmptyListTemplate').clone();
				if (emptyListItemContainer.length > 0) {

					emptyListItemContainer.appendTo(parentContainer);

				}
				else {

					parentContainer.text('No empty-list item template was defined.');

				}
				//END OF if (emptyListItemContainer.length > 0) {...
			}
			//END OF if (collection.length > 0) {...
		}
		else {

			//Remove our empty list template.
			parentContainer.find('.wssEmptyListTemplate').remove();
			parentContainer.text('No list item template was defined.');

		}
		//END OF if (listItemTemplate.length > 0) {...
	}
	else {

		//Remove our empty list template.
		parentContainer.find('.wssEmptyListTemplate').remove();
		parentContainer.text('No list item template was defined.');

	}
	//END OF if (template.length > 0) {...

}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.drawPlayersFace = function (id, playersName) {

	/// <summary>
	/// The canvas is used to stretch our tiny 8x8 faces into something more visible.
	/// </summary>

	var that = this;

	//Determine if we've loaded and cached the image.
	if (that._images[playersName] === undefined) {

		//Get the player's skin...if only we could get the case-sensitive name so we can pull the skins for players that do not have a completely lowercase name.
		var img = new Image();

		img.setAttribute("data-canvasId", id);
		img.onload = function () {

			var canvas = document.getElementById(this.getAttribute("data-canvasId"));
			if ( (canvas !== undefined) && (canvas) ) {

				var context = canvas.getContext("2d");

				if (context !== undefined) {

					context.mozImageSmoothingEnabled = that._imageSmoothing;
					context.webkitImageSmoothingEnabled = that._imageSmoothing;
					context.drawImage(this, 8, 8, 8, 8, 0, 0, canvas.width, canvas.height);

				}
				//END OF if(context !== undefined) {...

			}
			//END OF if(canvas !== undefined) {...

			//Cache the images so we don't attempt to pull again...although the browser should be handling this.
			that._images[playersName] = this;

		};
		img.onerror = function () {

			//this.src = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAMAAACVQ462AAAABGdBTUEAALGPC/xhBQAAAwBQTFRFAAAAHxALIxcJJBgIJBgKJhgLJhoKJxsLJhoMKBsKKBsLKBoNKBwLKRwMKh0NKx4NKx4OLR0OLB4OLx8PLB4RLyANLSAQLyIRMiMQMyQRNCUSOigUPyoVKCgoPz8/JiFbMChyAFtbAGBgAGhoAH9/Qh0KQSEMRSIOQioSUigmUTElYkMvbUMqb0UsakAwdUcvdEgvek4za2trOjGJUj2JRjqlVknMAJmZAJ6eAKioAK+vAMzMikw9gFM0hFIxhlM0gVM5g1U7h1U7h1g6ilk7iFo5j14+kF5Dll9All9BmmNEnGNFnGNGmmRKnGdIn2hJnGlMnWpPlm9bnHJcompHrHZaqn1ms3titXtnrYBttIRttolsvohst4Jyu4lyvYtyvY5yvY50xpaA////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPSUN6AAAAQB0Uk5T////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////AFP3ByUAAAAYdEVYdFNvZnR3YXJlAFBhaW50Lk5FVCB2My4zNqnn4iUAAAKjSURBVEhLpZSLVtNAEIYLpSlLSUITLCBaGhNBQRM01M2mSCoXNUURIkZFxQvv/wz6724Wij2HCM7J6UyS/b+dmZ208rsww6jiqo4FhannZb5yDqjaNgDVwE/8JAmCMqF6fwGwbU0CKjD/+oAq9jcM27gxAFpNQxU3Bwi9Ajy8fgmGZuvaGAcIuwFA12CGce1jJESr6/Ot1i3Tnq5qptFqzet1jRA1F2XHWQFAs3RzwTTNhQd3rOkFU7c0DijmohRg1TR9ZmpCN7/8+PX954fb+sTUjK7VLKOYi1IAaTQtUrfm8pP88/vTw8M5q06sZoOouSgHEDI5vrO/eHK28el04yxf3N8ZnyQooZiLfwA0arNb6d6bj998/+vx8710a7bW4E2Uc1EKsEhz7WiQBK9eL29urrzsB8ngaK1JLDUXpYAkGSQH6e7640fL91dWXjxZ33138PZggA+Sz0WQlAL4gmewuzC1uCenqXevMPWc9XrMX/VXh6Hicx4ByHEeAfRg/wtgSMAvz+CKEkYAnc5SpwuD4z70PM+hUf+4348ixF7EGItjxmQcCx/Dzv/SOkuXAF3PdT3GIujjGLELNYwxhF7M4oi//wsgdlYZdMXCmEUUSsSu0OOBACMoBTiu62BdRPEjYxozXFyIpK7IAE0IYa7jOBRqGlOK0BFq3Kdpup3DthFwP9QDlBCGKEECoHEBEDLAXHAQMQnI8jwFYRQw3AMOQAJoOADoAVcDAh0HZAKQZUMZdC43kdeqAPwUBEsC+M4cIEq5KEEBCl90mR8CVR3nxwCdBBS9OAe020UGnXb7KcxzPY9SXoEEIBZtgE7UDgBKyLMhgBS2YdzjMJb4XHRDAPiQhSGjNOxKQIZTgC8BiMECgarxprjjO0OXiV4MAf4A/x0nbcyiS5EAAAAASUVORK5CYII="
			this.src = this._defaultSkin;

			//Cache the images so we don't attempt to pull again...although the browser should be handling this.
			that._images[playersName] = this;

		};

		//If this is enabled we will no longer hit the Amazon or ImageServer and just use the default Steve skin.
		if (this._forceDefaultSkin) {

			img.src = this._defaultSkin;

		}
		else {

			img.src = this._imageServerURL + playersName + '.png';

		}
		//END OF if (this._forceDefaultSkin) {...
	}
	else {

		var img = this._images[playersName];

		var canvas = document.getElementById(id);
		if (canvas !== undefined) {

			var context = canvas.getContext("2d");

			if (context !== undefined) {

				context.mozImageSmoothingEnabled = that._imageSmoothing;
				context.webkitImageSmoothingEnabled = that._imageSmoothing;
				context.drawImage(img, 8, 8, 8, 8, 0, 0, canvas.width, canvas.height);

			}
			//END OF if(context !== undefined) {...

		}
		//END OF if(canvas !== undefined) {...
	}
	//END OF if (this._images[playersName] === undefined) {...
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.playerInfoMapping = function (player, container, template) {

	/// <summary>
	/// Performs mapping of the current player object to the container based on the template.
	/// </summary>

	//template.text(template.text().replace(/ /g, ''));

	var onlineTime = 'Online: Now';

	if (player.onlineTime === undefined) {

		if (!player.isOnline) {

			onlineTime = 'Last Played: ' + player.lastPlayed + ''

		}

	}
	else {

		onlineTime = 'Online: ' + player.onlineTime + ''

	}

	//Make srue we clone our template so we don't modify the DOM elements in it.
	var element = template.clone();

	//Show the mod icon if the user is a moderator.
	if (player.isOperator) {

		element.find('.wssPlayerIsNotOperator').remove();

	}
	else {

		element.find('.wssPlayerIsOperator').remove();

	}

	//Create the canvas element that will display the player's face.
	var canvasId = "canvas" + this.getCanvasId() + player.name;

	//Define the environment if one is provided.
	var environment = "normal";
	if (player.environment !== undefined) {

		environment = player.environment.replace('_', '').toLowerCase();

	}

	//Do a global search and replace for these tags specifically.
	element.html(element.html().replace(/#wssPlayerFace#/g, '<canvas class="playersFace" id="' + canvasId + '"></canvas>'));
	element.html(element.html().replace(/#wssPlayerIsOperator#/g, player.isOperator));
	element.html(element.html().replace(/#wssPlayerName#/g, player.name));
	element.html(element.html().replace(/#wssPlayerEnvironment#/g, environment));
	element.html(element.html().replace(/#wssPlayerOnlineTime#/g, onlineTime));

	element.appendTo(container);

	//Resize the player's face.
	this.drawPlayersFace(canvasId, player.name);
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.pluginInfoMapping = function (plugin, container, template) {
	
	/// <summary>
	/// Performs mapping of the current plug-in object to the container based on the template.
	/// </summary>

	var element = template.clone();

	element.html(element.html().replace(/#wssPluginName#/g, plugin.name));
	element.html(element.html().replace(/#wssPluginVersion#/g, plugin.version));
	element.html(element.html().replace(/#wssPluginAuthor#/g, plugin.author));
	element.html(element.html().replace(/#wssPluginDescription#/g, plugin.description));

	element.appendTo(container);

}

//-----------------------------------------------------------------
// Public methods
//-----------------------------------------------------------------

CaffeinatedRat.Minecraft.WebSocketServices.prototype.ping = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.connectedCallback !== undefined) {

		this._pingConnectedCallback = parameters.connectedCallback;

	}

	if (parameters.disconnectedCallback !== undefined) {

		this._pingDisconnectedCallback = parameters.disconnectedCallback;

	}

	if (parameters.serverTimeCallback !== undefined) {

		this._pingserverTimeCallback = parameters.serverTimeCallback;

	}

	//Set the update time.
	if (parameters.updateTime !== undefined) {

		this._pingInterval = parameters.updateTime;

	}

	var connectionOpened = false;
	var ws = new WebSocket(this._websocketAddress);
	var that = this;
	ws.onopen = function () {

		connectionOpened = true;
		ws.send('ping');

	};
	ws.onmessage = function (msg) {

		if (that._debug) {

			console.log(msg.data);

		}

		var json = jQuery.parseJSON(msg.data);
		if (json.Status == "SUCCESSFUL") {

			if (that._pingserverTimeCallback) {

				that._pingserverTimeCallback(json.serverTime);

			}

			//Check the version of the WebSocketServices plug-in.
			if (json.wssVersion != CaffeinatedRat.Minecraft.WebSocketServices.VERSION) {

				console.log('[WARNING] CaffeinatedRat.Minecraft.WebSocketServices:  Version mismatch (Plugin: ' + json.wssVersion + ', Client: ' + CaffeinatedRat.Minecraft.WebSocketServices.VERSION + ')');

			}

		}
		//END OF if(json.Status == "SUCCESSFUL") {...

	}
	ws.onclose = function () {

		if (connectionOpened) {

			if (that._pingConnectedCallback) {

				that._pingConnectedCallback();

			}

		}
		else {

			if (that._pingDisconnectedCallback) {

				that._pingDisconnectedCallback();

			}

		}
		//END OF if(connectionOpened) {...

		var callMethod = function () { that.ping(); }

		//We only want to continue this timer when the socket closes.
		that._pingTimerPID = setTimeout(callMethod, that._pingInterval);

	};
	ws.onerror = function (error) {

		console.log('WebSocket Error ' + error);

	};
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.getServerInfo = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.callback !== undefined) {

		this._serverInfoCallback = parameters.callback;

	}

	var ws = new WebSocket(this._websocketAddress);
	var that = this;
	ws.onopen = function () {

		ws.send('info');

	};
	ws.onmessage = function (msg) {

		if (msg !== undefined) {

			if (that._debug) {

				console.log(msg.data);

			}

			var json = jQuery.parseJSON(msg.data);

			if (json.Status == "SUCCESSFUL") {

				try {

					//Perform callback.
					if (that._serverInfoCallback) {

						that._serverInfoCallback(json);

					}

					$(".wssMinecraftServerName").text(json.serverName);

					$('.wssMinecraftServerType').text(json.name);
					$('.wssMinecraftVersion').text(json.version);
					$('.wssMinecraftBukkitVersion').text(json.bukkitVersion);
					$('.wssMinecraftMOTD').text(json.motd.replace(/\u00A7/g, ''));
					$('.wssMinecraftWorldType').text(json.worldType);
					$('.wssMinecraftGameMode').text(json.gameMode);
					$('.wssMinecraftIsWhiteListed').text(json.isWhiteListed);
					$('.wssMinecraftAllowsNether').text(json.allowsNether);
					$('.wssMinecraftAllowsEnd').text(json.allowsEnd);
					$('.wssMinecraftAllowsFlight').text(json.allowsFlight);
					$('.wssMinecraftPort').text(json.port);
					$('.wssMinecraftIPAddress').text(json.ipAddress);
					$('.wssMinecraftTime').text((json.serverTime % 23000 >= 13000) ? "Night" : "Day");

				}
				catch (err) {

					console.log(err);

				}
			}
			//END OF if(json.Status == "SUCCESSFUL") {...

		}
		//END OF if(msg !== undefined) {...

	}
	ws.onerror = function (error) {

		console.log('WebSocket Error ' + error);

	};
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.getPlayerInfo = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	//Set the update time.
	if (parameters.updateTime !== undefined) {

		this._onlineInterval = parameters.updateTime;

	}

	if (parameters.callback !== undefined) {

		this._playerInfoCallback = parameters.callback;

	}

	var ws = new WebSocket(this._websocketAddress);
	var that = this;
	ws.onopen = function () {
		ws.send('who');
	};
	ws.onmessage = function (msg) {

		if (msg !== undefined) {

			if (that._debug) {

				console.log(msg.data);

			}

			try {

				var json = jQuery.parseJSON(msg.data);

				if (json.Status == "SUCCESSFUL") {

					//Perform callback.
					if (that._playerInfoCallback) {

						that._playerInfoCallback(json);

					}

					$('.wssMinecraftMaxNumberOfPlayers').text(json.MaxPlayers);
					$('.wssMinecraftTotalPlayersOnline').text(json.Players.length);

					var itemList = $('.wssMinecraftPlayerList');
					if (itemList.length > 0) {

						if (that._templateWho == null) {

							that._templateWho = itemList.clone();

						}

					}

					that.mapToTemplate(that._templateWho, itemList, json.Players, 'playerInfoMapping');

				}
				//END OF if(json.Status == "SUCCESSFUL") {...

				$('.wssMinecraftPlayerList').show();

			}
			catch (exception) {

				console.log(exception);

			}

		}
		//END OF if(msg !== undefined) {...

	};
	ws.onclose = function () {

		var callMethod = function () { that.getPlayerInfo(); }

		//We only want to continue this timer when the socket closes.
		if (that._onlineInterval > 0) {

			that._onlineTimerPID = setTimeout(callMethod, that._onlineInterval);

		}
	}
	ws.onerror = function (error) {

		console.log('WebSocket Error ' + error);

	};
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.getWhiteListing = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.callback !== undefined) {

		this._whiteListInfoCallback = parameters.callback;

	}

	var ws = new WebSocket(this._websocketAddress);
	var that = this;
	ws.onopen = function () {
		ws.send('whitelist');
	};
	ws.onmessage = function (msg) {
		if (msg !== undefined) {

			if (that._debug) {
				console.log(msg.data);
			}

			try {

				var json = jQuery.parseJSON(msg.data);

				if (json.Status == "SUCCESSFUL") {

					//Perform callback.
					if (that._whiteListInfoCallback) {

						that._whiteListInfoCallback(json);

					}

					$('.wssTotalWhitelistedPlayers').text(json.Whitelist.length);

					var itemList = $('.wssMinecraftWhiteList');
					if (itemList.length > 0) {

						if (that._templateWhiteList == null) {

							that._templateWhiteList = itemList.clone();

						}

					}

					that.mapToTemplate(that._templateWhiteList, itemList, json.Whitelist, 'playerInfoMapping');

				}
				//END OF if(json.Status == "SUCCESSFUL") {...

				$('.wssMinecraftWhiteList').show();

			}
			catch (exception) {

				console.log(exception);

			}

		}
		//END OF if(msg !== undefined) {...

	};
	ws.onerror = function (error) {

		console.log('WebSocket Error ' + error);

	};
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.getOfflinePlayers = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.callback !== undefined) {

		this._offlineInfoCallback = parameters.callback;

	}

	var ws = new WebSocket(this._websocketAddress);
	var that = this;
	ws.onopen = function () {

		ws.send('offlinePlayers');

	};
	ws.onmessage = function (msg) {

		if (msg !== undefined) {

			if (that._debug) {

				console.log(msg.data);

			}

			try {

				var json = jQuery.parseJSON(msg.data);

				if (json.Status == "SUCCESSFUL") {

					//Perform callback.
					if (that._offlineInfoCallback) {

						that._offlineInfoCallback(json);

					}

					$('#wssTotalOfflinePlayers').text(json.OfflinePlayers.length);

					var itemList = $('.wssMinecraftOfflineList');
					if (itemList.length > 0) {

						if (that._templateOfflinePlayerList == null) {

							that._templateOfflinePlayerList = itemList.clone();

						}

					}

					that.mapToTemplate(that._templateOfflinePlayerList, itemList, json.OfflinePlayers, 'playerInfoMapping');

				}
				//END OF if(json.Status == "SUCCESSFUL") {...

				$('.wssMinecraftOfflineList').show();

			}
			catch (exception) {

				console.log(exception);

			}

		}
		//END OF if(msg !== undefined) {...

	};
	ws.onerror = function (error) {

		console.log('WebSocket Error ' + error);

	};
}

CaffeinatedRat.Minecraft.WebSocketServices.prototype.getPluginInfo = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.callback !== undefined) {

		this._pluginInfoCallback = parameters.callback;

	}

	var ws = new WebSocket(this._websocketAddress);
	var that = this;
	ws.onopen = function () {

		ws.send('plugins');

	};
	ws.onmessage = function (msg) {

		if (msg !== undefined) {

			if (that._debug) {

				console.log(msg.data);

			}

			try {

				var json = jQuery.parseJSON(msg.data);

				if (json.Status == "SUCCESSFUL") {

					//Perform callback.
					if (that._pluginInfoCallback) {

						that._pluginInfoCallback(json);

					}

					var itemList = $('.wssPluginList');
					if (itemList.length > 0) {

						if (that._templatePluginList == null) {

							that._templatePluginList = itemList.clone();

						}

					}

					that.mapToTemplate(that._templatePluginList, itemList, json.Plugins, 'pluginInfoMapping');

				}
				//END OF if(json.Status == "SUCCESSFUL") {...

				$('.wssMinecraftPluginList').show();

			}
			catch (exception) {

				console.log(exception);

			}

		}
		//END OF if(msg !== undefined) {...

	};
	ws.onerror = function (error) {

		console.log('WebSocket Error ' + error);

	};
}

//-----------------------------------------------------------------
// Exceptions
//-----------------------------------------------------------------

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices.Exception = function (caller, message) {

	var internalMessage = "CaffeinatedRat.Minecraft.WebSocketServices" + ( (caller !== undefined) ? ( "." + caller) : "" );
	internalMessage += ": " + message;

	this.toString = function () {

		return internalMessage;

	}

}

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices.NotSupportedException = function (caller) {

	CaffeinatedRat.Minecraft.WebSocketServices.Exception.call(this, caller, "Websockets are not supported by this browser.");

}

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices.InvalidAddressException = function (caller) {

	CaffeinatedRat.Minecraft.WebSocketServices.Exception.call(this, caller, "No WebSocket address has been provided.");

}