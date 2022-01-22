
package com.mygdx.game;

import java.util.ArrayList;
import java.util.Iterator;

import com.mygdx.game.objectives.Objective;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;


public class Game extends ApplicationAdapter implements InputProcessor {

	// Declare config, variables
	public static final float PPT = 16; // Pixel Per Tile - Used to standardize scaling
	public static final long startTime = System.currentTimeMillis();
	private final float ZoomFriction = 0.86f;
	private final float[] ZoomLim = { PPT * 0.005f, PPT * 0.02f };
	private final float initialZoom = PPT * 0.01f;

	private OrthographicCamera camera;
	private SpriteBatch gameBatch;
	private SpriteBatch UIBatch;
	private TiledMap tiledMap;
	private TiledMapRenderer tiledMapRenderer;
	private MapObjects collisionObjects;

	private Sprite startSprite;
	private Sprite winSprite;
	private Sprite lostSprite;

	private float inputZoomed;
	private float zoomVel;
	private Vector3 worldMousePos;

	private Objective objective;
	private Player player;
	private ArrayList<College> colleges;
	private ArrayList<Projectile> projectiles;
	private ArrayList<Particle> particles;
	private ArrayList<IHittable> hittables;

	private enum GameState { READY, RUNNING, FINISHED };
	private GameState gameState;
	private boolean hasWon;


	@Override
	public void create() {
		// Run setup functions
		setupScene();
		setupTextures();
		resetGame();
	}

	private void setupScene() {
		// Setup scene input / output
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gameBatch = new SpriteBatch();
		UIBatch = new SpriteBatch();
		Gdx.input.setInputProcessor(this);
	}

