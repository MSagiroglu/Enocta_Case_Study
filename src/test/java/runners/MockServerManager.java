package runners;

public class MockServerManager {
    private static Process nodeProcess;
    private static boolean isStarted = false;

    public static synchronized void startServer() {
        if (isStarted) return;
        try {
            ProcessBuilder pb = new ProcessBuilder("node", "mock-server/server.js");
            pb.redirectErrorStream(true);
            nodeProcess = pb.start();
            System.out.println("===> Started Mock Server programmatically! <===");
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if(nodeProcess != null) {
                    nodeProcess.destroy();
                    System.out.println("===> Stopped Mock Server <===");
                }
            }));
            
            Thread.sleep(3000);
            isStarted = true;
        } catch(Exception e) {
            System.err.println("Could not start mock server. Ensure Node.js is installed. Error: " + e.getMessage());
        }
    }
}
