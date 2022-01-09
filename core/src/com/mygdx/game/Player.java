
package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;


public class Player {

    // Declare config, variables
    public final float shipWidth = Game.PPT * 1f;
    public final float maxSpeed = Game.PPT * 1.5f; // Units / Second
    private final float acceleration = Game.PPT * 7f; // Units / Second^2
    private final float friction = 0.985f;

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
        if (game.getRunning()) handleInput();
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

        // Move ship with velocity
        float diffX = vel.x * Gdx.graphics.getDeltaTime();
        float diffY =  vel.y * Gdx.graphics.getDeltaTime();
        pos.x += diffX;
        pos.y += diffY;
        vel.x *= friction;
        vel.y *= friction;

        // Check if overlapping, and if so move back
        Rectangle rect = getCollisionRect();
        if (game.checkCollision(rect)) {
            pos.x -= diffX;
            pos.y -= diffY;
            vel.x = 0;
            vel.y = 0;
        }
    }


    private void updateSprite() {
        // Update the ship sprite position and flip
        if (!shipSprite.isFlipX() && (vel.x < 0)
            || shipSprite.isFlipX() && (vel.x > 0)) shipSprite.flip(true, false);
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


    public Rectangle getCollisionRect() {
        // Calculate collision rectangle
        return new Rectangle(
            pos.x - shipWidth * 0.4f, pos.y,
            shipWidth * 0.8f, shipWidth * 0.2f
        );
    }


    public Vector2 getPosition() { return pos; }
}
