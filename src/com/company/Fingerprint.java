package com.company;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class Fingerprint {
    public static HashMap<Integer, Fingerprint> fingerprints = new HashMap<>();
    public HashMap<Integer, Consumer<IP>> semantics = new HashMap<>();
    public int ident;

    public Fingerprint(String str) {
        ident = Utility.encodeString(str);
        fingerprints.put(ident, this);
    }

    public void addSemantic(int num, Consumer<IP> semantic) {
        semantics.put(num, semantic);
    }

    public static void initializeFingerprints() {
        Fingerprint NULL = new Fingerprint("NULL");
        for (int i = 0; i < 26; i++) {
            NULL.addSemantic(i, IP::reflect);
        }
        Fingerprint ROMA = new Fingerprint("ROMA");
        ROMA.addSemantic('I'-'A', (ip) -> ip.push(1));
        ROMA.addSemantic('V'-'A', (ip) -> ip.push(5));
        ROMA.addSemantic('X'-'A', (ip) -> ip.push(10));
        ROMA.addSemantic('L'-'A', (ip) -> ip.push(50));
        ROMA.addSemantic('C'-'A', (ip) -> ip.push(100));
        ROMA.addSemantic('D'-'A', (ip) -> ip.push(500));
        ROMA.addSemantic('M'-'A', (ip) -> ip.push(1000));

        Fingerprint MODU = new Fingerprint("MODU");
        MODU.addSemantic('M'-'A', (ip) -> {
            int a = ip.pop();
            int b = ip.pop();
            if (a == 0) ip.push(0);
            else ip.push(a < 0 ? -b%a : b%a);
        });
        MODU.addSemantic('R'-'A', (ip) -> {
            int a = ip.pop();
            int b = ip.pop();
            if (a == 0) ip.push(0);
            else ip.push(b%a);
        });
        MODU.addSemantic('U'-'A', (ip) -> {
            int a = Math.abs(ip.pop());
            int b = Math.abs(ip.pop());
            if (a == 0) ip.push(0);
            else ip.push(b%a);
        });

        /*Fingerprint HRTI = new Fingerprint("HRTI");
        HRTI.addSemantic('G'-'A', (ip) -> ip.push(1)); // granularity of timer in microseconds
        HRTI.addSemantic('M'-'A', (ip) -> ip.timerMark = System.nanoTime());
        HRTI.addSemantic('T'-'A', (ip) -> {
            if (ip.timerMark == 0) {
                ip.reflect();
                return;
            }
            ip.push((int)((System.nanoTime()-ip.timerMark)/1000));
        });
        HRTI.addSemantic('E'-'A', (ip) -> ip.timerMark = 0);
        HRTI.addSemantic('S'-'A', (ip) -> ip.push((int)(System.currentTimeMillis()%1000)));*/

        Fingerprint MODE = new Fingerprint("MODE");
        MODE.addSemantic('H'-'A', (ip) -> ip.hoverMode = !ip.hoverMode);
        MODE.addSemantic('I'-'A', (ip) -> ip.invertMode = !ip.invertMode);
        MODE.addSemantic('Q'-'A', (ip) -> ip.queueMode = !ip.queueMode);
        MODE.addSemantic('S'-'A', (ip) -> ip.switchMode = !ip.switchMode);
    }

    public static boolean loadFingerprint(IP ip, int ident) {
        if (!fingerprints.containsKey(ident)) {
            ip.reflect();
            return false;
        }
        for (Map.Entry<Integer, Consumer<IP>> semantic : fingerprints.get(ident).semantics.entrySet()) {
            ip.semantics[semantic.getKey()].push(semantic.getValue());
        }
        return true;
    }

    public static boolean unloadFingerprint(IP ip, int ident) {
        if (!fingerprints.containsKey(ident)) {
            ip.reflect();
            return false;
        }
        for (Integer num : fingerprints.get(ident).semantics.keySet()) {
            if (!ip.semantics[num].isEmpty()) {
                ip.semantics[num].pop();
            }
        }
        return true;
    }
}
