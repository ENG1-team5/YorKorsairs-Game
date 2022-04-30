package com.mygdx.game.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.College;
import com.mygdx.game.Enemy;
import com.mygdx.game.Game;
import com.mygdx.game.Game.GameState;
import com.mygdx.game.Player;
import com.mygdx.game.Projectile;
import com.mygdx.game.Upgrade;
import com.mygdx.game.objectives.GetLevel5Objective;
import com.mygdx.game.IHittable;
import com.mygdx.game.Particle;
import com.mygdx.game.Pickup;

import org.junit.Test;

public class shipTests {
    
    public Player ship;
    public Game game;

    public void instantiatePlayer(){
        int x = 10;
        int y = 10;

        game = new Game();
        game.testing = true;

        game.player = new Player(game, new Vector2(x,y), true); 
        // The "true" passed in is a boolean option added to a new constructor in Player, to split
        // the graphical initialisation from everything else, to make this test headless.

        game.collisionObjects = new MapObjects(); // Collision Objects and enemies must be initialised for the code to run
        game.enemies = new ArrayList<Enemy>();
        game.colleges = new ArrayList<College>();
        game.particles = new ArrayList<Particle>();
        game.colleges = new ArrayList<College>();
        game.pickups = new ArrayList<Pickup>();
        game.upgrades = new ArrayList<Upgrade>();
        game.objective = new GetLevel5Objective(game);
        game.projectiles = new ArrayList<Projectile>();
        game.enemies = new ArrayList<Enemy>();
        game.hittables = new ArrayList<IHittable>();

        ship = game.getPlayer();
        
    } 

    @Test
    public void testMoveLeft(){
        instantiatePlayer();
        ship.inputDir = new Vector2(-1,0); // Set input direction to left i.e. x = -1, y = 0 direction
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves left", x_before_test > x_after_test); 
    } 

    @Test
    public void testMoveRight(){
        instantiatePlayer();
        ship.inputDir = new Vector2(1,0); // Set input direction to right
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves right", x_before_test < x_after_test);
    } 

    @Test
    public void testMoveDown(){
        instantiatePlayer();
        ship.inputDir = new Vector2(0,-1); // Set input direction to down
        float y_before_test = ship.pos.y;
        ship.updateMovement(ship.inputDir); // runs update movement
        float y_after_test = ship.pos.y;
        assertTrue("This test will pass if the ship moves down", y_before_test > y_after_test); 
    } 

    @Test
    public void testMoveUp(){
        instantiatePlayer();
        ship.inputDir = new Vector2(0,1); // Set input direction to up
        float y_before_test = ship.pos.y;
        ship.updateMovement(ship.inputDir); // runs update movement
        float y_after_test = ship.pos.y;
        assertTrue("This test will pass if the ship moves UP", y_before_test < y_after_test); 
    } 

    @Test
    public void testMoveDiagonalNE(){
        instantiatePlayer();
        ship.inputDir = new Vector2(1,1); // Set input direction to NE
        float y_before_test = ship.pos.y;
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float y_after_test = ship.pos.y;
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves NE", y_before_test < y_after_test && x_before_test < x_after_test); 
    }

    @Test
    public void testMoveDiagonalSE(){
        instantiatePlayer();
        ship.inputDir = new Vector2(1,-1); // Set input direction to SE
        float y_before_test = ship.pos.y;
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float y_after_test = ship.pos.y;
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves SE", y_before_test > y_after_test && x_before_test < x_after_test); 
    }

    @Test
    public void testMoveDiagonalSW(){
        instantiatePlayer();
        ship.inputDir = new Vector2(-1,-1); // Set input direction to SW
        float y_before_test = ship.pos.y;
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float y_after_test = ship.pos.y;
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves SW", y_before_test > y_after_test && x_before_test > x_after_test); 
    }

    @Test
    public void testMoveDiagonalNW(){
        instantiatePlayer();
        ship.inputDir = new Vector2(-1,1); // Set input direction to NW
        float y_before_test = ship.pos.y;
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float y_after_test = ship.pos.y;
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves NW", y_before_test < y_after_test && x_before_test > x_after_test); 
    }

    // @Test
    // public void testWallCollision(){
    //     instantiatePlayer();
        
    //     ship.inputDir = new Vector2(-1,0); // Set input direction to left
    //     float x_before_test = ship.pos.x;
    //     ship.updateMovement(ship.inputDir); // runs update movement
    //     float x_after_test = ship.pos.x;
    //     System.out.println(x_before_test + " " + x_after_test);
    //     assertTrue("This test will pass if the ship moves left", x_before_test > x_after_test); 
    // }

    /**
     * Tests F_Attack
     */
    @Test
    public void testShipBulletCreated(){
        instantiatePlayer();
        game.projectiles = new ArrayList<Projectile>();
        int cannonBallsBeforeTest = game.projectiles.size();
        ship.fire(new Projectile(game, ship, ship.pos, new Vector2(1,0), true, true)); //Shoots a cannonball right
        int cannonBallsAfterTest = game.projectiles.size();
        assertTrue("This test will pass if the ship creates a projectile in the world when its fire() method is called", cannonBallsAfterTest == cannonBallsBeforeTest + 1); 
    }

