
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Class to represent enemy ships within the game that are hostile to the player with basic movement
 */
public class Enemy implements IHittable{

    // Declare config, variables
    private static Texture shipTexture;
    
    private static Texture healthbarBackTexture;
    private static Texture healthbarFillTexture;

    private final float shipWidth = Game.PPT * 1.4f;
    private final float maxSpeed = Game.PPT * 1f;
    private final float acceleration = Game.PPT * 7f; // Units / Second^2
    private final float idleSwayMag = 0.16f;
    private final float idleSwayFreq = 0.2f;
    private final float swayAcceleration = 20f;
    private final float shootRange = Game.PPT * 6.5f;
    private final float followRange = Game.PPT * 8f;
    private final float smokeTimerMax = 0.1f;

    private Game game;
    private Sprite shipSprite;
    private Sprite healthbarBackSprite;
    private Sprite healthbarFillSprite;
    
    private float maxHealth = 45;
    public float health;

    private boolean toRemove;

    public Vector2 pos;
    private Vector2 vel;
    private Vector2 inputDir;

    private float shotTimerMax = 3f;
    public float shotTimer = shotTimerMax;
    private float smokeTimer = smokeTimerMax;
    
    private boolean testing;

    /**
     * Instantiates a new enemy at a given position
     * @param game_ - Game that enemy belongs to
     * @param pos_ - Position to add enemy in
     */
    public Enemy(Game game_, Vector2 pos_, boolean testing) {
        // Initialize variables
        game = game_;
        pos = pos_;
        health = maxHealth;
        vel = new Vector2(0f, 0f);
        this.testing = testing;
        inputDir = new Vector2(0f, 0f);
    }

    Enemy(Game game_, Vector2 pos) {
        this(game_, pos, false);
        initialiseTextures();
    }

    public void initialiseTextures(){
        shipTexture = new Texture(Gdx.files.internal("ships/enemy.png"));
        healthbarBackTexture = new Texture(Gdx.files.internal("UI/healthbarBack.png"));
        healthbarFillTexture = new Texture(Gdx.files.internal("UI/healthbarFill.png"));

        // Setup sprite
        shipSprite = new Sprite(shipTexture);
        shipSprite.setSize(shipWidth, shipWidth * shipTexture.getHeight() / shipTexture.getWidth());
        shipSprite.setOrigin(shipSprite.getWidth() * 0.5f, 0f);

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
     * updates enemy movement, swaying and collision
     */
    public void update() {

        Player player = game.getPlayer();
        Vector2 dir = new Vector2(player.getPosition()).sub(pos);
        // Calculate input movement direction
        // If in range, move towards player
        if (dir.len2() < (followRange * followRange)){
            inputDir.x = player.pos.x - pos.x;
            inputDir.y = player.pos.y - pos.y;
        }
        else{
        // else move randomly
            if (Math.random() < Gdx.graphics.getDeltaTime() * 1f) {
                inputDir.x = (float) Math.random() * 2f - 1f;
                inputDir.y = (float) Math.random() * 2f - 1f;
            }
        }
        inputDir = inputDir.nor();
        
        

        // Update velocity
        if (!testing){
            vel.x += inputDir.x * acceleration * Gdx.graphics.getDeltaTime();
            vel.y += inputDir.y * acceleration * Gdx.graphics.getDeltaTime();
        }
        else{
            vel.x += inputDir.x * acceleration;
            vel.y += inputDir.y * acceleration;
        }
        if (vel.len2() > (maxSpeed * maxSpeed))
            vel = vel.setLength(maxSpeed);

        // Update movement
        float diffX = vel.x;
        float diffY = vel.y;
        if (!testing){
            diffX = vel.x * Gdx.graphics.getDeltaTime();
            diffY = vel.y * Gdx.graphics.getDeltaTime();
        }
        
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

        if (!testing){
            // Handle swaying
            float current = shipSprite.getRotation();
            float target;
            target = -vel.x / maxSpeed * 0.5f * (2 * (float) Math.PI);
            shipSprite.rotate((target - current) * swayAcceleration * Gdx.graphics.getDeltaTime());

            // Update sprite
            shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());
            if (!shipSprite.isFlipX() && (vel.x < 0)
                    || shipSprite.isFlipX() && (vel.x > 0))
                shipSprite.flip(true, false);
            shipSprite.setPosition(pos.x - shipSprite.getOriginX(), pos.y - shipSprite.getOriginY());

            //Update health bar
            healthbarBackSprite.setPosition(
                 pos.x - healthbarBackSprite.getOriginX(),
                pos.y - healthbarBackSprite.getOriginY() - healthbarBackSprite.getHeight());
            healthbarFillSprite.setPosition(
             pos.x - healthbarFillSprite.getOriginX(),
                pos.y - healthbarFillSprite.getOriginY() - healthbarBackSprite.getHeight());
            healthbarFillSprite.setScale(health / maxHealth, 1.0f);
        }

        // Check whether player in range and shoot at them
        if (dir.len2() < (shootRange * shootRange)) {
            if (shotTimer == 0.0f) {
                Vector2 projPos = new Vector2(pos.x, pos.y + shipWidth * 0.1f);
                Vector2 projVel = new Vector2(player.getPosition()).sub(projPos);
                
                if (!testing){ //If not testing overwrite with a non-testing varient of projectile
                    Projectile proj = new Projectile(game, this, projPos, projVel.nor(), false);
                    game.addProjectile(proj);
                }
                else{   
                    Projectile proj = new Projectile(game, this, projPos, new Vector2(-1,0), false, true);
                    game.addProjectile(proj);
                }
                
                
                shotTimer = shotTimerMax;
            }
        }

        if (!testing){
            // Update shot timer
            shotTimer = (float) Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0.0f);
        }
        else{shotTimer = 0.0f;}
        

