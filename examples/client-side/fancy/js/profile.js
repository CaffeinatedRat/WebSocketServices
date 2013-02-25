//-----------------------------------------------------------------
// Namespace
//-----------------------------------------------------------------
var CaffeinatedRat = CaffeinatedRat || {};
CaffeinatedRat.Minecraft = CaffeinatedRat.Minecraft || {};

//-----------------------------------------------------------------
// ComputedModel Class
//-----------------------------------------------------------------
CaffeinatedRat.Minecraft.ComputedModel = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.image !== undefined) {

		this._image = parameters.image;

	}
	else {

		this._image = null;

	}

	if (parameters.hideHelmet !== undefined) {

		this._hideHelment = parameters.hideHelmet;

	}
	else {

		this._hideHelmet = false;

	}

	if (parameters.scale !== undefined) {

		this._scale = parameters.scale;

	}
	else {

		this._scale = 300;

	}

	//3d Model
	this._headMesh = null;
	this._helmetMesh = null;
	this._bodyMesh = null;
	this._rightArmMesh = null;
	this._leftArmMesh = null;
	this._leftLegMesh = null;
	this._rightLegMesh = null;
}

CaffeinatedRat.Minecraft.ComputedModel.prototype.init = function (scene) {

	//Throw an exception if the scene is undefined since we cannot attach to an undefined scene.
	if (scene === undefined) {

		throw new CaffeinatedRat.Minecraft.ComputedModel.SceneNotDefinedException();

	}

	this._scene = scene;

	texture = new THREE.Texture(this._image);
	texture.magFilter = THREE.NearestFilter;
	texture.minFilter = THREE.NearestFilter;
	texture.needsUpdate = true;

	//----------------------------------------------
	// Define the texture mapping UV.
	//----------------------------------------------

	//Each face texture is 8x8.
	var headGeometry = new THREE.CubeGeometry(this._scale, this._scale, this._scale);

	//Right-side of the head.
	headGeometry.faceVertexUvs[0][1][0] = new THREE.UV(0.0, 0.75);
	headGeometry.faceVertexUvs[0][1][1] = new THREE.UV(0.0, 0.5);
	headGeometry.faceVertexUvs[0][1][2] = new THREE.UV(0.125, 0.5);
	headGeometry.faceVertexUvs[0][1][3] = new THREE.UV(0.125, 0.75);

	//Face...
	headGeometry.faceVertexUvs[0][4][0] = new THREE.UV(0.125, 0.75);
	headGeometry.faceVertexUvs[0][4][1] = new THREE.UV(0.125, 0.5);
	headGeometry.faceVertexUvs[0][4][2] = new THREE.UV(0.25, 0.5);
	headGeometry.faceVertexUvs[0][4][3] = new THREE.UV(0.25, 0.75);

	//Left-side of the head.
	headGeometry.faceVertexUvs[0][0][0] = new THREE.UV(0.25, 0.75);
	headGeometry.faceVertexUvs[0][0][1] = new THREE.UV(0.25, 0.5);
	headGeometry.faceVertexUvs[0][0][2] = new THREE.UV(0.375, 0.5);
	headGeometry.faceVertexUvs[0][0][3] = new THREE.UV(0.375, 0.75);

	//Back of the head.
	headGeometry.faceVertexUvs[0][5][0] = new THREE.UV(0.375, 0.75);
	headGeometry.faceVertexUvs[0][5][1] = new THREE.UV(0.375, 0.5);
	headGeometry.faceVertexUvs[0][5][2] = new THREE.UV(0.5, 0.5);
	headGeometry.faceVertexUvs[0][5][3] = new THREE.UV(0.5, 0.75);

	//Top of the head.
	headGeometry.faceVertexUvs[0][2][0] = new THREE.UV(0.125, 1.00);
	headGeometry.faceVertexUvs[0][2][1] = new THREE.UV(0.125, 0.75);
	headGeometry.faceVertexUvs[0][2][2] = new THREE.UV(0.25, 0.75);
	headGeometry.faceVertexUvs[0][2][3] = new THREE.UV(0.25, 1.00);

	//Bottom of the head.
	headGeometry.faceVertexUvs[0][3][0] = new THREE.UV(0.25, 1.00);
	headGeometry.faceVertexUvs[0][3][1] = new THREE.UV(0.25, 0.75);
	headGeometry.faceVertexUvs[0][3][2] = new THREE.UV(0.375, 0.75);
	headGeometry.faceVertexUvs[0][3][3] = new THREE.UV(0.375, 1.00);

	headMaterial = new THREE.MeshBasicMaterial({ map: texture });
	this._headMesh = new THREE.Mesh(headGeometry, headMaterial);
	this._headMesh.dynamic = true;

	this._scene.add(this._headMesh);

	//Each face texture is 8x8.
	var helmetGeometry = new THREE.CubeGeometry(1.125 * this._scale, 1.125 * this._scale, 1.125 * this._scale);

	//Right-side of the head.
	helmetGeometry.faceVertexUvs[0][1][0] = new THREE.UV(0.5, 0.75);
	helmetGeometry.faceVertexUvs[0][1][1] = new THREE.UV(0.5, 0.5);
	helmetGeometry.faceVertexUvs[0][1][2] = new THREE.UV(0.625, 0.5);
	helmetGeometry.faceVertexUvs[0][1][3] = new THREE.UV(0.625, 0.75);

	//Face...
	helmetGeometry.faceVertexUvs[0][4][0] = new THREE.UV(0.625, 0.75);
	helmetGeometry.faceVertexUvs[0][4][1] = new THREE.UV(0.625, 0.5);
	helmetGeometry.faceVertexUvs[0][4][2] = new THREE.UV(0.75, 0.5);
	helmetGeometry.faceVertexUvs[0][4][3] = new THREE.UV(0.75, 0.75);

	//Left-side of the head.
	helmetGeometry.faceVertexUvs[0][0][0] = new THREE.UV(0.75, 0.75);
	helmetGeometry.faceVertexUvs[0][0][1] = new THREE.UV(0.75, 0.5);
	helmetGeometry.faceVertexUvs[0][0][2] = new THREE.UV(0.875, 0.5);
	helmetGeometry.faceVertexUvs[0][0][3] = new THREE.UV(0.875, 0.75);

	//Back of the head.
	helmetGeometry.faceVertexUvs[0][5][0] = new THREE.UV(0.875, 0.75);
	helmetGeometry.faceVertexUvs[0][5][1] = new THREE.UV(0.875, 0.5);
	helmetGeometry.faceVertexUvs[0][5][2] = new THREE.UV(1.0, 0.5);
	helmetGeometry.faceVertexUvs[0][5][3] = new THREE.UV(1.0, 0.75);

	//Top of the head.
	helmetGeometry.faceVertexUvs[0][2][0] = new THREE.UV(0.625, 1.00);
	helmetGeometry.faceVertexUvs[0][2][1] = new THREE.UV(0.625, 0.75);
	helmetGeometry.faceVertexUvs[0][2][2] = new THREE.UV(0.75, 0.75);
	helmetGeometry.faceVertexUvs[0][2][3] = new THREE.UV(0.75, 1.00);

	//Bottom of the head.
	helmetGeometry.faceVertexUvs[0][3][0] = new THREE.UV(0.75, 1.00);
	helmetGeometry.faceVertexUvs[0][3][1] = new THREE.UV(0.75, 0.75);
	helmetGeometry.faceVertexUvs[0][3][2] = new THREE.UV(0.875, 0.75);
	helmetGeometry.faceVertexUvs[0][3][3] = new THREE.UV(0.875, 1.00);

	var helmetMaterial = new THREE.MeshBasicMaterial({ map: texture, transparent: true });
	helmetMaterial.side = THREE.DoubleSide;

	this._helmetMesh = new THREE.Mesh(helmetGeometry, helmetMaterial);
	this._helmetMesh.dynamic = true;

	this._scene.add(this._helmetMesh);

	//Each body texture is 8 x 12 and 8 x 4
	var bodyGeometry = new THREE.CubeGeometry(this._scale, this._scale * 1.5, this._scale * 0.5);

	//Right-side of the body.
	bodyGeometry.faceVertexUvs[0][1][0] = new THREE.UV(0.25, 0.375);
	bodyGeometry.faceVertexUvs[0][1][1] = new THREE.UV(0.25, 0.0);
	bodyGeometry.faceVertexUvs[0][1][2] = new THREE.UV(0.3125, 0.0);
	bodyGeometry.faceVertexUvs[0][1][3] = new THREE.UV(0.3125, 0.375);

	//Front...
	bodyGeometry.faceVertexUvs[0][4][0] = new THREE.UV(0.3125, 0.375);
	bodyGeometry.faceVertexUvs[0][4][1] = new THREE.UV(0.3125, 0.0);
	bodyGeometry.faceVertexUvs[0][4][2] = new THREE.UV(0.4375, 0.0);
	bodyGeometry.faceVertexUvs[0][4][3] = new THREE.UV(0.4375, 0.375);

	//Left-side of the body.
	bodyGeometry.faceVertexUvs[0][0][0] = new THREE.UV(0.4375, 0.375);
	bodyGeometry.faceVertexUvs[0][0][1] = new THREE.UV(0.4375, 0.0);
	bodyGeometry.faceVertexUvs[0][0][2] = new THREE.UV(0.5, 0.0);
	bodyGeometry.faceVertexUvs[0][0][3] = new THREE.UV(0.5, 0.375);

	//Back
	bodyGeometry.faceVertexUvs[0][5][0] = new THREE.UV(0.5, 0.375);
	bodyGeometry.faceVertexUvs[0][5][1] = new THREE.UV(0.5, 0.0);
	bodyGeometry.faceVertexUvs[0][5][2] = new THREE.UV(0.625, 0.0);
	bodyGeometry.faceVertexUvs[0][5][3] = new THREE.UV(0.625, 0.375);

	//Neck Hole
	bodyGeometry.faceVertexUvs[0][2][0] = new THREE.UV(0.3125, 0.375);
	bodyGeometry.faceVertexUvs[0][2][1] = new THREE.UV(0.3125, 0.5);
	bodyGeometry.faceVertexUvs[0][2][2] = new THREE.UV(0.4375, 0.5);
	bodyGeometry.faceVertexUvs[0][2][3] = new THREE.UV(0.4375, 0.375);

	//Waist.
	bodyGeometry.faceVertexUvs[0][3][0] = new THREE.UV(0.4375, 0.375);
	bodyGeometry.faceVertexUvs[0][3][1] = new THREE.UV(0.4375, 0.5);
	bodyGeometry.faceVertexUvs[0][3][2] = new THREE.UV(0.5625, 0.5);
	bodyGeometry.faceVertexUvs[0][3][3] = new THREE.UV(0.5625, 0.375);

	var bodyMaterial = new THREE.MeshBasicMaterial({ map: texture });
	this._bodyMesh = new THREE.Mesh(bodyGeometry, bodyMaterial);

	//The body's y-position will be: (Body / 2) + (Head / 2)
	this._bodyMesh.position.y -= ((this._scale * 0.75) + (this._scale / 2));
	this._bodyMesh.dynamic = true;

	this._scene.add(this._bodyMesh);

	//Each arm texture is 4 x 12 and 4 x 4
	var armGeometry = new THREE.CubeGeometry(this._scale * 0.5, this._scale * 1.5, this._scale * 0.5);

	//Right-side of the arm.
	armGeometry.faceVertexUvs[0][1][0] = new THREE.UV(0.625, 0.375);
	armGeometry.faceVertexUvs[0][1][1] = new THREE.UV(0.625, 0.0);
	armGeometry.faceVertexUvs[0][1][2] = new THREE.UV(0.6875, 0.0);
	armGeometry.faceVertexUvs[0][1][3] = new THREE.UV(0.6875, 0.375);

	//Front...
	armGeometry.faceVertexUvs[0][4][0] = new THREE.UV(0.6875, 0.375);
	armGeometry.faceVertexUvs[0][4][1] = new THREE.UV(0.6875, 0.0);
	armGeometry.faceVertexUvs[0][4][2] = new THREE.UV(0.75, 0.0);
	armGeometry.faceVertexUvs[0][4][3] = new THREE.UV(0.75, 0.375);

	//Left-side of the arm.
	armGeometry.faceVertexUvs[0][0][0] = new THREE.UV(0.75, 0.375);
	armGeometry.faceVertexUvs[0][0][1] = new THREE.UV(0.75, 0.0);
	armGeometry.faceVertexUvs[0][0][2] = new THREE.UV(0.8125, 0.0);
	armGeometry.faceVertexUvs[0][0][3] = new THREE.UV(0.8125, 0.375);

	//Back
	armGeometry.faceVertexUvs[0][5][0] = new THREE.UV(0.8125, 0.375);
	armGeometry.faceVertexUvs[0][5][1] = new THREE.UV(0.8125, 0.0);
	armGeometry.faceVertexUvs[0][5][2] = new THREE.UV(0.875, 0.0);
	armGeometry.faceVertexUvs[0][5][3] = new THREE.UV(0.875, 0.375);

	//Shoulder
	armGeometry.faceVertexUvs[0][2][0] = new THREE.UV(0.6875, 0.375);
	armGeometry.faceVertexUvs[0][2][1] = new THREE.UV(0.6875, 0.5);
	armGeometry.faceVertexUvs[0][2][2] = new THREE.UV(0.75, 0.5);
	armGeometry.faceVertexUvs[0][2][3] = new THREE.UV(0.75, 0.375);

	//Hand.
	armGeometry.faceVertexUvs[0][3][0] = new THREE.UV(0.75, 0.375);
	armGeometry.faceVertexUvs[0][3][1] = new THREE.UV(0.75, 0.5);
	armGeometry.faceVertexUvs[0][3][2] = new THREE.UV(0.8125, 0.5);
	armGeometry.faceVertexUvs[0][3][3] = new THREE.UV(0.8125, 0.375);

	var armMaterial = new THREE.MeshBasicMaterial({ map: texture });
	this._leftArmMesh = new THREE.Mesh(armGeometry, armMaterial);
	this._leftArmMesh.dynamic = true;

	//The left arms's y-position will be: (Body / 2) + (Head / 2)
	this._leftArmMesh.position.y -= ((this._scale * 0.75) + (this._scale / 2));

	//The left arms's x-position will be: (Body / 2) + (Arm / 2)
	this._leftArmMesh.position.x += ((this._scale / 4) + (this._scale / 2));

	this._scene.add(this._leftArmMesh);

	this._rightArmMesh = new THREE.Mesh(armGeometry, armMaterial);
	this._rightArmMesh.dynamic = true;

	//The right arms's y-position will be: (Body / 2) + (Head / 2)
	this._rightArmMesh.position.y -= ((this._scale * 0.75) + (this._scale / 2));

	//The right arms's x-position will be: (Body / 2) + (Arm / 2)
	this._rightArmMesh.position.x -= ((this._scale / 4) + (this._scale / 2));

	this._scene.add(this._rightArmMesh);

	//Each leg texture is 4 x 8 and 4 x 4
	var legGeometry = new THREE.CubeGeometry(this._scale * 0.5, this._scale * 1.5, this._scale * 0.5);

	//Right-side of the leg.
	legGeometry.faceVertexUvs[0][1][0] = new THREE.UV(0.0, 0.375);
	legGeometry.faceVertexUvs[0][1][1] = new THREE.UV(0.0, 0.0);
	legGeometry.faceVertexUvs[0][1][2] = new THREE.UV(0.0625, 0.0);
	legGeometry.faceVertexUvs[0][1][3] = new THREE.UV(0.0625, 0.375);

	//Front...
	legGeometry.faceVertexUvs[0][4][0] = new THREE.UV(0.0625, 0.375);
	legGeometry.faceVertexUvs[0][4][1] = new THREE.UV(0.0625, 0.0);
	legGeometry.faceVertexUvs[0][4][2] = new THREE.UV(0.125, 0.0);
	legGeometry.faceVertexUvs[0][4][3] = new THREE.UV(0.125, 0.375);

	//Left-side of the legt.
	legGeometry.faceVertexUvs[0][0][0] = new THREE.UV(0.125, 0.375);
	legGeometry.faceVertexUvs[0][0][1] = new THREE.UV(0.125, 0.0);
	legGeometry.faceVertexUvs[0][0][2] = new THREE.UV(0.1875, 0.0);
	legGeometry.faceVertexUvs[0][0][3] = new THREE.UV(0.1875, 0.375);

	//Back
	legGeometry.faceVertexUvs[0][5][0] = new THREE.UV(0.1875, 0.375);
	legGeometry.faceVertexUvs[0][5][1] = new THREE.UV(0.1875, 0.0);
	legGeometry.faceVertexUvs[0][5][2] = new THREE.UV(0.25, 0.0);
	legGeometry.faceVertexUvs[0][5][3] = new THREE.UV(0.25, 0.375);

	//Top
	legGeometry.faceVertexUvs[0][2][0] = new THREE.UV(0.6875, 0.375);
	legGeometry.faceVertexUvs[0][2][1] = new THREE.UV(0.6875, 0.5);
	legGeometry.faceVertexUvs[0][2][2] = new THREE.UV(0.75, 0.5);
	legGeometry.faceVertexUvs[0][2][3] = new THREE.UV(0.75, 0.375);

	//Foot.
	legGeometry.faceVertexUvs[0][3][0] = new THREE.UV(0.75, 0.375);
	legGeometry.faceVertexUvs[0][3][1] = new THREE.UV(0.75, 0.5);
	legGeometry.faceVertexUvs[0][3][2] = new THREE.UV(0.8125, 0.5);
	legGeometry.faceVertexUvs[0][3][3] = new THREE.UV(0.8125, 0.375);

	var legMaterial = new THREE.MeshBasicMaterial({ map: texture });
	this._leftLegMesh = new THREE.Mesh(legGeometry, legMaterial);
	this._leftLegMesh.dynamic = true;

	//The left leg's y-position will be: Body + (Head / 2) + (Leg / 2)
	this._leftLegMesh.position.y -= (this._scale * 1.5) + (this._scale / 2) + (this._scale * 0.75);

	//The left leg's x-position will be: (leg / 2)
	this._leftLegMesh.position.x -= (this._scale * 0.25);

	this._scene.add(this._leftLegMesh);

	this._rightLegMesh = new THREE.Mesh(legGeometry, legMaterial);
	this._rightLegMesh.dynamic = true;

	//The right leg's y-position will be: Body + (Head / 2) + (Leg / 2)
	this._rightLegMesh.position.y -= (this._scale * 1.5) + (this._scale / 2) + (this._scale * 0.75);

	//The right leg's x-position will be: (leg / 2)
	this._rightLegMesh.position.x += (this._scale * 0.25);

	this._scene.add(this._rightLegMesh);

}

