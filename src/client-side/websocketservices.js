/** @license
* Copyright (c) 2012-2015, Ken Anderson <caffeinatedrat at gmail dot com>
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

* -----------------------------------------------------------------
* Required Libraries:
* 1) Jquery.js
* -----------------------------------------------------------------

* -----------------------------------------------------------------
* Change Log
* Revision 3 (3/9/13)
* 1) Fixed an issue with the cross-origin property being set for dataurl images.
* 2) An image is now preloaded for the face canvases until the remote images load.
* Revision 4 (3/14/13)
* 1) Fixed some more issues with CORS
* 2) Fixed an issue where the default skin was not loading when a remote skin cannot be loaded.
* Revision 5 (3/31/13)
* 1) Fixed an issue with the preloaded images disappearing.
* 2) Added a loading image feature.
* 3) Added a property to disable the version mismatch warning.
* Revision 6 (5/1/13)
* 1) Added support for the player service.
* Revision 7 (5/11/13)
* 1) Added handling for the template tag wssPlayerIsDead.
* Revision 8 (6/23/13)
* 1) Updated the template item wssHealth to be supported as a class.
* Revision 9 (7/7/13)
* 1) Started using the CaffeinatedRat namespace and crlib library and a new version object.
* 2) Changed the way the prototype methods are written.
* Revision 10 (7/14/13)
* 1) Fixed a major bug in managing the hearts.
* Revision 11 (1/13/14)
* 1) Support for BungeeCord and WebSocketServicesBridge plugin.
* Revision 12 (1/18/15)
* 1) Support for Spigot 1.8.  BungeeCord support is incomplete and has been disabled.
* 2) Support for the seed service added.
* 3) Added undocumented JSON support for mutliple commands & arguments at once, such as '{ "commands": [{ "command": "info"}, { "command": "seed"}]}'
* -----------------------------------------------------------------
*/

"use strict";

