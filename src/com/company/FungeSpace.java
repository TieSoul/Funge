package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class FungeSpace {
    public boolean end = false;
    public BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    public LinkedList<IP> ips = new LinkedList<>();
    public int dim;
    public HashMap<Vector<Integer>, Integer> data;
    public Vector<LinkedList<Integer>> minVals;
    public Vector<LinkedList<Integer>> maxVals;
    public int currIPID = 0;
    public Random rand = new Random();
    public int putWithSO(Vector<Integer> vec, Integer val, IP ip) {
        Vector<Integer> newVec = new Vector<>();
        for (int i = 0; i < dim; i++) {
            newVec.add(vec.get(i) + ip.storageOffset.get(i));
        }
        return put(newVec, val);
    }
    public int getWithSO(Vector<Integer> vec, IP ip) {
        Vector<Integer> newVec = new Vector<>();
        for (int i = 0; i < dim; i++) {
            newVec.add(vec.get(i) + ip.storageOffset.get(i));
        }
        return get(newVec);
    }
    public int put(Vector<Integer> vec, Integer val) {
        while (vec.size() > dim) {
            vec.remove(vec.size()-1);
        }
        if (val == 32) {
            if (data.containsKey(vec)) {
                data.remove(vec);
            }
            return val;
        }
        for (int i = 0; i < (dim > 3 ? 3 : dim); i++) {
            if (vec.get(i) <= minVals.get(i).peek() && !data.containsKey(vec)) {
                minVals.get(i).push(vec.get(i));
            }
            if (vec.get(i) >= maxVals.get(i).peek() && !data.containsKey(vec)) {
                maxVals.get(i).push(vec.get(i));
            }
        }
        data.put(vec, val);
        return val;
    }
    public int get(Vector<Integer> vec) {
        if (!data.containsKey(vec)) return 32;
        return data.get(vec);
    }

    public void doInstr(IP ip, int instr) {
        if (ip.stringMode) {
            if (instr == '"') {
                ip.stringMode = false;
                return;
            }
            ip.push(instr);
            return;
        }
        if (instr == '>') {
            if (ip.hoverMode) {
                ip.delta.set(0, ip.delta.get(0)+1);
                return;
            }
            ip.delta.set(0, 1);
            for (int i = 1; i < dim; i++) {
                ip.delta.set(i, 0);
            }
        } else if (instr == '<') {
            if (ip.hoverMode) {
                ip.delta.set(0, ip.delta.get(0)-1);
                return;
            }
            ip.delta.set(0, -1);
            for (int i = 1; i < dim; i++) {
                ip.delta.set(i, 0);
            }
        } else if (instr == '^' && dim > 1) {
            if (ip.hoverMode) {
                ip.delta.set(1, ip.delta.get(1)-1);
                return;
            }
            ip.delta.set(0, 0);
            ip.delta.set(1, -1);
            for (int i = 2; i < dim; i++) {
                ip.delta.set(i, 0);
            }
        } else if (instr == 'v' && dim > 1) {
            if (ip.hoverMode) {
                ip.delta.set(1, ip.delta.get(1)+1);
                return;
            }
            ip.delta.set(0, 0);
            ip.delta.set(1, 1);
            for (int i = 2; i < dim; i++) {
                ip.delta.set(i, 0);
            }
        } else if (instr == 'l' && dim > 2) {
            if (ip.hoverMode) {
                ip.delta.set(2, ip.delta.get(2)-1);
                return;
            }
            ip.delta.set(0, 0);
            ip.delta.set(1, 0);
            ip.delta.set(2, -1);
            for (int i = 3; i < dim; i++) {
                ip.delta.set(i, 0);
            }
        } else if (instr == 'h' && dim > 2) {
            if (ip.hoverMode) {
                ip.delta.set(2, ip.delta.get(2)+1);
                return;
            }
            ip.delta.set(0, 0);
            ip.delta.set(1, 0);
            ip.delta.set(2, 1);
            for (int i = 3; i < dim; i++) {
                ip.delta.set(i, 0);
            }
        } else if (instr == '?') {
            int cardinal = rand.nextInt(dim*2);
            int d = cardinal/2;
            int sign = (cardinal%2 == 0 ? -1 : 1);
            for (int i = 0; i < dim; i++) {
                if (i == d) {
                    ip.delta.set(i, sign);
                } else {
                    ip.delta.set(i, 0);
                }
            }
        } else if (instr == '[' && dim > 1) {
            int temp = ip.delta.get(0);
            ip.delta.set(0, ip.delta.get(1));
            ip.delta.set(1, -temp);
            if (ip.switchMode) {
                put(ip.pos, (int)']');
            }
        } else if (instr == ']' && dim > 1) {
            int temp = ip.delta.get(0);
            ip.delta.set(0, -ip.delta.get(1));
            ip.delta.set(1, temp);
            if (ip.switchMode) {
                put(ip.pos, (int)'[');
            }
        } else if (instr == 'r') {
            ip.reflect();
        } else if (instr == 'x') {
            ip.delta = ip.vecPop(dim);
        } else if (instr == '#') {
            ip.step();
        } else if (instr == '@') {
            ips.remove(ip);
        } else if (instr == 'j') {
            int j = ip.pop();
            boolean r = j < 0;
            if (r) {
                ip.reflect();
                j = -j;
            }
            for (int i = 0; i < j; i++) {
                ip.step();
            }
            if (r) ip.reflect();
        } else if (instr == 'q') {
            System.exit(ip.pop());
        } else if (instr == 'k') {
            int inst2 = get(ip.nextMov());
            int n = ip.pop();
            for (int i = 0; i < n; i++) {
                doInstr(ip, inst2);
            }
            if (n == 0) ip.step();
        } else if (instr == '!') {
            ip.push(ip.pop() == 0 ? 1 : 0);
        } else if (instr == '`') {
            ip.push(ip.pop() <= ip.pop() ? 1 : 0);
        } else if (instr == '_') {
            if (ip.pop() == 0) {
                doInstr(ip, '>');
            } else {
                doInstr(ip, '<');
            }
        } else if (instr == '|' && dim > 1) {
            if (ip.pop() == 0) {
                doInstr(ip, 'v');
            } else {
                doInstr(ip, '^');
            }
        } else if (instr == 'm' && dim > 2) {
            if (ip.pop() == 0) {
                doInstr(ip, 'l');
            } else {
                doInstr(ip, 'h');
            }
        } else if (instr == 'w' && dim > 1) {
            int b = ip.pop(),
                a = ip.pop();
            if (a > b) {
                int temp = ip.delta.get(0);
                ip.delta.set(0, -ip.delta.get(1));
                ip.delta.set(1, temp);
            } else if (a < b) {
                int temp = ip.delta.get(0);
                ip.delta.set(0, ip.delta.get(1));
                ip.delta.set(1, -temp);
            }
        } else if ((instr - '0' >= 0 && instr - '0' < 10) || (instr - 'a' >= 0 && instr - 'a' < 6)) {
            ip.push(Integer.valueOf(Character.toString((char)instr), 16));
        } else if (instr == '+') {
            ip.push(ip.pop() + ip.pop());
        } else if (instr == '-') {
            ip.push(-ip.pop() + ip.pop());
        } else if (instr == '*') {
            ip.push(ip.pop() * ip.pop());
        } else if (instr == '/') {
            int a = ip.pop();
            int b = ip.pop();
            if (a == 0) {
                ip.push(0);
            } else {
                ip.push(b/a);
            }
        } else if (instr == '%') {
            int a = ip.pop();
            int b = ip.pop();
            if (a == 0) {
                ip.push(0);
            } else {
                ip.push(b%a);
            }
        } else if (instr == '"') {
            ip.stringMode = true;
        } else if (instr == '\'') {
            ip.step();
            ip.push(get(ip.pos));
        } else if (instr == 's') {
            ip.step();
            put(ip.pos, ip.pop());
        } else if (instr == '$') {
            ip.pop();
        } else if (instr == ':') {
            int p = ip.pop();
            ip.push(p);
            ip.push(p);
        } else if (instr == '\\') {
            int a = ip.pop();
            int b = ip.pop();
            ip.push(a);
            ip.push(b);
        } else if (instr == 'n') {
            ip.getTOSS().clear();
        } else if (instr == '{') {
            int n = ip.pop();
            ip.stackStack.push(new LinkedList<>());
            if (n < 0) {
                for (int i = 0; i < -n; i++) {
                    ip.getSOSS().push(0);
                }
            } else {
                int k = ip.getSOSS().size();
                if (k < n) {
                    for (int i = 0; i < n - k; i++) {
                        ip.getTOSS().push(0);
                    }
                }
                for (int i = n-1; i >= 0; i--) {
                    ip.push(ip.getSOSS().remove(i));
                }
            }
            ip.SOSSvecPush(ip.storageOffset);
            ip.storageOffset = ip.next();
            if (ip.switchMode) {
                put(ip.pos, (int)'}');
            }
        } else if (instr == '}') {
            if (ip.stackStack.size() == 1) {
                ip.reflect();
                return;
            }
            int n = ip.pop();
            ip.storageOffset = ip.SOSSvecPop(dim);
            if (n < 0) {
                for (int i = 0; i < -n; i++) {
                    ip.SOSSpop();
                }
            } else {
                int k = ip.getTOSS().size();
                if (k < n) {
                    for (int i = 0; i < n - k; i++) {
                        ip.SOSSpush(0);
                    }
                    k = n;
                }
                for (int i = n-1; i >= 0; i--) {
                    ip.SOSSpush(ip.getTOSS().remove(i));
                }
            }
            ip.stackStack.pop();
            if (ip.switchMode) {
                put(ip.pos, (int)'{');
            }
        } else if (instr == 'u') {
            if (ip.stackStack.size() == 1) {
                ip.reflect();
            }
            int n = ip.pop();
            if (n < 0) {
                for (int i = 0; i < -n; i++) {
                    ip.SOSSpush(ip.pop());
                }
            } else {
                for (int i = 0; i < n; i++) {
                    ip.push(ip.SOSSpop());
                }
            }
        } else if (instr == 'g') {
            ip.push(getWithSO(ip.vecPop(dim), ip));
        } else if (instr == 'p') {
            putWithSO(ip.vecPop(dim), ip.pop(), ip);
        } else if (instr == '.') {
            System.out.print(ip.pop() + " ");
        } else if (instr == ',') {
            System.out.print((char)ip.pop());
        } else if (instr == '&') {
            int r;
            try {
                while ((r = in.read()) != -1) {
                    if (r >= '0' && r <= '9') {
                        break;
                    }
                }
                if (r == -1) throw new IOException();
                String numstr = Character.toString((char)r);
                while ((r = in.read()) >= '0' && r <= '9') {
                    in.mark(100);
                    numstr += (char)r;
                }
                in.reset();
                ip.push(Integer.parseInt(numstr));
            } catch (IOException e) {
                ip.reflect(); // EOF
            }
        } else if (instr == '~') {
            try {
                int r = in.read();
                if (r == -1) throw new IOException();
                ip.push((char)r);
            } catch (IOException e) {
                ip.reflect(); // EOF
            }
        } else if (instr == 'i') {
            String fileName = ip.strPop();
            int flags = ip.pop();
            boolean binary = false;
            if ((flags & 1) == 1) {
                binary = true;
            }
            Vector<Integer> va = ip.vecPop(dim);
            String str;
            try {
                str = String.join("\n", Files.readAllLines(Paths.get(fileName)));
            } catch (IOException e) {
                ip.reflect();
                return;
            }
            int x = 0;
            int maxX = 0;
            int y = 0;
            int maxY = 0;
            int z = 0;
            for (int i = 0; i < str.length(); i++) {
                if (dim > 1 && str.charAt(i) == '\n' && !binary) {
                    if (x > maxX) maxX = x;
                    x = 0;
                    y++;
                    continue;
                }
                if (dim > 2 && str.charAt(i) == '\f' && !binary) {
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                    x = y = 0;
                    z++;
                    continue;
                }
                if (str.charAt(i) == ' ') {
                    x++;
                    continue;
                }
                Vector<Integer> vb = (Vector<Integer>)va.clone();
                vb.set(0, vb.get(0) + x++);
                if (dim > 1) vb.set(1, vb.get(1) + y);
                if (dim > 2) vb.set(2, vb.get(2) + z);
                put(vb, (int)str.charAt(i));
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
            Vector<Integer> vb = new Vector<>(dim);
            if (dim == 1) vb.add(x+1);
            else if (dim > 1) vb.add(maxX);
            if (dim == 2) vb.add(y+1);
            else if (dim > 2) vb.add(maxY);
            if (dim > 2) vb.add(z+1);
            for (int i = 3; i < dim; i++) {
                vb.add(0);
            }
            ip.vecPush(vb);
            ip.vecPush(va);
        } else if (instr == 'o') {
            String fileName = ip.strPop();
            int flags = ip.pop();
            boolean linear = false;
            if ((flags & 1) == 1) {
                linear = true;
            }
            Vector<Integer> va = ip.vecPop(dim);
            Vector<Integer> vb = ip.vecPop(dim);
            FileWriter out;
            try {
                out = new FileWriter(new File(fileName));
            } catch (IOException e) {
                ip.reflect();
                return;
            }
            Vector<Integer> offset = new Vector<>();
            for (int i = 0; i < dim; i++) {
                offset.add(0);
            }
            String file = "";
            while (true) {
                Vector<Integer> newVec = (Vector<Integer>)va.clone();
                for (int i = 0; i < dim; i++) {
                    newVec.set(i, newVec.get(i) + offset.get(i));
                }
                file += (char)get(newVec);
                int i = 0;
                do {
                    offset.set(i, offset.get(i) + 1);
                    for (int j = i-1; j >= 0; j--) {
                        offset.set(j, 0);
                    }
                    if (i == 1) {
                        file += "\n";
                    }
                    if (i == 2) {
                        file += "\f";
                    }
                    i++;
                } while (i < dim && offset.get(i-1) > vb.get(i-1));
                if (i == dim && offset.get(i-1) > vb.get(i-1)) break;
            }
            if (linear) file = file.replaceAll(" +$", "").replaceAll("\\n+\\Z", "");
            try {
                out.write(file);
                out.close();
            } catch (IOException e) {
                ip.reflect();
            }
        } else if (instr == '=') {
            try {
                Process p = Runtime.getRuntime().exec(ip.strPop());
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));
                String s;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }
                stdInput.close();
                ip.push(0);
            } catch (Exception e) {
                ip.push(1);
            }
        } else if (instr == 'y') {
            int arg = ip.pop();
            int stackSize = ip.getTOSS().size();
            int len = 0;
            ip.push(0); len++; // start of env variables
            Map<String, String> env = System.getenv();
            ArrayList<String> keys = new ArrayList<>(env.keySet());
            keys.sort(String::compareToIgnoreCase);
            for (String key : keys) {
                String newstr = key + "=" + env.get(key);
                ip.strPush(newstr);
                len += newstr.length() + 1;
            }
            ip.push(0); ip.push(0); len+= 2; // start of command line arguments
            for (String carg : Main.args) {
                ip.strPush(carg);
                len += carg.length() + 1;
            }
            for (LinkedList<Integer> stack : ip.stackStack) {
                ip.push(stack.size()); len++;
            }
            ip.pop();
            ip.push(stackSize);
            ip.push(ip.stackStack.size()); len++;
            LocalDateTime dt = LocalDateTime.now();
            ip.push(dt.getHour()*256*256 + dt.getMinute()*256 + dt.getSecond()); len++;
            ip.push((dt.getYear()-1900)*256*256 + dt.getMonthValue()*256 + dt.getDayOfMonth()); len++;
            if ((arg > dim*3+9 && arg <= dim*5+9) || arg <= 0) {
                LinkedList<Vector<Integer>> sorted = new LinkedList<>(data.keySet());
                LinkedList<Integer> mins = new LinkedList<>();
                for (int i = 0; i < dim; i++) {
                    final int kek = i;
                    sorted.sort((x,y) -> x.get(kek).compareTo(y.get(kek)));
                    int min = sorted.get(0).get(i);
                    int max = sorted.get(sorted.size()-1).get(i);
                    ip.push(max - min); len++;
                    mins.add(min); len++;
                }
                mins.forEach(ip::push);
            } else {
                for (int i = 0; i < dim*2; i++) {
                    ip.push(0); len++; // hack to make y more efficient when bounds aren't asked for.
                }
            }
            ip.vecPush(ip.storageOffset); len += dim;
            ip.vecPush(ip.delta); len += dim;
            ip.vecPush(ip.pos); len += dim;
            ip.push(0); len++; // "team number", not relevant for this implementation
            ip.push(ip.id); len++;
            ip.push(dim); len++;
            ip.push(File.separatorChar); len++;
            ip.push(1); len++; // '=' semantics; 1 means equivalent to a C system() call.
            ip.push(100); len++; // version number (1.0.0)
            ip.push(Utility.encodeString("FUNJ")); len++; // Handprint; in this case, "FUNJ" (for FungeJ)
            ip.push(4); len++; // amount of bytes per cell
            ip.push(0b01111); len++; // several flags for implementation of certain facets of Funge-98:
                                     // byte 0: `t` implemented
                                     // byte 1: `i` implemented
                                     // byte 2: `o` implemented
                                     // byte 3: `=` implemented
                                     // byte 4: unbuffered input
                                     // in this case, I use buffered input, and all of the above instructions are implemented
            if (arg > 0) {
                int temp;
                if (arg <= ip.getTOSS().size()) {
                    temp = ip.getTOSS().get(arg-1);
                } else {
                    temp = 0;
                }
                for (int i = 0; i < len; i++) {
                    ip.pop();
                }
                ip.push(temp);
            }
        } else if ('A' <= instr && instr <= 'Z') {
            int num = instr - 'A';
            if (ip.semantics[num].isEmpty()) {
                ip.reflect();
                System.out.println("[FungeJ] WARNING: unloaded semantic " + (char)instr + " called at " + ip.pos + ".");
            } else {
                ip.semantics[num].peek().accept(ip);
            }
        } else if (instr == '(') {
            int r = 0;
            int n = ip.pop();
            for (int i = 0; i < n; i++) {
                r <<= 8;
                r |= ip.pop();
            }
            if (Fingerprint.loadFingerprint(ip, r)) {
                ip.push(r);
                ip.push(1);
            }
            if (ip.switchMode) {
                put(ip.pos, (int)')');
            }
        } else if (instr == ')') {
            int r = 0;
            int n = ip.pop();
            for (int i = 0; i < n; i++) {
                r <<= 8;
                r |= ip.pop();
            }
            Fingerprint.unloadFingerprint(ip, r);
            if (ip.switchMode) {
                put(ip.pos, (int)'(');
            }
        } else if (instr == 'z') {
            // kek it does nothing
        } else if (instr == 't') {
            new IP(ip).step();
        }
        else {
            ip.reflect();
            System.out.println("[FungeJ] WARNING: unknown instruction " + instr + " encountered at " + ip.pos +
                    " by IP #" + ip.id + ".");
        }
    }

    public void doStep() {
        for (int i = 0; i < ips.size(); i++) {
            IP ip = ips.get(i);
            doInstr(ip, get(ip.pos));
            ip.move();
            if (!ips.contains(ip)) {
                i--;
            }
        }
        if (ips.isEmpty()) {
            end = true;
        }
    }

    public void runProgram() {
        new IP(this);
        while (true) {
            doStep();
            if (end) break;
        }
    }

    public FungeSpace(int dim) {
        this.dim = dim;
        minVals = new Vector<>(dim);
        maxVals = new Vector<>(dim);
        for (int i = 0; i < dim; i++) {
            minVals.add(new LinkedList<>());
            maxVals.add(new LinkedList<>());
            minVals.get(i).add(0);
            maxVals.get(i).add(0);
        }
        data = new HashMap<>();
    }

    public FungeSpace(String program, int dim) {
        this(dim);
        int x = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i < program.length(); i++) {
            if (dim > 1 && program.charAt(i) == '\n') {
                x = 0;
                y++;
                continue;
            }
            if (dim > 2 && program.charAt(i) == '\f') {
                x = y = 0;
                z++;
                continue;
            }
            if (program.charAt(i) == '\f') {
                continue;
            }
            Vector<Integer> putVec = new Vector<>();
            putVec.add(x++);
            putVec.add(y);
            putVec.add(z);
            for (int j = 3; j < dim; j++) {
                putVec.add(0);
            }
            put(putVec, (int)program.charAt(i));
        }
    }
}
