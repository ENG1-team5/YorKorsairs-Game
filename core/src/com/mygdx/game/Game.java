
package com.mygdx.game;

import java.util.ArrayList;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;


public class Game extends ApplicationAdapter implements InputProcessor {

	// Declare config, variables
	private final float tileSize = 50;
	private final float scrollFriction = 0.9f;
	private final float scrollAcceleration = 0.5f;
	private final float[] scrollLim = { 0.3f, 2.0f };

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private TiledMap tiledMap;
	private TiledMapRenderer tiledMapRenderer;

	private Player player;
	private ArrayList<College> colleges;
	private float inputScrolled;
	private float scrollVel;


	@Override
	public void create() {
		// Setup scene
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		tiledMap = new TmxMapLoader().load("map.tmx");
		float unitScale = (float) tileSize / tiledMap.getProperties().get("tilewidth", Integer.class);
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, unitScale);
		Gdx.input.setInputProcessor(this);

		// Initialize player and colleges
		player = new Player(this, new Vector2(400, 300));
		colleges = new ArrayList<College>();
		colleges.add(new College(this, new Vector2(300, 600)));
		inputScrolled = 0.0f;
		scrollVel = 0.0f;
	}


	@Override
	public void resize(int width, int height) {
		// Resize camera and move to maintain centre
		Vector3 position = new Vector3(camera.position);
		camera.setToOrtho(false, width, height);
		camera.translate(-camera.viewportWidth * 0.5f, -camera.viewportHeight * 0.5f);
		camera.translate(position.x, position.y);
	}


	private void update() {
		// Run update functions
		updateCamera();
		for (College college : colleges) college.update();
		player.update();
	}


	private void updateCamera() {
		// Follow player with camera
		Vector2 position = player.getPosition();
		float diffX = position.x - camera.position.x;
		float diffY = position.y - camera.position.y;
		camera.translate(diffX, diffY);

		// Scroll if scrolling
		scrollVel += inputScrolled * scrollAcceleration * Gdx.graphics.getDeltaTime();
		scrollVel *= scrollFriction;
		camera.zoom += scrollVel;
		camera.zoom = Math.max(Math.min(camera.zoom, scrollLim[1]), scrollLim[0]);
		inputScrolled = 0.0f;

		// Update camera
		camera.update();
	}


	@Override
	public void render() {
		update();

		// Setup screen
		ScreenUtils.clear(0.443f, 0.718f, 0.467f, 1.0f);
		batch.setProjectionMatrix(camera.combined);
		tiledMapRenderer.setView(camera);
		batch.begin();

		// Render Render functions
		tiledMapRenderer.render();
		for (College college : colleges) college.render(batch);
		player.render(batch);

		// Draw to screen
		batch.end();
	}


	@Override
	public void dispose() {
		// Run dispatch functions on player / colleges
		for (College college : colleges) college.dispose();
		player.dispose();
		tiledMap.dispose();
	}


	@Override
	public boolean keyDown (int keycode) { return false; }

	@Override
	public boolean keyUp (int keycode) { return false; }

	@Override
	public boolean keyTyped (char character) { return false; }

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) { return false; }

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) { return false; }

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) { return false; }

	@Override
	public boolean mouseMoved (int screenX, int screenY) { return false; }

	@Override
	public boolean scrolled(float amountX, float amountY) {
		inputScrolled = amountY;
		return true;
	}
}
