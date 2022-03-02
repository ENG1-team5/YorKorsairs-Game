package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ArrayMap;

public class Buff {
    
    private Boolean expires = true;
    public float time;
    private ArrayMap<String, Float> stats;

    /**
     * Create a new buff object
     * @param stats The buffed stats, name: value
     * @param duration The length of the buff
     */
    public Buff(String stat, float amount, float duration) {
        if (duration <= 0) { throw new IllegalArgumentException("time must be greater than 0"); }
        this.time = duration;
        
        ArrayMap<String, Float> stats = new ArrayMap<String, Float>();
        stats.put(stat, amount);
        this.stats = stats;
    }

    /**
     * Create a new buff objects
     * @param stats The buffed stats, name: value
     */
    public Buff(String stat, float amount) {
        this.expires = false;
        
        ArrayMap<String, Float> stats = new ArrayMap<String, Float>();
        stats.put(stat, amount);
        this.stats = stats;
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

    private Float getStat(String name) {
        return stats.get(name, 0f);
    }

    public float getMaxHealthBuff() { return getStat("maxHealth"); }
    public float getTopSpeedBuff() { return getStat("topSpeed"); }
    public float getAccelerationBuff() { return getStat("acceleration"); }
    public float getDamageBuff() { return getStat("damage"); }
    public float getProjectileSpeedBuff() { return getStat("projectileSpeed"); }
    public float getFireRateBuff() { return getStat("fireRate"); }

}
