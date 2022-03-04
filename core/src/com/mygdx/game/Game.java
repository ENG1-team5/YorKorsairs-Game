package com.mygdx.game;

import java.util.ArrayList;
import java.util.Iterator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.game.objectives.Objective;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ArrayMap;
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
	public static final float PPT = 128; // Pixel Per Tile - Used to standardize scaling
	public static final long startTime = System.currentTimeMillis();
	public static BitmapFont mainFont;
	private final float ZoomFriction = 0.86f;
	private final float[] ZoomLim = { PPT * 0.005f, PPT * 0.022f };
	private final float initialZoom = PPT * 0.0135f;
	private final float zoomSpeed = PPT * 0.0075f;
	private final float splashWidth = 500f;
	private final int xpPerLevel = 50;
	private final float xpGain = 0.4f;
	private final float levelUpTimerMax = 1.5f;

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

	private float zoomVel;
	private Vector3 worldMousePos;

	private enum GameState {
		READY, RUNNING, FINISHED
	};

	private GameState gameState;
	private boolean hasWon;
	public float currentGold;
	public int currentLevel;
	public float currentXP;
	private int tutorialState = 0;
	private float levelUpTimer;

	private Objective objective;
	private Player player;
	private ArrayList<College> colleges;
	private ArrayList<Projectile> projectiles;
	private ArrayList<Particle> particles;
	private ArrayList<IHittable> hittables;
	private ArrayList<Enemy> enemies;
	private ArrayList<Pickup> pickups;


	/**
	 * Creates an instance of the game class, which implements 3 setup functions
	 */
	@Override
	public void create() {
		setupScene();
		setupAssets();
		resetGame();
	}

	/**
	 * Initialises the camera object, and the SpriteBatch, which is the graphics target for the sprite renders
	 */
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

	/**
	 * Creates the map, as well as initialising the starting sprites onto it by defining their perimeters
	 * Loads in the splash sprites for when the game is either won, started or lost
	 * Creates text font for UI
	 */
	private void setupAssets() {
		// Setup tiled map
		tiledMap = new TmxMapLoader().load("./tiles/map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		MapLayer collisionLayer = tiledMap.getLayers().get(1);
		collisionObjects = collisionLayer.getObjects();

		// Load splash textures
		startSprite = new Sprite(new Texture(Gdx.files.internal("./splashes/startSplash.png")));
		startSprite.setSize(splashWidth, splashWidth * (float) startSprite.getHeight() / startSprite.getWidth());
		startSprite.setOrigin(startSprite.getWidth() * 0.5f, startSprite.getHeight() * 0.5f);
		startSprite.setPosition(
				Gdx.graphics.getWidth() * 0.5f - startSprite.getWidth() * 0.5f,
				Gdx.graphics.getHeight() * 0.5f - startSprite.getHeight() * 0.5f);
		winSprite = new Sprite(new Texture(Gdx.files.internal("./splashes/winSplash.png")));
		winSprite.setSize(splashWidth, splashWidth * (float) winSprite.getHeight() / winSprite.getWidth());
		winSprite.setOrigin(startSprite.getWidth() * 0.5f, winSprite.getHeight() * 0.5f);
		winSprite.setPosition(
				Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
				Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
		lostSprite = new Sprite(new Texture(Gdx.files.internal("./splashes/lostSplash.png")));
		lostSprite.setSize(splashWidth, splashWidth * (float) lostSprite.getHeight() / lostSprite.getWidth());
		lostSprite.setOrigin(lostSprite.getWidth() * 0.5f, lostSprite.getHeight() * 0.5f);
		lostSprite.setPosition(
				Gdx.graphics.getWidth() * 0.5f - lostSprite.getWidth() * 0.5f,
				Gdx.graphics.getHeight() * 0.5f - lostSprite.getHeight() * 0.5f);

		// Setup font
		mainFont = new BitmapFont(Gdx.files.internal("./fonts/Pixellari.fnt"));
		mainFont.setColor(0f, 0f, 0f, 1f);
	}


	/**
	 * Reset game state back to start
	 */
	public void resetGame() {
		// Reset main variables
		camera.zoom = initialZoom;
		zoomVel = 0.0f;
		gameState = GameState.READY;
		hasWon = false;
		currentGold = 0f;
		currentLevel = 1;
		currentXP = 0;
		levelUpTimer = 0f;

		// Initialize objects
		colleges = new ArrayList<>();
		projectiles = new ArrayList<>();
		particles = new ArrayList<>();
		hittables = new ArrayList<>();
		enemies = new ArrayList<>();
		pickups = new ArrayList<>();

		player = new Player(this, new Vector2(PPT * 19f, PPT * 17.5f));
		colleges.add(new College("Goodricke", this, new Vector2(PPT * 25f, PPT * 14.5f), true));
		colleges.add(new College("Constantine", this, new Vector2(PPT * 22f, PPT * 24.5f), false));
		colleges.add(new College("AnneLister", this, new Vector2(PPT * 57f, PPT * 10.5f), false));
		colleges.add(new College("Langwith", this, new Vector2(PPT * 48f, PPT * 24.5f), false));
		colleges.add(new College("Vanbrugh", this, new Vector2(PPT * 62f, PPT * 26.5f), false));
		colleges.add(new College("EvilGoodricke", this, new Vector2(PPT * 34f, PPT * 39f), false));
		enemies.add(new Enemy(this, new Vector2(PPT * 33f, PPT * 36f)));
		enemies.add(new Enemy(this, new Vector2(PPT * 26f, PPT * 35f)));
		enemies.add(new Enemy(this, new Vector2(PPT * 40f, PPT * 25f)));
		// colleges.add(new College("Derwent", this, new Vector2(PPT * 34f, PPT *
		// 21.5f), false));
		// colleges.add(new College("James", this, new Vector2(PPT * 26f, PPT * 29.5f),
		// false));
		// colleges.add(new College("Alcuin", this, new Vector2(PPT * 39f, PPT * 13f),
		// false));
		hittables.add(player);
		for (College college : colleges)
			hittables.add(college);
		for (Enemy enemy : enemies){
			hittables.add(enemy);
		}

		pickups.add(new Pickup(this, new Vector2(PPT * 28f, PPT * 17.5f), new Buff("speed", 10f*PPT, 100f)));
		pickups.add(new Pickup(this, new Vector2(PPT * 28f, PPT * 19f), new Buff("damage", 1000f, 100f)));
		pickups.add(new Pickup(this, new Vector2(PPT * 25f, PPT * 17.5f), new Buff("projectileSpeed", 1000f, 100f)));
		pickups.add(new Pickup(this, new Vector2(PPT * 25f, PPT * 19f), new Buff("maxHealth", 1000f, 100f)));
		pickups.add(new Pickup(this, new Vector2(PPT * 26.5f, PPT * 17.5f), new Buff("fireRate", 1000f, 100f)));
		pickups.add(new Pickup(this, new Vector2(PPT * 26.5f, PPT * 19f), new Buff("regen", 1000f, 100f)));

		objective = Objective.getRandomObjective(this);
	}

	/**
	 * Starts game running
	 */
	public void startGame() {
		gameState = GameState.RUNNING;
	}

	/**
	 * Stops game from running and win
	 */
	public void winGame() {
		hasWon = true;
		gameState = GameState.FINISHED;
	}

	/**
	 * Stops game from running and set to lost
	 */
	public void loseGame() {
		hasWon = false;
		gameState = GameState.FINISHED;
	}

	/**
	 * Close game window
	 */
	public void closeGame() {
		Gdx.app.exit();
	}


	/**
	 * Resizes camera and moves to maintain centre based on player movement
	 * 
	 * @param width new window width
	 * @param height new window height
	 */
	@Override
	public void resize(int width, int height) {
		Vector3 position = new Vector3(camera.position);
		camera.setToOrtho(false, width, height);
		UICamera.setToOrtho(false, width, height);
		camera.translate(-camera.viewportWidth * 0.5f, -camera.viewportHeight * 0.5f);
		camera.translate(position.x, position.y);
	}


	/**
	 * Updates the parameters of the game class
	 */
	private void update() {
		// Run update functions
		handleInput();
		updateCamera();
		updateLogic();
		for (College college : colleges)
			college.update();
		for (Particle particle : particles)
			particle.update();
		player.update();
		for (Iterator<Enemy> eItr = enemies.iterator(); eItr.hasNext();){
			Enemy e = eItr.next();
			e.update();
			if (e.shouldRemove()){
				eItr.remove();
			}
		}

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

		// Update pickups, allowing for deletion
		for (Iterator<Pickup> pItr = pickups.iterator(); pItr.hasNext();) {
			Pickup p = pItr.next();
			p.update();
			if (p.shouldRemove()) {
				pItr.remove();
				p.beenRemoved();
			}
		}

		// Check if objective is complete
		if (objective.checkComplete(this))
			winGame();
	}

	/**
	 * Controls the game states based on the inputs
	 */
	private void handleInput() {
		// Start, stop, restart game on "startGame"
		if (gameState == GameState.READY && Binding.getInstance().isActionJustPressed("startGame"))
			startGame();
		if (gameState == GameState.RUNNING && Binding.getInstance().isActionJustPressed("resetGame"))
			resetGame();
		else if (gameState == GameState.FINISHED && Binding.getInstance().isActionJustPressed("startGame")
				|| gameState == GameState.FINISHED && Binding.getInstance().isActionJustPressed("resetGame"))
			resetGame();

		// Close game on "closeGame"
		if (Binding.getInstance().isActionJustPressed("closeGame"))
			closeGame();
	}

	/**
	 * Changes camera position based on player's location
	 */
	private void updateCamera() {
		// Follow player with camera
		Vector2 position = player.getPosition();
		float diffX = (float) Math.round(position.x * 100f) / 100f - camera.position.x;
		float diffY = (float) Math.round(position.y * 100f) / 100f - camera.position.y;
		camera.translate(diffX, diffY);

		// Zoom camera based on input
		if (gameState == GameState.RUNNING) {
			zoomVel += Binding.getInstance().getScrollAmount() * zoomSpeed * Gdx.graphics.getDeltaTime();
			zoomVel *= ZoomFriction;
			camera.zoom += zoomVel;
			camera.zoom = Math.max(Math.min(camera.zoom, ZoomLim[1]), ZoomLim[0]);
		}

		// Update camera
		camera.update();
		UICamera.update();

		// Get world mouse pos
		Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
		worldMousePos = camera.unproject(mousePos);
	}

	/**
	 * Handles the in game statistics related to XP
	 */
	private void updateLogic() {
		// Increase XP and handle levelling up
		if (gameState == GameState.RUNNING) {
			addResources(0, Gdx.graphics.getDeltaTime() * xpGain);
			if (currentXP > xpPerLevel) {
				currentXP = currentXP % xpPerLevel;
				currentLevel++;
				levelUpTimer = levelUpTimerMax;
			}
			levelUpTimer = (float) Math.max(levelUpTimer - Gdx.graphics.getDeltaTime(), 0f);

			// Tutorial checks
			if (tutorialState == 0 && player.getIsMoving())
				tutorialState = 1;
			if (tutorialState == 1 && player.getHasShot())
				tutorialState = 2;
		}
	}


	/**
	 * renders the game UI
	 */
	@Override
	public void render() {
		update();

		// Clear then run render functions
		ScreenUtils.clear(122 / 255f, 183 / 255f, 84 / 255f, 1f);
		renderGame();
		renderUI();
	}

	/**
	 * renders main objects and displays tutorial
	 */
	private void renderGame() {
		// Setup batch
		gameBatch.setProjectionMatrix(camera.combined);
		gameBatch.begin();
		tiledMapRenderer.setView(camera);

		// Render terrain tilemap
		tiledMapRenderer.render();

		// Render projectiles, colleges, players, particles
		for (College college : colleges)
			college.render(gameBatch);
		for (Particle particle : particles)
			particle.render(gameBatch);
		for (Pickup pickup : pickups)
			pickup.render(gameBatch);
		for (Enemy enemy : enemies)
			enemy.render(gameBatch);
		player.render(gameBatch);
		for (Projectile projectile : projectiles)
			projectile.render(gameBatch);

		// Render tutorial
		if (gameState == GameState.RUNNING) {

			// Movement tutorial
			if (tutorialState == 0) {
				mainFont.getData().setScale(0.55f);
				Vector2 playerPos = player.getPosition();
				currentUITextGlyph.setText(mainFont, "Use 'WASD' to move.");
				float px = playerPos.x - currentUITextGlyph.width * 0.5f;
				float py = playerPos.y - player.shipWidth * 0.35f;
				mainFont.draw(gameBatch, "Use 'WASD' to move.", px, py);

			// Shoot tutorial
			} else if (tutorialState == 1) {
				mainFont.getData().setScale(0.55f);
				College closestCollege = colleges.get(1);
				Vector2 collegePos = closestCollege.getPosition();
				currentUITextGlyph.setText(mainFont, "Use LMB / RMB to shoot.");
				float px = collegePos.x - currentUITextGlyph.width * 0.5f;
				float py = collegePos.y - closestCollege.collegeWidth * 0.75f;
				mainFont.draw(gameBatch, "Use LMB / RMB to shoot.", px, py);
			}
		}

		// Draw batch
		gameBatch.end();
	}

	/**
	 * renders UI into UIBatch on top of game
	 */
	private void renderUI() {
		// Setup batch
		UIBatch.setProjectionMatrix(UICamera.combined);
		UIBatch.begin();

		// Draw start splash
		float time = (System.currentTimeMillis() - startTime) / 100f;
		if (gameState == GameState.READY) {
			startSprite.setScale(0.95f + 0.1f * (float) Math.sin(time / 2f));
			startSprite.setPosition(
					Gdx.graphics.getWidth() * 0.5f - startSprite.getWidth() * 0.5f,
					Gdx.graphics.getHeight() * 0.5f - startSprite.getHeight() * 0.5f);
			startSprite.draw(UIBatch);

		} else if (gameState == GameState.FINISHED) {

			// Draw win splash
			if (hasWon) {
				winSprite.setScale(0.95f + 0.1f * (float) Math.sin(time / 2f));
				winSprite.setPosition(
						Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
						Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
				winSprite.draw(UIBatch);

				// Draw lost splash
			} else {
				lostSprite.setScale(0.95f + 0.1f * (float) Math.sin(time / 2f));
				lostSprite.setPosition(
						Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
						Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
				lostSprite.draw(UIBatch);
			}
		}

		// Draw objective UI
		if (gameState == GameState.RUNNING)
			objective.renderUI(UIBatch);

		mainFont.getData().setScale(0.4f);

		// Draw Info UI
		float currentHeight = 0f;
		float spacing = 20f;
		String goldText = "Gold: " + currentGold;
		currentUITextGlyph.setText(mainFont, goldText);
		currentHeight += currentUITextGlyph.height + spacing;
		mainFont.draw(UIBatch, goldText, spacing, currentHeight);
		String xpText = "XP: " + ((float) Math.round(currentXP * 100f) / 100f) + " / " + xpPerLevel;
		currentUITextGlyph.setText(mainFont, xpText);
		currentHeight += currentUITextGlyph.height + spacing;
		mainFont.draw(UIBatch, xpText, spacing, currentHeight);
		String levelText = "Level " + currentLevel;
		currentUITextGlyph.setText(mainFont, levelText);
		currentHeight += currentUITextGlyph.height + spacing;
		mainFont.draw(UIBatch, levelText, spacing, currentHeight);

		// Draw help UI
		currentHeight = Gdx.graphics.getHeight() - spacing;
		String[] helpText = new String[] { "'Tab': reset", "WASD: movement", "LMB / RMB: Shoot" };
		for (String s : helpText) {
			currentUITextGlyph.setText(mainFont, s);
			mainFont.draw(UIBatch, s, spacing, currentHeight);
			currentHeight -= currentUITextGlyph.height + spacing;
		}

		// Draw level up popup
		if (levelUpTimer > 0f) {
			mainFont.getData().setScale(1f);
			currentUITextGlyph.setText(mainFont, "Level " + currentLevel + "!");
			mainFont.draw(UIBatch, "Level " + currentLevel + "!", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		mainFont.getData().setScale(1f);

		// Draw batch
		UIBatch.end();
	}


	/**
	 * Deletes old instances of sprites in order to be more efficient
	 */
	@Override
	public void dispose() {
		mainFont.dispose();
		tiledMap.dispose();
		startSprite.getTexture().dispose();
		winSprite.getTexture().dispose();
		lostSprite.getTexture().dispose();

		for (College college : colleges)
			college.dispose();

		College.staticDispose();
		Player.staticDispose();
		Projectile.staticDispose();
		Particle.staticDispose();
		Enemy.staticDispose();
	}


	/**
	 * Add instance of projectile
	 * @param projectile projectile to be added
	 */
	public void addProjectile(Projectile projectile) {
		projectiles.add(projectile);
	}

	/**
	 * Add instance of particle
	 * @param particle particle to be added
	 */
	public void addParticle(Particle particle) {
		particles.add(particle);
	}

	/**
	 * Adds specified resources
	 * @param gold amount to be added
	 * @param xp amount to be added
	 */
	public void addResources(float gold, float xp) {
		currentGold += gold;
		currentXP += xp;
	}


	/**
	 * Checks whether a rectangle object overlap any collision objects
	 * @param rect rect to check against
	 * @return boolean
	 */
	public boolean checkCollision(Rectangle rect) {
		for (RectangleMapObject rectObj : collisionObjects.getByType(RectangleMapObject.class)) {
			if (Intersector.overlaps(rectObj.getRectangle(), rect)) {
				return true;

			}
		}
		for (Enemy enemy : enemies) {
			Rectangle enemyRect = enemy.getCollisionRect();
			if (Intersector.overlaps(enemyRect,rect) && !rect.equals(enemyRect)){
				return true;
			}
		}
		Rectangle playerRect = player.getCollisionRect();
		if (Intersector.overlaps(playerRect,rect) && !rect.equals(playerRect)){
			return true;
		}
		return false;
		
	}
	/**
	 * Checks whether rect overlaps the player collision rect
	 * @param rect Rect to check against
	 * @return boolean
	 */
	public boolean checkHitPlayer(Rectangle rect) {
		return Intersector.overlaps(player.getCollisionRect(), rect);
	}

	/**
	 * Checks collision against each hittable
	 * @param rect Rect to check against
	 * @return IHittable
	 */
	public IHittable checkHitHittable(Rectangle rect) {
		for (IHittable hittable : hittables) {
			Rectangle hittableRect = hittable.getCollisionRect();
			if (Intersector.overlaps(rect, hittableRect)) {
				return hittable;
			}
		}
		return null;
	}


	/**
	 * @return Player
	 */
	public Player getPlayer() {
		// Return player
		return player;
	}

	/**
	 * @return boolean
	 */
	public boolean getRunning() {
		// Getter for isRunning
		return gameState == GameState.RUNNING;
	}

	/**
	 * Getter for world mouse pos
	 * @return Vector2
	 */
	public Vector2 getWorldMousePos() {
		return new Vector2(worldMousePos.x, worldMousePos.y);
	}

	/**
	 * Return list of colleges
	 * @return ArrayList
	 */
	public ArrayList<College> getColleges() {
		return colleges;
	}
}
