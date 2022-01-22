
package com.mygdx.game.objectives;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mygdx.game.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public abstract class Objective {

    // Declare variables
    private static BitmapFont font = new BitmapFont();


    public static Objective getRandomObjective() {
        // Pick a random objective
        float r = (float)Math.random();
        if (r < 1f / 3f) return new DestroyCollegeObjective();
        else if (r < 2f / 3f) return new AttainGoldObjective();
        else return new KillShipObjective();
    }


    public void renderUI(SpriteBatch batch) {
        // Draw requirement text to screen
        // TODO:
        //  - Figure out why font positioning is wrong
        // TODO:
        //  - Get a better font and sort antialiasing
        font.setColor(0, 0, 0, 1);
        font.getData().setScale(3.5f);
        font.draw(batch, getRequirementText(), Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() - 25.0f);
    }


    protected abstract String getRequirementText();

    // TODO:
    //  - Make checkComplete functions for each child using game.getPlayer() and new variables
    public abstract boolean checkComplete(Game game);
}
