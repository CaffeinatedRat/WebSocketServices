
//Globals...
var wss = null;
var profile = null;


function pingConnected() {

	$('#serverStatus').text('ONLINE');
	$('#serverStatus').addClass('online');
	$('#serverStatus').removeClass('offline');

	$('#websocketSupported').show();
	$('#offlineDiv').hide();

}

function pingDisconnected() {

	$("body").addClass("imgOffline").removeClass("imgDay").removeClass("imgNight");

	$('#serverStatus').text('OFFLINE');
	$('#serverStatus').addClass('offline');
	$('#serverStatus').removeClass('online');

	$('#websocketSupported').hide();
	$('#offlineDiv').show();

}

function pingServerTime(serverTime) {

	if (serverTime % 23000 >= 13000) {

		$("body").addClass("imgNight").removeClass("imgOffline").removeClass("imgDay");

	}
	else {

		$("body").addClass("imgDay").removeClass("imgOffline").removeClass("imgNight");

	}

}

function Init() {

	//Instantiate our WebSocketServer object.
	//If websockets are not supported then an exception will be thrown.
	//Let the exception be caught at the top level.
	wss = new CaffeinatedRat.Minecraft.WebSocketServices({
		websocketAddress: 'ws://<Your IP Address here>:25564'
	});

	//If we made it this far then everything is going well so far.  Attempt to ping the server.
	wss.ping({
		connectedCallback: pingConnected,
		disconnectedCallback: pingDisconnected,
		serverTimeCallback: pingServerTime
	});

	wss.getServerInfo();

	//Apply thet JQuery UI tabs & modal dialog to our page.
	$('#tabs').tabs({});

	//The modal pop-up will contain our player's profile model.
	$("#skinProfile-modal").dialog({
		height: 600,
		width: 570,
		resizable: false,
		modal: true,
		autoOpen: false,
		close: function (event, ui) {

			if ( profile != null ) {
				
				profile.stop();
				wss.stopSpecificPlayerInfo();

			}

		}
	});
}

$(document).ready(function () {

	try {

		Init();

		//Show server info.
		$('#serverTab').click(function () {
			wss.getServerInfo();
		});

		//Show players online.
		$('#playerTab').click(function () {

			wss.getPlayerInfo({ updateTime: 15000 });
		});

		//Show white-listed players.
		$('#whitelistTab').click(function () {
			wss.getWhiteListing();
		});

		//Show offline players.
		$('#offlinePlayersTab').click(function () {
			wss.getOfflinePlayers();
		});

		//Show the plug-ins.
		$('#pluginsTab').click(function () {
			wss.getPluginInfo();
		});

		//Toggle the helment.
		$('#toggleHelmet').click(function () {

			if ((profile !== undefined) && (profile != null)) {

				if ($('#toggleHelmet').is(':checked')) {

					profile.showHelmet();

				}
				else {

					profile.hideHelmet();

				}

			}
		});

		$('.playerName').live('click', function (e) {

			var playersName = $(this).data('name');

			$('#skinWrapper').text('');
            //Show the dialog.
            $('#skinProfile-modal').dialog('open');
            $('#skinProfile-modal').dialog('option', 'title', playersName + '\'s profile');

			//Update the background based on the environment.
			$('#skinWrapper').data('name', playersName);
			$('#skinWrapper').removeClass();
			$('#skinWrapper').addClass('profileBackground_' + $(this).data('environment'));
            wss.stopSpecificPlayerInfo();
            wss.getSpecificPlayerInfo(playersName, { updateTime: 3000 });

			try {

				profile = new CaffeinatedRat.Minecraft.SkinProfile({
					container: $('#skinWrapper'),
					skinImage: wss.getImage(playersName),
					useWebGL: false,
					scale: 200,
                    positionVector3: new THREE.Vector3(0.0, 200.0, 0.0)
				});

				profile.init();
				profile.animate();

			}
			catch (err) {

				if (err instanceof CaffeinatedRat.Minecraft.SkinProfile.BrowserNotSupported) {

					$('#elementOverlay').hide();
					$('#webGLNotSupported').show();

				}

				console.log(err);
			}

			e.preventDefault();
		});

	}
	catch (exception) {

		console.log(exception.toString());

		//We're assuming the exception was caused by WebSockets not being supported.
		//Most of the code has been tested to confirm that it is a high likely hood that this issue is a WebSockets one.
		$('#websocketNotSupported').show();
		$('#offlineDiv').hide();

	}
});