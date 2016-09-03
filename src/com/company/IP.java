package com.company;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class IP {
    LinkedList<Consumer<IP>> semantics[] = new LinkedList[26];

    public long timerMark = 0; // HRTI fingerprint

    public boolean hoverMode = false; //MODE fingerprint
    public boolean invertMode = false;
    public boolean queueMode = false;
    public boolean switchMode = false;

    public FungeSpace space;
    public int id;
    public Vector<Integer> pos;
    public Vector<Integer> delta;
    public Vector<Integer> storageOffset;
    public boolean stringMode;
    public LinkedList<LinkedList<Integer>> stackStack;
    public LinkedList<Integer> getTOSS() {
        return stackStack.peek();
    }
    public LinkedList<Integer> getSOSS() {
        if (stackStack.size() == 1) return null;
        return stackStack.get(1);
    }
    public int pop() {
        if (getTOSS().isEmpty()) return 0;
        if (queueMode) return getTOSS().removeLast();
        return getTOSS().pop();
    }
    public Vector<Integer> vecPop(int dim) {
        Integer arr[] = new Integer[dim];
        for (int i = dim-1; i >= 0; i--) {
            arr[i] = pop();
        }
        return new Vector<>(Arrays.asList(arr));
    }
    public String strPop() {
        String str = "";
        int val = pop();
        while (val != 0) {
            str += (char)val;
            val = pop();
        }
        return str;
    }
    public int SOSSpop() {
        if (getSOSS().isEmpty()) return 0;
        if (queueMode) return getSOSS().removeLast();
        return getSOSS().pop();
    }
    public Vector<Integer> SOSSvecPop(int dim) {
        Integer arr[] = new Integer[dim];
        for (int i = dim-1; i >= 0; i--) {
            arr[i] = SOSSpop();
        }
        return new Vector<>(Arrays.asList(arr));
    }
    public int push(int val) {
        if (invertMode) {
            getTOSS().addLast(val);
            return val;
        }
        getTOSS().push(val);
        return val;
    }
    public Vector<Integer> vecPush(Vector<Integer> vec) {
        vec.forEach(this::push);
        return vec;
    }
    public String strPush(String str) {
        push(0);
        for (int i = str.length() - 1; i >= 0; i--) {
            push(str.charAt(i));
        }
        return str;
    }
    public int SOSSpush(int val) {
        if (invertMode) {
            getSOSS().addLast(val);
            return val;
        }
        getSOSS().push(val);
        return val;
    }
    public Vector<Integer> SOSSvecPush(Vector<Integer> vec) {
        vec.forEach(this::SOSSpush);
        return vec;
    }
    public String toString() {
        return "IP #" + id + " [\n" +
                "  pos " + pos + ", delta " + delta + "\n" +
                "  Stack stack " + stackStack + "\n" +
                "]";
    }
    public void reflect() {
        for (int i = 0; i < space.dim; i++) {
            delta.set(i, -delta.get(i));
        }
    }
    public void step() {
        for (int i = 0; i < space.dim; i++) {
            pos.set(i, pos.get(i) + delta.get(i));
        }
    }
    public void move() {
        boolean skip = false;
        boolean sgml = true;
        do {
            step();
            if (sgml && space.get(pos) == ' ' && stringMode) {
                sgml = false;
                push(' ');
            }
            for (int i = 0; i < space.dim; i++) {
                if (pos.get(i) < space.minVals.get(i).peek() || pos.get(i) > space.maxVals.get(i).peek()) {
                    wrap();
                    break;
                }
            }
            if (space.get(pos) == ';' && !stringMode) {
                skip = !skip;
                if (!skip) {
                    step();
                    if (space.get(pos) == ';') {
                        skip = true;
                    }
                }
            }
        } while (space.get(pos) == ' ' || skip);
    }
    public void wrap() {
        reflect();
        boolean stillOOB = false;
        do {
            step();
            for (int i = 0; i < space.dim; i++) {
                if (pos.get(i) < space.minVals.get(i).peek() || pos.get(i) > space.maxVals.get(i).peek()) {
                    stillOOB = true;
                    break;
                }
                if (i == space.dim - 1) {
                    stillOOB = false;
                }
            }
        } while (stillOOB);
        boolean done = false;
        while (!done) {
            step();
            for (int i = 0; i < space.dim; i++) {
                if (pos.get(i) < space.minVals.get(i).peek() || pos.get(i) > space.maxVals.get(i).peek()) {
                    done = true;
                    break;
                }
            }
        }
        reflect();
    }
    public Vector<Integer> next() {
        Vector<Integer> returnvec = (Vector<Integer>) pos.clone();
        for (int i = 0; i < space.dim; i++) {
            returnvec.set(i, returnvec.get(i) + delta.get(i));
        }
        return returnvec;
    }
    public Vector<Integer> nextMov() {
        Vector<Integer> tempPos = (Vector<Integer>) pos.clone();
        move();
        Vector<Integer> returnval = pos;
        pos = tempPos;
        return returnval;
    }
    public IP(FungeSpace space) {
        pos = new Vector<>(space.dim);
        delta = new Vector<>(space.dim);
        storageOffset = new Vector<>(space.dim);
        for (int i = 0; i < space.dim; i++) {
            pos.add(0);
            delta.add(0);
            storageOffset.add(0);
        }
        delta.set(0, 1);
        stackStack = new LinkedList<>();
        stackStack.add(new LinkedList<>());
        id = space.currIPID++;
        this.space = space;
        space.ips.add(this);
        for (int i = 0; i < semantics.length; i++) {
            semantics[i] = new LinkedList<>();
        }
    }
    public IP(IP ip) {
        pos = (Vector<Integer>)ip.pos.clone();
        delta = (Vector<Integer>)ip.delta.clone();
        storageOffset = (Vector<Integer>)ip.storageOffset.clone();
        stackStack = new LinkedList<>();
        for (LinkedList<Integer> stack : ip.stackStack) {
            stackStack.push((LinkedList<Integer>)stack.clone());
        }
        space = ip.space;
        id = space.currIPID++;
        space.ips.add(this);
        reflect();
        for (int i = 0; i < semantics.length; i++) {
            semantics[i] = (LinkedList<Consumer<IP>>)ip.semantics[i].clone();
        }
    }
}
