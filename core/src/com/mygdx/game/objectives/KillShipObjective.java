
package com.mygdx.game.objectives;

import com.mygdx.game.Game;


public class KillShipObjective  extends Objective {

    // Declare config, variables
    private static final int shipsRequired = 5;

    private int shipsLeft;


    KillShipObjective() {
        // Initialize variables
        shipsLeft = shipsRequired;
    }


    @Override
    protected String getRequirementText() {
        // Return requirement text
        return "Kill " + shipsLeft + " more ships!";
    }


    @Override
    public boolean checkComplete(Game game) {
        return false;
    }
}
