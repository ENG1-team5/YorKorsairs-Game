package com.mygdx.game.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Buff;
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

import org.junit.Test;


public class pickupTests {
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

        ship = game.getPlayer();
        
    } 

    /**
     * Tests maxHealth increases and buff disapears from world to test pickup collection
     */
    @Test
    public void TestPickupRemoved(){
        instantiatePlayer();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("maxHealth", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff and it is removed and applied to player",
                            ship.buffs.size() > 0 && game.pickups.size() == 0);
    }

    /**
     * Tests MaxHealth Pickup functionality
     */
    @Test
    public void TestPickupMaxHealth(){
        instantiatePlayer();

        float maxHealthBeforeTest = ship.getMaxHealth();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("maxHealth", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff and it is removed and player maxhealth increases",
                            maxHealthBeforeTest < ship.getMaxHealth());
    }

    /**
     * Tests speed pickup functionality
     */
    @Test
    public void TestPickupSpeed(){
        instantiatePlayer();

        float maxSpeedBeforeTest = ship.getMaxSpeed();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("speed", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff for speed and player speed increases",
                            maxSpeedBeforeTest < ship.getMaxSpeed());
    }

    
    /**
     * Tests regen pickup functionality
     */
    @Test
    public void TestPickupRegen(){
        instantiatePlayer();

        float maxRegenBeforeTest = ship.getPassiveHealthRegen();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("regen", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff for regen and player regen increases",
                maxRegenBeforeTest < ship.getPassiveHealthRegen());
    }

    /**
     * Tests damage increase pickup functionality
     */
    @Test
    public void TestPickupDamage(){
        instantiatePlayer();

        float DamageBeforeTest = ship.getDamage();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("damage", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff for regen and player damage increases",
                DamageBeforeTest < ship.getDamage());
    }

    /**
     * Tests projectileSpeed pickup functionality
     */
    @Test
    public void TestPickupProjSpeed(){
        instantiatePlayer();

        float projSpeedBeforeTest = ship.getProjectileSpeed();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("projectileSpeed", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff for projectileSpeed and player projSpeed increases",
            projSpeedBeforeTest < ship.getProjectileSpeed());
    }

    /**
     * Tests fire rate buff pickup functionality
     */
    @Test
    public void TestPickupFireRate(){
        instantiatePlayer();

        float projSpeedBeforeTest = ship.getShotTimerMax();

        Pickup pickup =  new Pickup(game, new Vector2(10, 10), new Buff("fireRate", 10f, 10f, true),true);
        game.pickups.add(pickup);

        pickup.update();
        game.update();
        
        assertTrue("This test passes if the ship picks up buff for fireRate and player fireRate increases",
            projSpeedBeforeTest > ship.getShotTimerMax());
    }


}
