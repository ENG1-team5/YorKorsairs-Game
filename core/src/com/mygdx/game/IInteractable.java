package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

public interface IInteractable {
    
    /**
     * Defines an area where the player can interact with the building
     * @return A rectangle representing the above
     */
    public abstract Rectangle getInteractRange();

}
