import javax.servlet.*;
import javax.servlet.http.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

import com.google.gson.*;
import java.util.Base64;

class Image{
    public String[] content;
    
    public Image(String imageBuffer[]){
        content = imageBuffer;
    }
}

class ImageSender{
    public int bufferSize = 0;
    public String imageBuffer[];
    private int imageAddCounter = 0;
    
    public ImageSender(int bufferSize){
        this.bufferSize = bufferSize;
        imageBuffer = new String[bufferSize];
    }
    
    public void addImage(String imageStr){
        if(imageAddCounter >= bufferSize){
            imageAddCounter = 0;
        }
        
        imageBuffer[imageAddCounter++] = imageStr;
    }
    
    public Image getImage(){
        Image img = new Image(imageBuffer);
        return img;
    }
}

class ScreenCapture{
    public boolean doClose = false;
    public ImageSender is;
    
    public ScreenCapture(ImageSender is){
        this.is = is;
    }
    
    public Image capture(){
        for(int i = 0; i < is.bufferSize; i++){
            try{
                is.addImage(new String(this.getImage()));
                Thread.sleep(1000/is.bufferSize);
            }catch(Exception e){
                System.out.println("Error : " + e);
            }
        }
        
        return is.getImage();
    }
    
    public byte[] getImage(){
        byte[] bytes = new byte[1024];
        try{
            // Captures an image
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            
            // Converts it into Base64 byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(capture, "jpg", bos);
            bytes = Base64.getEncoder().encode(bos.toByteArray());;
            
        }catch(Exception e){
            System.out.println("Error : " + e);
        }
        
        return bytes;
    }
}

public class view extends HttpServlet {
    ScreenCapture sc;
    ImageSender is;
    
    int serverFPS = 5;
    
    public void init() throws ServletException{
        is = new ImageSender(serverFPS);
        sc = new ScreenCapture(is);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("application/json;charset=UTF-8");
        //res.addHeader("connection", "keep-alive");
        
        // Capture image
        Image finalImage = sc.capture();
        
        // Convert to JSON
        String imageJSON = new Gson().toJson(finalImage);
        
        res.getWriter().print(imageJSON);
    }
}
