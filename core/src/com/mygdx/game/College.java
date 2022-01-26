
package com.mygdx.game;


import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class College implements IHittable {

    // TODO:
    //  - Add collegeName to constructor
    //  - Get image of college based on college name
    //  - Might not need to cache textures as there should only be 1 of each
    // TODO:
    //  - isAlive() function on college for use by the Game class


    // Declare config, variables
    private static final Texture healthbarBackTexture = new Texture(Gdx.files.internal("./UI/healthbarBack.png"));
    private static final Texture healthbarFillTexture = new Texture(Gdx.files.internal("./UI/healthbarFill.png"));
    private final float collegeWidth = Game.PPT * 2.5f;
    private final float shotTimerMax = 0.75f;
    private final float smokeTimerMax = 0.1f;
    private final float shootRange = Game.PPT * 6.5f;
    private final float maxHealth = 200.0f;

    private String name;
    private Game game;
    private Texture collegeTexture;
    private Texture collegeShotTexture;
    private Texture collegeDeadTexture;
    private Sprite collegeSprite;
    private Sprite healthbarBackSprite;
    private Sprite healthbarFillSprite;
    private GlyphLayout currentTextGlyph = new GlyphLayout();

    private Vector2 pos;
    private float health;
    private boolean isFriendly;
    private float shotTimer;
    private float smokeTimer;


    College(String name_, Game game_, Vector2 pos_, boolean isFriendly_) {
        // Initialize variables
        name = name_;
        game = game_;
        pos = pos_;
        health = maxHealth;
        isFriendly = isFriendly_;
        shotTimer = 0f;
        smokeTimer = 0f;

        // Initialize textures
        collegeTexture = new Texture(Gdx.files.internal("./colleges/" + name + ".png"));
        collegeDeadTexture = new Texture(Gdx.files.internal("./colleges/" + name + "Dead.png"));
        collegeShotTexture = new Texture(Gdx.files.internal("./colleges/" + name + "Shot.png"));

        // Initialize sprite
        float ratio = (float)collegeTexture.getHeight() / (float)collegeTexture.getWidth();
        collegeSprite = new Sprite(collegeTexture);
        collegeSprite.setSize(collegeWidth, collegeWidth * ratio);
        collegeSprite.setOrigin(collegeSprite.getWidth() * 0.5f, 0);
        collegeSprite.setPosition(pos.x - collegeSprite.getOriginX(), pos.y- collegeSprite.getOriginY());

        // Initialize health bar sprites
        float backRatio = (float)healthbarBackTexture.getHeight() / healthbarBackTexture.getWidth();
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


    public void update() {
        // Run update functions
        updateAI();
        updateParticles();
        updateSprite();
    }

    private void updateAI() {
        // Don't update AI if dead
        if (health == 0f) return;

        // Get direction to player
        if (game.getRunning()) {
            if (!isFriendly) {
                Player player = game.getPlayer();
                Vector2 newPos = new Vector2(pos);
                newPos.y += collegeSprite.getHeight() * 0.92f;
                Vector2 dir = new Vector2(player.getPosition()).sub(newPos);

                // Check whether player in range
                if (dir.len2() < (shootRange * shootRange)) {

                    // Create projectile towards player
                    if (shotTimer == 0.0f) {
                        Projectile projectile = new Projectile(game, this, newPos, dir.nor(), false);
                        game.addProjectile(projectile);
                        shotTimer = shotTimerMax;
                    }
                }
            }
        }

        // Update shot timer
        shotTimer = (float)Math.max(shotTimer - Gdx.graphics.getDeltaTime(), 0.0f);
    }

    private void updateParticles() {
        // Create smoke particles
        if (health != 0f) return;
        if (smokeTimer == 0.0f) {
            Particle particle = new Particle(
                    "rock",
                    new Vector2(pos).add(new Vector2(0f, collegeSprite.getHeight() * 0.5f)),
                    Game.PPT * 0.1f,
                    0f,
                    new Vector2(0, Game.PPT * ((float)Math.random() * 0.1f + 0.3f)).rotateDeg((float)Math.random() * 10f - 5f),
                    (float)Math.random() * 0.3f + 3f);
            game.addParticle(particle);
            smokeTimer = smokeTimerMax;
        }
        smokeTimer = Math.max(smokeTimer - Gdx.graphics.getDeltaTime(), 0f);
    }

    private void updateSprite() {
        // Change sprite based on shotTimer
        if (health != 0f) {
            if (shotTimer < shotTimerMax * 0.2f) {
                if (collegeSprite.getTexture() != collegeTexture) collegeSprite.setTexture(collegeTexture);
            } else if (collegeSprite.getTexture() != collegeShotTexture) collegeSprite.setTexture(collegeShotTexture);
        } else if (collegeSprite.getTexture() != collegeDeadTexture) collegeSprite.setTexture(collegeDeadTexture);

        // Update health bar sprite
        healthbarFillSprite.setScale(health / maxHealth, 1.0f);
        if (isFriendly) healthbarFillSprite.setColor(0.6f, 0.6f, 1f, 1f);
    }


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
            Game.mainFont.draw(batch, "(home)", pos.x - currentTextGlyph.width * 0.5f, pos.y - currentTextGlyph.height * 2.4f);
        }
        Game.mainFont.getData().setScale(1f);
    }


    public void dispose() {
        // Dispose of ship texture afterwards
        collegeSprite.getTexture().dispose();
    }


    private void destroy() {
        // Become destroyed
        if (health != 0f) return;

        // Add particles
        for (int i = 0; i < Math.random() * 3f + 12f; i++) {
            Particle particle = new Particle(
                "rock",
                new Vector2(pos),
                Game.PPT * 0.1f,
                Game.PPT * ((float)Math.random() * 0.2f + 0.5f),
                0.7f);
            game.addParticle(particle);
        }

        // Give gold and XP
        if (!getFriendly()) {
            game.addResources(
                50 + (int)Math.floor((float)Math.random() * 15),
                15 + (int)Math.floor((float)Math.random() * 10)
            );
        }
    }


    public boolean getAlive() {
        // Return if alive
        return health != 0f;
    }


    @Override
    public Rectangle getCollisionRect() {
        return new Rectangle(
            pos.x - collegeSprite.getOriginX(),
            pos.y - collegeSprite.getOriginY() + collegeSprite.getHeight() * 0.2f,
            collegeSprite.getWidth(), collegeSprite.getHeight() * 0.5f
        );
    }

    @Override
    public boolean getFriendly() {
        return isFriendly;
    }

    @Override
    public boolean damage(float amount) {
        if (health == 0f) return false;
        health = Math.max(health - amount, 0);
        if (health == 0f) destroy();
        return true;
    }
}
