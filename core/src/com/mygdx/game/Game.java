
package com.mygdx.game;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.game.objectives.Objective;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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


public class Game extends ApplicationAdapter {

	// Declare config, variables
	public static final float PPT = 16; // Pixel Per Tile - Used to standardize scaling
	public static final long startTime = System.currentTimeMillis();
	public static BitmapFont mainFont;
	private final float ZoomFriction = 0.86f;
	private final float[] ZoomLim = { PPT * 0.005f, PPT * 0.025f };
	private final float initialZoom = PPT * 0.017f;
	private final int xpPerLevel = 100;

	private OrthographicCamera camera;
	private OrthographicCamera UICamera;
	private SpriteBatch gameBatch;
	private SpriteBatch UIBatch;
	private TiledMap tiledMap;
	private TiledMapRenderer tiledMapRenderer;
	private MapObjects collisionObjects;
	private GlyphLayout currentUITextGlyph = new GlyphLayout();

	private Sprite startSprite;
	private Sprite winSprite;
	private Sprite lostSprite;

	private float inputZoomed;
	private float zoomVel;
	private Vector3 worldMousePos;
	private enum GameState { READY, RUNNING, FINISHED };
	private GameState gameState;
	private boolean hasWon;
	public float currentGold;
	public int currentLevel;
	public int currentXP;

	private Objective objective;
	private Player player;
	private ArrayList<College> colleges;
	private ArrayList<Projectile> projectiles;
	private ArrayList<Particle> particles;
	private ArrayList<IHittable> hittables;


	@Override
	public void create() {
		// Run setup functions
		setupScene();
		setupAssets();
		resetGame();
	}

	private void setupScene() {
		// Setup scene input / output
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		UICamera = new OrthographicCamera();
		UICamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gameBatch = new SpriteBatch();
		UIBatch = new SpriteBatch();
		Gdx.input.setInputProcessor(Binding.getInstance());
	}

	private void setupAssets() {
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

		// Setup font
		mainFont = new BitmapFont(Gdx.files.internal("./fonts/Pixellari.fnt"));
		mainFont.setColor(0f, 0f, 0f, 1f);
	}


	public void resetGame() {
		// Reset main variables
		camera.zoom = initialZoom;
		inputZoomed = 0.0f;
		zoomVel = 0.0f;
		gameState = GameState.READY;
		hasWon = false;
		currentGold = 0f;
		currentLevel = 1;
		currentXP = 0;

		// Initialize objects
		objective = Objective.getRandomObjective(this);
		colleges = new ArrayList<>();
		projectiles = new ArrayList<>();
		particles = new ArrayList<>();
		hittables = new ArrayList<>();
		player = new Player(this, new Vector2(PPT * 19f, PPT * 17.5f));
		colleges.add(new College(this, new Vector2(PPT * 25f, PPT * 14.5f), true));
		colleges.add(new College(this, new Vector2(PPT * 22f, PPT * 24.5f), false));
		colleges.add(new College(this, new Vector2(PPT * 35f, PPT * 24.5f), false));
		colleges.add(new College(this, new Vector2(PPT * 38f, PPT * 24.5f), false));
		colleges.add(new College(this, new Vector2(PPT * 41f, PPT * 24.5f), false));
		hittables.add(player);
		for (College college : colleges) hittables.add(college);
	}

	public void startGame() {
		// Start game running
		gameState = GameState.RUNNING;
	}

	public void winGame() {
		// Stop game from running and win
		hasWon = true;
		gameState = GameState.FINISHED;
	}

	public void loseGame() {
		// Stop game from running and lose
		hasWon = false;
		gameState = GameState.FINISHED;
	}

	public void closeGame() {
		// Close game window
		Gdx.app.exit();
	}


	@Override
	public void resize(int width, int height) {
		// Resize camera and move to maintain centre
		Vector3 position = new Vector3(camera.position);
		camera.setToOrtho(false, width, height);
		UICamera.setToOrtho(false, width, height);
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
			if (p.shouldRemove()) {
				pItr.remove();
				p.beenRemoved();
			}
		}

