
package com.mygdx.game;


import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class College {

    // Declare config, variables
    private final float collegeWidth = 200;

    private Game game;
    private Sprite collegeSprite;
    private Vector2 pos;


    College(Game game_, Vector2 pos_) {
        // Initialize variables
        game = game_;
        pos = pos_;

        // Initialize sprite
        Texture collegeTexture = new Texture(Gdx.files.internal("college.png"));
        float ratio = (float)collegeTexture.getHeight() / (float)collegeTexture.getWidth();
        collegeSprite = new Sprite(collegeTexture);
        collegeSprite.setSize(collegeWidth, collegeWidth * ratio);
        collegeSprite.setOrigin(collegeSprite.getWidth() * 0.5f, 0);
        collegeSprite.setPosition(pos.x - collegeSprite.getOriginX(), pos.y- collegeSprite.getOriginY());
    }


    public void update() { }


    public void render(SpriteBatch batch) {
        // Draw college to screen
        collegeSprite.draw(batch);
    }


    public void dispose() {
        // Dispose of ship texture afterwards
        collegeSprite.getTexture().dispose();
    }
}