	private void setupTextures() {
		// Setup tiled map
		tiledMap = new TmxMapLoader().load("./tiles/map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		MapLayer collisionLayer = tiledMap.getLayers().get(1);
		collisionObjects = collisionLayer.getObjects();

		// Load splash textures
		float splashWidth = 350f;
		startSprite = new Sprite(new Texture(Gdx.files.internal("./splashes/startSplash.png")));
		startSprite.setSize(splashWidth, splashWidth * (float)startSprite.getHeight() / startSprite.getWidth());
		startSprite.setOrigin(startSprite.getWidth() * 0.5f, startSprite.getHeight() * 0.5f);
		startSprite.setPosition(
			Gdx.graphics.getWidth() * 0.5f - startSprite.getWidth() * 0.5f,
			Gdx.graphics.getHeight() * 0.5f - startSprite.getHeight() * 0.5f);
		winSprite = new Sprite(new Texture(Gdx.files.internal("./splashes/winSplash.png")));
		winSprite.setSize(splashWidth, splashWidth * (float)winSprite.getHeight() / winSprite.getWidth());
		winSprite.setOrigin(startSprite.getWidth() * 0.5f, winSprite.getHeight() * 0.5f);
		winSprite.setPosition(
			Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
			Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
		lostSprite = new Sprite(new Texture(Gdx.files.internal("./splashes/lostSplash.png")));
		lostSprite.setSize(splashWidth, splashWidth * (float)lostSprite.getHeight() / lostSprite.getWidth());
		lostSprite.setOrigin(lostSprite.getWidth() * 0.5f, lostSprite.getHeight() * 0.5f);
		lostSprite.setPosition(
			Gdx.graphics.getWidth() * 0.5f - lostSprite.getWidth() * 0.5f,
			Gdx.graphics.getHeight() * 0.5f - lostSprite.getHeight() * 0.5f);
	}


	private void resetGame() {
		// Reset camera zoom
		inputZoomed = 0.0f;
		zoomVel = 0.0f;
		camera.zoom = initialZoom;

		// Initialize player and colleges
		objective = Objective.getRandomObjective();
		colleges = new ArrayList<>();
		projectiles = new ArrayList<>();
		particles = new ArrayList<>();
		hittables = new ArrayList<>();
		player = new Player(this, new Vector2(PPT * 17f, PPT * 14.5f));
		colleges.add(new College(this, new Vector2(PPT * 25f, PPT * 14.5f)));
		colleges.add(new College(this, new Vector2(PPT * 22f, PPT * 24.5f)));
		colleges.add(new College(this, new Vector2(PPT * 35f, PPT * 24.5f)));
		hittables.add(player);
		for (College college : colleges) hittables.add(college);
		inputZoomed = 0.0f;
		zoomVel = 0.0f;

		// reset game state
		gameState = GameState.READY;
		hasWon = false;
	}

	private void startGame() {
		// Start game running
		gameState = GameState.RUNNING;
	}

	private void stopGame() {
		// Stop game from running
		gameState = GameState.FINISHED;
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
		handleInput();
		updateCamera();
		for (College college : colleges) college.update();
		for (Particle particle : particles) particle.update();
		player.update();

		// Update projectiles, allowing for deletion
		for (Iterator<Projectile> pItr = projectiles.iterator(); pItr.hasNext();) {
			Projectile p = pItr.next();
			p.update();
			if (p.shouldRemove()) pItr.remove();
		}

		// Check if objective is complete
		if (objective.checkComplete(this)) stopGame();
	}

	private void handleInput() {
		// Start game on space
		if (gameState == GameState.READY && Gdx.input.isKeyPressed(Input.Keys.SPACE)) startGame();

		// Stop game on escape
		else if (gameState == GameState.RUNNING && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) stopGame();

		// Restart game on escape
		else if (gameState == GameState.FINISHED && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) resetGame();
	}

	private void updateCamera() {
		// Follow player with camera
		Vector2 position = player.getPosition();
		float diffX = position.x - camera.position.x;
		float diffY = position.y - camera.position.y;
		camera.translate(diffX, diffY);

		// Zoom camera based on input
		if (gameState == GameState.RUNNING) {
			float ZoomAcceleration = 0.5f;
			zoomVel += inputZoomed * ZoomAcceleration * Gdx.graphics.getDeltaTime();
			zoomVel *= ZoomFriction;
			camera.zoom += zoomVel;
			camera.zoom = Math.max(Math.min(camera.zoom, ZoomLim[1]), ZoomLim[0]);
		}
		inputZoomed = 0.0f;

		// Update camera
		camera.update();

		// Get world mouse pos
		Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
		worldMousePos = camera.unproject(mousePos);
	}


	@Override
	public void render() {
		update();

		// Clear then run render functions
		ScreenUtils.clear(169/255f, 208/255f, 137/255f, 1f);
		renderGame();
		renderUI();
	}

	private void renderGame() {
		// Setup batch
		gameBatch.setProjectionMatrix(camera.combined);
		gameBatch.begin();
		tiledMapRenderer.setView(camera);

		// Render terrain tilemap
		tiledMapRenderer.render();

		// Render projectiles, colleges, players, particles
		for (College college : colleges) college.render(gameBatch);
		for (Projectile projectile : projectiles) projectile.render(gameBatch);
		player.render(gameBatch);
		for (Particle particle : particles) particle.render(gameBatch);

		// Draw batch
		gameBatch.end();
	}

	private void renderUI() {
		// Setup batch
		UIBatch.begin();

		// Draw start splash
		float time = (System.currentTimeMillis() - startTime) / 100f;
		if (gameState == GameState.READY) {
			startSprite.setScale(0.95f + 0.1f * (float)Math.sin(time / 2f));
			startSprite.draw(UIBatch);

		} else if (gameState == GameState.FINISHED) {

			// Draw win splash
			if (hasWon) {
				winSprite.setScale(0.95f + 0.1f * (float)Math.sin(time / 2f));
				winSprite.draw(UIBatch);

			// Draw lost splash
			} else {
				lostSprite.setScale(0.95f + 0.1f * (float)Math.sin(time / 2f));
				lostSprite.draw(UIBatch);
			}
		}

		// Draw objective
		objective.renderUI(UIBatch);

		// Draw batch
		UIBatch.end();
	}


	@Override
	public void dispose() {
		// Run dispatch functions on player / colleges
		for (College college : colleges) college.dispose();
		player.dispose();
		tiledMap.dispose();
	}


	public void addProjectile(Projectile projectile) {
		// Add projectile
		projectiles.add(projectile);
	}


	public boolean checkCollision(Rectangle rect) {
		// Check whether rect overlaps any collision objects
		for (RectangleMapObject rectObj : collisionObjects.getByType(RectangleMapObject.class)) {
			if (Intersector.overlaps(rectObj.getRectangle(), rect)) {
				return true;
			}
		}
		return false;
	}

	public boolean checkHitPlayer(Rectangle rect) {
		// Check whether rect overlaps the player collision rect
		return Intersector.overlaps(player.getCollisionRect(), rect);
	}


	public IHittable checkHitHittable(Rectangle rect) {
		// Loop over and check collision against each hittable
		for (IHittable hittable : hittables) {
			Rectangle hittableRect = hittable.getCollisionRect();
			if (Intersector.overlaps(rect, hittableRect)) {
				return hittable;
			}
		}
		return null;
	}


	public Player getPlayer() {
		// Return player
		return player;
	}

	public boolean getRunning() {
		// Getter for isRunning
		return gameState == GameState.RUNNING;
	}

	public Vector2 getWorldMousePos() {
		// Getter for world mouse pos
		return new Vector2(worldMousePos.x, worldMousePos.y);
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
		inputZoomed = amountY;
		return true;
	}
}
