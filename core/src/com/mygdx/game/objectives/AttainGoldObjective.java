
package com.mygdx.game.objectives;

import com.mygdx.game.Game;


public class AttainGoldObjective extends Objective {

    // Declare config, variables
    private static int goldRequired = 300;

    private int goldLeft;


    AttainGoldObjective() {
        // Initialize variables
        goldLeft = goldRequired;
    }


    @Override
    protected String getRequirementText() {
        // Return requirement text
        return "Get " + goldLeft + " more gold!";
    }


    @Override
    public boolean checkComplete(Game game) {
        return false;
    }
}
