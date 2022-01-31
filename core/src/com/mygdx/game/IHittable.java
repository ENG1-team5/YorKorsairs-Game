
package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

public abstract interface IHittable {

    /**
     * Calculates and returns collision rectangle
     * @return Rectangle
     */
    public abstract Rectangle getCollisionRect();

    /**
     * receive certain amount of damage
     * @param damage amount of damage taken
     * @return boolean true if successful
     */
    public abstract boolean damage(float damage);

    /**
     * return if college is friendly
     * @return boolean
     */
    public abstract boolean getFriendly();
}
