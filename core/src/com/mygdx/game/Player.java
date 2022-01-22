
package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;


public class Player implements IHittable {

    // TODO:
    //  - Health variable which is limited, and detects when dead
    //  - This needs to trigger a function on Game to say player died
    //  - Also probably need a shipDead.png
    // TODO:
    //  - method of UI to show this below the ship
    //  - Potentially just a green texture scaled based on health overtop a red texture
    //  - Will probably need a renderUI() function that is called by Game()
    // TODO:
    //  - Generate growing circular wave particles behind ship while moving


    // Declare static, config, variables
    private static final Texture idleTexture = new Texture(Gdx.files.internal("./ships/ship.png"));
    private static final Texture movingTexture = new Texture(Gdx.files.internal("./ships/shipMoving.png"));
    private static final Texture healthbarBackTexture = new Texture(Gdx.files.internal("./UI/healthbarBack.png"));
    private static final Texture healthbarFillTexture = new Texture(Gdx.files.internal("./UI/healthbarFill.png"));

    private final float shipWidth = Game.PPT * 1.3f;
    private final float maxSpeed = Game.PPT * 1.5f; // Units / Second
    private final float acceleration = Game.PPT * 7f; // Units / Second^2
    private final float friction = 0.985f;
    private final float idleSpeed = Game.PPT * 0.4f;
    private final float idleSwayMag = 0.12f;
    private final float idleSwayFreq = 0.2f;
    private final float swayAcceleration = Game.PPT * 2.0f;
    private final float maxHealth = 100;
    private final float shotTimerMax = 0.8f;

    private Game game;
    private Sprite shipSprite;
    private Sprite healthbarBackSprite;
    private Sprite healthbarFillSprite;

    private Vector2 pos;
    private Vector2 vel;
    private Vector2 inputDir;
    private float health;
    private float shotTimer;


    Player(Game game_, Vector2 pos_) {
        // Initialize variables
        game = game_;
        pos = pos_;
        vel = new Vector2(Vector2.Zero);
        inputDir = new Vector2(Vector2.Zero);
        health = 100;
        shotTimer = 0.0f;

        // Initialize ship sprite
        float ratio = (float)idleTexture.getHeight() / (float)idleTexture.getWidth();
        shipSprite = new Sprite(idleTexture);
        shipSprite.setSize(shipWidth, shipWidth * ratio);
        shipSprite.setOrigin(shipSprite.getWidth() * 0.5f, 0);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y- shipSprite.getOriginY());

        // Initialize health bar sprites
        float backRatio = (float)healthbarBackTexture.getHeight() / healthbarBackTexture.getWidth();
        float backWidth = shipWidth * 0.8f;
        float backHeight = backWidth * backRatio;
        float pixelSize = backHeight / 12f;
        float fillWidth = backWidth - (pixelSize * 3) * 2;
        float fillHeight = backHeight - (pixelSize * 2) * 2;
        healthbarBackSprite = new Sprite(healthbarBackTexture);
        healthbarBackSprite.setSize(backWidth, backHeight);
        healthbarBackSprite.setOrigin(backWidth * 0.5f, backHeight * 0.5f);
        healthbarFillSprite = new Sprite(healthbarFillTexture);
        healthbarFillSprite.setSize(fillWidth, fillHeight);
        healthbarFillSprite.setOrigin(fillWidth * 0.5f, fillHeight * 0.5f);
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

        // Shoot if clicked
        if (Gdx.input.isButtonJustPressed(0) && shotTimer == 0.0f) {
            Vector2 dir = game.getWorldMousePos().sub(pos);
            Vector2 newPos = new Vector2(pos);
            Projectile projectile = new Projectile(game, this, newPos, dir.nor(), true);
            game.addProjectile(projectile);
            shotTimer = shotTimerMax;
        }

        // Update shot timer
        shotTimer = Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0);
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
        // Update sprite based on whether moving
        if (vel.len2() < (idleSpeed * idleSpeed)) {
            if (shipSprite.getTexture() != idleTexture) shipSprite.setTexture(idleTexture);
        } else if (shipSprite.getTexture() != movingTexture) shipSprite.setTexture(movingTexture);


        // Update the ship sprite position and flip
        if (!shipSprite.isFlipX() && (vel.x < 0)
            || shipSprite.isFlipX() && (vel.x > 0)) shipSprite.flip(true, false);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());


        // Setup sway variables
        float current = shipSprite.getRotation();
        float target;

        // Rock back and forth
        if (vel.len2() < (idleSpeed * idleSpeed)) {
            float time = (System.currentTimeMillis() - Game.startTime) / 100f;
            target = (-idleSwayMag + idleSwayMag * 2.0f * (float)Math.sin(time * idleSwayFreq)) * (2f * (float)Math.PI);

        // Sway backwards if moving
        } else target = -vel.x / maxSpeed * 0.5f * (2 * (float)Math.PI);

        // Sway towards target rotation
        shipSprite.rotate((target - current) * swayAcceleration * Gdx.graphics.getDeltaTime());
    }


    public void render(SpriteBatch batch) {
        // Draw ship
        shipSprite.draw(batch);

        // Update then draw health bar
        healthbarBackSprite.setPosition(
        pos.x - healthbarBackSprite.getOriginX(),
        pos.y - healthbarBackSprite.getOriginY() - healthbarBackSprite.getHeight());
        healthbarFillSprite.setPosition(
                pos.x - healthbarFillSprite.getOriginX(),
                pos.y - healthbarFillSprite.getOriginY() - healthbarBackSprite.getHeight());
        healthbarFillSprite.setScale(health / maxHealth, 1.0f);
        healthbarBackSprite.draw(batch);
        healthbarFillSprite.draw(batch);
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


    public boolean damage(float damage) {
        health -= damage;
        return true;
    }


    public boolean getFriendly() { return true; }


    public Vector2 getPosition() { return pos; }
}
