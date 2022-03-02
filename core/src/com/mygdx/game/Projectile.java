
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Projectile {

    // Declare config, variables
    private static final Texture texture = new Texture(Gdx.files.internal("./projectiles/cannonball.png"));
    private final float width = Game.PPT * 0.22f;
    private final float timeMax = 3f;
    private float speed = Game.PPT * 3f;
    private float damage = 15f;

    private Game game;
    IHittable source;
    private Sprite sprite;
    private Vector2 pos;
    private Vector2 vel;
    private boolean isFriendly;
    private float currentTime;
    private boolean toRemove;

    Projectile(Game game_, IHittable source_, Vector2 pos_, Vector2 vel_, boolean isFriendly_) {
        // Declare variables
        game = game_;
        source = source_;
        sprite = new Sprite(texture);
        pos = pos_;
        vel = vel_.nor();
        isFriendly = isFriendly_;
        currentTime = 0.0f;
        toRemove = false;

        // Setup sprite
        sprite.setPosition(pos.x, pos.y);
        sprite.setSize(width, width * texture.getHeight() / texture.getWidth());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
    }
    
    Projectile(Game game_, IHittable source_, Vector2 pos_, Vector2 vel_, boolean isFriendly_, float damage_, float speed_) {
        // Declare variables
        game = game_;
        source = source_;
        sprite = new Sprite(texture);
        pos = pos_;
        vel = vel_.nor();
        isFriendly = isFriendly_;
        currentTime = 0.0f;
        toRemove = false;

        damage = damage_;
        speed = speed_;

        // Setup sprite
        sprite.setPosition(pos.x, pos.y);
        sprite.setSize(width, width * texture.getHeight() / texture.getWidth());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
    }

    /**
     * updates velocity, position, sprite, collision and timer
     */
    public void update() {
        // Update position with velocity
        pos.x += vel.x * speed * Gdx.graphics.getDeltaTime();
        pos.y += vel.y * speed * Gdx.graphics.getDeltaTime();

        // Update sprite
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());

        // Update timer
        currentTime += Gdx.graphics.getDeltaTime();
        if (currentTime > timeMax)
            toRemove = true;

        // Check if hit anything
        Rectangle rect = getCollisionRect();
        IHittable hittableHit = game.checkHitHittable(rect);
        if (hittableHit != null) {
            if (isFriendly != hittableHit.getFriendly() && hittableHit != source) {

                // Hit something that is not friendly
                hittableHit.damage(damage);
                toRemove = true;

                for (int i = 0; i < Math.random() * 3f + 4f; i++) {
                    Particle particle = new Particle("rock", new Vector2(pos), Game.PPT * 0.1f, Game.PPT * 0.5f, 0.7f);
                    game.addParticle(particle);
                }
            }
        }

        // Rotate sprite in correct direction
        float angle = MathUtils.radiansToDegrees * (float) Math.atan2(vel.y, vel.x);
        sprite.setRotation(angle);
    }


    /**
     * renders projectile sprite to output batch
     * @param batch graphical output to be rendered to
     */
    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }


    /**
     * Dispose static textures
     */
    public static void staticDispose() {
        texture.dispose();
    }


    /**
     * Deletes projectile to conserve processor if dead
     * @return boolean
     */
    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() { }


    /**
     * Calculates and returns collision rectangle
     * @return Rectangle
     */
    public Rectangle getCollisionRect() {
        return new Rectangle(
                pos.x - width * 0.5f,
                pos.y - width * 0.5f,
                width, width);
    }
}
