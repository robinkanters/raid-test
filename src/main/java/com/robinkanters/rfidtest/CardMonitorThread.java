package com.robinkanters.rfidtest;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

class CardMonitorThread extends Thread {
    private final CardEventListener listener;
    private CardTerminal terminal;

    CardMonitorThread(CardEventListener listener, CardTerminal terminal) {
        this.listener = listener;
        this.terminal = terminal;
    }

    public void run() {
        try {
            tryRun();
        } catch (InterruptedException | CardException ignored) {}

        run();
    }

    private void tryRun() throws InterruptedException, CardException {
        if (isCardPresent()) {
            terminal.waitForCardAbsent(50);
            if (isCardPresent()) return;

            listener.cardRemoved(terminal);
        } else {
            terminal.waitForCardPresent(50);
            if (!isCardPresent()) return;

            final Card card = terminal.connect("*");
            listener.cardInserted(terminal, card);
        }
    }

    private boolean isCardPresent() throws CardException {
        return terminal.isCardPresent();
    }
}
