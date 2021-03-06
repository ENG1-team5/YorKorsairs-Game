package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * An item the player can collect
 */
public class Collectable {

    // Declare config, variables
    protected static Texture texture;
    protected final float width = Game.PPT * .5f;

    protected final float bounceMag = 0.2f;
    protected final float bounceFreq = 0.5f;

    protected Game game;
    protected Buff buff;
    protected Sprite sprite;
    protected Vector2 pos;
    protected Vector2 startPos;
    protected boolean toRemove;
    public boolean testing;
    /**
     * Creates a new collectable at position, applying buff_ on colision
     * @param game_ 
     * @param pos_ - position to spawn collectable at
     * @param buff_ - buff to apply
     */
    Collectable(Game game_, Vector2 pos_, Buff buff_, boolean testing) {
        game = game_;
        buff = buff_;
        pos = pos_;
        startPos = pos_;
        toRemove = false;
        this.testing  = testing;
    }

    Collectable(Game game_, Vector2 pos_, Buff buff_){
        this(game_, pos_, buff_, false);
        texture = buff.getTexture();
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
        if (!testing){
            float time = (System.currentTimeMillis() - Game.startTime) / 100f;
            float rel = (float) Math.sin(time * bounceFreq) * bounceMag;
            pos.set(startPos.x, startPos.y + rel); 
            sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
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
     * Deletes collectable to conserve processor if dead
     * @return boolean
     */
    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() { }


}
