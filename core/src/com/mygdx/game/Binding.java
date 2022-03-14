
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Binding implements InputProcessor {

    // Declare variables
    private static HashMap<String, List<Integer>> keyBindings = new HashMap<String, List<Integer>>() {
        {
            put("moveLeft", Arrays.asList(Input.Keys.A, Input.Keys.LEFT));
            put("moveUp", Arrays.asList(Input.Keys.W, Input.Keys.UP));
            put("moveRight", Arrays.asList(Input.Keys.D, Input.Keys.RIGHT));
            put("moveDown", Arrays.asList(Input.Keys.S, Input.Keys.DOWN));
            put("interact", Arrays.asList(Input.Keys.E));
            put("startGame", Arrays.asList(Input.Keys.SPACE));
            put("closeGame", Arrays.asList(Input.Keys.ESCAPE));
            put("resetGame", Arrays.asList(Input.Keys.TAB));
        }
    };
    private static HashMap<String, List<Integer>> buttonBindings = new HashMap<String, List<Integer>>() {
        {
            put("shoot", Arrays.asList(Input.Buttons.LEFT, Input.Buttons.RIGHT));
        }
    };
    private static Binding inst;
    private float scrollAmount;


    /**
     * return single instance of binding class
     * @return Binding
     */
    public static Binding getInstance() {
        // Singleton handling
        if (inst == null)
            inst = new Binding();
        return inst;
    }


    /**
     * Returns scroll amount
     * @return float amount
     */
    public float getScrollAmount() {
        float amount = scrollAmount;
        scrollAmount = 0f;
        return amount;
    }


    /**
     * takes in key input and matches to an action and checks for button holds
     * @param action name of action to check
     * @return boolean
     */
    public boolean isActionPressed(String action) {
        // Check action exists
        List<Integer> actionKeys = keyBindings.get(action);
        List<Integer> actionButtons = buttonBindings.get(action);

        // Check if any bindings match
        if (actionKeys != null) {
            for (int key : actionKeys) {
                if (Gdx.input.isKeyPressed(key))
                    return true;
            }
        }
        if (actionButtons != null) {
            for (int button : actionButtons) {
                if (Gdx.input.isButtonPressed(button))
                    return true;
            }
        }

        // No bindings match
        return false;
    }


    /**
     * takes in key input and matches to an action and checks for clicks
     * @param action name of action to check
     * @return boolean
     */
    public boolean isActionJustPressed(String action) {
        // Check action exists
        List<Integer> actionKeys = keyBindings.get(action);
        List<Integer> actionButtons = buttonBindings.get(action);

        // Check if any bindings match
        if (actionKeys != null) {
            for (int key : actionKeys) {
                if (Gdx.input.isKeyJustPressed(key))
                    return true;
            }
        }
        if (actionButtons != null) {
            for (int button : actionButtons) {
                if (Gdx.input.isButtonJustPressed(button))
                    return true;
            }
        }

        // No bindings match
        return false;
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollAmount = amountY;
        return true;
    }
}
