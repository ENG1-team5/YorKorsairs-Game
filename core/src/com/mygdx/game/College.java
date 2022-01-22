
package com.mygdx.game;


import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class College implements IHittable {

    // TODO:
    //  - Add a teamID variable that allows it to decide whether to shoot player / enemy
    //      that is set in the constructor.
    //  - Player would need a public getTeam() function.
    //      - Maybe worth thinking about other ships and their team
    //  - This would also allows some way of differentiating visuals.
    //  - Can also pass this into the projectile.


    // Declare config, variables
    private static final Texture collegeTexture = new Texture(Gdx.files.internal("./colleges/college.png"));
    private static final Texture collegeShotTexture = new Texture(Gdx.files.internal("./colleges/collegeShot.png"));
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
        if (game.getRunning()) updateAI();
        updateSprite();
    }


    private void updateAI() {
        // Get direction to player
        Player player = game.getPlayer();
        Vector2 newPos = new Vector2(pos);
        newPos.y += collegeSprite.getHeight() * 0.92f;
        Vector2 dir = new Vector2(player.getPosition()).sub(newPos);

        // Check whether player in range
        if (dir.len2() < (shootRange * shootRange)) {

            // Create projectile towards player
            if (shotTimer == 0.0f) {
                Projectile projectile = new Projectile(game, this, newPos, dir.nor(), false);
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


    @Override
    public Rectangle getCollisionRect() {
        return new Rectangle(
            pos.x - collegeSprite.getOriginX() - collegeWidth * 0.5f,
            pos.y - collegeSprite.getOriginY(),
            collegeSprite.getWidth(), collegeSprite.getHeight() * 0.5f
        );
    }


    @Override
    public boolean getFriendly() {
        return false;
    }


    @Override
    public boolean damage(float amount) {
        return false;
    }
}
