package com.mygdx.game.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.naming.InitialContext;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.College;
import com.mygdx.game.Enemy;
import com.mygdx.game.Game;
import com.mygdx.game.Player;
import com.mygdx.game.Projectile;
import com.mygdx.game.Upgrade;
import com.mygdx.game.objectives.GetLevel5Objective;
import com.mygdx.game.IHittable;
import com.mygdx.game.Particle;
import com.mygdx.game.Pickup;
import com.mygdx.game.Obstacles;
import com.mygdx.game.Weather;
import com.mygdx.game.Game.GameState;

import org.junit.Test;

public class obstacleWeatherTests {
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
        game.hittables = new ArrayList<IHittable>();

        game.obstacles = new ArrayList<Obstacles>();
        game.weather = new ArrayList<Weather>();

        ship = game.getPlayer();
        
    } 

    /**
     * Test rocks obstacle, player should take damage, but rock should remain
     */
    @Test
    public void testRockObstacle(){
        instantiatePlayer();
        Obstacles obstacle = new Obstacles(game, new Vector2(10, 10), "Rock", true);
        game.obstacles.add(obstacle);
        game.hittables.add(ship);
        float shipHealthBeforeTest = ship.health;
        obstacle.update();
        float shiphealthAfterTest = ship.health; // must measure here or else regen takes place in game.update()
        game.update();
        assertTrue("only pass if rock colides, damages player and does not remove from game", game.obstacles.size() == 1 &&  shipHealthBeforeTest > shiphealthAfterTest);
    }

    /**
     * Test iceberg obstacle, player should take damage and rock should be removed and player slowed down
     */
    @Test
    public void testIcebergObstacle(){
        instantiatePlayer();
        Obstacles obstacle = new Obstacles(game, new Vector2(10, 10), "Iceberg", true);
        game.obstacles.add(obstacle);
        game.hittables.add(ship);
        float shipHealthBeforeTest = ship.health;
        obstacle.update();
        float shiphealthAfterTest = ship.health; // must measure here or else regen takes place in game.update()
        game.update();
        assertTrue("only pass if iceberg colides, damages player and removes from game", game.obstacles.size() == 0 &&  shipHealthBeforeTest > shiphealthAfterTest);
    }

        /**
     * Test seamine obstacle, player should take damage and seamine should be removed and player not slowed down
     */
    @Test
    public void testSeamineObstacle(){
        instantiatePlayer();
        Obstacles obstacle = new Obstacles(game, new Vector2(10, 10), "Seamine", true);
        game.obstacles.add(obstacle);
        game.hittables.add(ship);
        float shipHealthBeforeTest = ship.health;
        obstacle.update();
        float shiphealthAfterTest = ship.health; // must measure here or else regen takes place in game.update()
        game.update();
        assertTrue("only pass if seamine colides, damages player and removes from game", game.obstacles.size() == 0 &&  shipHealthBeforeTest > shiphealthAfterTest);
    }

    @Test
    public void testRandomWeather(){
        instantiatePlayer();
        game.addRandomWeather();
        assertTrue("Checks if weather has been added", game.weather.size() == 1);
    }
    
}
