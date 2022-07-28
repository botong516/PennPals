package org.cis120;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Initializes and starts a {@link ServerBackend}, a {@link ServerModel},
 * and a very basic UI to indicate that the server is running. When that
 * UI is closed, using the standard operating system "X" control, the
 * server is shut down.
 *
 * You do not need to modify this file.
 */

public final class ServerMain {

    public static void main(String[] args) {
        final JFrame frame = new JFrame("PennPals Server");
        JLabel message = new JLabel(
                "<html>Hello, my name is Grace.<br />" +
                        "I will be your server for the evening. <br /><br />" +
                        "(This window lets you know that the server is running, " +
                        "but does not do anything else.)</html>"
        );
        message.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(message);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(250, 150));

        final ServerModel state = new ServerModel();
        final ServerBackend server = new ServerBackend(state);
        final Timer timer = new Timer(100, null);

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!frame.isDisplayable() || !server.isRunning()) {
                    frame.dispose();
                    server.stop();
                    timer.stop();
                }
            }
        });
        timer.start();
        new Thread(server, "Connection acceptor").start();
        frame.setVisible(true);
    }

    // Prevents the instantiation of ServerMain objects
    private ServerMain() {
    }
}
