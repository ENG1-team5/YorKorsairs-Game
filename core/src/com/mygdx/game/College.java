
package com.mygdx.game;


import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class College {

    // Declare config, variables
    private static final Texture collegeTexture = new Texture(Gdx.files.internal("college.png"));
    private static final Texture collegeShotTexture = new Texture(Gdx.files.internal("collegeShot.png"));
    private final float collegeWidth = Game.PPT * 2.5f;
    private final float shotTimerMax = 1.5f;
    private final float shootRange = Game.PPT * 4.0f;

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
        float ratio = (float)collegeTexture.getHeight() / (float)collegeTexture.getWidth();
        collegeSprite = new Sprite(collegeTexture);
        collegeSprite.setSize(collegeWidth, collegeWidth * ratio);
        collegeSprite.setOrigin(collegeSprite.getWidth() * 0.5f, 0);
        collegeSprite.setPosition(pos.x - collegeSprite.getOriginX(), pos.y- collegeSprite.getOriginY());
    }


    public void update() {
        // Run update functions
        if (game.getRunning()) checkAttack();
        updateSprite();
    }


    private void checkAttack() {
        // Check whether player in range
        Player player = game.getPlayer();
        Vector2 dir = new Vector2(player.getPosition()).sub(pos);
        if (dir.len2() < (shootRange * shootRange)) {

            // Create projectile towards player
            if (shotTimer == 0.0f) {
                Vector2 newPos = new Vector2(pos);
                newPos.y += collegeSprite.getHeight() * 0.92f;
                Projectile projectile = new Projectile(game, newPos, dir.nor());
                game.addProjectile(projectile);
                shotTimer = shotTimerMax;
            }
        }

        // Update shot timer
        shotTimer = (float)Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0.0f);
    }


    private void updateSprite() {
        // Change sprite based on shotTimer
        if (shotTimer < shotTimerMax * 0.2f) {
            if (collegeSprite.getTexture() != collegeTexture) collegeSprite.setTexture(collegeTexture);
        } else if (collegeSprite.getTexture() != collegeShotTexture) collegeSprite.setTexture(collegeShotTexture);
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