CaffeinatedRat.Minecraft.ComputedModel.prototype.animate = function () {

	if (this._leftArmMesh != null) {

		//this._leftArmMesh.translateX(1.00);
		//this._leftArmMesh.rotation.y += 0.01;
		//this._leftArmMesh.rotation.z += 0.01

	}

	this._helmetMesh.visible = !this._hideHelmet;

}

CaffeinatedRat.Minecraft.ComputedModel.prototype.showHelmet = function () {

	this._hideHelmet = false;

}


CaffeinatedRat.Minecraft.ComputedModel.prototype.hideHelmet = function () {

	this._hideHelmet = true;

}

//-----------------------------------------------------------------
// Profile Class
//-----------------------------------------------------------------
CaffeinatedRat.Minecraft.Profile = function (parameters) {

	//-----------------------------------------------------------------
	// Parameterization
	//-----------------------------------------------------------------

	parameters = parameters || {};

	if (parameters.image !== undefined) {

		this._image = parameters.image;

	}
	else {

		this._image = null;

	}

	//We need the websocket Address, otherwise what's the purpose.
	if (parameters.ctrl !== undefined) {

		this._ctrl = parameters.ctrl;

	}
	else {

		this._ctrl = null;

	}

	if (parameters.useWebGL !== undefined) {

		this._useWebGL = parameters.useWebGL;

	}
	else {

		this._useWebGL = false;

	}

	if (parameters.canvasWidth !== undefined) {

		this._canvasWidth = parameters.canvasWidth;

	}
	else {

		this._canvasWidth = 540;

	}

	if (parameters.canvasHeight !== undefined) {

		this._canvasHeight = parameters.canvasHeight;

	}
	else {

		this._canvasHeight = 540;

	}

	if (parameters.hideHelmet !== undefined) {

		this._hideHelmet = parameters._hideHelmet;

	}
	else {

		this._hideHelmet = false;

	}

	if (parameters.positionVector3 !== undefined) {

		this._positionVector3 = parameters.positionVector3;

	}
	else {

		this._positionVector3 = new THREE.Vector3(0.0, 0.0, 0.0);

	}

	if (parameters.scale !== undefined) {

		this._scale = parameters.scale;

	}
	else {

		this._scale = 300;

	}

	this._camera = null;
	this._scene = null;
	this._renderer = null;
	this._controls = null;
	this._animationId = 0;

	this._3dModel = new CaffeinatedRat.Minecraft.ComputedModel({

		hideHelmet: this._hideHelmet,
		image: this._image,
		scale: this._scale

	});
}

