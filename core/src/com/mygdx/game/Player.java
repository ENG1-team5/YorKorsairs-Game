
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the player's boat in the game, handling all colisions, input and firing
 */
public class Player implements IHittable {

    // Declare static, config, variables
    private static Texture idleTexture;
    private static Texture[] idleShotTextures;
    private static Texture movingTexture;
    private static Texture[] movingShotTextures;
    private static Texture deadTexture;
    private static Texture healthbarBackTexture;
    private static Texture healthbarFillTexture;

    public final float shipWidth = Game.PPT * 1.4f;

    private final float maxSpeed = Game.PPT * 4f; // Units / Second
    private final float maxSpeedScale = Game.PPT * 0.5f;
    private final float acceleration = Game.PPT * 7f; // Units / Second^2
    private final float accelerationScale = Game.PPT * 1f;
    private final float friction = 0.985f;

    private final float idleSpeed = Game.PPT * 0.55f;
    private final float idleSwayMag = 0.16f;
    private final float idleSwayFreq = 0.2f;
    private final float swayAcceleration = 20f;

    public float maxHealth = 100;
    private float passiveHealthRegen = 2.5f;
    private final float regenRange = Game.PPT * 5f;

    private final int shotCount = 4;
    private float shotTimerMax = 0.55f;
    private float shotTimerMaxScale = -0.1f;
    private float shotDamage = 15f;
    public float shotSpeed = Game.PPT * 3f;

    private final float combatTimerMax = 2.0f;
    private final float particleTimerMax = 0.4f;
    private final float smokeTimerMax = 0.1f;

    public List<Buff> buffs;

    private Game game;
    private Sprite shipSprite;
    private Sprite healthbarBackSprite;
    private Sprite healthbarFillSprite;
    private GlyphLayout currentTextGlyph = new GlyphLayout();

    public Vector2 pos;
    private Vector2 vel;
    public Vector2 inputDir;
    public float health;
    private float shotTimer;
    private int shotTurn;
    private boolean toShoot;
    private boolean hasShot;

    private float particleTimer;
    public float combatTimer;
    private float smokeTimer;
    private boolean atHome;

    private boolean testing;
    
    private float homeHealthRegen = passiveHealthRegen * 3;
    public boolean toInteract;

    /**
     * Part player(game, pos) constructor changed for accomodating testing
     * @param game_
     * @param pos_
     * @param testing
     */
    public Player(Game game_, Vector2 pos_, boolean testing) {
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
        this.testing = testing;
        buffs = new ArrayList<Buff>();
    }

    /**
    * Instantiates a player belonging to game class at position pos_
    * @param game_ - Game that the player belongs to
    * @param pos_  - Where to spawn the player
    */
    public Player(Game game_, Vector2 pos_){
        this(game_, pos_, false);
        initialiseTextures();
    }

    public void initialiseTextures(){
        idleTexture = new Texture(Gdx.files.internal("ships/ship.png"));
        idleShotTextures = new Texture[] {
            new Texture(Gdx.files.internal("ships/shipShot0.png")),
            new Texture(Gdx.files.internal("ships/shipShot1.png")),
            new Texture(Gdx.files.internal("ships/shipShot2.png")),
            new Texture(Gdx.files.internal("ships/shipShot3.png")) 
        };
        movingTexture = new Texture(Gdx.files.internal("ships/shipMoving.png"));
        movingShotTextures = new Texture[] {
            new Texture(Gdx.files.internal("ships/shipMovingShot0.png")),
            new Texture(Gdx.files.internal("ships/shipMovingShot1.png")),
            new Texture(Gdx.files.internal("ships/shipMovingShot2.png")),
            new Texture(Gdx.files.internal("ships/shipMovingShot3.png")) 
        };
        deadTexture = new Texture(Gdx.files.internal("ships/shipDead.png"));
        healthbarBackTexture = new Texture(Gdx.files.internal("UI/healthbarBack.png"));
        healthbarFillTexture = new Texture(Gdx.files.internal("UI/healthbarFill.png"));

        // Initialize ship sprite
        float ratio = (float) idleTexture.getHeight() / (float) idleTexture.getWidth();
        shipSprite = new Sprite(idleTexture);
        shipSprite.setSize(shipWidth, shipWidth * ratio);
        shipSprite.setOrigin(shipSprite.getWidth() * 0.5f, 0);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());

        // Initialize health bar sprites
        float backRatio = (float) healthbarBackTexture.getHeight() / healthbarBackTexture.getWidth();
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

