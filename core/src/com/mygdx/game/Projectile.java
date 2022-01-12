
package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class Projectile {

    // TODO:
    //  - Make a IHittable interface that Player and College inherits
    //  - Game can then perform checkHitHittable that returns either interface or null
    // TODO:
    //  - Call triggerHit on the IHittable returned when hit
    //  - This function should affect health / generate particles


    // Declare config, variables
    private static final Texture texture = new Texture(Gdx.files.internal("projectile.png"));
    private final float width = Game.PPT * 0.15f;

    private Game game;
    private Sprite sprite;
    private Vector2 pos;
    private Vector2 vel;
    private boolean canHit;
    private boolean toRemove;


    Projectile(Game game_, Vector2 pos_, Vector2 vel_) {
        // Declare variables
        game = game_;
        sprite = new Sprite(texture);
        pos = pos_;
        vel = vel_;
        canHit = false;

        // Setup sprite
        sprite.setPosition(pos.x, pos.y);
        sprite.setSize(width, width);
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
    }


    public void update() {
        // Update position with velocity
        pos.x += vel.x;
        pos.y += vel.y;

        // Update sprite
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());


        // Detect hitting player
        Rectangle rect = getCollisionRect();
        if (game.checkHitPlayer(rect)) {
            if (canHit) toRemove = true;

        // Detect hitting wall
        } else if (game.checkCollision(rect)) {
            if (canHit) toRemove = true;

        // Once off of wall then can hit players
        } else canHit = true;
    }


    public void render(SpriteBatch batch) {
        // Draw sprite
        sprite.draw(batch);
    }


    public boolean shouldRemove() {
        // Return whether it should be removed
        return toRemove;
    }


    public Rectangle getCollisionRect() {
        // Calculate collision rectangle
        return new Rectangle(
            pos.x - width * 0.5f,
            pos.y - width * 0.5f,
            width, width
        );
    }
}
