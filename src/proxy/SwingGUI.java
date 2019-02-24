/**
* SwingGUI
*
* Simple Listener with a Swing GUI - which listens on an IP port
* and reports on requests.
* 
* worked on project with John Stafford-Langan (my dad)
* https://github.com/johnsl01/SwingListener/
*
*/

package proxy;

import java.util.ArrayList;

// AWT imports
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.*;

// SWING imports
import javax.swing.JFrame ;
import javax.swing.GroupLayout;
import javax.swing.JLabel ;
import javax.swing.JButton ;
import javax.swing.JTextField ;
import javax.swing.JTextArea ;
import javax.swing.JScrollPane ;
import javax.swing.JComponent ;

public class SwingGUI
            extends JFrame   // because it needs to have a Swing GUI interface
            implements ActionListener  // because user gui interaction has to trigger actions
            
{
    // class constants
	private static final int PORT = 8082;
    private static final long serialVersionUID = 1L;
    public static final String TITLE = "Swing GUI (V 1.00 24/02/2019)" ;
    public static final String NEWLINE = "\n";
    
    // class variables   
    static int port = 8080 ;  // the port on which to listen
    
    static ArrayList<Thread> listeningThreads;
    
    // gui components - all within the class's JFrame
    static JLabel portLabel ;    
    static JTextField portText ;
    
    static JButton startButton ;
    static String startButtonText = "Start" ;
    static JButton clearTextButton ;
    static String clearTextButtonText = "Clear Results" ;
    
    static JTextArea reportArea ;
    static JScrollPane reportScrollPane ;

    // put the gui components into the layout 
	// note comments to keep track of numbers - becomes important with complex layouts	
                  
    // Class constructor
    public SwingGUI()
    {
        initUI();
    } // SwingGUI()
    
    private void initUI()
    {
      	// define the gui components being used
        portLabel = new JLabel ( "Port  : " ) ;
      	portText = new JTextField ( 6 ) ;
        startButton = new JButton ( startButtonText ) ;
        clearTextButton = new JButton ( clearTextButtonText ) ;
	      // JTextArea is within a JScrollPane
        reportArea = new JTextArea( "Output : \n", 20, 60  ) ;
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportScrollPane = new JScrollPane(reportArea);

        // put the gui components into the layout 
        // note comments to keep track of numbers - becomes important with complex layouts	
        createLayout (
                      portLabel, // item 0  
                      portText,
                      startButton,
                      clearTextButton,
                      reportScrollPane // item 4
		      ) ;
                      
        // basic window stuff
        setTitle ( TITLE ) ;
        setLocationRelativeTo ( null ) ;
        setDefaultCloseOperation(EXIT_ON_CLOSE) ;  
        
        // default port value - 8080 a typical proxy port
        // ... and a great little cpu back in the day ...
        portText.setText ( Integer.toString(PORT) ) ;
        
        // add the listeners to the components which need to trigger actions
        // note - changing the URL text is not set to trigger anything
        startButton.addActionListener(this) ;
        clearTextButton.addActionListener(this) ; 
    } // initUI()        
        
    private void createLayout (JComponent... arg)
    {
        Container pane = getContentPane() ;		
	GroupLayout gl = new GroupLayout ( pane ) ;		
	pane.setLayout( gl ) ;
        
        gl.setAutoCreateContainerGaps(true) ;
        gl.setAutoCreateGaps(true) ;
        
	// this is a really tedious mechanism
	// the horizontal and vertical groups define the same stuff
	// but horizontal is parallel then sequential groups
	// whereas vertical is sequential then parallel groups
	// it becomes very messy for complex gui layouts
	// and argument numbering makes it easy to mess up
	// there is probably a better way - but this works.
	// The layout we want here is :
	//   0     1   :      portLabel       portText
	//   2     3   :      startButton     cleartextButton
	//      4      :           reportSrollPane
        gl.setHorizontalGroup (
            gl.createParallelGroup() 
                .addGroup(gl.createSequentialGroup()
                    .addComponent(arg[0])
                    .addComponent(arg[1]) 
                )
                .addGroup(gl.createSequentialGroup()
                    .addComponent(arg[2])
                    .addComponent(arg[3]) 
                )
                .addComponent(arg[4]) 
        ) ;
        gl.setVerticalGroup (
            gl.createSequentialGroup() 
                .addGroup(gl.createParallelGroup()
                    .addComponent(arg[0])
                    .addComponent(arg[1]) 
                )
                .addGroup(gl.createParallelGroup()
                    .addComponent(arg[2])
                    .addComponent(arg[3]) 
                )
                .addComponent(arg[4]) 
        ) ;        
        gl.linkSize(arg[1])	;	
	    pack() ;        
    }  // createLayout ()    
    
    
    public void actionPerformed ( ActionEvent e )			
    {
        String actionCommandText ;
        actionCommandText = e.getActionCommand() ;
        
        println("In actionPerformed ....." ) ;
        println("Action Command Text :>" + actionCommandText + "<") ;
        
        
        if ( actionCommandText.equals( startButtonText ) )
	{
            println("Start Button Pressed .... " ) ;
            try 
            {
                port = Integer.parseInt(portText.getText());
                println("Starting Listener on " + port  + NEWLINE ) ;
                startListener(port);
                //startButton.setVisible(false) ; // hide the button - there is no Stop ! 
            }
            catch (Exception f)
            {
				f.printStackTrace();
                println("Oops ... something went wrong trying to listen on the port \n" ) ;
            }
        }
        else if ( actionCommandText.equals( clearTextButtonText ) )
        { 
            reportArea.setText( "Output (reset) : \n" );
        }
        else 
        {
            println("unidentified action requested >" + actionCommandText + "<" ) ;
        }
           
    } // actionPerformed ()
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {        
        // System.out.println("Hello World");  // console diag message 
        
        EventQueue.invokeLater
        (
            () -> 
            {
                SwingGUI gui = new SwingGUI();
                gui.setVisible(true);
            }
        );	       
    } // main()
    
    
    public static void println (String message )
    {
		// Standard Output :		
        // System.out.println ( message ) ;

        // Text Area Output : 
        reportArea.append ( NEWLINE + message ) ;
        // Make sure the newest text is visible
        reportArea.setCaretPosition(reportArea.getDocument().getLength());

        // update report area after each message is appended
        reportArea.repaint() ;	
    }   
        
    /**
    *
    * @param port - int : port to listen on 
    * @throws  Exception - not handled (or is it?) 
    */
    private void startListener ( int port)
                          throws Exception  
    // there are too many different ways to start threads 
    // not clear which is correct in which case.
    {
        try 
        {	
        	// Starting the thread  1 : calls the class constuctor
        	// Thread lthread = new Thread(new Slistener(port)) ;
        	// listeningThreads.add(lthread) ;
        	// Then this calls the run() method 
        	// lthread.start() ;
        	
        	
        	// Starting the thread  2 : calls the class constructor 
        	Listener sl = new Listener(port) ;
        	// Then this calls the run() method
        	new Thread(sl).start();		
        }
        catch  (Exception e)
        {
        	println("Something went wrong starting the listener" ) ;
        }
	
	// do I need to keep this running (I don't think so) ?
    }
        
} //  Class SwingListener       
        
 