    /**
     * Updates position, velocity, input and sprite
     */
    public void update() {
        if (game.getRunning()) {
            if (!testing){
                handleInput();
                updateMovement(inputDir);
            }
        }
        if (!testing){
            updateLogic();
            updateSprite();

            // Update buffs
            // todo: buffs nullify themself but it may be worth cleaning them up
    
            for (Buff buff : buffs) {
                buff.update();
            }
        }   
    }

    /**
     * detects input and keeps track
     */
    private void handleInput() {
        // Calculate input direction
        inputDir = new Vector2(0, 0);
        if (Binding.getInstance().isActionPressed("moveLeft"))
            inputDir.x--;
        if (Binding.getInstance().isActionPressed("moveRight"))
            inputDir.x++;
        if (Binding.getInstance().isActionPressed("moveDown"))
            inputDir.y--;
        if (Binding.getInstance().isActionPressed("moveUp"))
            inputDir.y++;

        // Shoot on "shoot"
        if (Binding.getInstance().isActionPressed("shoot") && shotTimer == 0.0f) {
            toShoot = true;
            hasShot = true;
        }

        if (Binding.getInstance().isActionPressed("interact")) {
            toInteract = true;
        } else {
            toInteract = false;
        }
    }

    /**
     * uses input to move and detect collision
     * @param inputDirection
     */
    public void updateMovement(Vector2 inputDirection) {
        // Accelerate velocity in inputDir and limit to maxSpeed
        vel.x += inputDirection.x * getAcceleration() * 0.015;
        vel.y += inputDirection.y * getAcceleration() * 0.015;
        if (vel.len2() > (getMaxSpeed() * getMaxSpeed()))
            vel = vel.setLength(getMaxSpeed());

        // Move ship with velocity
        float diffX = vel.x * 0.015f;
        float diffY = vel.y * 0.015f;
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

    /**
     * updates sprite image and health bar display
     */
    private void updateSprite() {
        // Update sprite based on whether moving
        Texture targetTexture;
        if (health > 0.0f) {
            if (vel.len2() < (idleSpeed * idleSpeed)) {
                if (shotTimer > (getShotTimerMax() * 0.5f)) {
                    targetTexture = idleShotTextures[shotTurn];
                } else
                    targetTexture = idleTexture;
            } else {
                if (shotTimer > (getShotTimerMax() * 0.5f)) {
                    targetTexture = movingShotTextures[shotTurn];
                } else
                    targetTexture = movingTexture;
            }
        } else
            targetTexture = deadTexture;
        if (shipSprite.getTexture() != targetTexture)
            shipSprite.setTexture(targetTexture);

        // Update the ship sprite position and flip
        if (!shipSprite.isFlipX() && (vel.x < 0)
                || shipSprite.isFlipX() && (vel.x > 0))
            shipSprite.flip(true, false);
        shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());

        // Handle swaying
        float current = shipSprite.getRotation();
        float target;
        if (vel.len2() < (idleSpeed * idleSpeed)) {
            float time = (System.currentTimeMillis() - Game.startTime) / 100f;
            target = (-idleSwayMag + idleSwayMag * 2.0f * (float) Math.sin(time * idleSwayFreq))
                    * (2f * (float) Math.PI);
        } else
            target = -vel.x / getMaxSpeed() * 0.5f * (2 * (float) Math.PI);
        shipSprite.rotate((target - current) * swayAcceleration * Gdx.graphics.getDeltaTime());

        // Update health bar sprites
        healthbarBackSprite.setPosition(
                pos.x - healthbarBackSprite.getOriginX(),
                pos.y - healthbarBackSprite.getOriginY() - healthbarBackSprite.getHeight());
        healthbarFillSprite.setPosition(
                pos.x - healthbarFillSprite.getOriginX(),
                pos.y - healthbarFillSprite.getOriginY() - healthbarBackSprite.getHeight());
        healthbarBackSprite.setScale(getMaxHealth() / 100f, 1.0f);
        healthbarFillSprite.setScale(health / 100f, 1.0f);
    }

