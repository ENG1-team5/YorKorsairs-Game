package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

/**
 * Interface to provide interactability with the player to perform effects
 */
public interface IInteractable {

    /**
     * Defines an area where the player can interact with the building
     * @return A rectangle representing the above
     */
    public abstract Rectangle getInteractRange();

    /**
     * Executed when the player interacts with the building
     */
    public abstract void onInteraction();
}