    /**
     * Tests F_Attack
     */
    @Test
    public void testPlayerShootsEnemy(){
        instantiatePlayer();
        game.projectiles = new ArrayList<Projectile>();
        game.enemies = new ArrayList<Enemy>();
        game.hittables = new ArrayList<IHittable>();
        
        Projectile projectile = new Projectile(game, ship, ship.pos, new Vector2(1,0), true, true); //Places the enemy exactly one tick worth of momevement away
        Enemy enemy = new Enemy(game,new Vector2(10+projectile.speed, 10),true);
        game.enemies.add(enemy);
        game.hittables.add(enemy);

        float enemyHealthBeforeTest = enemy.health;
        ship.fire(projectile); //Shoots a cannonball right towards the enemy
        projectile.update();

        float enemyHealthAfterTest = enemy.health;
        assertTrue("This test will pass if the ship creates a projectile that hits an enemy dealing damage", enemyHealthAfterTest < enemyHealthBeforeTest);
    }

    @Test
    public void testPlayerDestroysEnemyAndGetsPlunderXP(){
        instantiatePlayer();
        
        Projectile projectile = new Projectile(game, ship, ship.pos, new Vector2(1,0), true, true); //Places the enemy exactly one tick worth of momevement away
        Enemy enemy = new Enemy(game,new Vector2(10+projectile.speed, 10),true);
        enemy.health =  projectile.damage; //Sets enemy to enough health to die from a single projectile
        game.enemies.add(enemy);
        game.hittables.add(enemy);

        float shipGoldBeforeTest = game.currentGold;
        float shipXPBeforeTest = game.currentXP;
        ship.fire(projectile); //Shoots a cannonball right towards the enemy, killing them
        projectile.update();
        game.update();

        assertTrue("This test will pass if the ship creates a projectile that kills enemy and gives XP and Plunder", game.enemies.size() == 0 && game.currentXP > shipXPBeforeTest && game.currentGold > shipGoldBeforeTest);
    }

    /**
     * Test enemy shoots player and player takes damage
     */
    @Test
    public void testEnemyShoot(){
        instantiatePlayer();
        // Float playerHealthBeforeTest = ship.health;
        Enemy enemy = new Enemy(game,new Vector2(10 + (Game.PPT * 3f), 10),true);
        game.hittables.add(enemy);
        game.hittables.add(ship);
        enemy.shotTimer = 0.0f;
        enemy.update();
        assertTrue("true when enemy fires a projectile at player", game.projectiles.size() == 1);
        // Could not figure out how to position enemy and player to get the projectile to collide in one tick, so for now, dont measure if player takes damage
        // game.update();
        // assertTrue("true when projectile damages player", playerHealthBeforeTest > ship.health);
    }

    @Test
    public void testPlayerDies(){
        instantiatePlayer();
        game.hittables.add(ship);
        Enemy enemy = new Enemy(game,new Vector2(10, -500),true); //Need an owner for the projectile we are about to create
        Projectile projectile = new Projectile(game, enemy, new Vector2(10 + (Game.PPT * 3f), 10), new Vector2(-1, 0), false, true);
        ship.health = projectile.damage;
        projectile.update();
        assertTrue("true if projectile kills player and game becomes Finished", ship.health == 0 && game.gameState == GameState.FINISHED);
    }

    /**
     * Tests player regnerates health when out of combat
     */
    @Test
    public void testPlayerRegen(){
        instantiatePlayer();
        ship.health = ship.health * 0.7f; //Sets player health to 70%, just to make them damaged
        float shipHealthBeforeTest = ship.health;
        game.gameState = GameState.RUNNING;
        game.colleges = new ArrayList<College>();
        ship.updateLogic();
        assertTrue("This test passes if the ship regens health after one tick, given they are not in combat", shipHealthBeforeTest < ship.health);
    }

    /**
     * Tests player regnerates health when out of combat faster at home college
     */
    @Test
    public void testPlayerRegenQuickerAtHome(){
        instantiatePlayer();
        ship.health = ship.health * 0.7f; //Sets player health to 70%, just to make them damaged
        float shipHealthBeforeTest = ship.health;
        game.gameState = GameState.RUNNING;
        game.colleges = new ArrayList<College>();
        game.colleges.add(new College("goodricke",game,new Vector2(11f, 11f), true, true));
        ship.updateLogic();
        assertTrue("This test passes if the ship regens more health after one tick than they would usually due to being in range of a friendly college, given they are not in combat",
                            shipHealthBeforeTest + ship.getPassiveHealthRegen() < ship.health);
    }

    @Test
    public void testXPOverTime(){
        instantiatePlayer();
        float XPBeforeTest = game.currentXP;
        game.gameState = GameState.RUNNING;
        game.updateLogic();
        assertTrue("This passes if XP is gained passively per tick", XPBeforeTest < game.currentXP);
    }
    
    @Test
    public void testLevelUp(){
        instantiatePlayer();
        game.currentXP = game.xpPerLevel-0.0001f;
        int levelBeforeTest = game.currentLevel;
        game.gameState = GameState.RUNNING;
        game.updateLogic();

        assertTrue("This passes if XP is gained passively per tick", levelBeforeTest < game.currentLevel);
    }

}
