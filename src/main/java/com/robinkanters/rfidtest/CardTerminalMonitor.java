package com.robinkanters.rfidtest;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

class CardTerminalMonitor implements CardEventListener {
    private List<CardEventListener> listeners = new ArrayList<>();

    CardTerminalMonitor(CardEventListener... listeners) {
        this.listeners.addAll(asList(listeners));
    }

    void monitorTerminal(CardTerminal... terminals) {
        asList(terminals).forEach(this::startMonitor);
    }

    private void startMonitor(CardTerminal terminal) {
        final Thread thread = new CardMonitorThread(this, terminal);
        thread.start();
    }

    public void cardInserted(CardTerminal terminal, Card card) {
        listeners.forEach(l -> l.cardInserted(terminal, card));
    }

    public void cardRemoved(CardTerminal terminal) {
        listeners.forEach(l -> l.cardRemoved(terminal));
    }
}
