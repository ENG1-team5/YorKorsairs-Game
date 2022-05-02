package com.mygdx.game.tests;

import com.badlogic.gdx.Gdx;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class AssetTests {

    @Test
    public void testShipAssetExists() {
        assertTrue("This test will only pass when the ship.png asset exists.", Gdx.files
            .internal("./ships/ship.png").exists());
    }

    @Test
    public void testCannonballAssetExists() {
        assertTrue("This test will only pass when the ship.png asset exists.", Gdx.files
            .internal("./projectiles/cannonball.png").exists());
    }
}