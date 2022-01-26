
package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;


public class Player implements IHittable {

    // TODO:
    //  - It is VERY inefficient to use entirely different files for each frame
    //  - Need to change to using a spritesheet and TextureRegions

    // Declare static, config, variables
    private static final Texture idleTexture = new Texture(Gdx.files.internal("./ships/ship.png"));
    private static final Texture[] idleShotTextures = new Texture[] {
            new Texture(Gdx.files.internal("./ships/shipShot0.png")),
            new Texture(Gdx.files.internal("./ships/shipShot1.png")),
            new Texture(Gdx.files.internal("./ships/shipShot2.png")),
            new Texture(Gdx.files.internal("./ships/shipShot3.png")) };
    private static final Texture movingTexture = new Texture(Gdx.files.internal("./ships/shipMoving.png"));
    private static final Texture[] movingShotTextures = new Texture[] {
            new Texture(Gdx.files.internal("./ships/shipMovingShot0.png")),
            new Texture(Gdx.files.internal("./ships/shipMovingShot1.png")),
            new Texture(Gdx.files.internal("./ships/shipMovingShot2.png")),
            new Texture(Gdx.files.internal("./ships/shipMovingShot3.png")) };
    private static final Texture deadTexture = new Texture(Gdx.files.internal("./ships/shipDead.png"));
    private static final Texture healthbarBackTexture = new Texture(Gdx.files.internal("./UI/healthbarBack.png"));
    private static final Texture healthbarFillTexture = new Texture(Gdx.files.internal("./UI/healthbarFill.png"));

    public final float shipWidth = Game.PPT * 1.4f;
    private final int shotCount = 4;
    private final float maxSpeed = Game.PPT * 1.25f; // Units / Second
    private final float maxSpeedScale = Game.PPT * 0.5f;
    private final float acceleration = Game.PPT * 7f; // Units / Second^2
    private final float accelerationScale = Game.PPT * 1f;
    private final float friction = 0.985f;
    private final float idleSpeed = Game.PPT * 0.55f;
    private final float idleSwayMag = 0.16f;
    private final float idleSwayFreq = 0.2f;
    private final float swayAcceleration = 20f;
    private final float maxHealth = 100;
    private final float passiveHealthRegen = 2.5f;
    private final float homeHealthRegen = 7.5f;
    private final float regenRange = Game.PPT * 5f;
    private final float shotTimerMax = 0.55f;
    private final float shotTimerMaxScale = -0.1f;
    private final float particleTimerMax = 0.4f;
    private final float smokeTimerMax = 0.1f;
    private final float combatTimerMax = 2.0f;

    private Game game;
    private Sprite shipSprite;
    private Sprite healthbarBackSprite;
    private Sprite healthbarFillSprite;
    private GlyphLayout currentTextGlyph = new GlyphLayout();

    private Vector2 pos;
    private Vector2 vel;
    private Vector2 inputDir;
    private float health;
    private float shotTimer;
    private int shotTurn;
    private boolean toShoot;
    private boolean hasShot;
    private float particleTimer;
    private float combatTimer;
    private float smokeTimer;
    private boolean atHome;


    Player(Game game_, Vector2 pos_) {
        // Initialize variables
        game = game_;
        pos = pos_;
        vel = new Vector2(Vector2.Zero);
        inputDir = new Vector2(Vector2.Zero);
        health = 100;
        shotTimer = 0.0f;
        shotTurn = 0;
        toShoot = false;
        hasShot = false;
        particleTimer = 0.0f;
        combatTimer = 0.0f;
        smokeTimer = 0.0f;
        atHome = false;

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
        if (game.getRunning()) {
            handleInput();
            updateMovement();
        }
        updateLogic();
        updateSprite();
    }


    private void handleInput() {
        // Calculate input direction
        inputDir = new Vector2(0, 0);
        if (Binding.getInstance().isActionPressed("moveLeft")) inputDir.x--;
        if (Binding.getInstance().isActionPressed("moveRight")) inputDir.x++;
        if (Binding.getInstance().isActionPressed("moveDown")) inputDir.y--;
        if (Binding.getInstance().isActionPressed("moveUp")) inputDir.y++;

        // Shoot on "shoot"
        if (Binding.getInstance().isActionPressed("shoot") && shotTimer == 0.0f) { toShoot = true; hasShot = true; }
    }


