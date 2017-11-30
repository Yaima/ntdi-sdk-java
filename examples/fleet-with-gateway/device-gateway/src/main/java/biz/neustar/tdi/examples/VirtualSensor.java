package biz.neustar.tdi.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Yaima Valdivia on 10/5/17.
 */
public class VirtualSensor {
    private static final Logger log = LoggerFactory.getLogger(VirtualSensor.class);

    public enum SwitchState {
        on, off
    }

    public enum MotionState {
        active, inactive
    }

    private String name;
    private SwitchState switchState;

    public VirtualSensor(String name) {
        this.name = name;
        this.switchState = SwitchState.off;
    }

    public void setSwitchState(SwitchState state) {
        this.switchState = state;
    }

    public Map<String, Object> getState() {
        MotionState[] motion_states = MotionState.values();
        Map<String, Object> state = new HashMap<>();

        state.put("name", this.name);
        state.put("battery", ThreadLocalRandom.current().nextInt(100));
        state.put("motion", motion_states[ThreadLocalRandom.current().nextInt(motion_states.length)].toString());
        state.put("switch", this.switchState.toString());
        log.debug("Sensor: state={}", this.name, state);

        return state;
    }
}
