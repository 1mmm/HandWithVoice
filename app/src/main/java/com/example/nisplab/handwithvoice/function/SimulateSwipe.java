package com.example.nisplab.handwithvoice.function;

import static com.example.nisplab.handwithvoice.ConstantValue.DOWN;
import static com.example.nisplab.handwithvoice.ConstantValue.LEFT;
import static com.example.nisplab.handwithvoice.ConstantValue.RIGHT;
import static com.example.nisplab.handwithvoice.ConstantValue.UP;

/**
 * Created by NIS&PLAB on 2017/7/26.
 */

public class SimulateSwipe {

    private boolean state = false;
    private Gesture gesture = new Gesture();

    public void setState(boolean a) {
        state = a;
    }

    public boolean getState() {return state; }

    public void swipe(int direction) {
        switch (direction) {
            case RIGHT:
                gesture.swipe(RIGHT);
                break;
            case LEFT:
                gesture.swipe(LEFT);
                break;
            case UP:
                gesture.swipe(UP);
                break;
            case DOWN:
                gesture.swipe(DOWN);
                break;
        }
    }
}
