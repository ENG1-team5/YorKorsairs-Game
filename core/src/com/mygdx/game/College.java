
package com.mygdx.game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Class to represent a college that is hittable by the player while being hostile to them
 */
public class College implements IHittable {

    // Declare config, variables
    private static ArrayList<String> implemented = new ArrayList<String>() {
        {
            add("goodricke");
            add("constantine");
            add("langwith");
            add("annelister");
            add("vanbrugh");
            add("evilgoodricke");
        }
    };
    private static Texture healthbarBackTexture;
    private static Texture healthbarFillTexture;
    public final float collegeWidth = Game.PPT * 2.5f;
    private final float smokeTimerMax = 0.1f;
    private final float shootRange = Game.PPT * 6.5f;
    
    public String name;
    private Game game;
    private Texture collegeTexture;
    private Texture collegeShotTexture;
    private Texture collegeDeadTexture;
    private Sprite collegeSprite;
    private Sprite healthbarBackSprite;
    private Sprite healthbarFillSprite;
    private GlyphLayout currentTextGlyph = new GlyphLayout();

    public Vector2 pos;
    private float maxHealth = 300f;
    public float health;
    public boolean isFriendly;
    private float shotTimerMax = 0.8f;
    private float shotTimer;
    private float smokeTimer;
    private boolean testing;
    /**
     * Instantiate a new college object
     * @param name_ - Name of the college, used to find appropriate images (use lower case)
     * @param game_ - game that college belongs to
     * @param pos_ -  position to spawn the college at
     * @param isFriendly_ - Value representing if the college is friendly to the player 
     */
    public College(String name_, Game game_, Vector2 pos_, boolean isFriendly_, boolean testing){
        // Initialize variables
        name = name_;
        game = game_;
        pos = pos_;
        health = maxHealth;
        isFriendly = isFriendly_;
        shotTimer = 0f;
        smokeTimer = 0f;
        this.testing = testing;
    }

    public College(String name_, Game game_, Vector2 pos_, boolean isFriendly_) {
        this(name_, game_, pos_, isFriendly_, false);
        initialiseTextures();
    }

    public void initialiseTextures(){
        healthbarBackTexture = new Texture(Gdx.files.internal("UI/healthbarBack.png"));
        healthbarFillTexture = new Texture(Gdx.files.internal("UI/healthbarFill.png"));
        // Initialize textures
        String path = name.toLowerCase();
        if (!implemented.contains(path))
            path = "goodricke";
        collegeTexture = new Texture(Gdx.files.internal("colleges/" + path + ".png"));
        collegeDeadTexture = new Texture(Gdx.files.internal("colleges/" + path + "Dead.png"));
        collegeShotTexture = new Texture(Gdx.files.internal("colleges/" + path + "Shot.png"));

        // Initialize sprite
        float ratio = (float) collegeTexture.getHeight() / (float) collegeTexture.getWidth();
        collegeSprite = new Sprite(collegeTexture);
        collegeSprite.setSize(collegeWidth, collegeWidth * ratio);
        collegeSprite.setOrigin(collegeSprite.getWidth() * 0.5f, 0);
        collegeSprite.setPosition(pos.x - collegeSprite.getOriginX(), pos.y - collegeSprite.getOriginY());

        // Initialize health bar sprites
        float backRatio = (float) healthbarBackTexture.getHeight() / healthbarBackTexture.getWidth();
        float backWidth = collegeWidth * 0.8f;
        float backHeight = backWidth * backRatio;
        float pixelSize = backHeight / 12f;
        float fillWidth = backWidth - (pixelSize * 3) * 2;
        float fillHeight = backHeight - (pixelSize * 2) * 2;
        healthbarBackSprite = new Sprite(healthbarBackTexture);
        healthbarBackSprite.setSize(backWidth, backHeight);
        healthbarBackSprite.setOrigin(backWidth * 0.5f, backHeight * 0.5f);
        healthbarBackSprite.setPosition(
                pos.x - healthbarBackSprite.getOriginX(),
                pos.y - healthbarBackSprite.getOriginY());
        healthbarFillSprite = new Sprite(healthbarFillTexture);
        healthbarFillSprite.setSize(fillWidth, fillHeight);
        healthbarFillSprite.setOrigin(fillWidth * 0.5f, fillHeight * 0.5f);
        healthbarFillSprite.setPosition(
                pos.x - healthbarFillSprite.getOriginX(),
                pos.y - healthbarFillSprite.getOriginY());
    }

