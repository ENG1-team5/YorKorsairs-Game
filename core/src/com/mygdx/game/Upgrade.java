package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Variant of Collectable that provides a buff at a cost of gold
 */
public class Upgrade extends Collectable implements IInteractable {

    public float cost;

    private boolean testing;

    public Upgrade(Game game_, Vector2 pos_, Buff buff_, float cost_) {
        super(game_, pos_, buff_);

        cost = cost_;
    }

    public Upgrade(Game game_, Vector2 pos_, Buff buff_, float cost_, boolean testing) {
        super(game_, pos_, buff_, testing);
        this.testing = testing;
        cost = cost_;
    }

    /**
     * Update object each tick
     */
    public void update() {
        super.update();
    }

    public void render(SpriteBatch batch) {
        super.render(batch);

        Game.mainFont.getData().setScale(.5f);
        Game.mainFont.draw(batch, buff.describeBuffedStats() + ": " + String.valueOf((int) cost), pos.x - 30f, pos.y - 15f);
        Game.mainFont.getData().setScale(1f);
    }

    /**
     * Get the area that the player can buy the upgrade
     */
    public Rectangle getInteractRange() {
        if (!testing){
            return sprite.getBoundingRectangle();
        }
        return new Rectangle(
            pos.x - width * 0.5f,
            pos.y - width * 0.5f,
            width, width);
    }

    /**
     * run when the player interacts with the object
     */
    public void onInteraction() {
        if (game.chargePlayer(cost)) {
            game.getPlayer().addBuff(buff);
            toRemove = true;
        }
    }
    
}
