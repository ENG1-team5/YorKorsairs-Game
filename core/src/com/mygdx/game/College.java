
package com.mygdx.game;


import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class College {

    // Declare config, variables
    private final float collegeWidth = Game.PPT * 2f;
    private final float shotTimerMax = 0.4f;
    private final float shootRange = Game.PPT * 6.0f;

    private Game game;
    private Sprite collegeSprite;

    private Vector2 pos;
    private float shotTimer;


    College(Game game_, Vector2 pos_) {
        // Initialize variables
        game = game_;
        pos = pos_;
        shotTimer = 0.0f;

        // Initialize sprite
        Texture collegeTexture = new Texture(Gdx.files.internal("college.png"));
        float ratio = (float)collegeTexture.getHeight() / (float)collegeTexture.getWidth();
        collegeSprite = new Sprite(collegeTexture);
        collegeSprite.setSize(collegeWidth, collegeWidth * ratio);
        collegeSprite.setOrigin(collegeSprite.getWidth() * 0.5f, 0);
        collegeSprite.setPosition(pos.x - collegeSprite.getOriginX(), pos.y- collegeSprite.getOriginY());
    }


    public void update() {
        // Run update functions
        if (game.getRunning()) checkAttack();
    }


    private void checkAttack() {
        // Check whether player in range
        Player player = game.getPlayer();
        Vector2 dir = new Vector2(player.getPosition()).sub(pos);
        if (dir.len2() < (shootRange * shootRange)) {

            // Create projectile towards player
            if (shotTimer == 0.0f) {
                Projectile projectile = new Projectile(game, new Vector2(pos), dir.nor());
                game.addProjectile(projectile);
                shotTimer = shotTimerMax;
            }
        }

        // Update shot timer
        shotTimer = (float)Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0.0f);
    }


    public void render(SpriteBatch batch) {
        // Draw college to screen
        collegeSprite.draw(batch);
    }


    public void dispose() {
        // Dispose of ship texture afterwards
        collegeSprite.getTexture().dispose();
    }
}
