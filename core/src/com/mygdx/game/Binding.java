
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Binding implements InputProcessor {

    // TODO:
    //  - Potentially move settings into some config file that allows easy access for people?
    //      This is probably worth asking stakeholder about


    // Declare variables
    private static HashMap<String, List<Integer>> keyBindings = new HashMap<String, List<Integer>>() {{
        put("moveLeft", Arrays.asList(Input.Keys.A, Input.Keys.LEFT));
        put("moveUp", Arrays.asList(Input.Keys.W, Input.Keys.UP));
        put("moveRight", Arrays.asList(Input.Keys.D, Input.Keys.RIGHT));
        put("moveDown", Arrays.asList(Input.Keys.S, Input.Keys.DOWN));
        put("startGame", Arrays.asList(Input.Keys.SPACE));
        put("closeGame", Arrays.asList(Input.Keys.ESCAPE));
    }};
    private static HashMap<String, List<Integer>> buttonBindings = new HashMap<String, List<Integer>>() {{
        put("shoot", Arrays.asList(Input.Buttons.LEFT, Input.Buttons.RIGHT));
        put("startGame", Arrays.asList(Input.Buttons.LEFT));
    }};
    private static Binding inst;
    private float scrollAmount;


    public static Binding getInstance() {
        // Singleton handling
        if (inst == null) inst = new Binding();
        return inst;
    }


    public float getScrollAmount() {
        // Return scroll amount
        float amount = scrollAmount;
        scrollAmount = 0f; // Hacky 1 time usage TODO: Figure out how to set to 0 after stopping scrolling
        return amount;
    }


    public boolean isActionPressed(String action) {
        // Check action exists
        List<Integer> actionKeys = keyBindings.get(action);
        List<Integer> actionButtons = buttonBindings.get(action);

        // Check if any bindings match
        if (actionKeys != null) {
            for (int key : actionKeys) {
                if (Gdx.input.isKeyPressed(key)) return true;
            }
        }
        if (actionButtons != null) {
            for (int button : actionButtons) {
                if (Gdx.input.isButtonPressed(button)) return true;
            }
        }

        // No bindings match
        return false;
    }

    public boolean isActionJustPressed(String action) {
        // Check action exists
        List<Integer> actionKeys = keyBindings.get(action);
        List<Integer> actionButtons = buttonBindings.get(action);

        // Check if any bindings match
        if (actionKeys != null) {
            for (int key : actionKeys) {
                if (Gdx.input.isKeyJustPressed(key)) return true;
            }
        }
        if (actionButtons != null) {
            for (int button : actionButtons) {
                if (Gdx.input.isButtonJustPressed(button)) return true;
            }
        }

        // No bindings match
        return false;
    }


    @Override
    public boolean keyDown (int keycode) { return false; }
    @Override
    public boolean keyUp (int keycode) { return false; }
    @Override
    public boolean keyTyped (char character) { return false; }
    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved (int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { scrollAmount = amountY; return true; }
}