//-----------------------------------------------------------------
// Namespace
//-----------------------------------------------------------------
var CaffeinatedRat = CaffeinatedRat || {};
CaffeinatedRat.Minecraft = CaffeinatedRat.Minecraft || {};

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices = function (parameters) {

    //-----------------------------------------------------------------
    // Versioning
    //-----------------------------------------------------------------
    CaffeinatedRat.Minecraft.WebSocketServices.SERVER_VERSION = new CaffeinatedRat.Version(2, 0, 0);
    CaffeinatedRat.Minecraft.WebSocketServices.CLIENT_VERSION = '12';

    console.log('CaffeinatedRat.Minecraft.WebSocketServices.Version: ' + CaffeinatedRat.Minecraft.WebSocketServices.SERVER_VERSION.toString() + '-R.' + CaffeinatedRat.Minecraft.WebSocketServices.CLIENT_VERSION);

    //-----------------------------------------------------------------
    // Member vars
    //-----------------------------------------------------------------

    var _canvasId = 0;

    //PIDS for polling.
    this._pingTimerPID = 0;
    this._onlineTimerPID = 0;
    this._specificPlayerInfoPID = 0;

    //Default values.
    this._images = [];
    this._defaultSkin = null;
    this._preloadFaceDataUrl = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAYAAACinX6EAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAAOwwAADsMBx2+oZAAAABp0RVh0U29mdHdhcmUAUGFpbnQuTkVUIHYzLjUuMTAw9HKhAAAEJ0lEQVRoQ+WXOUtsSxSF/Q8mziPOaCAmBmJg4oSBkeKImojigAMiRiaKBgoqaugv3fd9Ra1mv+JUN9crr/v13bDYdap2lfWtU11tN1SKzs5OQ83NzSGrjRobG215ebms4jL/3xBwd3d3kDcAATk/P2+bm5uljOrKAMG3trZmDfin9F+iry4MANK/ebJMIde9AR6WE4B0Iv4aA4rgKxkwMTFRPwYIvKOjI7S5/VMDilQ3Bgjci34ZUE5xmdqNtrY2Q0CRge3r67Oenp6SAOWNt7e3h4vQG6Aa5jDXr4VU5y9Q2oiTFLdRvdBG9Wa7uroCCBvt7+8vbVrPY2NjNjo6GuowRDXMoU/reAOA9feGDEBxG9ULNt3S0hI2LQi9aRnx8vJiHx8f9vX1Ze/v7/b5+Wm3t7c2MDAQaqhljsxjLdbkmfmCp7/mDNCbYrPIP/f29trd3Z09PT2F/Pj4aDc3N/b29mbn5+dB1KTz/DOQ/s2TZQo5bqN6oY0i3lBTU5Pt7OzYycmJjYyMBHhiYWHBHh4eAjxtAlOooZY5zPXHH3lYxpBORM0ZoGO7sbFhh4eHtru7G+Du7+/t+fm59PX2+vpql5eXtr6+HmqoZQ5zWcOvCWQRfM0YoONKnpubC2DHx8elHzT7+/t2cHBgg4ODARgNDw+HfqCpoZY5zGUNv6bgAdb94P+PiNuoXujXm6DPzs7CG93b2wtwS0tLtri4GAzgqw54tLW1ZSsrK6GGWu4D5p6enoas9RA1OgGSjIvbqJ1YW1szL8C9Ylk2uDOAKxJjMzMzZRWXycbFxUVYB9HmIzk5OVnKsez78RMG6K5I9RMGCF5i3b/WAE6AhyfHsu/HTxhQdPzRTxiQfgT+2IDx8XFDQ0NDIXOLe/mxdHxqaspWV1dLmp6eDt8YR0dH4RuCbwUyog/5ekQfINL19XWQnnP1qknHNV+KmPkQ4HcMQPzR2dnZINrauEwQvDbk65FgUmkdXytAzZGh6bhXxMxHJcBK40UbRDKBE+EhfT1Sf5GYlwL6tVDVDeDYawO0/R+XAb4vV686AH07BfSn6T87Ab6Gz72XQCQ276HVVr+vRYyl0H4+/5RJ29vb4feHF32+Jh2PmPnwcAhowckAxnMGeBjMEGgOzBtGm7GiOeScAVdXVyVA9atG/VLEzEeRAV6VxlMggQhMIKkBZBmQQtPWOh4uNYBcZAD9qomY+RBYDpC+cgakn0FtXCCpAb5eBqS1tLWOhxOglBogg7wiZj4ElgOsNF7uEvJQei5XXyQPlzNA+pYBOr45QPWrTm9HEhAZ+TH/9sm+XnN8vWrUZp6HI6eAGpfo0/EnR8x8CCwHWGkcc/QRIacGIi5LbwB1mqNxBLDW5ZkxgaUGCLLohFTNAAEVCaBK9arxfTJA8nBI4NLvGdDQ8AsymdS9DKPFOgAAAABJRU5ErkJggg==';
    this._specificPlayerInterval = 0;

    //Callbacks
    this._pingConnectedCallback = null;
    this._pingDisconnectedCallback = null;
    this._pingserverTimeCallback = null;
    this._serverInfoCallback = null;
    this._playerInfoCallback = null;
    this._whiteListInfoCallback = null;
    this._offlineInfoCallback = null;
    this._pluginInfoCallback = null;
    this._specificPlayerInfoCallback = null;

    //Templates
    this._templateWho = null;
    this._templateWhiteList = null;
    this._templateOfflinePlayerList = null;
    this._templatePluginList = null;
    this._templateHearts = null;

    //WSSB template
    this._templateWSSBServerInfo = null;

    //-----------------------------------------------------------------
    // Parameterization
    //-----------------------------------------------------------------

    parameters = parameters || {};

    //By default, enable the websocket check.
    parameters.websocketCheck = (parameters.websocketCheck === undefined) ? true : parameters.websocketCheck;

    //If the websocket check is enabled throw an exception for browsers that do not support it.
    if ((!('WebSocket' in window)) && (parameters.websocketCheck)) {

        throw new CaffeinatedRat.Minecraft.WebSocketServices.NotSupportedException('constructor');

    }

    //We need the websocket Address, otherwise what's the purpose.
    if (parameters.websocketAddress !== undefined) {

        this._websocketAddress = parameters.websocketAddress;

    }
    else {

        throw new CaffeinatedRat.Minecraft.WebSocketServices.InvalidAddressException('constructor');

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

        this._defaultSkin = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAMAAACVQ462AAAABGdBTUEAALGPC/xhBQAAAwBQTFRFAAAAHxALIxcJJBgIJBgKJhgLJhoKJxsLJhoMKBsKKBsLKBoNKBwLKRwMKh0NKx4NKx4OLR0OLB4OLx8PLB4RLyANLSAQLyIRMiMQMyQRNCUSOigUPyoVKCgoPz8/JiFbMChyAFtbAGBgAGhoAH9/Qh0KQSEMRSIOQioSUigmUTElYkMvbUMqb0UsakAwdUcvdEgvek4za2trOjGJUj2JRjqlVknMAJmZAJ6eAKioAK+vAMzMikw9gFM0hFIxhlM0gVM5g1U7h1U7h1g6ilk7iFo5j14+kF5Dll9All9BmmNEnGNFnGNGmmRKnGdIn2hJnGlMnWpPlm9bnHJcompHrHZaqn1ms3titXtnrYBttIRttolsvohst4Jyu4lyvYtyvY5yvY50xpaA////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPSUN6AAAAQB0Uk5T////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////AFP3ByUAAAAYdEVYdFNvZnR3YXJlAFBhaW50Lk5FVCB2My4zNqnn4iUAAAKjSURBVEhLpZSLVtNAEIYLpSlLSUITLCBaGhNBQRM01M2mSCoXNUURIkZFxQvv/wz6724Wij2HCM7J6UyS/b+dmZ208rsww6jiqo4FhannZb5yDqjaNgDVwE/8JAmCMqF6fwGwbU0CKjD/+oAq9jcM27gxAFpNQxU3Bwi9Ajy8fgmGZuvaGAcIuwFA12CGce1jJESr6/Ot1i3Tnq5qptFqzet1jRA1F2XHWQFAs3RzwTTNhQd3rOkFU7c0DijmohRg1TR9ZmpCN7/8+PX954fb+sTUjK7VLKOYi1IAaTQtUrfm8pP88/vTw8M5q06sZoOouSgHEDI5vrO/eHK28el04yxf3N8ZnyQooZiLfwA0arNb6d6bj998/+vx8710a7bW4E2Uc1EKsEhz7WiQBK9eL29urrzsB8ngaK1JLDUXpYAkGSQH6e7640fL91dWXjxZ33138PZggA+Sz0WQlAL4gmewuzC1uCenqXevMPWc9XrMX/VXh6Hicx4ByHEeAfRg/wtgSMAvz+CKEkYAnc5SpwuD4z70PM+hUf+4348ixF7EGItjxmQcCx/Dzv/SOkuXAF3PdT3GIujjGLELNYwxhF7M4oi//wsgdlYZdMXCmEUUSsSu0OOBACMoBTiu62BdRPEjYxozXFyIpK7IAE0IYa7jOBRqGlOK0BFq3Kdpup3DthFwP9QDlBCGKEECoHEBEDLAXHAQMQnI8jwFYRQw3AMOQAJoOADoAVcDAh0HZAKQZUMZdC43kdeqAPwUBEsC+M4cIEq5KEEBCl90mR8CVR3nxwCdBBS9OAe020UGnXb7KcxzPY9SXoEEIBZtgE7UDgBKyLMhgBS2YdzjMJb4XHRDAPiQhSGjNOxKQIZTgC8BiMECgarxprjjO0OXiV4MAf4A/x0nbcyiS5EAAAAASUVORK5CYII=';

    }

    //Determines if image smoothing is enabled.
    //By default is enabled.
    if (parameters.imageSmoothing !== undefined) {

        this._imageSmoothing = parameters.imageSmoothing;

    }
    else {

        this._imageSmoothing = true;

    }

    //Determines if a version mismatch warning is shown if a server's version does not match the client's.
    //By default is disabled.
    if (parameters.disableVersionMismatchWarning !== undefined) {

        this._disableVersionMismatchWarning = parameters.disableVersionMismatchWarning;

    }
    else {

        this._disableVersionMismatchWarning = false;

    }

    //Assigns a servername that will is the main server or lobby.
    //This is only useful for use with the WebSocketServicesBridge.
    //By default the first server to response is considered the lobby if one is not specified.
    if (parameters.lobbyServerName !== undefined) {

        this._lobbyServerName = parameters.lobbyServerName;

    }

    //Flattens the server info into one container of data, only showing the lobby server info and hiding player's servernames.
    if (parameters.flattenServerInfo !== undefined) {

        this._flattenServerInfo = parameters.flattenServerInfo;

    }
    else {

        this._flattenServerInfo = true;

    }

    if (parameters.loadingImage !== undefined) {

        this._loadingImageDataUrl = parameters.loadingImage;

    }
    else {

        this._loadingImageDataUrl = 'data:image/gif;base64,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';

    }

    if (parameters.showLoadingImage !== undefined) {

        this._showLoadingImage = parameters.showLoadingImage;

    }
    else {

        this._showLoadingImage = false;

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
    $('.wssMinecraftSpecificPlayer').hide();
    $('.wssMinecraftServerInfo').hide();

};

CaffeinatedRat.Minecraft.WebSocketServices.prototype = {

    constructor: CaffeinatedRat.Minecraft.WebSocketServices,

    //-----------------------------------------------------------------
    // Internal methods
    //-----------------------------------------------------------------

    getImage: function (key) {

        if (this._images !== undefined) {

            var imagePair = this._images[key];
            if (imagePair !== undefined) {

                return this._images[key].img;

            }

        }

        return undefined;

    },

    mapToTemplate: function (mapFunction, $template, $parentContainer, collection, global) {

        /// <summary>
        /// Maps the data for the specific mapping function to the template and parent container.
        /// </summary>

        if (mapFunction === undefined) {

            console.log('CaffeinatedRat.Minecraft.WebSocketServices.mapToTemplate: No mapping function was defined.');
            $parentContainer.text('No mapping function was defined.');
            return;

        }

        if ($template != null) {

            //Attempt to get the item template.
            var listItemTemplate = $template.find('.wssListItemTemplate');
            if (listItemTemplate.length > 0) {

                if (collection.length > 0) {

                    //Remove the templates only, do not remove any one else's content.
                    $parentContainer.find('.wssListItemTemplate').remove();
                    $parentContainer.find('.wssEmptyListTemplate').remove();

                    //Now purge the container.
                    var listItemContainer = listItemTemplate.clone();
                    var itemTemplate = listItemContainer.children(':first').detach();
                    listItemContainer.appendTo($parentContainer);

                    //And attach all processed player elements to our container.
                    if (collection.length === 1) {
                        this[mapFunction](collection[0], listItemContainer, itemTemplate, global);
                    }
                    else {
                        for (var i = 0; i < collection.length; i++) {
                            this[mapFunction](collection[i], listItemContainer, itemTemplate, global);
                        }
                    }

                }
                else {

                    //Remove the templates only, do not remove any one else's content.
                    $parentContainer.find('.wssListItemTemplate').remove();
                    $parentContainer.find('.wssEmptyListTemplate').remove();

                    var emptyListItemContainer = $template.find('.wssEmptyListTemplate').clone();
                    if (emptyListItemContainer.length > 0) {

                        emptyListItemContainer.appendTo($parentContainer);

                    }
                    else {

                        $parentContainer.text('No empty-list item template was defined.');

                    }
                    //END OF if (emptyListItemContainer.length > 0) {...
                }
                //END OF if (collection.length > 0) {...

                return;

            }
            //END OF if (listItemTemplate.length > 0) {...
        }
        //END OF if (template.length > 0) {...

        //Remove our empty list template.
        $parentContainer.find('.wssEmptyListTemplate').remove();
        $parentContainer.text('No list item template was defined.');

    },

    drawFace: function (canvas, image) {

        /// <summary>
        /// Draw just the face for the minecraft skin.
        /// </summary>

        if (image !== undefined) {

            if ((canvas !== undefined) && (canvas)) {

                var context = canvas.getContext('2d');

                if (context !== undefined) {

                    context.mozImageSmoothingEnabled = this._imageSmoothing;
                    context.webkitImageSmoothingEnabled = this._imageSmoothing;
                    context.drawImage(image, 8, 8, 8, 8, 0, 0, canvas.width, canvas.height);

                }
                //END OF if(context !== undefined) {...
            }
            //END OF if(canvas !== undefined) {...
        }
        //END OF if (image !== undefined) {...
    },

    manageSkins: function (id, playersName) {

        /// <summary>
        /// Manage the player's skins and then draw the face.
        /// </summary>

        var that = this;

        //Determine if we've loaded and cached the image.
        if (that._images[playersName] === undefined || that._images[playersName].preloaded) {

            //Preload a temporary image...
            var preImg = new Image();
            preImg.onload = function () {

                //Preload the image.
                var canvas = document.getElementById(id);
                that.drawFace(canvas, this);
                that._images[playersName] = { img: this, preloaded: true };

                //Get the player's skin...if only we could get the case-sensitive name so we can pull the skins for players that do not have a completely lowercase name.
                var img = new Image();

                // --- CR (2/27/13) --- Add Cross-Origin capability for servers that enable it.
                // --- CR (3/14/13) --- This has to be here before we even request an image, otherwise we'll get a CORS error.
                // So we'll try to attempt to load the CORS images first.
                img.crossOrigin = '';

                img.setAttribute('data-canvasId', id);
                img.onload = function () {

                    var canvas = document.getElementById(this.getAttribute('data-canvasId'));
                    that.drawFace(canvas, this);

                    //Cache the images so we don't attempt to pull again...although the browser should be handling this.
                    that._images[playersName] = { img: this };

                };
                //The image failed to load, but this could be due to the following conditions, but we do not know which one.
                //1) CORS is not enabled for this image.
                //2) The image does not exist.
                img.onerror = function () {

                    //We only need one reference to the canvas that can be shared among all events below.
                    var canvas = document.getElementById(this.getAttribute('data-canvasId'));

                    // --- CR (3/14/13) --- So...let's try to load our image again without CORS enabled.
                    var imgNoCORS = new Image();
                    imgNoCORS.onload = function () {

                        that.drawFace(canvas, this);

                        //Cache the images so we don't attempt to pull again...although the browser should be handling this.
                        that._images[playersName] = { img: this };

                    }
                    //The image failed to load because it does not exist at this point.
                    imgNoCORS.onerror = function () {

                        // --- CR (3/14/13) --- If we reach this point then the image does not exist and we want to use our default skin.
                        var defaultImg = new Image();
                        defaultImg.onload = function () {

                            that.drawFace(canvas, this);

                            //Cache the images so we don't attempt to pull again...although the browser should be handling this.
                            that._images[playersName] = { img: this };

                        }

                        defaultImg.src = that._defaultSkin;

                    }
                    //END No CORS loading....

                    //If this is enabled we will no longer hit the Amazon or ImageServer and just use the default Steve skin.
                    if (that._forceDefaultSkin) {
                        imgNoCORS.src = that._defaultSkin;
                    }
                    else {
                        imgNoCORS.src = that._imageServerURL + playersName + '.png';
                    }
                    //END OF if (this._forceDefaultSkin) {...

                };
                //END CORS image loading...

                //If this is enabled we will no longer hit the Amazon or ImageServer and just use the default Steve skin.
                if (that._forceDefaultSkin) {
                    img.src = that._defaultSkin;
                }
                else {
                    img.src = that._imageServerURL + playersName + '.png';
                }
                //END OF if (this._forceDefaultSkin) {...

            }
            //END OF preImg.onload = function() {...

            preImg.src = that._preloadFaceDataUrl;

        }
        else {
            that.drawFace(document.getElementById(id), this._images[playersName].img, this._images[playersName].preloaded);
        }
        //END OF if (this._images[playersName] === undefined) {...
    },

    playerTemplateTags: function (element, player) {

        var onlineTime = 'Online: Now';

        if (player.onlineTime === undefined) {

            if (!player.isOnline) {
                onlineTime = 'Last Played: ' + player.lastPlayed + ''
            }

        }
        else {
            onlineTime = 'Online: ' + player.onlineTime + ''
        }

        //Define the environment if one is provided.
        var environment = 'normal';
        if (player.environment !== undefined) {
            environment = player.environment.replace('_', '').toLowerCase();
        }

        //Template Fields
        element.html(element.html().replace(/#wssPlayerIsOperator#/g, player.isOperator));
        element.html(element.html().replace(/#wssPlayerName#/g, player.name));
        element.html(element.html().replace(/#wssPlayerEnvironment#/g, environment));
        element.html(element.html().replace(/#wssPlayerOnlineTime#/g, onlineTime));
        element.html(element.html().replace(/#wssPlayerId#/g, player.uuid));

        //Replace the content of all containers as well.
        element.find('.wssPlayerOnlineTime').text(onlineTime);
        element.find('.wssPlayerName').text(player.name);
        element.find('.wssPlayerEnvironment').text(environment);

    },

    //-----------------------------------------------------------------
    // Templates
    //-----------------------------------------------------------------

    serverInfoMapping: function (info, container, template) {

        //Make sure we clone our template so we don't modify the DOM elements in it.
        var element = template.clone();

        if (info.wssbServerName !== '') {
            if (element.find('legend').length === 0) {
                element.prepend('<legend>' + info.wssbServerName + '</legend>');
            }
        }

        element.find('.wssMinecraftServerName').text(info.serverName);
        element.find('.wssMinecraftServerType').text(info.name);
        element.find('.wssMinecraftVersion').text(info.version);
        element.find('.wssMinecraftBukkitVersion').text(info.bukkitVersion);
        element.find('.wssMinecraftMOTD').text(info.motd.replace(/\u00A7/g, ''));
        element.find('.wssMinecraftWorldType').text(info.worldType);
        element.find('.wssMinecraftGameMode').text(info.gameMode);
        element.find('.wssMinecraftIsWhiteListed').text(info.isWhiteListed);
        element.find('.wssMinecraftAllowsNether').text(info.allowsNether);
        element.find('.wssMinecraftAllowsEnd').text(info.allowsEnd);
        element.find('.wssMinecraftAllowsFlight').text(info.allowsFlight);
        element.find('.wssMinecraftPort').text(info.port);
        element.find('.wssMinecraftIPAddress').text(info.ipAddress);
        element.find('.wssMinecraftTime').text((info.serverTime % 23000 >= 13000) ? 'Night' : 'Day');
        element.find('.wssMinecraftSeed').text(info.seed);

        element.appendTo(container);

    },

    playerInfoMapping: function (player, container, template) {

        /// <summary>
        /// Performs mapping of the current player object to the container based on the template.
        /// </summary>

        //Make srue we clone our template so we don't modify the DOM elements in it.
        var element = template.clone();

        //Show the mod icon if the user is a moderator.
        if (player.isOperator) {
            element.find('.wssPlayerIsNotOperator').remove();
        }
        else {
            element.find('.wssPlayerIsOperator').remove();
        }

        //Do a global search and replace for these tags specifically.
        //Create the canvas element that will display the player's face.
        var canvasId = 'canvas' + this.getCanvasId() + player.name;
        var canvasElement = '<canvas class="playersFace" id="' + canvasId + '"></canvas>';

        //Template Fields
        element.html(element.html().replace(/#wssPlayerFace#/g, canvasElement));
        element.html(element.html().replace(/#wssPlayerId#/g, player.uuid));
        element.find('.wssPlayerFace').html(canvasElement);

        this.playerTemplateTags(element, player);

        element.appendTo(container);

        //Resize the player's face.
        this.manageSkins(canvasId, player.name);
    },

    heartInfoMapping: function (heart, container, template, json) {

        /// <summary>
        /// Performs mapping of the hearts  to the template.
        /// </summary>

        var element = template.clone();

        //Determine the number of hearts and if they're full or empty.
        var numberofFullHearts = Math.ceil(json.health / 2);
        if ((json.health > 0) && ((heart + 1) <= numberofFullHearts)) {

            //Determine if it is the last heart and if it's half instead of whole.
            if ((json.health % 2 === 1) && ((heart + 1) === numberofFullHearts)) {
                element.find('.wssPlayerFullHeart').remove();
            }
            else {
                element.find('.wssPlayerHalfHeart').remove();
            }

            element.find('.wssPlayerEmptyHeart').remove();
        }
        //No hearts...
        else {

            element.find('.wssPlayerFullHeart').remove();
            element.find('.wssPlayerHalfHeart').remove();

        }

        element.html(element.html().replace(/#wssHealth#/g, json.health));
        element.find('.wssHealth').text(json.health);

        element.appendTo(container);

    },

    pluginInfoMapping: function (plugin, container, template) {

        /// <summary>
        /// Performs mapping of the current plug-in object to the container based on the template.
        /// </summary>

        var element = template.clone();

        //Template Fields
        element.html(element.html().replace(/#wssPluginName#/g, plugin.name));
        element.html(element.html().replace(/#wssPluginVersion#/g, plugin.version));
        element.html(element.html().replace(/#wssPluginAuthor#/g, plugin.author));
        element.html(element.html().replace(/#wssPluginDescription#/g, plugin.description));

        //Replace the content of all containers as well.
        element.find('.wssPluginName').text(plugin.name);
        element.find('.wssPluginVersion').text(plugin.version);
        element.find('.wssPluginAuthor').text(plugin.author);
        element.find('.wssPluginDescription').html(plugin.description);

        element.appendTo(container);

    },

    //-----------------------------------------------------------------
    // Public methods
    //-----------------------------------------------------------------

    ping: function (parameters) {

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

        //Store each event that occurs and handle it on the close event.
        var _messageStack = new Array();

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

            //Add to the stack.
            _messageStack.push(msg.data);

        };
        ws.onclose = function () {

            var serverInfo = new Array();
            var lobbyServerTime = '';
            var status = 'FAILED';

            var iteration = 0;
            while (_messageStack.length > 0) {

                var json = jQuery.parseJSON(_messageStack.pop());
                var wssVersion = '';
                var serverName = '';

                //Detect if the bridge service is being used.
                if (json && 'wssb' in json) {

                    //Get the server name.
                    serverName = json.serverName;

                    //Capture the server time for the lobby, if one is not specified then use the first one that is encountered.
                    if (((that._lobbyServerName === undefined) && (iteration === 0)) || (that._lobbyServerName.toLowerCase() === serverName.toLowerCase())) {
                        lobbyServerTime = json.serverInfo.serverTime;
                    }

                    status = json.serverInfo.status;
                    wssVersion = json.serverInfo.wssVersion;
                    serverInfo.push({ serverName: serverName, serverTime: json.serverInfo.serverTime, wssVersion: json.serverInfo.wssVersion });

                }
                //Non-bridge service.
                else {

                    lobbyServerTime = json.serverTime;
                    status = json.status;
                    wssVersion = json.wssVersion;
                    serverInfo.push({ serverName: '', serverTime: lobbyServerTime, wssVersion: wssVersion });

                }

                if (status === 'SUCCESSFUL') {

                    //Check the version of the WebSocketServices plug-in.
                    if ((!that._disableVersionMismatchWarning) && (wssVersion !== CaffeinatedRat.Minecraft.WebSocketServices.SERVER_VERSION.toString())) {

                        if (serverName === '') {
                            console.log('[WARNING] CaffeinatedRat.Minecraft.WebSocketServices:  Version mismatch (Plugin: ' + wssVersion + ', Client: ' + CaffeinatedRat.Minecraft.WebSocketServices.SERVER_VERSION + ')');
                        }
                        else {
                            console.log('[WARNING] CaffeinatedRat.Minecraft.WebSocketServices:  Version mismatch (Server: ' + serverName + ', Plugin: ' + wssVersion + ', Client: ' + CaffeinatedRat.Minecraft.WebSocketServices.SERVER_VERSION + ')');
                        }

                    }

                }

                iteration++;
            }
            //END OF while (_messageStack.length > 0) {...

            if (status == 'SUCCESSFUL') {

                if (that._pingserverTimeCallback) {
                    that._pingserverTimeCallback(lobbyServerTime, serverInfo);
                }

            }
            //END OF if(status == 'SUCCESSFUL') {...

            if (connectionOpened) {

                if (that._pingConnectedCallback) {
                    that._pingConnectedCallback(serverInfo);
                }

            }
            else {

                if (that._pingDisconnectedCallback) {
                    that._pingDisconnectedCallback(serverInfo);
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
    },

    getServerInfo: function (parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        if (parameters.callback !== undefined) {
            this._serverInfoCallback = parameters.callback;
        }

        //Store each event that occurs and handle it on the close event.
        var _messageStack = new Array();

        var ws = new WebSocket(this._websocketAddress);
        var that = this;
        var show = false;
        ws.onopen = function () {
            ws.send('{ "commands": [{ "command": "info"}, { "command": "seed"}]}');
        };
        ws.onmessage = function (msg) {

            if (msg !== undefined) {

                if (that._debug) {
                    console.log(msg.data);
                }

                //Add to the stack.
                _messageStack.push(msg.data);

            }
            //END OF if(msg !== undefined) {...

        };
        ws.onclose = function () {

            try {

                var itemList = $('.wssMinecraftServerInfo');
                if (itemList.length > 0) {

                    if (that._templateWSSBServerInfo == null) {
                        that._templateWSSBServerInfo = itemList.clone();
                    }

                }

                var collection = new Array();
                while (_messageStack.length > 0) {

                    //Perform callback and cancel the native functionality if the returned value is false.
                    if ((that._serverInfoCallback) && (!that._serverInfoCallback(json))) {
                        continue;
                    }

                    var json = jQuery.parseJSON(_messageStack.pop());

                    if (json.status === "SUCCESSFUL") {

                        //Detect if the bridge service is being used.
                        if (json && 'wssb' in json) {
                            json.serverInfo.wssbServerName = json.serverName;
                            json = json.serverInfo;
                        }
                        else {
                            json.wssbServerName = '';
                        }

                        collection.push(json);

                    }
                    //END OF if (json.status === "SUCCESSFUL") {...

                }
                //END OF while (_messageStack.length > 0) {...

                that.mapToTemplate('serverInfoMapping', that._templateWSSBServerInfo, itemList, collection, null);
                show = true;
            }
            catch (err) {
                console.log(err);
            }

            if (show) {
                $('.wssMinecraftServerInfo').show();
            }
            else {
                $('.wssMinecraftServerInfo').hide();
            }
            //END OF if (show) {...
        };
        ws.onerror = function (error) {
            console.log('WebSocket Error ' + error);
        };
    },

    getPlayerInfo: function (parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        if (parameters.callback !== undefined) {
            this._playerInfoCallback = parameters.callback;
        }

        //Set the update time.
        if (parameters.updateTime !== undefined) {
            this._onlineInterval = parameters.updateTime;
        }

        //Internal parameter to prevent additional timers from firing.
        if (parameters.internalCall === undefined) {
            this.stopPlayerUpdates();
        }

        //Store each event that occurs and handle it on the close event.
        var _messageStack = new Array();

        var ws = new WebSocket(this._websocketAddress);
        var show = false;
        var that = this;
        ws.onopen = function () {
            ws.send('who');
        };
        ws.onmessage = function (msg) {

            if (msg !== undefined) {

                if (that._debug) {
                    console.log(msg.data);
                }

                //Add to the stack.
                _messageStack.push(msg.data);

            }
            //END OF if(msg !== undefined) {...
        };
        ws.onclose = function () {

            try {

                var itemList = $('.wssMinecraftPlayerList');
                if (itemList.length > 0) {
                    if (that._templateWho == null) {
                        that._templateWho = itemList.clone();
                    }
                }

                var maxNumOfPlayers = 0;
                var numberOfPlayers = 0;
                var collection = new Array();
                while (_messageStack.length > 0) {

                    //Perform callback and cancel the native functionality if the returned value is false.
                    if ((that._playerInfoCallback) && (!that._playerInfoCallback(json))) {
                        continue;
                    }

                    var json = jQuery.parseJSON(_messageStack.pop());
                    
                    if (json.status === "SUCCESSFUL") {

                        //Detect if the bridge service is being used.
                        if (json && 'wssb' in json) {
                            json.serverInfo.wssbServerName = json.serverName;
                            json = json.serverInfo;
                        }
                        else {
                            json.wssbServerName = '';
                        }

                        //Get the maximum number of players for all servers.
                        maxNumOfPlayers = Math.max(json.MaxPlayers, maxNumOfPlayers);
                        numberOfPlayers = json.Players.length;

                        //BungeeCord support suppressed.
                        //Compile the collection of all of the players from all the subservers into one collection.
                        //NOTE: Missing template information to determine if the player information should be separated into different blocks per server or integrated as one server.
                        for (var i = 0; i < json.Players.length; i++) {
                            collection.push(json.Players[i]);
                        }
                    }
                    //END OF if (json.status === "SUCCESSFUL") {...

                }
                //END OF while (_messageStack.length > 0) {...

                $('.wssMinecraftMaxNumberOfPlayers').text(maxNumOfPlayers);
                $('.wssMinecraftTotalPlayersOnline').text(numberOfPlayers);

                that.mapToTemplate('playerInfoMapping', that._templateWho, itemList, collection, null);
                show = true;

            }
            catch (exception) {
                console.log(exception);
            }

            //We only want to continue this timer when the socket closes.
            if (that._onlineInterval > 0) {
                var callMethod = function () { that.getPlayerInfo({ internalCall: true }); }
                that._onlineTimerPID = setTimeout(callMethod, that._onlineInterval);
            }

            if (show) {
                $('.wssMinecraftPlayerList').show();
            }
            else {
                $('.wssMinecraftPlayerList').hide();
            }
            //END OF if (show) {...

        };
        ws.onerror = function (error) {
            console.log('WebSocket Error ' + error);
        };
    },

    stopPlayerUpdates: function () {

        if (this._onlineTimerPID > 0) {
            clearTimeout(this._onlineTimerPID);
            this._onlineTimerPID = 0;
        }

    },

    getWhiteListing: function (parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        if (parameters.callback !== undefined) {
            this._whiteListInfoCallback = parameters.callback;
        }

        //Store each event that occurs and handle it on the close event.
        var _messageStack = new Array();

        var ws = new WebSocket(this._websocketAddress);
        var show = false;
        var that = this;
        ws.onopen = function () {

            ws.send('whitelist');

        };
        ws.onmessage = function (msg) {

            if (msg !== undefined) {

                if (that._debug) {
                    console.log(msg.data);
                }

                //Add to the stack.
                _messageStack.push(msg.data);

            }
            //END OF if(msg !== undefined) {...
        };
        ws.onclose = function () {

            try {

                var itemList = $('.wssMinecraftWhiteList');
                if (itemList.length > 0) {
                    if (that._templateWhiteList == null) {
                        that._templateWhiteList = itemList.clone();
                    }
                }

                var maxNumOfPlayers = 0;
                var collection = new Array();
                while (_messageStack.length > 0) {

                    //Perform callback.
                    if ((that._whiteListInfoCallback) && (that._whiteListInfoCallback(json) )) {
                        continue;
                    }

                    var json = jQuery.parseJSON(_messageStack.pop());

                    if (json.status === 'SUCCESSFUL') {

                        //Detect if the bridge service is being used.
                        if (json && 'wssb' in json) {
                            json.serverInfo.wssbServerName = json.serverName;
                            json = json.serverInfo;
                        }
                        else {
                            json.wssbServerName = '';
                        }

                        maxNumOfPlayers = json.Whitelist.length;

                        //BungeeCord support suppressed.
                        //Compile the collection of all of the players from all the subservers into one collection.
                        //NOTE: Missing template information to determine if the player information should be separated into different blocks per server or integrated as one server.
                        for (var i = 0; i < json.Whitelist.length; i++) {
                            collection.push(json.Whitelist[i]);
                        }
                    }
                    //END OF if (json.status === "SUCCESSFUL") {...

                }
                //END OF while (_messageStack.length > 0) {...

                $('.wssTotalWhitelistedPlayers').text(maxNumOfPlayers);

                that.mapToTemplate('playerInfoMapping', that._templateWhiteList, itemList, collection, null);
                show = true;

            }
            catch (exception) {
                console.log(exception);
            }

            if (show) {
                $('.wssMinecraftWhiteList').show();
            }
            else {
                $('.wssMinecraftWhiteList').hide();
            }

        };
        ws.onerror = function (error) {
            console.log('WebSocket Error ' + error);
        };
    },

    getOfflinePlayers: function (parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        if (parameters.callback !== undefined) {
            this._offlineInfoCallback = parameters.callback;
        }

        var ws = new WebSocket(this._websocketAddress);
        var show = false;
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

                    //Perform callback.
                    if (that._offlineInfoCallback) {
                        that._offlineInfoCallback(json);
                    }

                    if (json.status == 'SUCCESSFUL') {

                        $('#wssTotalOfflinePlayers, .wssTotalOfflinePlayers').text(json.OfflinePlayers.length);

                        var itemList = $('.wssMinecraftOfflineList');
                        if (itemList.length > 0) {

                            if (that._templateOfflinePlayerList == null) {

                                that._templateOfflinePlayerList = itemList.clone();

                            }

                        }

                        that.mapToTemplate('playerInfoMapping', that._templateOfflinePlayerList, itemList, json.OfflinePlayers);

                        show = true;

                    }
                    //END OF if(json.status == 'SUCCESSFUL') {...

                }
                catch (exception) {
                    console.log(exception);
                }

            }
            //END OF if(msg !== undefined) {...

        };
        ws.onclose = function () {

            if (that._showLoadingImage) {
                $('#pendingOfflinePlayers').remove();
            }

            if (show) {
                $('.wssMinecraftOfflineList').show();
            }
            else {
                $('.wssMinecraftOfflineList').hide();
            }

        };
        ws.onerror = function (error) {
            console.log('WebSocket Error ' + error);
        };
    },

    getPluginInfo: function (parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        if (parameters.callback !== undefined) {
            this._pluginInfoCallback = parameters.callback;
        }

        var ws = new WebSocket(this._websocketAddress);
        var show = false;
        var that = this;
        ws.onopen = function () {

            if (that._showLoadingImage) {
                $('<div id="pendingPlugins"><img src="' + that._loadingImageDataUrl + '"></img></div>').insertBefore('.wssMinecraftPluginList');
            }

            ws.send('plugins');

        };
        ws.onmessage = function (msg) {

            if (msg !== undefined) {

                if (that._debug) {
                    console.log(msg.data);
                }

                try {

                    var json = jQuery.parseJSON(msg.data);

                    //Perform callback.
                    if (that._pluginInfoCallback) {
                        that._pluginInfoCallback(json);
                    }

                    if (json.status == 'SUCCESSFUL') {

                        var itemList = $('.wssMinecraftPluginList');
                        if (itemList.length > 0) {

                            if (that._templatePluginList == null) {
                                that._templatePluginList = itemList.clone();
                            }

                        }

                        that.mapToTemplate('pluginInfoMapping', that._templatePluginList, itemList, json.Plugins, null);
                        show = true;

                    }
                    //END OF if(json.status == 'SUCCESSFUL') {...

                }
                catch (exception) {
                    console.log(exception);
                }

            }
            //END OF if(msg !== undefined) {...

        };
        ws.onclose = function () {

            if (that._showLoadingImage) {
                $('#pendingPlugins').remove();
            }

            if (show) {
                $('.wssMinecraftPluginList').show();
            }
            else {
                $('.wssMinecraftPluginList').hide();
            }

        };
        ws.onerror = function (error) {
            console.log('WebSocket Error ' + error);
        };
    },

    getSpecificPlayerInfo: function (name, parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        if (parameters.callback !== undefined) {
            this._specificPlayerInfoCallback = parameters.callback;
        }

        if (parameters.updateTime !== undefined) {
            this._specificPlayerInterval = parameters.updateTime;
        }

        //Internal parameter.
        if (parameters.internalCall === undefined) {

            //Prevent additional timers from firing.
            this.stopSpecificPlayerUpdates();

        }

        //Ignore invalid name parameters.
        if ((name === undefined) || (name === '') || (name === null)) {

            if (this._debug) {
                console.log('Invalid name argument');
            }

            return;

        }

        var ws = new WebSocket(this._websocketAddress);
        var show = false;
        var that = this;
        ws.onopen = function () {
            ws.send('player ' + name);
        };
        ws.onmessage = function (msg) {

            if (msg !== undefined) {

                if (that._debug) {
                    console.log(msg.data);
                }

                try {

                    var json = jQuery.parseJSON(msg.data);

                    //Perform callback.
                    if (that._specificPlayerInfoCallback) {
                        that._specificPlayerInfoCallback(name, json);
                    }

                    if (json.status == 'SUCCESSFUL') {

                        //Append the name.
                        json.name = name;

                        var itemList = $('.wssMinecraftSpecificPlayer');
                        if (itemList.length > 0) {

                            $('.wssExperienceBar').css('width', (json.experience * 100) + '%');
                            $('.wssLevel').text(json.level);
                            $('.wssPlayerIsDead').hide();

                            //that.playerTemplateTags(itemList, json);

                            var heartContainer = $('.wssHealthContainer');

                            if (heartContainer.length > 0) {

                                if (that._templateHearts == null) {

                                    that._templateHearts = heartContainer.clone();

                                }
                                //END OF if (that._templateHearts == null) {...

                                if (!json.isDead) {

                                    var collection = new Int32Array(10);
                                    for (var i = 0; i < 10; i++) {
                                        collection[i] = i;
                                    }

                                    that.mapToTemplate('heartInfoMapping', that._templateHearts, heartContainer, collection, json);
                                    $('.wssHealthContainer').show();

                                }
                                else {

                                    $('.wssPlayerIsDead').show();
                                    $('.wssHealthContainer').hide();

                                }
                                //END OF if (!json.isDead) {...
                            }
                            //END OF  if (heartContainer.length > 0) {...
                        }
                        //END OF if (itemList.length > 0) {...

                        show = true;

                    }
                    //END OF if(json.status == 'SUCCESSFUL') {...

                }
                catch (exception) {

                    console.log(exception);

                }

            }
            //END OF if(msg !== undefined) {...

        };
        ws.onclose = function () {

            //We only want to continue this timer when the socket closes.
            if (that._specificPlayerInterval > 0) {

                var callMethod = function () { that.getSpecificPlayerInfo(name, { internalCall: true }); }
                that._specificPlayerInfoPID = setTimeout(callMethod, that._specificPlayerInterval);

            }

            if (show) {
                $('.wssMinecraftSpecificPlayer').show();
            }
            else {
                $('.wssMinecraftSpecificPlayer').hide();
            }

        };
        ws.onerror = function (error) {
            console.log('WebSocket Error ' + error);
        };
    },

    stopSpecificPlayerUpdates: function () {

        if (this._specificPlayerInfoPID > 0) {

            clearTimeout(this._specificPlayerInfoPID);
            this._specificPlayerInfoPID = 0;

        }

    },

    callService: function (service, parameters) {

        //-----------------------------------------------------------------
        // Parameterization
        //-----------------------------------------------------------------

        parameters = parameters || {};

        var ws = new WebSocket(this._websocketAddress);
        var that = this;
        ws.onopen = function () {

            ws.send(service);

        };
        ws.onmessage = function (msg) {

            if (msg !== undefined) {

                if (that._debug) {

                    console.log(msg.data);

                }

                try {

                    var json = jQuery.parseJSON(msg.data);

                    //Perform callback.
                    if (parameters.callback) {

                        parameters.callback(json);

                    }

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

}

//-----------------------------------------------------------------
// Exceptions
//-----------------------------------------------------------------

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices.NotSupportedException = function (caller) {

    CaffeinatedRat.Exception.call(this, 'Minecraft.WebSocketServices.' + caller, 'Websockets are not supported by this browser.');

}

/**
* @constructor
*/
CaffeinatedRat.Minecraft.WebSocketServices.InvalidAddressException = function (caller) {

    CaffeinatedRat.Exception.call(this, 'Minecraft.WebSocketServices.' + caller, 'No WebSocket address has been provided.');

}