package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Pickup extends Collectable {

    public Pickup(Game game_, Vector2 pos_, Buff buff_) {
        super(game_, pos_, buff_);
    }

    public Pickup(Game game_, Vector2 pos_, Buff buff_, boolean testing){
        super(game_, pos_, buff_, true);
    }

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
