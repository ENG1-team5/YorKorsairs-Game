
package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;


public class Player {

    // Declare config, variables
    public final float shipWidth = 65;
    public final float maxSpeed = 65;
    private final float acceleration = 200;
    private final float friction = 0.99f;

    private Game game;
    private Sprite shipSprite;
    private Vector2 pos;
    private Vector2 vel;
    private Vector2 inputDir;


    Player(Game game_, Vector2 pos_) {
        // Initialize variables
        game = game_;
        pos = pos_;
        vel = new Vector2(Vector2.Zero);
        inputDir = new Vector2(Vector2.Zero);

        // Initialize sprite
        Texture shipTexture = new Texture(Gdx.files.internal("ship.png"));
        float ratio = (float)shipTexture.getHeight() / (float)shipTexture.getWidth();
        shipSprite = new Sprite(shipTexture);
        shipSprite.setSize(shipWidth, shipWidth * ratio);
        shipSprite.setOrigin(shipSprite.getWidth() * 0.5f, 0);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y- shipSprite.getOriginY());
    }


    public void update() {
        handleInput();
        updateMovement();
        updateSprite();
    }


    private void handleInput() {
        // Calculate input direction
        inputDir = new Vector2(0, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.A)) inputDir.x--;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) inputDir.x++;
        if(Gdx.input.isKeyPressed(Input.Keys.S)) inputDir.y--;
        if(Gdx.input.isKeyPressed(Input.Keys.W)) inputDir.y++;
    }


    private void updateMovement() {
        // Accelerate velocity in inputDir and limit to maxSpeed
        vel.x += inputDir.x * acceleration * Gdx.graphics.getDeltaTime();
        vel.y += inputDir.y * acceleration * Gdx.graphics.getDeltaTime();
        if (vel.len2() > (maxSpeed * maxSpeed)) vel = vel.setLength(maxSpeed);

        // Update position and apply friction
        pos.x += vel.x * Gdx.graphics.getDeltaTime();
        pos.y += vel.y * Gdx.graphics.getDeltaTime();
        vel.x *= friction;
        vel.y *= friction;
    }


    private void updateSprite() {
        // Update the ship sprite position and flip
        shipSprite.setFlip((vel.x < 0), false);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());
    }


    public void render(SpriteBatch batch) {
        // Draw ship to screen
        shipSprite.draw(batch);
    }


    public void dispose() {
        // Dispose of ship texture afterwards
        shipSprite.getTexture().dispose();
    }


    public Vector2 getPosition() { return pos; }
}
