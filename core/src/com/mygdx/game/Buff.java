package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ArrayMap;

public class Buff {

    // Possible buff icons
    private static final Texture multi = new Texture(Gdx.files.internal("./pickups/multi1.png"));
    private static final Texture damage = new Texture(Gdx.files.internal("./pickups/dmg1.png"));
    private static final Texture fireRate = new Texture(Gdx.files.internal("./pickups/speed2.png"));
    private static final Texture projectileSpeed = new Texture(Gdx.files.internal("./pickups/dmgspeed.png"));
    private static final Texture regen = new Texture(Gdx.files.internal("./pickups/health1.png"));
    private static final Texture maxHealth = new Texture(Gdx.files.internal("./pickups/health2.png"));
    private static final Texture speed = new Texture(Gdx.files.internal("./pickups/speed1.png"));
    private static final Texture err = new Texture(Gdx.files.internal("./pickups/err.png"));
    
    private Boolean expires = true;
    public float time;
    private ArrayMap<String, Float> stats;

    /**
     * Create a new buff object with specified buff and duration
     * @param stat stat to buff
     * @param amount value of buff
     * @param duration The length of the buff
     */
    public Buff(String stat, float amount, float duration) {
        if (duration <= 0) { throw new IllegalArgumentException("time must be greater than 0"); }
        this.time = duration;
        
        ArrayMap<String, Float> stats = new ArrayMap<String, Float>();
        stats.put(stat, amount);
        this.stats = stats;
        checkValid();
    }

    /**
     * Create a new buff object with specified
     * @param stat stat to buff
     * @param amount value of buff
     */
    public Buff(String stat, float amount) {
        this.expires = false;
        
        ArrayMap<String, Float> stats = new ArrayMap<String, Float>();
        stats.put(stat, amount);
        this.stats = stats;
        checkValid();

    }
    /**
     * Create a new buff object from ArrayMap with specified duration
     * @param stats The buffed stats, see Buff.java for valid keys
     * @param duration The length of the buff
     */
    public Buff(ArrayMap<String, Float> stats_, float duration) {
        if (duration <= 0) { throw new IllegalArgumentException("time must be greater than 0"); }
        time = duration;
        stats = stats_;
        checkValid();
    }

    /**
     * Create a new buff object from ArrayMap
     * @param stats The buffed stats, name: value
     */
    public Buff(ArrayMap<String, Float> stats_) {
        this.expires = false;
        this.stats = stats_;
        checkValid();
    }

    /**
     * Check if buff is valid
     */
    private void checkValid() {
        if (getBuffedStats().size() == 0) {
            throw new IllegalArgumentException("No valid stats provided");
        }
    }

    /**
     * Update the time remaining. If the buff is no longer active, clear its stats.
     * @return true if buff is still active, false otherwise
     */
    public Boolean update() {
        if (expires) {
            time -= Gdx.graphics.getDeltaTime();
            if (time <= 0) {
                stats = new ArrayMap<String, Float>();
                return false;
            }
        }
        return true;
    }

    /**
     * alias for stats.get(name, 0f);
     * @param name
     * @return
     */
    private Float getStat(String name) {
        return stats.get(name, 0f);
    }

    /**
     * Get the stats buffed by this object
     * @return List of buff names
     */
    public ArrayList<String> getBuffedStats() {
        ArrayList<String> r = new ArrayList<>();

        for (String n : stats.keys()) {
            if (getStat(n) != 0f) {
                r.add(n);
            }
        }

        return r;
    }

    /**
     * Get the icon of the buff
     * @return the icon as a Texture
     */
    public Texture getTexture() {

        // Get the texture from the buff
        if (getBuffedStats().size() > 1) {
            // Multibuff
            return multi;
        } else if (getBuffedStats().size() < 1) {
            return err;
        } else { //buffed.size() == 1
            switch ( getBuffedStats().get(0) ) {
                case "maxHealth":
                    return maxHealth;

                case "speed":
                    return speed;

                case "regen":
                    return regen;
                
                case "damage":
                    return damage;
                    
                case "projectileSpeed":
                    return projectileSpeed;
                    
                case "fireRate":
                    return fireRate;
                    
                default:
                    return err;
            }
        }
    }

    /**
     * Dispose static textures
     */
    public static void staticDispose() {
        multi.dispose();
        damage.dispose();
        fireRate.dispose();
        projectileSpeed.dispose();
        regen.dispose();
        maxHealth.dispose();
        speed.dispose();
        err.dispose();
    }

    // Various getters for convinience
    public float getMaxHealthBuff() { return getStat("maxHealth"); }
    public float getRegenBuff() { return getStat("regen"); }
    public float getSpeedBuff() { return getStat("speed"); }
    public float getDamageBuff() { return getStat("damage"); }
    public float getProjectileSpeedBuff() { return getStat("projectileSpeed"); }
    public float getFireRateBuff() { return getStat("fireRate"); }

}
