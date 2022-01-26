
package com.mygdx.game.objectives;

import com.mygdx.game.Game;
import com.mygdx.game.College;
import java.util.ArrayList;


public class DestroyCollegeObjective extends Objective {

    // Declare variables
    private ArrayList<College> colleges;
    private College nextCollege;
    private int collegesRequired;
    private int killableColleges;


    DestroyCollegeObjective(Game game_) {
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
    public boolean checkComplete(Game game) {
        return killableColleges == 0;
    }
}