		// Update particles, allowing for deletion
		for (Iterator<Particle> pItr = particles.iterator(); pItr.hasNext();) {
			Particle p = pItr.next();
			p.update();
			if (p.shouldRemove()) {
				pItr.remove();
				p.beenRemoved();
			}
		}

		// Check if objective is complete
		if (objective.checkComplete(this)) winGame();
	}

	private void handleInput() {
		// Start, stop, restart game on "startGame"
		if (gameState == GameState.READY && Binding.getInstance().isActionJustPressed("startGame")) startGame();
		else if (gameState == GameState.FINISHED && Binding.getInstance().isActionJustPressed("startGame")) resetGame();

		// Close game on "closeGame"
		if (Binding.getInstance().isActionJustPressed("closeGame")) closeGame();
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
			zoomVel += Binding.getInstance().getScrollAmount() * ZoomAcceleration * Gdx.graphics.getDeltaTime();
			zoomVel *= ZoomFriction;
			camera.zoom += zoomVel;
			camera.zoom = Math.max(Math.min(camera.zoom, ZoomLim[1]), ZoomLim[0]);
		}
		inputZoomed = 0.0f;

		// Update camera
		camera.update();
		UICamera.update();

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
		for (Particle particle : particles) particle.render(gameBatch);
		player.render(gameBatch);
		for (Projectile projectile : projectiles) projectile.render(gameBatch);

		// Draw batch
		gameBatch.end();
	}

	private void renderUI() {
		// Setup batch
		UIBatch.setProjectionMatrix(UICamera.combined);
		UIBatch.begin();

		// Draw start splash
		float time = (System.currentTimeMillis() - startTime) / 100f;
		if (gameState == GameState.READY) {
			startSprite.setScale(0.95f + 0.1f * (float)Math.sin(time / 2f));
			startSprite.setPosition(
					Gdx.graphics.getWidth() * 0.5f - startSprite.getWidth() * 0.5f,
					Gdx.graphics.getHeight() * 0.5f - startSprite.getHeight() * 0.5f);
			startSprite.draw(UIBatch);

		} else if (gameState == GameState.FINISHED) {

			// Draw win splash
			if (hasWon) {
				winSprite.setScale(0.95f + 0.1f * (float)Math.sin(time / 2f));
				winSprite.setPosition(
					Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
					Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
				winSprite.draw(UIBatch);

			// Draw lost splash
			} else {
				lostSprite.setScale(0.95f + 0.1f * (float)Math.sin(time / 2f));
				lostSprite.setPosition(
					Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
					Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
				lostSprite.draw(UIBatch);
			}
		}

		// Draw objective UI
		objective.renderUI(UIBatch);


		// Draw gold count
		mainFont.getData().setScale(0.5f);
		float currentHeight = 0f;
		float spacing = 10f;

		String goldText = "Gold: " + currentGold;
		currentUITextGlyph.setText(mainFont, goldText);
		currentHeight += currentUITextGlyph.height + spacing;
		mainFont.draw(UIBatch, goldText, spacing, currentHeight);

		String xpText = "XP: " + currentXP + " / " + xpPerLevel;
		currentUITextGlyph.setText(mainFont, xpText);
		currentHeight += currentUITextGlyph.height + spacing;
		mainFont.draw(UIBatch, xpText, spacing, currentHeight);

		String levelText = "Level " + currentLevel;
		currentUITextGlyph.setText(mainFont, levelText);
		currentHeight += currentUITextGlyph.height + spacing;
		mainFont.draw(UIBatch, levelText, spacing, currentHeight);
		mainFont.getData().setScale(1f);

		// Draw batch
		UIBatch.end();
	}


	@Override
	public void dispose() {
		// Run dispatch functions on player / colleges
		tiledMap.dispose();
		startSprite.getTexture().dispose();
		winSprite.getTexture().dispose();
		lostSprite.getTexture().dispose();
		player.dispose();
		for (College college : colleges) college.dispose();
		Projectile.staticDispose();
		Particle.staticDispose();
		mainFont.dispose();
	}


	public void addProjectile(Projectile projectile) {
		// Add projectile
		projectiles.add(projectile);
	}

	public void addParticle(Particle particle) {
		// Add particle
		particles.add(particle);
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
}
