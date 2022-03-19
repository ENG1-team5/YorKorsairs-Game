
package com.mygdx.game.objectives;

import com.mygdx.game.Game;


public class GetLevel5Objective extends Objective {

    // Declare config, variables
    private static int levelRequired = 5;


    public GetLevel5Objective(Game game_) { super(game_); }


    @Override
    /**
     * return contextual text required for objective
     */
    protected String getRequirementText() {
        // Return requirement text
        return "Reach Level 5!";
    }

    /**
     * returns whether reached enough gold
     * @param game
     * @return boolean
     */
    @Override
    public boolean checkComplete(Game game) {
        return game.currentLevel >= levelRequired;
    }
}