        if (!testing){
            // Smoke if below half health
            if (health < (maxHealth * 0.5f)) {
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
 
    }
    

    /**
     * Renders sprite to screen
     * @param batch graphical output to be rendered to
     */
    public void render(SpriteBatch batch) {
        shipSprite.draw(batch);
        
        healthbarBackSprite.draw(batch);
        healthbarFillSprite.draw(batch);
    }


    /**
     * Deletes enemy sprites to conserve processor if dead
     */
    public static void staticDispose() {
        shipTexture.dispose();
    }


    /**
     * Calculates and returns collision rectangle
     * @return Rectangle
     */
    public Rectangle getCollisionRect() {
        // Calculate collision rectangle
        return new Rectangle(
                pos.x - shipWidth * 0.4f, pos.y,
                shipWidth * 0.8f, shipWidth * 0.2f);
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
        

        if (!getFriendly()) {
            game.addResources(
                    10 + (int) Math.floor((float) Math.random() * 10),
                    5 + (int) Math.floor((float) Math.random() * 5));
        }

        toRemove = true;
    }

    /**
     * enemy ship receives certain amount of damage
     * @param damage amount of damage taken
     * @return boolean true if successful
     */
    @Override
    public boolean damage(float damage) {
        health = (float) Math.max(health - damage, 0.0f);
        if (health == 0f){
            destroy();
            return true;
        }
        return false;
    }

    /**
     * @return false, as all enemy ships are not friendly
     */
    @Override
    public boolean getFriendly() {
        return false;
    }

    public boolean shouldRemove() {
        return toRemove;
    }


    public float getMaxHealth(){
        return maxHealth;
    }

    public void setHealth(float value){
        // If they are at max health, change it to the new max health
        if (maxHealth == health){
            health = value;
        }
        maxHealth = value;
        
    }

    public float getShotTimerMax(){
        return shotTimerMax;
    }

    public void setShotTimerMax(float value){
        shotTimerMax = value;
    }
}