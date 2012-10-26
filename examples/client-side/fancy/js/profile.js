$(document).ready(function(){

});


var camera, scene, renderer;
var geometry, material, mesh;
var animationId;

function init_profile() {

	clearTimeout(animationId);

	$('#skinWrapper').html('');

	var canvasWidth = 540;
	var canvasHeight = 540;
	
	camera = new THREE.PerspectiveCamera( 75, (canvasWidth / canvasHeight), 1, 10000 );
	camera.position.z = 1000;

	scene = new THREE.Scene();

	geometry = new THREE.CubeGeometry( 200, 200, 200 );
	material = new THREE.MeshBasicMaterial( { color: 0xffffff, wireframe: false } );

	mesh = new THREE.Mesh( geometry, material );
	scene.add( mesh );

	renderer = new THREE.CanvasRenderer();
	renderer.setSize( canvasWidth, canvasHeight );
	renderer.setClearColor( new THREE.Color( 0x000000 ), 0.0 );

	$('#skinWrapper').append(renderer.domElement);
}

function animate() {

	// note: three.js includes requestAnimationFrame shim
	animationId = requestAnimationFrame( animate );
	
	mesh.rotation.x += 0.01;
	mesh.rotation.y += 0.02;

	renderer.render( scene, camera );

}