package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Pickup {

    // Declare config, variables
    private static final Texture texture = new Texture(Gdx.files.internal("./projectiles/cannonball.png"));
    private final float width = Game.PPT * .5f;

    private final float bounceMag = 0.2f;
    private final float bounceFreq = 0.5f;

    private Game game;
    private Buff buff;
    private Sprite sprite;
    private Vector2 pos;
    private Vector2 startPos;
    private boolean toRemove;

    Pickup(Game game_, Vector2 pos_, Buff buff_) {

        game = game_;
        buff = buff_;
        sprite = new Sprite(texture);
        pos = pos_;
        startPos = pos_;
        toRemove = false;
    
        sprite.setPosition(pos.x, pos.y);
        sprite.setSize(width, width * texture.getHeight() / texture.getWidth());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
    }

    /**
     * Update object each tick
     */
    public void update() {

        // Bouncing animation
        float time = (System.currentTimeMillis() - Game.startTime) / 100f;
        float rel = (float) Math.sin(time * bounceFreq) * bounceMag;

        pos.set(startPos.x, startPos.y + rel);

        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());

        Rectangle rect = getCollisionRect();
        IHittable hittableHit = game.checkHitHittable(rect);

        if (hittableHit instanceof Player) {
            ((Player)hittableHit).addBuff(buff);
            toRemove = true;
        }
    }

    /**
     * renders projectile sprite to output batch
     * @param batch graphical output to be rendered to
     */
    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

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



}
