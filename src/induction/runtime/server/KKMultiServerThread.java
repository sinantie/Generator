package induction.runtime.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author sinantie
 */
public class KKMultiServerThread extends Thread
{

    private Socket socket = null;

    public KKMultiServerThread(Socket socket)
    {
        super("KKMultiServerThread");
        this.socket = socket;
    }

    @Override
    public void run()
    {

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    socket.getInputStream()));

            String inputLine, outputLine;
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye")) {
                    break;
                }
            }
            out.close();
            in.close();
            socket.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
