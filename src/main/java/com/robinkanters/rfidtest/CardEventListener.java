package com.robinkanters.rfidtest;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

public interface CardEventListener {
    void cardInserted(CardTerminal terminal, Card card);
    void cardRemoved(CardTerminal terminal);
}
