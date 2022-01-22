
package com.mygdx.game.objectives;

import com.mygdx.game.Game;


public class DestroyCollegeObjective extends Objective {

    // Declare config, variables
    private final int collegesRequired = 5;

    private int collegesLeft;


    DestroyCollegeObjective() {
        // Initialize variables
        collegesLeft = collegesRequired;
    }


    @Override
    protected String getRequirementText() {
        // Return requirement text
        return "Destroy " + collegesLeft + " more colleges!";
    }


    @Override
    public boolean checkComplete(Game game) {
        return false;
    }
}