    /**
     * Runs update functions
     */
    public void update() {
        updateAI();
        if (!testing){
            updateParticles();
            updateSprite();
        }
    }

    /**
     * Updates AI to check for player ship and shoot
     */
    public void updateAI() {
        // Don't update AI if dead
        if (health == 0f)
            return;

        // Get direction to player
        if (game.getRunning()) {
            if (!isFriendly) {
                Player player = game.getPlayer();
                Vector2 dir = new Vector2(player.getPosition()).sub(pos);

                // Check whether player in range
                if (dir.len2() < (shootRange * shootRange)) {
                    if (shotTimer == 0.0f) {
                        //Testing mode values
                        Vector2 pos1 = new Vector2(pos)
                            .add(new Vector2(-320 * 0.45f, 320 * 0.65f));
                        Vector2 pos2 = new Vector2(pos)
                            .add(new Vector2(320 * 0.45f, 320 * 0.65f));
                        if (!testing){ //If not testin overwrite pos with accurate
                            pos1 = new Vector2(pos)
                                .add(new Vector2(-collegeSprite.getWidth() * 0.45f, collegeSprite.getHeight() * 0.65f));
                            pos2 = new Vector2(pos)
                                .add(new Vector2(collegeSprite.getWidth() * 0.45f, collegeSprite.getHeight() * 0.65f));
                        }
                        
                        Vector2 vel1 = new Vector2(player.getPosition()).sub(pos1);
                        Vector2 vel2 = new Vector2(player.getPosition()).sub(pos2);
                        //Testing mode projectiles
                        
                        if (!testing){ // If not testing overwrite with different non testing projectiles
                            Projectile p1 = new Projectile(game, this, pos1, vel1.nor(), false);
                            Projectile p2 = new Projectile(game, this, pos2, vel2.nor(), false);
                            game.addProjectile(p1);
                            game.addProjectile(p2);
                        }
                        else{
                            Projectile p1 = new Projectile(game, this, pos1, vel1.nor(), false,true);
                            Projectile p2 = new Projectile(game, this, pos2, vel2.nor(), false,true);
                            game.addProjectile(p1);
                            game.addProjectile(p2);
                        }
                        shotTimer = shotTimerMax;
                    }
                }
            }
        }

        if (!testing){
            // Update shot timer
            shotTimer = (float) Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0.0f);
        }
        else{
            shotTimer = 0.f;
        }
    }

    /**
     * creates particles based on movement and projectiles
     */
    private void updateParticles() {
        // Create smoke particles
        if (health != 0f)
            return;
        if (smokeTimer == 0.0f) {
            Particle particle = new Particle(
                    "rock",
                    new Vector2(pos).add(new Vector2(0f, collegeSprite.getHeight() * 0.5f)),
                    Game.PPT * 0.1f,
                    0f,
                    new Vector2(0, Game.PPT * ((float) Math.random() * 0.1f + 0.3f))
                            .rotateDeg((float) Math.random() * 10f - 5f),
                    (float) Math.random() * 0.3f + 3f);
            game.addParticle(particle);
            smokeTimer = smokeTimerMax;
        }
        smokeTimer = Math.max(smokeTimer - Gdx.graphics.getDeltaTime(), 0f);
    }

    /**
     * changes image when shooting, updates health bar when hit
     */
    private void updateSprite() {
        // Change sprite based on shotTimer
        if (health != 0f) {
            if (shotTimer < shotTimerMax * 0.2f) {
                if (collegeSprite.getTexture() != collegeTexture)
                    collegeSprite.setTexture(collegeTexture);
            } else if (collegeSprite.getTexture() != collegeShotTexture)
                collegeSprite.setTexture(collegeShotTexture);
        } else if (collegeSprite.getTexture() != collegeDeadTexture)
            collegeSprite.setTexture(collegeDeadTexture);

        // Update health bar sprite
        healthbarFillSprite.setScale(health / maxHealth, 1.0f);
        if (isFriendly)
            healthbarFillSprite.setColor(0.6f, 0.6f, 1f, 1f);
    }

    /**
     * Renders colleges and name text to screen
     * @param batch graphical output
     */
    public void render(SpriteBatch batch) {
        // Draw college to screen
        collegeSprite.draw(batch);
        healthbarBackSprite.draw(batch);
        healthbarFillSprite.draw(batch);

        // Render name text
        Game.mainFont.getData().setScale(0.8f);
        Game.mainFont.getData().setScale(0.16f * Game.PPT / 16f);
        currentTextGlyph.setText(Game.mainFont, name);
        Game.mainFont.draw(batch, name, pos.x - currentTextGlyph.width * 0.5f, pos.y - currentTextGlyph.height);
        if (getFriendly()) {
            currentTextGlyph.setText(Game.mainFont, "(home)");
            Game.mainFont.draw(batch, "(home)", pos.x - currentTextGlyph.width * 0.5f,
                    pos.y - currentTextGlyph.height * 2.4f);
        }
        Game.mainFont.getData().setScale(1f);
    }


    /**
     * Deletes college sprites to conserve processor if dead
     */
    public void dispose() {
        collegeTexture.dispose();
        collegeDeadTexture.dispose();
        collegeShotTexture.dispose();
    }

    /**
     * Deletes college static sprites to conserve processor if dead
     */
    public static void staticDispose() {
        healthbarBackTexture.dispose();
        healthbarFillTexture.dispose();
    }


    /**
     * check is college is destroyed, gives player gold and XP and has a 50% chance of spawning a random weather event
     */
    private void destroy() {
        // Become destroyed
        if (health != 0f)
            return;

        if (!testing){
            // Add particles
            for (int i = 0; i < Math.random() * 3f + 12f; i++) {
                Particle particle = new Particle(
                    "rock",
                    new Vector2(pos),
                    Game.PPT * 0.1f,
                    Game.PPT * ((float) Math.random() * 0.2f + 0.5f),
                    0.7f);
                game.addParticle(particle);
            }
        }
        

        // Give gold and XP
        if (!getFriendly()) {
            game.addResources(
                    50 + (int) Math.floor((float) Math.random() * 15),
                    15 + (int) Math.floor((float) Math.random() * 10));
        }
        if(new Random().nextInt(2) == 1 && !testing){
            game.addRandomWeather();
        }
    }


    /**
     *
     * @return Vector2 pos returns pos of college
     */
    public Vector2 getPosition() {
        return pos;
    }

    /**
     * returns health of college if 0
     * @return health
     */
    public boolean getAlive() {
        return health != 0f;
    }


    /**
     * college receives certain amount of damage
     * @param damage amount of damage taken
     * @return boolean true if successful
     */
    @Override
    public boolean damage(float damage) {
        if (health == 0f)
            return false;
        health = Math.max(health - damage, 0);
        if (health == 0f)
            destroy();
        return true;
    }

    /**
     * returns rect representing collision bounds
     * @return Rectangle
     */
    @Override
    public Rectangle getCollisionRect() {
        if (!testing){
            return new Rectangle(
                pos.x - collegeSprite.getOriginX(),
                pos.y - collegeSprite.getOriginY() + collegeSprite.getHeight() * 0.2f,
                collegeSprite.getWidth(), collegeSprite.getHeight() * 0.5f);
        }
        else{
            //programmed using premeasured sizes, not the best...
            return new Rectangle(pos.x - 160, pos.y - 0, 320, (320 * 0.5f));
        }
        
    }

    /**
     * return if college is friendly
     * @return boolean
     */
    @Override
    public boolean getFriendly() {
        return isFriendly;
    }
    
    public float getMaxHealth(){
        return maxHealth;
    }

    public void setHealth(float value){
        // If at maxHealth, raise it to new maxHealth
        if (health == maxHealth){
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
