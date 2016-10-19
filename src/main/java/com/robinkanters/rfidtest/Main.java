package com.robinkanters.rfidtest;

import javax.smartcardio.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.String.format;
import static java.lang.System.*;

public class Main extends AuthorizationFSM implements CardEventListener {
    private static final String MASTER_KEY = "4EE7D3A8";

    private List<String> authorizedUids = new ArrayList<>();
    private String scannedUid;

    public static void main(String[] args) {
        new Main();
    }

    private Main() {
        try {
            final CardTerminals terminals = TerminalFactory.getDefault().terminals();
            CardTerminalMonitor monitor = new CardTerminalMonitor(this);
            for (CardTerminal cardTerminal : terminals.list()) {
                monitor.monitorTerminal(cardTerminal);
                printTerminalInfo(cardTerminal);
            }

            Scanner s = new Scanner(in).useDelimiter("\n");
            while (s.hasNext()) {
                String line = s.next();
                if (line.trim().equalsIgnoreCase("exit"))
                    return;
            }
        } catch (Exception e) {
            err.println(e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(e.hashCode());
        }

        System.exit(0);
    }

    private void printTerminalInfo(CardTerminal cardTerminal) throws CardException {
        final String present = cardTerminal.isCardPresent() ? "" : " not";
        out.printf("%s, card%s present\n", cardTerminal.getName(), present);
    }

    public void cardInserted(CardTerminal terminal, Card card) {
        scannedUid = requestUID(card.getBasicChannel());

        if (MASTER_KEY.equals(scannedUid))
            Master();
        else
            Card();
    }

    private void speakAloud(String message) {
        try {
            Runtime.getRuntime().exec(format("say \"%s\"", message));
        } catch (IOException e) {
            err.println("Failed to speak: " + e.getMessage());
        }
    }

    private String requestUID(CardChannel channel) {
        byte[] baReadUID = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00,
                (byte) 0x00, (byte) 0x00};

        return send(baReadUID, channel).substring(0, 8);
    }

    protected void authCard() {
        if (isAuthorized()) {
            out.printf("%s already authorized%n", scannedUid);
            return;
        }

        authorizedUids.add(scannedUid);
        out.printf("%s authorized!%n", scannedUid);
        speakAloud("Card authorized");
    }

    private boolean isAuthorized() {
        return authorizedUids.contains(scannedUid);
    }

    protected void deauthCard() {
        if (!isAuthorized()) {
            out.printf("%s was not authorized%n", scannedUid);
            return;
        }

        authorizedUids.remove(scannedUid);
        out.printf("%s deauthorized!%n", scannedUid);
        speakAloud("Card deauthorized");
    }

    private String send(byte[] cmd, CardChannel channel) {
        byte[] baResp = new byte[258];
        ByteBuffer commandBuffer = ByteBuffer.wrap(cmd);
        ByteBuffer responseBuffer = ByteBuffer.wrap(baResp);

        int output = 0;
        try {
            output = channel.transmit(commandBuffer, responseBuffer);
        } catch (CardException ex) {
            ex.printStackTrace();
        }

        String res = "";
        for (int i = 0; i < output; i++) {
            res += format("%02X", baResp[i]);
        }

        return res;
    }

    public void cardRemoved(CardTerminal terminal) {
        // do nothing
    }

    public void unhandledTransition(String state, String event) {
        err.printf("WTF: Should never happen; from %s to %s%n", state, event);
    }

    protected void beginAuth() {
        out.printf("Connect card to authorize  : ");
    }

    protected void beginDeauth() {
        out.printf("\nConnect card to deauthorize: ");
    }

    protected void cancel() {
        out.println("\nOK, auth action cancelled");
    }

    protected void checkAuth() {
        if (isAuthorized()) {
            final String accessGranted = "Access granted";

            out.println(accessGranted);
            speakAloud(accessGranted);
        } else {
            err.println("ALERT! UNAUTHORIZED ACCESS DETECTED!");
            speakAloud("Unauthorized access detected");
        }
    }

}
