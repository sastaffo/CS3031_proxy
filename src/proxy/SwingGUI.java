/**
* SwingGUI
*
* Swing GUI which creates Listener threads (which listen on an IP port) and reports on requests.
* 
*/

package proxy;

import java.util.ArrayList;

// AWT imports
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.*;

// SWING imports
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComponent;

public class SwingGUI
            extends JFrame   // because it needs to have a Swing GUI interface
            implements ActionListener  // because user gui interaction has to trigger actions
            
{
    // class constants
	private static final int PORT = 8080;
    private static final long serialVersionUID = 1L;
    public static final String TITLE = "Swing GUI (V 1.00 24/02/2019)";
    public static final String NEWLINE = "\n";
    
    // class variables   
    static int port = 8080;  // the port on which to listen
    
    static ArrayList<Thread> listeningThreads;
    
    // gui components - all within the class's JFrame
    
    static JLabel fillerLabel1;
    static JLabel fillerLabel2;
    static JLabel fillerLabel3;

    static JLabel portLabel;    
    static JTextField portText; // input field
    
    static JButton startButton;
    static final String startButtonText = "   Start   "; // text that appears on button
    static JButton clearTextButton;
    static final String clearTextButtonText = "Clear Results";
    
    static JLabel domainLabel;
    static JTextField domainText;
    
    static JButton blockButton;
    static final String blockButtonText = "   Block   ";
    static JButton unblockButton;
    static final String unblockButtonText = "  Unblock  ";
    static JButton listBlockedButton;
    static final String listBlockedButtonText = "List Blocked Sites";
    
    //static JLabel logLabel;
    static JCheckBox logCB;
    
    
    static JTextArea reportArea;
    static JScrollPane reportScrollPane;

    // put the gui components into the layout 
	// note comments to keep track of numbers - becomes important with complex layouts	
                  
    // instance variables
    public Blocker blocker;
    
    // Class constructor
    public SwingGUI()
    {
    	this.blocker = new Blocker();
        initUI();
    } // SwingGUI()
    
    /**
     * initUI()
     * initialises each part of the GUI
     */
    private void initUI()
    {
    	fillerLabel1 = new JLabel("                                                           ");
    	fillerLabel2 = new JLabel("");
    	fillerLabel3 = new JLabel("                              ");
    	
      	// define the gui components being used
        portLabel = new JLabel ("Port  : ");
      	portText = new JTextField ( 6 ); // width of field
        startButton = new JButton ( startButtonText );
        clearTextButton = new JButton ( clearTextButtonText );

        
        domainLabel = new JLabel("Domain  : ");
        domainText = new JTextField(20);
        blockButton = new JButton(blockButtonText);
        unblockButton = new JButton(unblockButtonText);
        listBlockedButton = new JButton(listBlockedButtonText);
        
        logCB = new JCheckBox("Print log");
        
        
	    // JTextArea is within a JScrollPane
        reportArea = new JTextArea( "Output : \n", 20, 80  );
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportScrollPane = new JScrollPane(reportArea);

        // put the gui components into the layout
        // commenting the index of the items helps within the layout function
        createLayout (
                      portLabel, 			// item  0  
                      portText, 			// item  1
                      startButton, 			// item  2
                      clearTextButton, 		// item  3
                      reportScrollPane, 	// item  4
                      fillerLabel1, 		// item  5
                      domainLabel, 			// item  6
                      domainText, 			// item  7
                      blockButton, 			// item  8
                      unblockButton, 		// item  9
                      listBlockedButton,	// item 10
                      logCB,				// item 11
                      fillerLabel2,			// item 12
                      fillerLabel3 			// item 13
                      
		      );
                      
        // basic window stuff
        setTitle ( TITLE );
        setLocationRelativeTo ( null );
        setDefaultCloseOperation(EXIT_ON_CLOSE);  

        portText.setText(Integer.toString(PORT));
        domainText.setText("www.irishcoinage.com");
        
        // add the listeners to the components which need to trigger actions
        startButton.addActionListener(this);
        clearTextButton.addActionListener(this);
        blockButton.addActionListener(this);
        unblockButton.addActionListener(this);
        listBlockedButton.addActionListener(this);
        
        logCB.setSelected(true);
        logCB.addActionListener(this);
    } // initUI()        
        
    /**
     * createLayout()
     * creates a layout for the GUI
     * @param arg - JComponent, the list of components initialised in the constructor
     */
    private void createLayout (JComponent... arg)
    {
        Container pane = getContentPane();		
        GroupLayout gl = new GroupLayout ( pane );		
        pane.setLayout( gl );
        
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);
        
        gl.setHorizontalGroup (
            gl.createParallelGroup() 
                .addGroup(gl.createSequentialGroup()
                    .addComponent(arg[0])
                    .addComponent(arg[1])
                    .addComponent(arg[5])
                    .addComponent(arg[6])
                    .addComponent(arg[7])
                    .addComponent(arg[12])
                    
                )
                .addGroup(gl.createSequentialGroup()
                    .addComponent(arg[2])
                    .addComponent(arg[3])
                    .addComponent(arg[11])
                    .addComponent(arg[13])
                    .addComponent(arg[8]) 
                    .addComponent(arg[9])
                    .addComponent(arg[10])
                )
                .addComponent(arg[4]) 
        );
        gl.setVerticalGroup (
            gl.createSequentialGroup() 
                .addGroup(gl.createParallelGroup()
                    .addComponent(arg[0])
                    .addComponent(arg[1]) 
                    .addComponent(arg[5])
                    .addComponent(arg[6])
                    .addComponent(arg[7])
                    .addComponent(arg[12])
                )
                .addGroup(gl.createParallelGroup()
                    .addComponent(arg[2])
                    .addComponent(arg[3])
                    .addComponent(arg[11])
                    .addComponent(arg[13])
                    .addComponent(arg[8]) 
                    .addComponent(arg[9])
                    .addComponent(arg[10])
                )
                .addComponent(arg[4]) 
        );        
        gl.linkSize(arg[1])	;	
	    pack();        
    }  // createLayout ()    
    
    /**
     * actionPerformed()
     * creates an 'interrupt' when the user clicks on the gui and takes action
     * @param e - ActionEvent, the interrupt generated by user interaction
     */
    public void actionPerformed ( ActionEvent e )			
    {
        String actionCommandText;
        actionCommandText = e.getActionCommand();
        
        println("\nIn actionPerformed ....." );
        println("Action Command Text :>" + actionCommandText.trim() + "<");
        
        
        if ( actionCommandText.equals( startButtonText ) )
	{
            println("Start Button Pressed .... " );
            try 
            {
                port = Integer.parseInt(portText.getText());
                println("Starting Listener on " + port  + NEWLINE );
                startListener(port);
            }
            catch (Exception f)
            {
				f.printStackTrace();
                println("Oops ... something went wrong trying to listen on the port \n" );
            }
        }
        else if ( actionCommandText.equals( clearTextButtonText ) )
        { 
            reportArea.setText( "Output (reset) : \n" );
        }
        else if ( actionCommandText.equals(blockButtonText))
        {
        	println("Block button pressed");
        	String domain = domainText.getText();
        	this.blocker.block(domain);
        }
        else if ( actionCommandText.equals(unblockButtonText))
        {
        	println("Unblock button pressed");
        	String domain = domainText.getText();
        	this.blocker.unblock(domain);
        }
        else if ( actionCommandText.equals(listBlockedButtonText))
        {
        	println("List blocked sites button pressed");
        	this.blocker.listBlockedSites();
        }
        else 
        {
            println("unidentified action requested >" + actionCommandText + "<" );
        }
           
    }
    
    
    /**
     * main()
     * sets up the GUI
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {        
        EventQueue.invokeLater
        (
            () -> 
            {
                SwingGUI gui = new SwingGUI();
                gui.setVisible(true);
            }
        );	       
    }
    
    
    /**
     * println()
     * takes in a message and prints it to the GUI text box
     * @param message
     */
    public static void println (String message )
    {
        // Text Area Output : 
        reportArea.append ( NEWLINE + message );
        // Make sure the newest text is visible
        reportArea.setCaretPosition(reportArea.getDocument().getLength());
        // update report area after each message is appended
        reportArea.repaint();	
    }   
        
    /**
    * startListener()
    * @param port - int : port to listen on 
    * @throws  Exception - not handled (or is it?) 
    */
    private void startListener ( int port) throws Exception  
    {
        try 
        { 
        	// Starting the thread: calls the Listener constructor 
        	Listener sl = new Listener(port, this.blocker);
        	// Then this calls the run() method
        	new Thread(sl).start();		
        }
        catch  (Exception e)
        {
        	println("Something went wrong starting the listener" );
        }
    }    
}   
        
 
