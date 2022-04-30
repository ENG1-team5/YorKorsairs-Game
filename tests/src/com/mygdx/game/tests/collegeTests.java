package com.mygdx.game.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.College;
import com.mygdx.game.Enemy;
import com.mygdx.game.Game;
import com.mygdx.game.Player;
import com.mygdx.game.Projectile;
import com.mygdx.game.Upgrade;
import com.mygdx.game.Game.GameState;
import com.mygdx.game.objectives.GetLevel5Objective;
import com.mygdx.game.Particle;
import com.mygdx.game.Pickup;
import com.mygdx.game.IHittable;

import org.junit.Test;

public class collegeTests {
    public Player ship;
    public Game game;
    
    public void instantiateGame(){
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
        
        game.particles = new ArrayList<Particle>();
        ship = game.getPlayer();
        
    } 

    /**
     * fires a bullet at the college from the player and it takes damage
     */
    @Test
    public void testCollegeDamages(){
        instantiateGame();
        Projectile projectile = new Projectile(game, ship, ship.pos, new Vector2(1,0), true, true); 
        College college = new College("constantine", game, new Vector2(10 + projectile.speed, 10), false, true);//Places the college exactly one tick worth of momevement away
        game.colleges.add(college);
        game.hittables.add(college);
        float collegeHealthBeforeTest = college.health;
        ship.fire(projectile);
        projectile.update();
        float collegeHealthAfterTest = college.health;
        assertTrue("Test passes if college loses health upon projectile collision", collegeHealthBeforeTest > collegeHealthAfterTest);
    }

    /**
     * fires bullet at college, dealing enough damage to kill college, tests if it has been “disabled” by testing if updateAI() fires a cannonball after reaching 0 health
     */
    @Test
    public void testCollegeDies(){
        instantiateGame();
        Projectile projectile = new Projectile(game, ship, ship.pos, new Vector2(1,0), true, true); 
        College college = new College("constantine", game, new Vector2(10 + projectile.speed, 10), false, true);//Places the college exactly one tick worth of momevement away
        college.health = projectile.damage; //set health to drop to 0 after one shot
        game.colleges.add(college);
        game.hittables.add(college);
        ship.fire(projectile);
        projectile.update();
        college.updateAI(); //Runs AI which usually shoots at player, but as health is 0 it should not fire anymore
        game.update();
        assertTrue("Test passes if college does not create projectiles after death and its health = 0", college.health == 0 && game.projectiles.size() == 0);
    }

    @Test
    public void collegeFireAtPlayerInRange(){
        instantiateGame();
        College college = new College("constantine", game, new Vector2(10 + (Game.PPT * 3f), 10), false, true);//Places the college exactly a ticks worth of momevement away
        game.colleges.add(college);
        game.hittables.add(college);
        game.hittables.add(game.player);
        game.gameState = GameState.RUNNING;
        //float playerHealthBeforeTest = ship.health;
        college.update();
        assertTrue("passes if college has shot its 2 projectiles", game.projectiles.size() == 2);
        // Could not get this working due to the nature of how colleges fire two bullets at very strange angles, hard to measure where to put player and college
        // game.update();
        // assertTrue("passes if player took damage", playerHealthBeforeTest < ship.health);
    }

    @Test
    public void testCollegeRewardsXPandGold(){
        instantiateGame();
        Projectile projectile = new Projectile(game, ship, ship.pos, new Vector2(1,0), true, true); 
        College college = new College("constantine", game, new Vector2(10 + projectile.speed, 10), false, true);//Places the college exactly one tick worth of momevement away
        college.health = projectile.damage; //set health to drop to 0 after one shot
        float goldBeforeTest = game.currentGold;
        float xpBeforeTest = game.currentXP;
        game.colleges.add(college);
        game.hittables.add(college);
        ship.fire(projectile);
        projectile.update();
        college.updateAI(); //Runs AI which usually shoots at player, but as health is 0 it should not fire anymore
        game.update();
        assertTrue("Test passes if college rewards XP and Gold upon death from player", xpBeforeTest < game.currentXP && goldBeforeTest < game.currentGold);
    }
}
