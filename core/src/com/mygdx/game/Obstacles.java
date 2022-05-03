package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Class for representing different kinds of obstacles on the map
 */
public class Obstacles {

    static Texture texture;
    public String choice;
    private final float width = Game.PPT * .5f;

    private Game game;
    private Sprite sprite;
    public Vector2 pos;
    private boolean toRemove;
    private String rock="Rock";
    private String seamine="Seamine";
    private String iceberg="Iceberg";
    private boolean testing;

    /**
     *constructs a different obstacle in a given position depending on the "choice"
     * @param game_ the main running class
     * @param pos_ position of which to spawn an obstacles
     * @param choice the choice of which obstacle to be spawned
     */
    public Obstacles (Game game_, Vector2 pos_, String choice, boolean testing) {

        this.choice = choice;
        game = game_;
        pos = pos_;
        toRemove = false;
        this.testing = testing;
    }

    Obstacles (Game game_, Vector2 pos_, String choice){
        this(game_, pos_, choice, false); 
        initialiseTextures();
    }
        /**
         * Setup texture based on choice string
         */
        public void initialiseTextures(){
            if (choice == rock ){
                texture = new Texture(Gdx.files.internal("obstacles/rocks.png"));
            }
            if (choice == seamine){
                texture = new Texture(Gdx.files.internal("obstacles/seamine.png"));
            }
            if (choice == iceberg){
                texture = new Texture(Gdx.files.internal("obstacles/ice.png")); 
            }
    
            sprite = new Sprite(texture);
    
            sprite.setPosition(pos.x, pos.y);
            sprite.setSize(width, width * texture.getHeight() / texture.getWidth());
            sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
            sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());    
        }

        /**
        * checks for collisions and applies the effects af the obstacles
        */
        public void update() {


            Rectangle rect = getCollisionRect();

            if (!testing){
                sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());
            }


            IHittable hittableHit = game.checkHitHittable(rect);



            if (hittableHit instanceof Player && choice=="Rock") {
                game.getPlayer().setVelocity(game.getPlayer().getVelocity().scl(0.5f));
                game.getPlayer().damage(0.1f);
            }

            if (hittableHit instanceof Player && choice == "Seamine"){
                game.getPlayer().damage(20.0f);
                toRemove = true;
            }

            if (hittableHit instanceof Player && choice=="Iceberg") {

                game.getPlayer().setVelocity(game.getPlayer().getVelocity().scl(0f));
                game.getPlayer().damage(10.0f);
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
     * renders obstacle sprite to output batch
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
     * Deletes obstacle to conserve processor if dead
     * @return boolean
     */
    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() { }




}
