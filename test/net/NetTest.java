/*
 * NetTest.java - Network code testing
 * Michael Dippery <mdippery@bucknell.edu>
 * 2008-01-20
 */

import snodes.net.*;


public final class NetTest
{
    private static final PacketListener pl = new PacketListener() {
        public void processPacket(SnodesConnection conn, Packet packet) {
            System.out.println("Got packet: " + packet.getType() + " from: " + conn);
        }
    };
    
    private static final PacketFilter pf = new PacketFilter() {
        public boolean accept(Packet.Type type) {
            return true;
        }
    };
    
    public static void main(String[] args) throws Exception
    {
        System.err.println("Running network tests...");
        
        SnodesServer server = SnodesServer.getInstance();
        /*
        ServerListener sl = new ServerListener() {
            public void processConnection(SnodesConnection conn) {
                System.out.println("Received new connection request: " + conn);
                try {
                    authorize(conn);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        };
        server.addListener(sl);
        */
        
        server.start();
        
        SnodesConnection local = new SnodesConnection("127.0.0.1");
        local.addListener(pl, pf);
        local.authenticate("mypasskey");
        local.connect();
        
        FileTransfer ft = local.createTransfer("myfile");
        PacketListener ftl = new PacketListener() {
            public void processPacket(SnodesConnection ft, Packet p) {
                System.out.println("Received file transfer packet: " + ft);
                System.out.println("  Type: " + p.getType());
            }
        };
        PacketFilter ftf = new PacketFilter() {
            public boolean accept(Packet.Type type) {
                if (type == Packet.Type.TransferFile) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        local.addListener(ftl, ftf);
        
        byte[] bytes = new byte[4];
        bytes[0] = bytes[1] = bytes[2] = bytes[3] = (byte) 94;
        ft.send(bytes, 1, 8L);
    
        local.disconnect();
    }
    
    private static void authorize(SnodesConnection conn) throws Exception
    {
        byte[] key = new byte[4];
        
        for (int i = 92; i < 96; i++) {
            key[i-92] = (byte) i;
        }
        
        conn.addListener(pl, pf);
        //conn.setID(4);
        conn.authenticate("mypasskey");
        conn.authorize(1, key);
    }
    
    private NetTest() {}
}
