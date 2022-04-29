package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Generic item in game world that provides a buff
*/
public class Pickup extends Collectable {

    Pickup(Game game_, Vector2 pos_, Buff buff_) {
        super(game_, pos_, buff_);
    }

    /**
     * Update object each tick
     */
    public void update() {
        super.update();

        if (game.checkHitPlayer(getCollisionRect())) {
            game.getPlayer().addBuff(buff);
            toRemove = true;
        }
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
}
