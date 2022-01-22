
package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;


public abstract interface IHittable {

    public abstract Rectangle getCollisionRect();

    public abstract boolean damage(float damage);

    public abstract boolean getFriendly();
}
