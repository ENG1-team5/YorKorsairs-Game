package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class Obstacles {

    static Texture texture;
    String choice;
    Player player;
    private final float width = Game.PPT * .5f;

    private Game game;
    private Sprite sprite;
    private Vector2 pos;
    private Vector2 startPos;
    private boolean toRemove;
    private Vector2 vel;
    private Vector2 inputDir;
    private final float maxSpeed = Game.PPT * 1f;
    private final float acceleration = Game.PPT * 3f;
    private String rock="Rock";
    private String seamine="Seamine";
    private String iceberg="Iceberg";


    Obstacles (Game game_, Vector2 pos_, String choice) {

        this.choice = choice;
        game = game_;
        player = game.getPlayer();
        pos = pos_;
        startPos = pos_;
        toRemove = false;
        vel = new Vector2(0f, 0f);
        inputDir = new Vector2(0f, 0f);

        if (choice == rock ){
            texture = new Texture(Gdx.files.internal("./obstacles/rocks.png"));
        }
        if (choice == seamine){
            texture = new Texture(Gdx.files.internal("./obstacles/seamine.png"));
        }
        if (choice == iceberg){
            texture = new Texture(Gdx.files.internal("./obstacles/ice.png"));
            inputDir.x = (float) Math.random() * 2f - 1f;
            inputDir.y = (float) Math.random() * 2f - 1f;
            inputDir = inputDir.nor();


        }

        sprite = new Sprite(texture);

        sprite.setPosition(pos.x, pos.y);
        sprite.setSize(width, width * texture.getHeight() / texture.getWidth());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());}

        public void update() {


            Rectangle rect = getCollisionRect();

            sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());


            IHittable hittableHit = game.checkHitHittable(rect);



            if (hittableHit instanceof Player && choice=="Rock") {
                player.setVelocity(player.getVelocity().scl(0.5f));
                player.damage(0.1f);
            }

            if (hittableHit instanceof Player && choice == "Seamine"){
                player.damage(20.0f);
                toRemove = true;
            }

            if (hittableHit instanceof Player && choice=="Iceberg") {

                if (Math.random() < Gdx.graphics.getDeltaTime() * 1f) {
                    inputDir.x = (float) Math.random() * 2f - 1f;
                    inputDir.y = (float) Math.random() * 2f - 1f;
                    inputDir = inputDir.nor();
                }
                float diffX = vel.x * Gdx.graphics.getDeltaTime();
                float diffY = vel.y * Gdx.graphics.getDeltaTime();
                pos.x += diffX;
                pos.y += diffY;

                if (game.checkCollision(rect)) {
                    pos.x -= diffX;
                    pos.y -= diffY;
                    vel.x = 0;
                    vel.y = 0;

                }


                // Update velocity
                vel.x += inputDir.x * acceleration * Gdx.graphics.getDeltaTime();
                vel.y += inputDir.y * acceleration * Gdx.graphics.getDeltaTime();
                if (vel.len2() > (maxSpeed * maxSpeed))
                    vel = vel.setLength(maxSpeed);

                player.setVelocity(player.getVelocity().scl(0f));
                player.damage(10.0f);
                toRemove = true;
            }

    }


        public Rectangle getCollisionRect() {
            return new Rectangle(
                pos.x - width * 0.5f,
                pos.y - width * 0.5f,
                width, width);

    }


    /**
     * renders projectile sprite to output batch
     * @param batch graphical output to be rendered to
     */
    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    /**
     * Dispose static textures
     */
    public static void staticDispose() {
        texture.dispose();
    }

    /**
     * Deletes projectile to conserve processor if dead
     * @return boolean
     */
    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() { }




}
