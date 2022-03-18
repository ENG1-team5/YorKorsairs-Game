
package com.mygdx.game.objectives;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.game.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public abstract class Objective {

    // Declare variables
    private GlyphLayout currentTextGlyph = new GlyphLayout();
    protected Game game;


    Objective(Game game_) {
        game = game_;
    }

    /**
     * assigns a random objective
     * @param game_ game
     * @return object Objective to be finished
     */
    public static Objective getRandomObjective(Game game_) {
        // Pick a random objective
        float r = (float)Math.random();
        float numChoices = 2f;
        if (r < 1f / numChoices) return new DestroyCollegeObjective(game_);
        else return new GetLevel5Objective(game_);
    }


    /**
     * Adds objective to UI
     * @param batch graphical output to be rendered to
     */
    public void renderUI(SpriteBatch batch) {
        // Draw requirement text to screen
        Game.mainFont.getData().setScale(0.8f);
        float time = (System.currentTimeMillis() - Game.startTime) / 100f;
        Game.mainFont.getData().setScale(0.99f + 0.02f * (float)Math.sin(time / 4f));
        currentTextGlyph.setText(Game.mainFont, getRequirementText());
        Game.mainFont.draw(batch, getRequirementText(), Gdx.graphics.getWidth() * 0.5f - currentTextGlyph.width * 0.5f, Gdx.graphics.getHeight() - currentTextGlyph.height * 0.5f);
        Game.mainFont.getData().setScale(1f);
    }

    /**
     * gets text of chosen objective
     * @return string
     */
    protected abstract String getRequirementText();

    /**
     * checks if chosen objective is complete
     * @param game game
     * @return boolean
     */
    public abstract boolean checkComplete(Game game);
}
