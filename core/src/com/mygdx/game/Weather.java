package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Weather {


    static Texture texture;;
    String choice;
    Player player;
    private final float width = Game.PPT * .5f;
    Sprite sprite;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    public Float duration;

    public float time;
    public float screenWidth;
    public float screenHeight;
    private Game game;
    public Vector2 pos;
    private Vector2 startPos;
    private boolean toRemove;
    private Vector2 vel;
    private Vector2 inputDir;

    Weather(Game game_, Vector2 pos_, String choice, Float duration ){
        this.choice = choice;
        this.duration= duration;
        game = game_;
        player = game.getPlayer();
        pos = pos_;
        startPos=pos_;
        toRemove=true;

        if (choice == "cloudy"){
            texture = new Texture(Gdx.files.internal("./Weather/badWeather.png"));


        }

        sprite = new Sprite(texture);
        sprite.setPosition(pos.x, pos.y);
        sprite.setSize(width, width * texture.getHeight() / texture.getWidth());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());

    }





    public void update() {
        sprite.setPosition(pos.x - sprite.getOriginX(), pos.y - sprite.getOriginY());

        if (choice == "cloudy" && duration>=0){
            toRemove = false;
        }
    }

    public static void staticDispose() {
        texture.dispose();}


    public boolean shouldRemove() {
        return toRemove;
    }

    public void beenRemoved() {
    }

    public void render (SpriteBatch batch) {
            sprite.draw(batch);
    }
}