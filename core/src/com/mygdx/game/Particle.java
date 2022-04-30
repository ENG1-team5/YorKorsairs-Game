
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;

/**
 * Class used to represent a particle of a given material, used upon collisions and damages
 */
public class Particle {

    // Declare variables
    private static final HashMap<String, Texture> textures = new HashMap<String, Texture>() {
        {
            put("rock", new Texture("./particles/rock.png"));
            put("splash", new Texture("./particles/splash.png"));
            put("wood", new Texture("./particles/wood.png"));
        }
    };

    private Sprite sprite;
    private Vector2 pos;
    private float sizeStart;
    private float sizeEnd;
    private Vector2 vel;
    private float time;
    private float maxTime;
    private boolean toRemove;

    /**
     * Instantiates a new particle with:
     * @param path - Filepath of particle material
     * @param pos_ - Position to spawn particle
     * @param size_ - Size of particle
     * @param speed_ - Speed of particle
     * @param time_ - Time the particle can exist
     */
    Particle(String path, Vector2 pos_, float size_, float speed_, float time_) {
        this(path, pos_, size_, size_, speed_, time_);
    }

    /**
     * See Particle(String path, Vector2 pos_, float size_, float speed_, float time_), but with variable size over time
     */
    Particle(String path, Vector2 pos_, float sizeStart_, float sizeEnd_, float speed_, float time_) {
        this(path, pos_, sizeStart_, sizeEnd_,
                new Vector2((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).nor().scl(speed_), time_);
    }

    /**
     * Variant of Particle(String path, Vector2 pos_, float size_, float speed_, float time_), but takes velocity instead of speed
     */
    Particle(String path, Vector2 pos_, float size_, Vector2 vel_, float time_) {
        this(path, pos_, size_, size_, vel_, time_);
    }

    /**
     * variant of Particle(String path, Vector2 pos_, float size_, float speed_, float time_), but applies variable particle size over time and velocity instead of speed
     */
    Particle(String path, Vector2 pos_, float sizeStart_, float sizeEnd_, Vector2 vel_, float time_) {
        // Initialize variables
        sprite = new Sprite(textures.get(path));
        pos = pos_;
        sizeStart = sizeStart_;
        sizeEnd = sizeEnd_;
        vel = vel_;
        time = time_;
        maxTime = time;
        toRemove = false;
    }

    /**
     * Updates position, velocity and timer
     */
    public void update() {
        // Update position
        pos.x += vel.x * Gdx.graphics.getDeltaTime();
        pos.y += vel.y * Gdx.graphics.getDeltaTime();

        // Update timer
        time = Math.max(time - Gdx.graphics.getDeltaTime(), 0);
        if (time <= 0.0f)
            toRemove = true;

        // Update sprite
        sprite.setColor(new Color(1f, 1f, 1f, time / maxTime));
        float newWidth = sizeStart + (sizeEnd - sizeStart) * (1f - time / maxTime);
        sprite.setSize(newWidth, newWidth * sprite.getTexture().getHeight() / sprite.getTexture().getWidth());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
    }


    /**
     * Renders particle sprite to batch
     * 
     * @param batch graphical output to be rendered to
     */
    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }


    /**
     * Returns whether it should be removed
     * 
     * @return boolean
     */
    public boolean shouldRemove() {
        return toRemove;
    }

    /**
     * call once particle is removed
     */
    public void beenRemoved() {
    }


    /**
     * Deletes particles sprites to conserve processor if dead
     */
    public static void staticDispose() {
        for (Texture texture : textures.values())
            texture.dispose();
    }
}
