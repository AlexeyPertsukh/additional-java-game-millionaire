package adapters_compatible_for_android;

import java.util.Timer;
import java.util.TimerTask;

//Самодельный Handler для совместимости класса Game с кодом для андроид,
//где есть стандартный класс Handler
public class Handler {

    public void postDelayed(ISimple iSimple, int period){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                iSimple.action();
            }
        };

        timer.schedule( timerTask, period );
    }

    public interface ISimple {
        void action();
    }
}
