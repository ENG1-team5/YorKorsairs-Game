package com.mygdx.game.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Enemy;
import com.mygdx.game.Game;
import com.mygdx.game.Player;

import org.junit.Test;

public class shipTests {
    
    @Test
    public void testMoveLeft(){
        int x = 10;
        int y = 10;

        Game game = new Game();

        game.player = new Player(game, new Vector2(x,y), true); 
        // The "true" passed in is a boolean option added to a new constructor in Player, to split
        // the graphical initialisation from everything else, to make this test headless.

        game.collisionObjects = new MapObjects(); // Collision Objects and enemies must be initialised for the code to run :(
        game.enemies = new ArrayList<Enemy>();

        Player ship = game.getPlayer();
        ship.inputDir = new Vector2(-1,0); // Set input direction to left i.e. x = -1, y = 0 direction
        float x_before_test = ship.pos.x;
        ship.updateMovement(ship.inputDir); // runs update movement
        float x_after_test = ship.pos.x;
        assertTrue("This test will pass if the ship moves left", x_before_test > x_after_test); // If old x is greater than after test, the ship has moved left
    } 

}