CaffeinatedRat.Minecraft.Profile.prototype.init = function () {

	clearTimeout(this._animationId);

	this._camera = new THREE.PerspectiveCamera(75, (this._canvasWidth / this._canvasHeight), 1, 10000);
	this._camera.position.z = 1000;

	this._scene = new THREE.Scene();

	this._3dModel.init(this._scene);

	//----------------------------------------------
	// Define the canvas & renderer
	//----------------------------------------------

	var canvas = document.createElement('canvas');
	canvas.style.width = '100%';
	canvas.style.height = '100%';
	//canvas.style.float = "left";
	//canvas.width = canvas.offsetWidth;
	//canvas.height = canvas.offsetHeight;

	if (this._useWebGL) {

		this._renderer = new THREE.WebGLRenderer({ antialias: false, canvas: canvas });

	}
	else {

		this._renderer = new THREE.CanvasRenderer({ canvas: canvas });

	}

	this._renderer.setSize(this._canvasWidth, this._canvasHeight);
	this._renderer.setClearColor(new THREE.Color(0x000000), 0.0);

	if (this._ctrl != null) {

		this._ctrl.append(this._renderer.domElement);

	}

	//----------------------------------------------
	// Attach the camera controls.
	//----------------------------------------------

	this._controls = new THREE.TrackballControls(this._camera, canvas);

}

CaffeinatedRat.Minecraft.Profile.prototype.animate = function () {

	var that = this;

	//Wrapper
	var callMethod = function () { that.animate(); }

	// note: three.js includes requestAnimationFrame shim
	this._animationId = requestAnimationFrame(callMethod);

	this._3dModel.animate();

	if (this._renderer != null) {

		this._renderer.render(this._scene, this._camera);

	}

	this._controls.update();

}

CaffeinatedRat.Minecraft.Profile.prototype.stop = function () {

	clearTimeout(this._animationId);

}

CaffeinatedRat.Minecraft.Profile.prototype.showHelmet = function () {

	this._3dModel.showHelmet();

}


CaffeinatedRat.Minecraft.Profile.prototype.hideHelmet = function () {

	this._3dModel.hideHelmet();

}

//-----------------------------------------------------------------
// Exceptions
//-----------------------------------------------------------------
CaffeinatedRat.Minecraft.ComputedModel.SceneNotDefinedException = function () {

	var message = "CaffeinatedRat.Minecraft.ComputedModel: A scene must be defined for this model.";

	this.toString = function () {

		return message;

	}
}
