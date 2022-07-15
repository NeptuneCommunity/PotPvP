package net.frozenorb.potpvp.nametag.framework;

import lombok.Getter;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class NametagThread extends Thread
{
    @Getter private static final Map<NametagUpdate, Boolean> pendingUpdates = new ConcurrentHashMap<>();

    public NametagThread() {
        super("PotPvPSI - Nametag Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            Iterator<NametagUpdate> pendingUpdatesIterator = NametagThread.pendingUpdates.keySet().iterator();
            while (pendingUpdatesIterator.hasNext()) {
                NametagUpdate pendingUpdate = pendingUpdatesIterator.next();
                try {
                    NametagHandler.applyUpdate(pendingUpdate);
                    pendingUpdatesIterator.remove();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(NametagHandler.getUpdateInterval() * 50L);
            }
            catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

}