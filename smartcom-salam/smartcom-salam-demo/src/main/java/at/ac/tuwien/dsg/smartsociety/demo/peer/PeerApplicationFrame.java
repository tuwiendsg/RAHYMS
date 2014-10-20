package at.ac.tuwien.dsg.smartsociety.demo.peer;

import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerApplicationFrame extends JFrame {

    private final Client client;

    private JLabel status;
    private JComboBox peerSelection;

    private JLabel messagesHeader;

    private Identifier peer;
    private List<MessageWrapper> currentPeerMessages;
    private Map<Identifier, List<MessageWrapper>> peerMessages = new HashMap<>();
    private MessageTableModel tableModel;

    private final List<String[]> tableDataList = new ArrayList<>();
    private final String url;

    public PeerApplicationFrame(String url) {
        this.url = url;
        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT,    1000)
                .build();

        setTitle("SmartCom-SALAM Peer GUI");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        PeerApplicationRestServer server = new PeerApplicationRestServer(8080, "peer", this);
        server.start();

        peer = null;

        prepareComponents();

        pack();
        setVisible(true);
    }

    private void prepareComponents() {
        setLayout(new BorderLayout());

        JPanel peerSelectionPanel = new JPanel();
        peerSelectionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,3,3));
        peerSelectionPanel.add(new JLabel("Select a peer:"));
        peerSelection = new JComboBox();
        peerSelectionPanel.add(peerSelection);
        JButton selectButton = new JButton("show messages");
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPeer((String) peerSelection.getSelectedItem());
            }
        });
        peerSelectionPanel.add(selectButton);
        add(peerSelectionPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        messagesHeader = new JLabel("No peer selected", SwingConstants.LEFT);
        mainPanel.add(messagesHeader, BorderLayout.NORTH);

        tableModel = new MessageTableModel();
        final JTable table = new JTable(tableModel) {

            private Border outside = new MatteBorder(1, 0, 1, 0, Color.RED);
            private Border inside = new EmptyBorder(0, 1, 0, 1);
            private Border highlight = new CompoundBorder(outside, inside);

            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                JComponent jc = (JComponent)c;

                c.setBackground(getBackground());

                MessageWrapper messageWrapper = currentPeerMessages.get(row);
                if (!messageWrapper.isRead()) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setBackground(Color.LIGHT_GRAY);
                }

                if (isRowSelected(row)) {
                    jc.setBorder(highlight);
                }

                return c;
            }
        };

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MessageWrapper messageWrapper = currentPeerMessages.get(table.getSelectedRow());
                messageWrapper.setRead();

                if (e.getClickCount()%2 == 0 && table.getSelectedRow() >= 0) {
                    createFrameForMessage(messageWrapper.getMessage());
                }
            }
        });
        table.setPreferredScrollableViewportSize(new Dimension(570, 250));
        table.setFillsViewportHeight(true);
        JScrollPane messagePane = new JScrollPane(table);
        mainPanel.add(messagePane);
        add(mainPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusPanel, BorderLayout.SOUTH);

        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        status = new JLabel("status");
        status.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(status);
    }

    private void createFrameForMessage(final Message msg) {
        final JFrame frame = new JFrame("Message for peer '"+peer.getId()+"'");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLayout(new BorderLayout(3,3));

        JPanel panel = new JPanel(new GridLayout(9, 2, 3, 3));
        panel.add(new JLabel("Id"));
        panel.add(createLabel(msg.getId().getId()));
        panel.add(new JLabel("Type"));
        panel.add(createLabel(msg.getType()));
        panel.add(new JLabel("Subtype"));
        panel.add(createLabel(msg.getSubtype()));
        panel.add(new JLabel("SenderId"));
        panel.add(createLabel(msg.getSenderId().getId()));
        panel.add(new JLabel("ReceiverId"));
        panel.add(createLabel(msg.getReceiverId().getId()));
        panel.add(new JLabel("ConversationId"));
        panel.add(createLabel(msg.getConversationId()));
        panel.add(new JLabel("TTL"));
        panel.add(createLabel(msg.getTtl() + ""));
        panel.add(new JLabel("Language"));
        panel.add(createLabel(msg.getLanguage()));
        panel.add(new JLabel("SecurityToken"));
        panel.add(createLabel(msg.getSecurityToken()));

        frame.add(panel, BorderLayout.NORTH);

        JPanel panel2 = new JPanel(new BorderLayout(3,3));
        panel2.add(new JLabel("Content"), BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(msg.getContent());
        area.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        panel2.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel2, BorderLayout.CENTER);

        JPanel panel3 = new JPanel(new GridLayout(1, 3, 3, 3));
        JButton acceptButton = new JButton("accept");
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message message = msg.clone();
                message.setSenderId(message.getReceiverId());
                message.setReceiverId(null);
                message.setSubtype("ACCEPT");
                message.setContent(null);
                sendMessage(message);

                frame.dispose();
            }
        });
        panel3.add(acceptButton);

        JButton delegateButton = new JButton("delegate");
        delegateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message message = msg.clone();
                message.setSenderId(message.getReceiverId());
                message.setReceiverId(null);
                message.setSubtype("delegate");
                message.setContent(null);
                sendMessage(message);

                frame.dispose();
            }
        });
        panel3.add(delegateButton);

        JButton terminateButton = new JButton("terminate");
        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message message = msg.clone();
                message.setSenderId(message.getReceiverId());
                message.setReceiverId(null);
                message.setSubtype("terminate");
                message.setContent(null);
                sendMessage(message);

                frame.dispose();
            }
        });
        panel3.add(terminateButton);


        frame.add(panel3, BorderLayout.SOUTH);

        //Display the window.
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel createLabel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new CompoundBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY), null));
        JLabel id = new JLabel(text);
        panel.setBackground(Color.WHITE);
        panel.add(id);
        return panel;
    }

    private void sendMessage(Message msg) {
        try {
            msg.setId(null);
            msg.setReceiverId(null);
            WebTarget target = client.target(url);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new JsonMessageDTO(msg)), Response.class);

            if (response.getStatus() != Response.Status.CREATED.getStatusCode() &&
                    response.getStatus() != Response.Status.OK.getStatusCode()) {
                status.setText("Could not send message!");
            } else {
                status.setText("Message sent successfully!");
            }
        } catch (Exception e) {
            status.setText("ERROR: Could not send message!");
        }
    }

    private void setPeer(String selectedItem) {
        if (selectedItem == null) {
            return;
        }
        Identifier oldPeer = peer;
        peer = Identifier.peer(selectedItem);
        currentPeerMessages = Collections.synchronizedList(peerMessages.get(peer));
        messagesHeader.setText("Messages for peer: '"+selectedItem+"'");

        if (oldPeer != null && oldPeer.equals(peer)) {
            refreshTable();
        } else {
            synchronized (tableDataList) {
                tableDataList.clear();
                for (MessageWrapper message : currentPeerMessages) {
                    String[] tableData = createTableEntryForMessage(message.getMessage());
                    tableDataList.add(tableData);
                }
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void refreshTable() {

    }

    protected synchronized void onMessage(Message message) {
        List<MessageWrapper> messages = peerMessages.get(message.getReceiverId());
        if (messages == null) {
            messages = new ArrayList<MessageWrapper>();
            peerMessages.put(message.getReceiverId(), messages);

            updatePeerSelection(message.getReceiverId());
        }

        messages.add(new MessageWrapper(message));

        if (message.getReceiverId().equals(peer)) {
            synchronized (tableDataList) {
                String[] tableData = createTableEntryForMessage(message);
                tableDataList.add(tableData);
                tableModel.fireTableDataChanged();
            }
        }

        status.setText("New message for peer: "+message.getReceiverId().getId());
    }

    private String[] createTableEntryForMessage(Message message) {
        int contentLength = message.getContent().length();
        return new String[]{
                message.getId().getId(),
                message.getType(),
                message.getSubtype(),
                message.getSenderId().getId(),
                message.getContent().substring(0, (contentLength > 20 ? 20 : contentLength))
        };
    }

    private synchronized void updatePeerSelection(Identifier receiverId) {
        synchronized (peerSelection) {
            peerSelection.addItem(receiverId.getId());
//            SwingUtilities.updateComponentTreeUI(this);
        }
    }

    class MessageTableModel extends AbstractTableModel {
        private String[] columnNames = {"Id", "Type", "Subtype", "Sender", "Content"};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            synchronized (tableDataList) {
                return tableDataList.size();
            }
        }

        public String getColumnName(int col) {
            synchronized (tableDataList) {
                return columnNames[col];
            }
        }

        public Object getValueAt(int row, int col) {
            synchronized (tableDataList) {
                return tableDataList.get(row)[col];
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private class MessageWrapper {
        private Message message;
        private boolean read = false;

        private MessageWrapper(Message message) {
            this.message = message;
        }

        public void setRead() {
            read = true;
        }

        public Message getMessage() {
            return message;
        }

        public boolean isRead() {
            return read;
        }
    }
}