    private void updateMovement() {
        // Accelerate velocity in inputDir and limit to maxSpeed
        vel.x += inputDir.x * getAcceleration() * Gdx.graphics.getDeltaTime();
        vel.y += inputDir.y * getAcceleration() * Gdx.graphics.getDeltaTime();
        if (vel.len2() > (getMaxSpeed() * getMaxSpeed())) vel = vel.setLength(getMaxSpeed());

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
        Texture targetTexture;
        if (health > 0.0f) {
            if (vel.len2() < (idleSpeed * idleSpeed)) {
                if (shotTimer > (shotTimerMax * 0.5f)) {
                    targetTexture = idleShotTextures[shotTurn];
                } else targetTexture = idleTexture;
            } else {
                if (shotTimer > (shotTimerMax * 0.5f)) {
                    targetTexture = movingShotTextures[shotTurn];
                } else targetTexture = movingTexture;
            }
        } else targetTexture = deadTexture;
        if (shipSprite.getTexture() != targetTexture) shipSprite.setTexture(targetTexture);

        // Update the ship sprite position and flip
        if (!shipSprite.isFlipX() && (vel.x < 0)
            || shipSprite.isFlipX() && (vel.x > 0)) shipSprite.flip(true, false);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());

        // Handle swaying
        float current = shipSprite.getRotation();
        float target;
        if (vel.len2() < (idleSpeed * idleSpeed)) {
            float time = (System.currentTimeMillis() - Game.startTime) / 100f;
            target = (-idleSwayMag + idleSwayMag * 2.0f * (float)Math.sin(time * idleSwayFreq)) * (2f * (float)Math.PI);
        } else target = -vel.x / getMaxSpeed() * 0.5f * (2 * (float)Math.PI);
        shipSprite.rotate((target - current) * swayAcceleration * Gdx.graphics.getDeltaTime());

