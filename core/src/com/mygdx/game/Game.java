
package com.mygdx.game;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.text.DefaultStyledDocument.ElementSpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.File;

import org.json.simple.parser.JSONParser;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.game.objectives.DestroyCollegeObjective;
import com.mygdx.game.objectives.GetLevel5Objective;
import com.mygdx.game.objectives.Objective;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;

/** Main Game class handling most important methods and providing an interface for classes to access others */
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
	public final int xpPerLevel = 50;
	private final float xpGain = 0.4f;
	private final float levelUpTimerMax = 1.5f;
	private final String[] weatherChoices = {"foggy","rainy"};

	private OrthographicCamera camera;
	private OrthographicCamera UICamera;
	private SpriteBatch gameBatch;
	SpriteBatch UIBatch;
	public TiledMap tiledMap;
	private TiledMapRenderer tiledMapRenderer;
	public MapObjects collisionObjects;
	private GlyphLayout currentUITextGlyph = new GlyphLayout();

	private String[] difficultyStrings = {"   Easy ->","<- Normal ->","<- Hard ->","<- Impossible   "};
	private float[] difficultyModifiers = {0.5f,1.0f,1.25f,1.5f};
	private String[] difficultyDescriptors = {"Weakens enemys, Strenghtens player \n      Bullets are easier to dodge ","Intended Difficulty","Strengthens enemys, Weakens player \n      Bullets are harder to dodge ", "Unfair enemies and colleges \n    No health regeneration"};
	private int difficultySelection = 1;

	private Sprite startSprite;
	private Sprite winSprite;
	private Sprite lostSprite;

	private float zoomVel;
	private Vector3 worldMousePos;

	public enum GameState {
		READY, RUNNING, FINISHED
	};

	public Game() {
	}

	public GameState gameState;
	private boolean hasWon;
	private boolean saveExists;
	public float currentGold;
	public int currentLevel;
	public float currentXP;
	private int tutorialState = 0;
	private float levelUpTimer;

	public Objective objective;
	public Player player;
	public ArrayList<College> colleges;
	public ArrayList<Projectile> projectiles;
	public ArrayList<Particle> particles;
	public ArrayList<Enemy> enemies;
	public ArrayList<Pickup> pickups;
	public ArrayList<IHittable> hittables;
	public ArrayList<Upgrade> upgrades;
	public ArrayList<Obstacles> obstacles;
	public ArrayList<Weather> weather;

	public boolean testing = false;


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

	public void setupMap(){
		// Setup tiled map
		tiledMap = new TmxMapLoader().load("tiles/map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		MapLayer collisionLayer = tiledMap.getLayers().get(1);
		collisionObjects = collisionLayer.getObjects();
	}

	/**
	 * Creates the map, as well as initialising the starting sprites onto it by defining their perimeters
	 * Loads in the splash sprites for when the game is either won, started or lost
	 * Creates text font for UI
	 */
	private void setupAssets() {
		setupMap();
		
		// Load splash textures
		startSprite = new Sprite(new Texture(Gdx.files.internal("splashes/startSplash.png")));
		startSprite.setSize(splashWidth, splashWidth * (float) startSprite.getHeight() / startSprite.getWidth());
		startSprite.setOrigin(startSprite.getWidth() * 0.5f, startSprite.getHeight() * 0.5f);
		startSprite.setPosition(
				Gdx.graphics.getWidth() * 0.5f - startSprite.getWidth() * 0.5f,
				Gdx.graphics.getHeight() * 0.5f - startSprite.getHeight() * 0.5f);
		winSprite = new Sprite(new Texture(Gdx.files.internal("splashes/winSplash.png")));
		winSprite.setSize(splashWidth, splashWidth * (float) winSprite.getHeight() / winSprite.getWidth());
		winSprite.setOrigin(startSprite.getWidth() * 0.5f, winSprite.getHeight() * 0.5f);
		winSprite.setPosition(
				Gdx.graphics.getWidth() * 0.5f - winSprite.getWidth() * 0.5f,
				Gdx.graphics.getHeight() * 0.5f - winSprite.getHeight() * 0.5f);
		lostSprite = new Sprite(new Texture(Gdx.files.internal("splashes/lostSplash.png")));
		lostSprite.setSize(splashWidth, splashWidth * (float) lostSprite.getHeight() / lostSprite.getWidth());
		lostSprite.setOrigin(lostSprite.getWidth() * 0.5f, lostSprite.getHeight() * 0.5f);
		lostSprite.setPosition(
				Gdx.graphics.getWidth() * 0.5f - lostSprite.getWidth() * 0.5f,
				Gdx.graphics.getHeight() * 0.5f - lostSprite.getHeight() * 0.5f);

		// Setup font
		mainFont = new BitmapFont(Gdx.files.internal("fonts/Pixellari.fnt"));
		mainFont.setColor(0f, 0f, 0f, 1f);


	}

	/**
	 * Applies a percentage change to attributes in the Enemy, Player and College classes to increase/difficulty based
	 * on the current difficulty choice "difficultySelection"
	 */
	private void setDifficulty(){
		//Takes raw difficulty selection text as displayed on screen
		String difficulty = difficultyStrings[difficultySelection];
		//Removes charecters <-> and spaces from the string
		difficulty = difficulty.replaceAll("[-<> ]",""); 
		//Gets appropriate percentage change to apply to attributes based on difficulty string
		float difficultyMod = difficultyModifiers[difficultySelection];

		// In the case where difficulty is Normal, nothing changes
		// Otherwise, apply a modification based on difficulty to health 
		if(!difficulty.equals("Normal")){
			
			// Alter all enemies health and shooting speed based on difficultyMod
			for (Enemy e : enemies){
				e.setHealth(e.getMaxHealth()*difficultyMod);
				e.setShotTimerMax(e.getShotTimerMax()*1/difficultyMod);
			}
			for (College c : colleges){
				c.setHealth(c.getMaxHealth()*difficultyMod);
				c.setShotTimerMax(c.getShotTimerMax()*1/difficultyMod);
			}

			//Changing health to 1/difficultyMod increases or decreases player health inverse to the changes made to enemy health
			player.setHealth(player.getMaxHealth()*1/difficultyMod);
		}

		if(difficulty.equals("Impossible")){
			// For impossible mode, health regen is disabled
			player.setHealthRegen(0f);
		}
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
		enemies = new ArrayList<>();
		pickups = new ArrayList<>();
		hittables = new ArrayList<>();
		upgrades = new ArrayList<>();
		obstacles = new ArrayList<>();
		weather = new ArrayList<>();

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

		pickups.add(new Pickup(this, getRandomOverWater(), new Buff("speed", 2f*PPT, 60f)));
		pickups.add(new Pickup(this, getRandomOverWater(), new Buff("damage", 10f, 60f)));
		pickups.add(new Pickup(this, getRandomOverWater(), new Buff("projectileSpeed", 10f, 60f)));
		pickups.add(new Pickup(this, getRandomOverWater(), new Buff("fireRate", 1f, 60f)));
		pickups.add(new Pickup(this, getRandomOverWater(), new Buff("maxHealth", 100f, 60f)));
		pickups.add(new Pickup(this, getRandomOverWater(), new Buff("regen", 2f, 60f)));

		upgrades.add(new Upgrade(this, new Vector2(PPT * 17f, PPT * 14f), new Buff("speed", 1f*PPT), 25));
		upgrades.add(new Upgrade(this, new Vector2(PPT * 13f, PPT * 14f), new Buff("damage", 10f), 50));
		upgrades.add(new Upgrade(this, new Vector2(PPT * 09f, PPT * 14f), new Buff("projectileSpeed", 5f*PPT), 25));
		upgrades.add(new Upgrade(this, new Vector2(PPT * 07f, PPT * 16f), new Buff("regen", 1f), 25));
		upgrades.add(new Upgrade(this, new Vector2(PPT * 13f, PPT * 18f), new Buff("fireRate", 0.5f), 50));
		upgrades.add(new Upgrade(this, new Vector2(PPT * 09f, PPT * 18f), new Buff("maxHealth", 25), 50));

		//Long list of manually configured obstacles
		obstacles.add(new Obstacles(this, new Vector2(PPT * 31f, PPT * 17.4f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 31f, PPT * 16.5f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 33f, PPT * 15.4f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 33f, PPT * 14.4f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 40f, PPT * 18f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 45f, PPT * 20f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 50f, PPT * 16f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 54.5f, PPT * 19f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 54.6f, PPT * 17.4f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 55f, PPT * 13.5f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 55.5f, PPT * 15f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 51f, PPT * 20f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 26.1f, PPT * 17.4f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 26.8f, PPT * 18.4f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 27.5f, PPT * 19.4f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 32f, PPT * 19.5f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 32f, PPT * 20.5f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 55f, PPT * 30f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 54.5f, PPT * 31f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 54.5f, PPT * 29f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 56f, PPT * 33f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 54f, PPT * 32f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 58f, PPT * 32.5f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 59f, PPT * 31f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 53f, PPT * 34f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 56f, PPT * 28f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 55f, PPT * 27f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 52f, PPT * 33f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 55.5f, PPT * 32f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 55f, PPT * 33f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 25f, PPT * 33f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 26f, PPT * 36f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 27f, PPT * 34f), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 26.1f, PPT * 35), "Rock"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 34f, PPT * 34.5f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 33f, PPT * 34f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 29f, PPT * 32), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 30, PPT * 33f), "Iceberg"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 40f, PPT * 29f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 39f, PPT * 28f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 40f, PPT * 28f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 39f, PPT * 29f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 44f, PPT * 33f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 43f, PPT * 32f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 44f, PPT * 32f), "Seamine"));
		obstacles.add(new Obstacles(this, new Vector2(PPT * 43f, PPT * 33f), "Seamine"));
		//weather.add(new Weather(this, "foggy", 40f)); // Lasts 10 seconds
		objective = Objective.getRandomObjective(this);

	}

	/**
	 * Saves current game state to JSON, serialising each object in an array of that type 
	 * that can be unwrapped logically upon loading
	 */
	public void saveGame(){
		
		// Non object game variables stored here
		HashMap<String, Object> gameAttributes = new HashMap<String, Object>();
		gameAttributes.put("currentXP", currentXP);
		gameAttributes.put("currentGold",currentGold);
		gameAttributes.put("currentLevel",currentLevel);
		gameAttributes.put("difficultySelection",difficultySelection);
		if (objective instanceof GetLevel5Objective){
			gameAttributes.put("objective","L5");
		}
		else {
			gameAttributes.put("objective","DC");
		}

		//Object holding arrays for each type of object that needs rebuilding upon restoring a save
		HashMap<String, Object> gameObjects = new HashMap<String,Object>();
		HashMap<String, Object> playerObject = new HashMap<String,Object>();

		playerObject.put("posX",player.pos.x);
		playerObject.put("posY",player.pos.y);
		playerObject.put("health",player.health);

		JSONArray buffArr = new JSONArray();
		for (Buff buff : player.getBuffs()){
			
			HashMap<String, Object> buffObject = new HashMap<String,Object>();
			// Only takes the first stat, alterations may need to be made for full serialisation of all stats
			// At present, >1 stats are not used on a single buff object
			if(!buff.stats.isEmpty()){
				buffObject.put("stat", buff.stats.getKeyAt(0));
				buffObject.put("amount",buff.stats.get(buff.stats.getKeyAt(0)));
				buffObject.put("duration",buff.time);
				buffArr.add(new JSONObject(buffObject));
			}
		}
		playerObject.put("buffs",buffArr);
		gameObjects.put("player",playerObject);
		
		JSONArray enemyArray = new JSONArray();
		for (Enemy e : enemies){
			HashMap<String, Object> enemyObject = new HashMap<String,Object>();
			enemyObject.put("posX",e.pos.x);
			enemyObject.put("posY",e.pos.y);
			enemyObject.put("health",e.health);
			enemyArray.add(new JSONObject(enemyObject));
		}
		gameObjects.put("enemies",enemyArray);

		JSONArray collegeArray = new JSONArray();
		for (College c : colleges){
			HashMap<String, Object> collegeObject = new HashMap<String,Object>();
			collegeObject.put("name",c.name);
			collegeObject.put("posX",c.pos.x);
			collegeObject.put("posY",c.pos.y);
			collegeObject.put("health",c.health);
			collegeObject.put("isFriendly",c.isFriendly);
			collegeArray.add(new JSONObject(collegeObject));
		}
		gameObjects.put("colleges",collegeArray);

		JSONArray pickupArray = new JSONArray();
		for (Pickup p : pickups){
			HashMap<String, Object> pickupObject = new HashMap<String,Object>();
			pickupObject.put("posX",p.pos.x);
			pickupObject.put("posY",p.pos.y);
			pickupObject.put("buffStat", p.buff.stats.getKeyAt(0));
			pickupObject.put("buffAmount",p.buff.stats.get(p.buff.stats.getKeyAt(0)));
			pickupObject.put("buffDuration",p.buff.time);
			pickupArray.add(new JSONObject(pickupObject));
		}
		gameObjects.put("pickups",pickupArray);

		JSONArray upgradeArray = new JSONArray();
		for (Upgrade u : upgrades){
			HashMap<String,Object> upgradeObject = new HashMap<String,Object>();
			upgradeObject.put("posX",u.pos.x);
			upgradeObject.put("posY",u.pos.y);
			upgradeObject.put("buffStat",u.buff.stats.getKeyAt(0));
			upgradeObject.put("buffAmount",u.buff.stats.get(u.buff.stats.getKeyAt(0)));
			upgradeObject.put("cost",u.cost);
			upgradeArray.add(new JSONObject(upgradeObject));
		}
		gameObjects.put("upgrades",upgradeArray);

		//Combine all subObjects, i.e. gameAttributes and gameObjects into one object for writing out
		HashMap<String, Object> mainMap = new HashMap<String, Object>();
		mainMap.put("gameObjects",new JSONObject(gameObjects));
		mainMap.put("gameAttributes",new JSONObject(gameAttributes));

		JSONObject mainObject = new JSONObject(mainMap);

		try (FileWriter file = new FileWriter("save.txt")){
			file.write(mainObject.toString());
			file.flush();
			file.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Loads game state from JSON file
	 * Functionally a variation of resetGame(), with loaded in values from JSON
	 */
	public void loadGame(){
		
		try (FileReader reader = new FileReader("save.txt")){
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(reader);
			JSONObject JSONfile = (JSONObject) obj;
			JSONObject gameAttributes = (JSONObject) JSONfile.get("gameAttributes");
			JSONObject gameObjects = (JSONObject) JSONfile.get("gameObjects");

			gameState = GameState.RUNNING; //As we are loading in, we immediately start the game
			currentXP =  ((Double)gameAttributes.get("currentXP")).floatValue();
			currentGold = ((Double)gameAttributes.get("currentGold")).floatValue();
			currentLevel = ((Long)gameAttributes.get("currentLevel")).intValue();
			difficultySelection = ((Long)gameAttributes.get("difficultySelection")).intValue();
			if (((String)gameAttributes.get("objective")).equals("L5")){
				objective = new GetLevel5Objective(this);
			}
			else{	
				objective = new DestroyCollegeObjective(this);
			}

			// Initialize objects
			colleges = new ArrayList<>();
			hittables = new ArrayList<>();
			enemies = new ArrayList<>();
			pickups = new ArrayList<>();
			upgrades = new ArrayList<>();

			//Rebuilding player state
			JSONObject playerObj = (JSONObject) gameObjects.get("player");
			player = new Player (this, new Vector2(((Double)playerObj.get("posX")).floatValue(),((Double)playerObj.get("posY")).floatValue()));
			player.health = ((Double)playerObj.get("health")).floatValue();
			hittables.add(player);

			//Rebuilding buff state
			JSONArray buffList = (JSONArray) playerObj.get("buffs");
			for (Object b : buffList){
				JSONObject bObj = (JSONObject) b;
				Buff buff;
				if(((Double)bObj.get("duration")).floatValue() == 0.0){
					buff = new Buff((String)bObj.get("stat"), ((Double)bObj.get("amount")).floatValue());
				}
				else{
					buff = new Buff((String)bObj.get("stat"), ((Double)bObj.get("amount")).floatValue() ,  ((Double)bObj.get("duration")).floatValue());
				}
				player.addBuff(buff);
			}

			//Rebuilding enemys
			JSONArray enemiesList = (JSONArray) gameObjects.get("enemies");
			for (Object e : enemiesList){
				JSONObject eObj = (JSONObject) e;
				Enemy enemy = new Enemy (this, new Vector2(((Double)eObj.get("posX")).floatValue(),((Double)eObj.get("posY")).floatValue()));
				enemy.health = ((Double)eObj.get("health")).floatValue();
				enemies.add(enemy);
				hittables.add(enemy);
			}

			JSONArray collegesList = (JSONArray) gameObjects.get("colleges");
			for (Object c : collegesList){
				JSONObject cObj = (JSONObject) c;
				College college = new College ((String)cObj.get("name"),this,new Vector2(((Double)cObj.get("posX")).floatValue(),((Double)cObj.get("posY")).floatValue()),(Boolean)cObj.get("isFriendly"));
				college.health = ((Double)cObj.get("health")).floatValue();
				colleges.add(college);
				hittables.add(college);
			}
			
			JSONArray pickupList = (JSONArray) gameObjects.get("pickups");
			for (Object p : pickupList){
				JSONObject pObj = (JSONObject) p;
				Buff pBuff = new Buff((String)pObj.get("buffStat"), ((Double)pObj.get("buffAmount")).floatValue() ,  ((Double)pObj.get("buffDuration")).floatValue());
				Pickup pickup = new Pickup(this, new Vector2(((Double)pObj.get("posX")).floatValue(), ((Double)pObj.get("posY")).floatValue()), pBuff);
				pickups.add(pickup);
			}

			JSONArray upgradeList = (JSONArray) gameObjects.get("upgrades");
			for (Object u : upgradeList){
				JSONObject uObj = (JSONObject) u;
				Buff uBuff = new Buff((String)uObj.get("buffStat"), ((Double)uObj.get("buffAmount")).floatValue());
				Upgrade upgrade = new Upgrade(this, new Vector2(((Double)uObj.get("posX")).floatValue(), ((Double)uObj.get("posY")).floatValue()), uBuff, ((Double)uObj.get("cost")).floatValue());
				upgrades.add(upgrade);
			}

			setDifficulty();

		} catch (Exception e){
			//If the save file does not exist, call the reset game function
			e.printStackTrace();
			System.out.println("Save file does not exist!");
		}
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
		saveGame();
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
	public void update() {
		// Run update functions
		if (!testing){
			handleInput();
			updateCamera();
			updateLogic();
		}
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

		if(!testing){
			// Update particles, allowing for deletion
			for (Iterator<Particle> pItr = particles.iterator(); pItr.hasNext();) {
				Particle p = pItr.next();
				p.update();
				if (p.shouldRemove()) {
					pItr.remove();
					p.beenRemoved();
				}
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

		// Update pickups, allowing for deletion
		for (Iterator<Pickup> pItr = pickups.iterator(); pItr.hasNext();) {
			Pickup p = pItr.next();
			p.update();
			if (p.shouldRemove()) {
				pItr.remove();
				p.beenRemoved();
			}
		}
		for (Iterator<Obstacles> pItr = obstacles.iterator(); pItr.hasNext();) {
			Obstacles p = pItr.next();
			p.update();
			if (p.shouldRemove()) {
				pItr.remove();
				p.beenRemoved();
			}
		}

		for (Iterator<Weather> pItr = weather.iterator(); pItr.hasNext();) {
			Weather p = pItr.next();
			p.update();
			if (p.shouldRemove()) {
				pItr.remove();
				p.beenRemoved();
			}
		}

		// Update upgrades, allowing for deletion
		for (Iterator<Upgrade> pItr = upgrades.iterator(); pItr.hasNext();) {
			Upgrade u = pItr.next();
			u.update();
			if (u.shouldRemove()) {
				pItr.remove();
				u.beenRemoved();
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
		if (gameState == GameState.READY){
			if(Binding.getInstance().isActionPressed("loadSave") && saveExists){
				loadGame();
			}
			if(Binding.getInstance().isActionJustPressed("difficultyIncrease")){
				increaseDifficulty();
			}
			if(Binding.getInstance().isActionJustPressed("difficultyDecrease")){
				decreaseDifficulty();
			}
			if(Binding.getInstance().isActionJustPressed("startGame")){
				setDifficulty();
				startGame();
			}
		}			
		if (gameState == GameState.RUNNING && Binding.getInstance().isActionJustPressed("resetGame")){
			resetGame();
		}else{ if (gameState == GameState.FINISHED && Binding.getInstance().isActionJustPressed("startGame")
				|| gameState == GameState.FINISHED && Binding.getInstance().isActionJustPressed("resetGame"))
			resetGame();
		}
		// Close game on "closeGame"
		if (Binding.getInstance().isActionJustPressed("closeGame")){
			closeGame();
		}
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
	public void updateLogic() {
		// Increase XP and handle levelling up
		if (gameState == GameState.RUNNING) {
			if(!testing){
				addResources(0, Gdx.graphics.getDeltaTime() * xpGain);
			}
			else{ //Applys a testing friendly xpGain per tick
				addResources(0, xpGain);
			}
			
			if (currentXP > xpPerLevel) {
				currentXP = currentXP % xpPerLevel;
				currentLevel++;
				levelUpTimer = levelUpTimerMax;
			}
			if (!testing){
				levelUpTimer = (float) Math.max(levelUpTimer - Gdx.graphics.getDeltaTime(), 0f);
			}

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
		for (Obstacles obstacle : obstacles)
			obstacle.render(gameBatch);
		player.render(gameBatch);
		for (Projectile projectile : projectiles)
			projectile.render(gameBatch);
		for (Upgrade upgrade : upgrades)
			upgrade.render(gameBatch);

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

		//weather

		for (Weather w : weather){
			w.render(UIBatch);
		}

		// Draw start splash
		float time = (System.currentTimeMillis() - startTime) / 100f;
		if (gameState == GameState.READY) {
			startSprite.setScale(0.95f + 0.1f * (float) Math.sin(time / 2f));
			startSprite.setPosition(
					Gdx.graphics.getWidth() * 0.5f - startSprite.getWidth() * 0.5f,
					Gdx.graphics.getHeight() * 0.5f - startSprite.getHeight() * 0.5f);
			startSprite.draw(UIBatch);

			mainFont.getData().setScale(0.60f);

			String text = "Use arrow keys to adjust difficulty";
			currentUITextGlyph.setText(mainFont, text);
			float px = (UICamera.viewportWidth/2) - currentUITextGlyph.width * 0.5f;
			float py = (UICamera.viewportHeight/4) - currentUITextGlyph.height;
			mainFont.draw(UIBatch, text, px, py);

			currentUITextGlyph.setText(mainFont, difficultyDescriptors[difficultySelection]);
			px = (UICamera.viewportWidth/2) - currentUITextGlyph.width * 0.5f;
			mainFont.draw(UIBatch, difficultyDescriptors[difficultySelection], px, py - 80);

			mainFont.getData().setScale(0.60f + 0.01f * (float) Math.sin(time / 2f));
			currentUITextGlyph.setText(mainFont, difficultyStrings[difficultySelection]);
			px = (UICamera.viewportWidth/2) - currentUITextGlyph.width * 0.5f;
			mainFont.draw(UIBatch, difficultyStrings[difficultySelection], px, py - 40);

			
			File f = new File("save.txt");
			saveExists = f.exists();
			if (saveExists){
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				text = "Press 'Enter' to load \n  recent save from:\n     " + sdf.format(f.lastModified());
			}
			else{
				text = "     No save file detected! \n  Save and quit at any point \n        by pressing 'Esc'";
			}
			currentUITextGlyph.setText(mainFont, text);
			//Places the saving tooltip 5/7ths of the way up the screen, this puts it out of the way of the shop area
			mainFont.draw(UIBatch, text, 20, ((UICamera.viewportHeight/7)*5)); 
		}
		


		else if (gameState == GameState.FINISHED) {

			// If game is won or lost, remove the save file
			File saveFile = new File("save.txt"); 
			saveFile.delete();

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

		// Draw weather info UI
		mainFont.getData().setScale(0.6f);
		String weatherText = "Weather : Clear";
		if (weather.size() == 1){
			Weather w = weather.get(0);
			weatherText = "Weather : " + w.choice + " for " + Math.round(w.duration) + " seconds ";
		}
		currentUITextGlyph.setText(mainFont, levelText);
		mainFont.draw(UIBatch, weatherText, Gdx.graphics.getWidth() - currentUITextGlyph.width - 450, currentHeight);
		

		// Draw help UI
		currentHeight = Gdx.graphics.getHeight() - spacing;
		// Font is changed to a larger size for "esc save and quit" to stand out to the player, then scale is reduced to normal
		mainFont.getData().setScale(0.60f);
		String[] helpText = new String[] { "'Esc' : Save and Quit", "'Tab': Reset", "WASD: Movement", "LMB / RMB: Shoot" };
		for (String s : helpText) {
			currentUITextGlyph.setText(mainFont, s);
			mainFont.draw(UIBatch, s, spacing, currentHeight);
			currentHeight -= currentUITextGlyph.height + spacing;
			mainFont.getData().setScale(0.4f);
		}

		// Draw level up popup
		if (levelUpTimer > 0f) {
			mainFont.getData().setScale(1f);
			currentUITextGlyph.setText(mainFont, "Level " + currentLevel + "!");
			mainFont.draw(UIBatch, "Level " + currentLevel + "!", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
		mainFont.getData().setScale(1f);

		// Draw buff icons
		float buff_width = 40f;
		float buff_x = Gdx.graphics.getWidth() - 30f;
		float buff_y = Gdx.graphics.getHeight() - 30f;
		
		mainFont.getData().setScale(.3f);

		for (Buff buff : player.getBuffs()) {
			if (buff.time < 1) {
				continue;
			}
			Sprite buff_sprite = new Sprite(buff.getTexture());
			buff_sprite.setSize(50f, 50f);
			buff_sprite.setCenter(buff_x, buff_y);
			UIBatch.draw(buff_sprite, buff_sprite.getX(), buff_sprite.getY());

			mainFont.draw(UIBatch, String.valueOf((int) buff.time), buff_x - 30f, buff_y + 15f);

			buff_x -= buff_width;
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
	 * Adds a random weather event to the game for 15 seconds, only if no current weather event exists
	*/
	public void addRandomWeather(){
		if(weather.size() < 1){
			int randomNum = new java.util.Random().nextInt(weatherChoices.length);
			String choice = weatherChoices[randomNum];
			if (!testing){weather.add(new Weather(this, choice, 15f));}
			else{weather.add(new Weather(this, choice, 15f,true));}
		}	 
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

	public IInteractable checkForInteractables(Rectangle rect) {
		for (IInteractable interactable : upgrades) {
			if (Intersector.overlaps(rect, interactable.getInteractRange())) {
				return interactable;
			}
		}
		return null;
	}

	public void increaseDifficulty(){
		if (difficultySelection + 1 < difficultyStrings.length){
			difficultySelection += 1;
		}
	}

	public void decreaseDifficulty(){
		if (difficultySelection - 1 >= 0){
			difficultySelection -= 1;
		}
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

	// Store water tiles for future use
	private ArrayList<Vector2> waterTiles = null;

	// Random instance used in getRandomOverWater()
	public Random random = new Random();
	/**
	 * returns a random position that is over water, i.e. reachable by the player
	 * selected positions are removed from possible positions
	 * the final position is offset by a random amount
	 * @return the position as a Vector2
	 */
	public Vector2 getRandomOverWater() {
		
		// Populate waterTiles if empty
		if (waterTiles == null) {
			waterTiles = new ArrayList<>();
			TiledMapTileLayer terrain = (TiledMapTileLayer) tiledMap.getLayers().get("Terrain");
			for (int x = 0; x < terrain.getWidth(); x++) {
				for (int y = 0; y < terrain.getHeight(); y++) {
					Cell cell = terrain.getCell(x, y);

					// If tile is water (ID 20)
					if (cell.getTile().getId() == 20) {
						waterTiles.add(new Vector2(x, y));
					}
				}
			}
		}

		// Locate and remove a tile
		int ind = random.nextInt(waterTiles.size());
		Vector2 pos = waterTiles.get(ind);
		waterTiles.remove(ind);

		// Return a randomised location within the tile
		return new Vector2((float)(pos.x + random.nextDouble()) * PPT, (float)(pos.y + random.nextDouble()) * PPT);
	}

	/**
     * attempts to charge the player a certain amount of gold
     * will only subtract money if the player can afford it
     * @param amount transaction value
     * @return true if the transaction was successful
     */
    public boolean chargePlayer(float amount) {
        if (currentGold - amount >= 0) {
			currentGold -= amount;
			return true;
		}
		return false;
    }

}