    /**
     * updates health regen, shoot if needed, and timers
     */
    public void updateLogic() {
        // Smoke if below half health
        if (!testing){
            if (health < (getMaxHealth() * 0.5f)) {
            smokeTimer = Math.max(smokeTimer - Gdx.graphics.getDeltaTime(), 0f);
            if (smokeTimer == 0.0f) {
                float pSize = 0.2f * shipWidth;
                float pTime = (float) Math.random() * 0.5f + 2.0f;
                Vector2 vel = (new Vector2(0, Game.PPT * (float) Math.random() * 0.3f + 1f))
                        .rotateDeg((float) Math.random() * 10f - 5f);
                Vector2 pPos = new Vector2(pos.x, pos.y + shipWidth * 0.15f);
                Particle particle = new Particle("rock", pPos, pSize, vel, pTime);
                game.addParticle(particle);
                smokeTimer = smokeTimerMax;
                }
            }
        }
        if (game.getRunning()) {
            // Create particles if moving
            if (!testing){
                if (vel.len2() > (idleSpeed * idleSpeed)) {
                    particleTimer = Math.max(particleTimer - Gdx.graphics.getDeltaTime(), 0f);
                    if (particleTimer == 0.0f) {
                        float pSizeStart = 0.0f;
                        float pSizeEnd = ((float) Math.random() * 0.3f + 0.6f)
                                * (vel.len2() / (getMaxSpeed() * getMaxSpeed())) * shipWidth;
                        float pTime = (float) Math.random() * 0.5f + 2.0f;
                        Particle particle = new Particle("splash", new Vector2(pos), pSizeStart, pSizeEnd, 0.0f, pTime);
                        game.addParticle(particle);
                        particleTimer = particleTimerMax;
                    }

                     // Reset particle timer if not moving
                } else
                    particleTimer = 0.0f;

                // Shoot if needed
                if (toShoot) {
                    Vector2 dir = game.getWorldMousePos().sub(pos);
                    Vector2 newPos = new Vector2(pos.x, pos.y + shipWidth * 0.1f);
                    Projectile projectile = new Projectile(game, this, newPos, dir.nor(), true, getDamage(), getProjectileSpeed());
                    fire(projectile);
                }
            }
            
                // Interact if needed
                if (toInteract) {
                    IInteractable inter = game.checkForInteractables(getCollisionRect());
                    if (inter != null) {
                        inter.onInteraction();
                    }
                }

                

            if (!testing){
                // Update timers
                shotTimer = Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0f);
                combatTimer = Math.max(combatTimer - Gdx.graphics.getDeltaTime(), 0f);
            }
            // Regen health passively
            if (!testing){
                if (combatTimer == 0f){
                    health += getPassiveHealthRegen() * Gdx.graphics.getDeltaTime();
                }
            }
            else{
                if (combatTimer == 0f){
                    health += getPassiveHealthRegen();
                }
            }
            // Regen faster if at home
            ArrayList<College> colleges = game.getColleges();
            atHome = false;
            for (College c : colleges) {
                if (c.getFriendly()) {
                    Vector2 dir = new Vector2(c.getPosition()).sub(pos);
                    if (dir.len2() < (regenRange * regenRange))
                        atHome = true;
                }
            }
            if (atHome){
                if (!testing){
                    health += (getPassiveHealthRegen() + homeHealthRegen) * Gdx.graphics.getDeltaTime();
                }
                else{
                    health += (getPassiveHealthRegen() + homeHealthRegen);
                }
            }
            
            // Limit to max
            health = Math.min(health, getMaxHealth());
        }
    }

    /**
     * Fires a cannonball at a location
     */
    public void fire(Projectile projectile ){
        game.addProjectile(projectile);
        shotTimer = getShotTimerMax();
        shotTurn = (shotTurn + 1) % shotCount;
        combatTimer = combatTimerMax;
        toShoot = false;
    }

    /**
     * renders in main ship sprite and health bar
     * @param batch graphical output to be rendered to
     */
    public void render(SpriteBatch batch) {
        // Draw ship
        shipSprite.draw(batch);
        healthbarBackSprite.draw(batch);
        healthbarFillSprite.draw(batch);

        // Render health regen for at home or passive
        if (atHome && health < getMaxHealth() && passiveHealthRegen != 0f) {
            Game.mainFont.getData().setScale(0.55f * Game.PPT / 128f);
            currentTextGlyph.setText(Game.mainFont, "++");
            Game.mainFont.draw(batch, "++",
                    healthbarBackSprite.getX() + healthbarBackSprite.getWidth() + 10f,
                    healthbarBackSprite.getY() + healthbarBackSprite.getHeight());
            Game.mainFont.getData().setScale(1f);
        } else if (combatTimer == 0f && health < getMaxHealth() && passiveHealthRegen != 0f) {
            Game.mainFont.getData().setScale(0.55f * Game.PPT / 128f);
            currentTextGlyph.setText(Game.mainFont, "+");
            Game.mainFont.draw(batch, "+",
                    healthbarBackSprite.getX() + healthbarBackSprite.getWidth() + 10f,
                    healthbarBackSprite.getY() + healthbarBackSprite.getHeight());
            Game.mainFont.getData().setScale(1f);
        }
    }

    /**
     * Deletes player sprites to conserve processor if dead
     */
    public static void staticDispose() {
        idleTexture.dispose();
        for (Texture t : idleShotTextures)
            t.dispose();
        movingTexture.dispose();
        for (Texture t : movingShotTextures)
            t.dispose();
        deadTexture.dispose();
        healthbarBackTexture.dispose();
        healthbarFillTexture.dispose();
    }

    /**
     * destroy the player object, checking health is 0
     */
    private void destroy() {
        if (health != 0)
            return;

        if (!testing){
            // Add particles
            for (int i = 0; i < Math.random() * 3f + 4f; i++) {
                Particle particle = new Particle("wood", new Vector2(pos), Game.PPT * 0.1f, Game.PPT * 0.5f, 0.7f);
                game.addParticle(particle);
            }
        }

        // Alert game of death
        game.loseGame();
    }


    /**
     * Getter for pos
     * @return pos
     */
    public Vector2 getPosition() {
        return pos;
    }

    /**
     * Check if player is moving
     * @return boolean
     */
    public boolean getIsMoving() {
        return inputDir.len2() > 0f;
    }

    /**
     * Check if player has shot at all (used in tutorial)
     * @return boolean
     */
    public boolean getHasShot() {
        return hasShot;
    }


    /**
     * @return maxSpeed scaled based on player level and buffs
     */
    public float getMaxSpeed() {
        float speed = maxSpeed + (game.currentLevel - 1) * maxSpeedScale;

        for (Buff buff : buffs) {
            speed += buff.getSpeedBuff();
        }
        return speed;
    }

    /**
     * @return acceleration scaled based on player level and buffs
     */
    private float getAcceleration() {
        float acc = acceleration + (game.currentLevel - 1) * accelerationScale;

        for (Buff buff : buffs) {
            acc += buff.getSpeedBuff() * 3;
        }
        return acc;
    }
   /**
     * @return shotTimerMax scaled based on player level and buffs
     */
    public float getShotTimerMax() {
        float st = shotTimerMax + (game.currentLevel - 1) * shotTimerMaxScale;

        for (Buff buff : buffs) {
            st -= buff.getFireRateBuff();
        }

        return Math.max(st, 0.1f);
    }

    /**
     * Returns projectile damage adjusted for buffs
     * @return float - Representing the current damage of a projectile
     */
    public float getDamage() {
        float dmg = shotDamage;

        for (Buff buff : buffs) {
            dmg += buff.getDamageBuff();
        }

        return dmg;
    }
    
    /**
     * Returns projectile speed adjusted for buffs
     * @return float - Representing the current speed of a projectile
     */
    public float getProjectileSpeed() {
        float projspeed = shotSpeed;

        for (Buff buff : buffs) {
            projspeed += buff.getProjectileSpeedBuff();
        }

        return projspeed;
    }

    /**
     * Returns max health adjusted for buffs
     * @return maxHealth based on buffs
     */
    public float getMaxHealth() {
        float mh = maxHealth;

        for (Buff buff : buffs) {
            mh += buff.getMaxHealthBuff();
        }

        return mh;
    
    }

    /**
     * Returns passive health regen adjusted for buffs
     * @return passive health regen based on buffs
     */
    public float getPassiveHealthRegen() {
        float mh = passiveHealthRegen;

        for (Buff buff : buffs) {
            mh += buff.getRegenBuff();
        }

        return mh;
    }

    /**
     * player receives certain amount of damage
     * @param damage amount of damage taken
     * @return boolean true if successful
     */
    @Override
    public boolean damage(float damage) {
        health = (float) Math.max(health - damage, 0.0f);
        combatTimer = combatTimerMax;
        if (health == 0f)
            destroy();
        return true;
    }

    /**
     * Calculates and returns collision rectangle
     * @return Rectangle
     */
    @Override
    public Rectangle getCollisionRect() {
        return new Rectangle(
                pos.x - shipWidth * 0.4f, pos.y,
                shipWidth * 0.8f, shipWidth * 0.2f);
    }

    /**
     * return if player is friendly
     * @return boolean
     */
    @Override
    public boolean getFriendly() {
        return true;
    }

    public void setHealth(float value){
        // If at maxHealth, raise it to new maxHealth
        if (health == maxHealth){
            health = value;
        }
        maxHealth = value;
    }

    public float getHealthRegen(){
        return passiveHealthRegen;
    }

    public void setHealthRegen(float value){
        passiveHealthRegen = value;
        homeHealthRegen = passiveHealthRegen * 3;
    }
  
    public void addBuff(Buff buff) {
        buffs.add(buff);
    }

    public List<Buff> getBuffs() {
        return buffs;
    }

    public void inHazard(){

    }

    public void setVelocity(Vector2 newVel){
        vel = newVel;
    }

    public Vector2 getVelocity(){
        return vel;
    }
}

