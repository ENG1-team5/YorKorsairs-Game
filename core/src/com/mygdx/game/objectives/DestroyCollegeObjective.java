
package com.mygdx.game.objectives;

import com.mygdx.game.Game;
import com.mygdx.game.College;
import java.util.ArrayList;


public class DestroyCollegeObjective extends Objective {

    // Declare variables
    private ArrayList<College> colleges;
    public College nextCollege;
    private int collegesRequired;
    private int killableColleges;


    public DestroyCollegeObjective(Game game_) {
        super(game_);

        // Initialize variables
        colleges = game.getColleges();
        collegesRequired = 0;
        for (College college : colleges) {
            if (!college.getFriendly()) collegesRequired++;
            if (nextCollege == null) nextCollege = college;
        }
        collegesRequired /= 2;

        // Call once to update killable colleges
        getRequirementText();
    }


    @Override
    /**
     * return contextual text required for objective
     * @return string
     */
    protected String getRequirementText() {
        // Return requirement text
        killableColleges = 0;
        if (!nextCollege.getAlive()) nextCollege = null;
        for (College college : colleges) {
            if (!college.getFriendly() && college.getAlive()) killableColleges++;
            if (nextCollege == null) nextCollege = college;
        }
        return "Destroy " + killableColleges + " more colleges!";
    }


    @Override
    /**
     * returns if amount of enemy colleges destroyed is enough
     * @return boolean
     */
    public boolean checkComplete(Game game) {
        return killableColleges == 0;
    }
}
