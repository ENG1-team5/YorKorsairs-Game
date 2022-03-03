package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Pickup {

    // Define textures
    private Texture texture;
    private static final Texture multi = new Texture(Gdx.files.internal("./pickups/multi1.png"));
    private static final Texture damage = new Texture(Gdx.files.internal("./pickups/dmg1.png"));
    private static final Texture fireRate = new Texture(Gdx.files.internal("./pickups/speed2.png"));
    private static final Texture projectileSpeed = new Texture(Gdx.files.internal("./pickups/dmgspeed.png"));
    private static final Texture regen = new Texture(Gdx.files.internal("./pickups/health1.png"));
    private static final Texture maxHealth = new Texture(Gdx.files.internal("./pickups/health2.png"));
    private static final Texture speed = new Texture(Gdx.files.internal("./pickups/multi1.png"));
    private static final Texture err = new Texture(Gdx.files.internal("./pickups/err.png"));

    // Declare config, variables
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
        pos = pos_;
        startPos = pos_;
        toRemove = false;

        ArrayList<String> buffed = buff.getBuffedStats();
        
        // Get the texture from the buff
        if (buffed.size() > 1) {
            // topSpeed + acceleration is treated as a special case
            // if both are present the texture will be speed
            // if only one the texture will be err
            if ((buffed.contains("topSpeed") && buffed.contains("acceleration")) && buffed.size() == 2 ) {
                texture = speed;
            }
            // Multibuff otherwise
            texture = multi;

        } else if (buffed.size() < 1) {
            texture = err;
        } else { //buffed.size() == 1
            switch ( buffed.get(0) ) {
                case "maxHealth":
                    texture = maxHealth;
                    break;

                case "regen":
                    texture = regen;
                    break;
                
                case "damage":
                    texture = damage;
                    break;
                    
                case "projectileSpeed":
                    texture = projectileSpeed;
                    break;
                    
                case "fireRate":
                    texture = fireRate;
                    break;
            
                default:
                    texture = err;
                    break;
            }
        }

        sprite = new Sprite(texture);
    
        sprite.setPosition(pos_.x, pos_.y);
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
        multi.dispose();
        damage.dispose();
        fireRate.dispose();
        projectileSpeed.dispose();
        regen.dispose();
        maxHealth.dispose();
        speed.dispose();
        err.dispose();
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