        // Update health bar sprites
        healthbarBackSprite.setPosition(
                pos.x - healthbarBackSprite.getOriginX(),
                pos.y - healthbarBackSprite.getOriginY() - healthbarBackSprite.getHeight());
        healthbarFillSprite.setPosition(
                pos.x - healthbarFillSprite.getOriginX(),
                pos.y - healthbarFillSprite.getOriginY() - healthbarBackSprite.getHeight());
        healthbarFillSprite.setScale(health / maxHealth, 1.0f);
    }

    private void updateLogic() {
        // Smoke if below half health
        if (health < (maxHealth * 0.5f)) {
            smokeTimer = Math.max(smokeTimer - Gdx.graphics.getDeltaTime(), 0f);
            if (smokeTimer == 0.0f) {
                float pSize = 0.2f * shipWidth;
                float pTime = (float)Math.random() * 0.5f + 2.0f;
                Vector2 vel = (new Vector2(0, Game.PPT * (float)Math.random() * 0.3f + 1f)).rotateDeg((float)Math.random() * 10f - 5f);
                Vector2 pPos = new Vector2(pos.x, pos.y + shipWidth * 0.15f);
                Particle particle = new Particle("rock", pPos, pSize, vel, pTime);
                game.addParticle(particle);
                smokeTimer = smokeTimerMax;
            }
        }

        if (game.getRunning()) {
            // Create particles if moving
            if (vel.len2() > (idleSpeed * idleSpeed)) {
                particleTimer = Math.max(particleTimer - Gdx.graphics.getDeltaTime(), 0f);
                if (particleTimer == 0.0f) {
                    float pSizeStart = 0.0f;
                    float pSizeEnd = ((float)Math.random() * 0.3f + 0.6f) * (vel.len2() / (getMaxSpeed() * getMaxSpeed())) * shipWidth;
                    float pTime = (float)Math.random() * 0.5f + 2.0f;
                    Particle particle = new Particle("splash", new Vector2(pos), pSizeStart, pSizeEnd, 0.0f, pTime);
                    game.addParticle(particle);
                    particleTimer = particleTimerMax;
                }

                // Reset particle timer if not moving
            } else particleTimer = 0.0f;

            // Shoot if needed
            if (toShoot) {
                Vector2 dir = game.getWorldMousePos().sub(pos);
                Vector2 newPos = new Vector2(pos.x, pos.y + shipWidth * 0.1f);
                Projectile projectile = new Projectile(game, this, newPos, dir.nor(), true);
                game.addProjectile(projectile);
                shotTimer = getShotTimerMax();
                shotTurn = (shotTurn + 1) % shotCount;
                combatTimer = combatTimerMax;
                toShoot = false;
            }

            // Update timers
            shotTimer = Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0);
            combatTimer = Math.max(combatTimer - Gdx.graphics.getDeltaTime(), 0f);

            // Regen health passively
            if (combatTimer == 0f) health += passiveHealthRegen * Gdx.graphics.getDeltaTime();

            // Regen faster if at home
            ArrayList<College> colleges = game.getColleges();
            atHome = false;
            for (College c : colleges) {
                if (c.getFriendly()) {
                    Vector2 dir = new Vector2(c.getPosition()).sub(pos);
                    if (dir.len2() < (regenRange * regenRange)) atHome = true;
                }
            }
            if (atHome) health += homeHealthRegen * Gdx.graphics.getDeltaTime();

            // Limit to max
            health = Math.min(health, maxHealth);
        }
    }


    public void render(SpriteBatch batch) {
        // Draw ship
        shipSprite.draw(batch);
        healthbarBackSprite.draw(batch);
        healthbarFillSprite.draw(batch);

        // Render health regen for at home or passive
        if (atHome && health < maxHealth) {
            Game.mainFont.getData().setScale(0.55f * Game.PPT / 128f);
            currentTextGlyph.setText(Game.mainFont, "++");
            Game.mainFont.draw(batch, "++",
                    healthbarBackSprite.getX() + healthbarBackSprite.getWidth() + 10f,
                    healthbarBackSprite.getY() + healthbarBackSprite.getHeight());
            Game.mainFont.getData().setScale(1f);

        } else if (combatTimer == 0f && health < maxHealth) {
            Game.mainFont.getData().setScale(0.55f * Game.PPT / 128f);
            currentTextGlyph.setText(Game.mainFont, "+");
            Game.mainFont.draw(batch, "+",
                    healthbarBackSprite.getX() + healthbarBackSprite.getWidth() + 10f,
                    healthbarBackSprite.getY() + healthbarBackSprite.getHeight());
            Game.mainFont.getData().setScale(1f);
        }
    }


    public void dispose() {
        // Dispose of all ship textures
        movingTexture.dispose();
        idleTexture.dispose();
        deadTexture.dispose();
        healthbarBackTexture.dispose();
        healthbarFillTexture.dispose();
    }


    private void destroy() {
        if (health != 0) return;

        // Add particles
        for (int i = 0; i < Math.random() * 3f + 4f; i++) {
            Particle particle = new Particle("wood", new Vector2(pos), Game.PPT * 0.1f, Game.PPT * 0.5f, 0.7f);
            game.addParticle(particle);
        }

        // Alert game of death
        game.loseGame();
    }


    public Vector2 getPosition() { return pos; }

    public boolean getIsMoving() { return inputDir.len2() > 0f; }

    public boolean getHasShot() { return hasShot; }

    private float getMaxSpeed() { return maxSpeed + (game.currentLevel - 1) * maxSpeedScale; }

    private float getAcceleration() { return acceleration + (game.currentLevel - 1) * accelerationScale; }

    private float getShotTimerMax() { return shotTimerMax + (game.currentLevel - 1) * shotTimerMaxScale; }


    @Override
    public Rectangle getCollisionRect() {
        // Calculate collision rectangle
        return new Rectangle(
            pos.x - shipWidth * 0.4f, pos.y,
            shipWidth * 0.8f, shipWidth * 0.2f
        );
    }

    @Override
    public boolean damage(float damage) {
        health = (float)Math.max(health - damage, 0.0f);
        combatTimer = combatTimerMax;
        if (health == 0f) destroy();
        return true;
    }

    @Override
    public boolean getFriendly() { return true; }
}
