package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

public class Weather {

    static Texture texture;
    String choice;
    Player player;
    Sprite sprite;
    
    public Float duration;
    private Game game;
    private boolean toRemove;

    Weather(Game game_, String choice, Float duration){
        this.choice = choice;
        this.duration= duration;
        game = game_;
        player = game.getPlayer();
        toRemove=false;

        // Any texture will get stretched out to fit the size of the screen in render() every frame
        if (choice == "cloudy"){
            texture = new Texture(Gdx.files.internal("./Weather/badWeather.png")); 
        }

        sprite = new Sprite(texture);

    }

    public void update() {
        duration -= Gdx.graphics.getDeltaTime(); //Reduce time
        System.out.println(duration);
        if (duration<=0){ //Sets toRemove to true if weather effect is expired
            toRemove = true;
        }

        // To make something happen, dependent on weather effect
        // if (choice == "thunder"){
        //     // Do something?? eg. damage player
        // }
    }

    public static void staticDispose() {
        texture.dispose();
    }

    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() {
    }

    public void render (SpriteBatch batch) { //Called in the renderUI method of Game

            // Add any animation effect here, eg. sprite = new Sprite(new Texture (texturePaths[i+1])), to move to the next texture in list of texture filepaths for example

            sprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Sets the object to take up the entire screen
            sprite.setPosition(0,0); // Sets sprite to 0,0 on the screen
            sprite.draw(batch); // Draws using the given batch, which is UIBatch in renderUI
    }
}