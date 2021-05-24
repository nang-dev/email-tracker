import java.net.*; 
import java.io.*; 
import java.time.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.Math;
import java.nio.charset.StandardCharsets;


// Twilio
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

// EmailTracker is a functioning web file server that serves files in ./content/
public class EmailTracker { 

    public static void main(String[] args) throws IOException { 
        ServerSocket serverSocket = null; 
        int portNumber = Integer.parseInt(args[0]);
        String timeStr = getHTTPTime();

        try { 
            serverSocket = new ServerSocket(portNumber); 
        } 
        catch (IOException e) { 
            System.err.println("Could not listen on port: " + portNumber); 
            System.err.println(e);
            System.exit(1); 
        } 

        while(true) {
            Socket clientSocket = null; 
            try { 
                // Wait for connection
                clientSocket = serverSocket.accept(); 
            } 
            catch (IOException e) { 
                System.err.println("Accept failed."); 
                System.exit(1); 
            } 

            try {
                System.out.println("Connected");
                serveClient(clientSocket);
            }
            catch(Exception e) {
                System.out.println("Exception!" + e);
            }

        }
        //serverSocket.close(); 
    } 

    public static void serveClient(Socket clientSocket) throws IOException {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream()); 
        BufferedReader in = new BufferedReader( 
                new InputStreamReader( clientSocket.getInputStream())); 

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            String [] inputSplit = inputLine.split(" ");
            System.out.print(inputLine);
            // We only care about GET requests
            if(inputSplit[0].equals("GET")) {
                // Read rest of request:
                while((inputLine = in.readLine()).length() > 0) {
                    System.out.print(inputLine);
                }
                try {
                    String [] sentMsg = inputSplit[1].split("/");
                    String fileName = sentMsg[1];
                    String receiver = sentMsg[2];
                    String subject = sentMsg[3];
                    
                    File requestedFile = new File("./" + fileName);
                    if(!requestedFile.exists()) {
                        replyFileNotFound(out);
                        continue;
                    }
                    int fileLength = (int)requestedFile.length();
                    String fileSuffix = fileName.split("\\.")[1];
                    String contentType = getContentType(fileSuffix);

                    processGET(fileName, receiver, subject, out);
                }
                catch(Exception e) {
                    System.err.println(e);
                }
            }
        } 
        out.close(); 
        in.close(); 
        clientSocket.close(); 
    }

    public static String getHTTPTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(cal.getTime());
    }

    public static void replyFileNotFound(DataOutputStream out) throws IOException {
        String msg = "HTTP/1.1 404 Not Found\r\n";
        out.writeBytes(msg); 
    }

    public static void replyFileInternalError(DataOutputStream out) throws IOException {
        String msg = "HTTP/1.1 500 Internal Server Error\r\n";
        out.writeBytes(msg); 
    }

    public static String getContentType(String fileSuffix) {
        /*
            text/plain (.txt)
            text/css (.css)
            text/html (.htm, .html)
            image/gif (.gif)
            image/jpeg (.jpg, .jpeg)
            image/png (.png)
            video/webm (.webm)
            video/mp4 (.mp4)
            application/javascript (.js)
            application/ogg (.ogg)
            application/octet-stream (anything else)
        */
        switch(fileSuffix) {
            case "txt":
                return "text/plain";
            case "css":
                return "text/css";
            case "htm":
                return "text/html";
            case "html":
                return "text/html";
            case "gif":
                return "image/gif";
            case "jpg":
                return "image/jpeg";
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webm":
                return "video/webm";
            case "mp4":
                return "video/mp4";
            case "js":
                return "application/javascript";
            case "ogg":
                return "application/ogg";
            default:
                return "application/octet-stream";
        }
    }

    public static void processGET(String fileName, String receiver, String subject, DataOutputStream out) throws IOException, FileNotFoundException {
        File requestedFile = new File("./" + fileName);
        if(!requestedFile.exists()) {
            replyFileNotFound(out);
            return;
        }
        int fileLength = (int)requestedFile.length();
        String fileSuffix = fileName.split("\\.")[1];
        sendWholeFile(requestedFile, fileSuffix, out);
        sendText(receiver, subject);
    }

    public static void sendWholeFile(File file, String fileSuffix, DataOutputStream out) throws IOException, FileNotFoundException {
        FileInputStream fin = new FileInputStream(file);
        //byte fileContent[] = new byte[CHUNK_SIZE];
        String msg = "HTTP/1.1 200 OK\r\nConnection: keep-alive\r\nKeep-Alive: timeout=50, max=1000\r\n";
        msg += "Date: " + getHTTPTime() + "\r\n";
        int contentLength = (int)file.length();
        int size = contentLength;
        msg += "Content-Length: " + contentLength + "\r\n";
        String contentType = getContentType(fileSuffix);
        msg += "Content-Type: " + contentType + "\r\n";
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("G  MT")); 
	    msg += "Last-Modified: " + dateFormat.format(file.lastModified()) + "\r\n" ;

        System.out.println(msg);
        out.writeBytes(msg + "\r\n");

        byte fileContent[] = new byte[size];
        fin.read(fileContent);
        out.write(fileContent, 0, size);
        out.writeBytes("\r\n");
    }

    public static void sendText(String receiver, String subject) {
        System.out.println("");
        System.out.println("Sent text!");
        System.out.println("Receiver: " + receiver);
        System.out.println("Subject: " + subject);
        // Get time
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy hh:mm aa z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        String timeString = dateFormat.format(cal.getTime());
        // Send text using twilio
        // Find your Account Sid and Auth Token at twilio.com/console

        // In shell: 
        // export TWILIO_ACCOUNT_SID="YOUR_SID"
        String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
        // In shell: 
        // export TWILIO_ACCOUNT_AUTH="YOUR_AUTH"
        String AUTH_TOKEN = System.getenv("TWILIO_ACCOUNT_AUTH");

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String messageText = "Message to: " + receiver + "\nwith Subject: " + subject + "\b has been read at: " + timeString + "!";

        // SET TO PHONE NUMBER
        // SET FROM PHONE NUMER
        String toNumber = "TO_NUM";
        String fromNumber = "FROM_NUM";
        Message message = Message
                .creator(new PhoneNumber(toNumber), // to
                        new PhoneNumber(fromNumber), // from
                        messageText)
                .create();
        System.out.println(message.getSid());
        System.out.println("Sent text!");
    }
}
