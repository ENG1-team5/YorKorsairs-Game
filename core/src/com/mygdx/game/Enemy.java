
package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

    // Declare config, variables
    private static final Texture shipTexture = new Texture(Gdx.files.internal("./ships/enemy.png"));
    private final float shipWidth = Game.PPT * 1.4f;
    private final float maxSpeed = Game.PPT * 1f;
    private final float acceleration = Game.PPT * 7f; // Units / Second^2
    private final float idleSwayMag = 0.16f;
    private final float idleSwayFreq = 0.2f;
    private final float swayAcceleration = 20f;

    private Game game;
    private Sprite shipSprite;

    private Vector2 pos;
    private Vector2 vel;
    private Vector2 inputDir;


    Enemy(Game game_, Vector2 pos_) {
        // Initialize variables
        game = game_;
        pos = pos_;
        vel = new Vector2(0f, 0f);
        inputDir = new Vector2(0f, 0f);

        // Setup sprite
        shipSprite = new Sprite(shipTexture);
        shipSprite.setSize(shipWidth, shipWidth * shipTexture.getHeight() / shipTexture.getWidth());
        shipSprite.setOrigin(shipSprite.getWidth() * 0.5f, 0f);
    }



    public void update() {
        // Calculate input movement direction
        if (Math.random() < Gdx.graphics.getDeltaTime() * 1f) {
            inputDir.x = (float)Math.random() * 2f - 1f;
            inputDir.y = (float)Math.random() * 2f - 1f;
            inputDir = inputDir.nor();
        }

        // Update velocity
        vel.x += inputDir.x * acceleration * Gdx.graphics.getDeltaTime();
        vel.y += inputDir.y * acceleration * Gdx.graphics.getDeltaTime();
        if (vel.len2() > (maxSpeed * maxSpeed)) vel = vel.setLength(maxSpeed);

        // Update movement
        float diffX = vel.x * Gdx.graphics.getDeltaTime();
        float diffY =  vel.y * Gdx.graphics.getDeltaTime();
        pos.x += diffX;
        pos.y += diffY;

        // Check if overlapping, and if so move back
        Rectangle rect = getCollisionRect();
        if (game.checkCollision(rect)) {
            pos.x -= diffX;
            pos.y -= diffY;
            vel.x = 0;
            vel.y = 0;
        }

        // Handle swaying
        float current = shipSprite.getRotation();
        float target;
        target = -vel.x / maxSpeed * 0.5f * (2 * (float)Math.PI);
        shipSprite.rotate((target - current) * swayAcceleration * Gdx.graphics.getDeltaTime());


        // Update sprite
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());
        if (!shipSprite.isFlipX() && (vel.x < 0)
                || shipSprite.isFlipX() && (vel.x > 0)) shipSprite.flip(true, false);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());

    }


    public void render(SpriteBatch batch) {
        // Render sprite to screen
        shipSprite.draw(batch);
    }


    public static void staticDispose() {
        // Dispose of textures
        shipTexture.dispose();
    }


    public Rectangle getCollisionRect() {
        // Calculate collision rectangle
        return new Rectangle(
            pos.x - shipWidth * 0.4f, pos.y,
            shipWidth * 0.8f, shipWidth * 0.2f
        );
    }
}