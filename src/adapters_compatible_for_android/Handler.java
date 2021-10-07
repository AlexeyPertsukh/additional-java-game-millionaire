package adapters_compatible_for_android;

import java.util.Timer;
import java.util.TimerTask;

public class Handler {

    public void postDelayed(OnSimple onSimple, int period){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                onSimple.actionOnSimple();
            }
        };

        timer.schedule( timerTask, period );
    }


    public interface OnSimple{
        void actionOnSimple();
    }


}
