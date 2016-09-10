package com.pennapps.blindnav;

/**
 * Created by Gene on 2016-09-09.
 */
public class MenuItem {

    public MenuItem next, prev, down, up;
    public int sound_ID;

    public MenuItem(int sound_ID){
        this.sound_ID = sound_ID;
    }
}
