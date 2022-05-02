package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;

/**
 * Class for representing multiple visual effects over the camera relating to weather
 */
public class Weather {

    static Texture texture;
    private float time;
    String choice;
    Player player;
    float alphameter = 255;
    Sprite sprite;
    Integer pathNum = 0;
    Animation<TextureRegion> animation;
    
    public Float duration;
    private Game game;
    private boolean toRemove;
    private boolean testing;

    /**
     * constructs a different weather condition for a given duration based on "choice"
     * @param game_ the main running class
     * @param choice the choice of which weather condition to be applied
     * @param duration how long the weather condition will last for
     */
    Weather(Game game_, String choice, Float duration){
        this(game_, choice, duration, false);
        initialiseTextures();
    }

    Weather(Game game_, String choice, Float duration, boolean testing){
        this.choice = choice;
        this.duration= duration;
        game = game_;
        player = game.getPlayer();
        toRemove=false;
        this.testing = testing;
    }

    /**
     * Setup textures based on choice string
     */
    public void initialiseTextures(){
        // Any texture will get stretched out to fit the size of the screen in render() every frame
        if (choice == "cloudy") {
            texture = new Texture(Gdx.files.internal("./Weather/cloudy.png"));
        }
        
        if (choice == "foggy"){
            texture = new Texture(Gdx.files.internal("./Weather/fog.png"));
            sprite= new Sprite(texture);
        }
        
        if (choice == "rainy"){
            texture = new Texture(Gdx.files.internal("./Weather/rain0.png"));
        }
        
        sprite= new Sprite(texture);
    }

    /**
     * checks for the conditions to apply the chosen weather effect
     * and animates the different weather conditions
     *
     */
    public void update() {



        if (time<= 0.0f && choice == "foggy"){
            if (alphameter>5){ //gradually decreases alpha from 255 to >5
                alphameter -=1;
                sprite.setAlpha(alphameter);
            }

        }

        time= Math.max(time-Gdx.graphics.getDeltaTime(),0);

        if (time<= 0.0f && choice == "rainy"){
            pathNum = (pathNum+1) % 3; // alternate between frames every 0.1 second
            String filepath = "./Weather/rain"+pathNum.toString()+".png";
            sprite = new Sprite(new Texture (Gdx.files.internal(filepath)));
            time=0.1f;

        }

        duration -= Gdx.graphics.getDeltaTime(); //Reduce time
        if (duration<=0){ //Sets toRemove to true if weather effect is expired
            toRemove = true;
        }

    }

    public static void staticDispose() {
        texture.dispose();
    }

    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() {
    }

    /**
     * renders the chosen weather condition
     * @param batch
     */
    public void render (SpriteBatch batch) { //Called in the renderUI method of Game

            // Add any animation effect here, eg. sprite = new Sprite(new Texture (texturePaths[i+1])), to move to the next texture in list of texture filepaths for example

            sprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Sets the object to take up the entire screen
            sprite.setPosition(0,0); // Sets sprite to 0,0 on the screen
            sprite.draw(batch); // Draws using the given batch, which is UIBatch in renderUI
    }
}