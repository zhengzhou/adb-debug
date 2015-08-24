package siir.es.adbWireless;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by xu on 2015/8/24 0024.
 */
public class BusPro {

    private static Bus mBus = new Bus(ThreadEnforcer.ANY);

    public static void post(Object event) {
        mBus.post(event);
    }

    public static void unregister(Object object) {
        mBus.unregister(object);
    }

    public static void register(Object object) {
        mBus.register(object);
    }
}
