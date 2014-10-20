package at.ac.tuwien.dsg.smartsociety.demo.peer;

import javax.swing.*;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerApplication {

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PeerApplicationFrame("http://localhost:9091/response");
            }
        });
    }
}
