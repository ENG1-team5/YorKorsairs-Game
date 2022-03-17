
package com.mygdx.game.objectives;

import com.mygdx.game.Game;


public class AttainGoldObjective extends Objective {

    // Declare config, variables
    private static int goldRequired = 300;


    public AttainGoldObjective(Game game_) { super(game_); }


    @Override
    /**
     * return contextual text required for objective
     */
    protected String getRequirementText() {
        // Return requirement text
        return "Get " + Math.max(goldRequired - game.currentGold, 0) + " more gold!";
    }

    /**
     * returns whether reached enough gold
     * @param game
     * @return boolean
     */
    @Override
    public boolean checkComplete(Game game) {
        return game.currentGold >= goldRequired;
    }
}
