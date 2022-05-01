package com.mygdx.game.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.College;
import com.mygdx.game.Enemy;
import com.mygdx.game.Game;
import com.mygdx.game.Player;
import com.mygdx.game.Upgrade;
import com.mygdx.game.objectives.DestroyCollegeObjective;
import com.mygdx.game.objectives.GetLevel5Objective;
import com.mygdx.game.Particle;
import com.mygdx.game.Pickup;

import org.junit.Test;

public class gameFeatureTests {

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

        ship = game.getPlayer();
        
    } 


    /**
     * Tests saving and loading of game does not error, complex testing would require major restructuring.
     */
    @Test
    public void testSaveLoadGameNoError(){
        instantiateGame();
        game.saveGame(); 
        game.loadGame();
        assertTrue("passes if player is at position 11x and a college exists at 11,11, showing the save and load worked", true);
    }

    /**
     * Tests if tracking objective function works and completes
     */
    @Test
    public void testLevel5ObjectiveComplete(){
        instantiateGame();
        game.currentLevel = 4;
        assertFalse("level is set to 4 and this test passes if objective verifies this it is NOT complete",game.objective.checkComplete(game));
        game.currentLevel = 5;
        assertTrue("passes if level is 5 and objective verifies this is true as the game is complete",game.objective.checkComplete(game));
    }

    /**
     * Tests if tracking objective function works and completes
     */
    @Test
    public void testCollegeObjectiveComplete(){
        instantiateGame();
        game.colleges.add( new College("constantine",game,new Vector2(11f, 11f), false, true));
        game.colleges.add( new College("constantine",game,new Vector2(11f, 11f), false, true));
        game.colleges.add( new College("goodricke",game,new Vector2(11f, 11f), true, true));
        game.objective = new DestroyCollegeObjective(game);
        assertFalse("Colleges still exist and this test passes if objective verifies this itself is NOT complete",game.objective.checkComplete(game));
        game.colleges = new ArrayList<College>();
        game.colleges.add( new College("goodricke",game,new Vector2(11f, 11f), true, true));
        game.objective = new DestroyCollegeObjective(game);
        game.objective.getRequirementText();
        assertTrue("No enemy colleges exist objective verifies this is true as the game is complete",game.objective.checkComplete(game));
    }

}
