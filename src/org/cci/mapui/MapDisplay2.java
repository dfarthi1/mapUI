/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cci.mapui;

import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import org.jgrapht.graph.DefaultWeightedEdge;
/**
 *
 * @author Dave
 */
public class MapDisplay2 extends JFrame {

    /**
     * Creates new form MapDisplay
     * @throws java.io.IOException
     */
    
    // Variables for MapDisplay components                     
    private JLabel firstFloorLabel;
    private JPanel firstTab;
    private JLabel fourthFloorLabel;
    private JPanel fourthTab;
    private JTabbedPane mapPane;
    private JComboBox nameComboBox;
    private JComboBox roomNameComboBox;
    private JComboBox roomNumberComboBox;
    private JPanel searchByPanel;
    private JLabel searchForLabel;
    private JTextField searchForTextField;
    private JLabel secondFloorLabel;
    private JPanel secondTab;
    private JButton submitButton;
    private JLabel thirdFloorLabel;
    private JPanel thirdTab;
    private JButton searchImageButton;
    
    // Variables for use by graphing methods
    private final ArrayList<Name> staffNames;
    private final ArrayList<RoomName> roomNameArrayList;
    private final ArrayList<String> roomNumberArrayList;
    private final ArrayList<Obstacle> obstacleList;
    private final WoodwardGraph graph;
    private ArrayList<Node> shortestPathNodes;  //list of nodes that will be drawn on map, if entire map is one a single floor
    private ArrayList<Node> nodeListA = new ArrayList<>();          //list of nodes to be drawn on starting floor
    private ArrayList<Node> nodeListB = new ArrayList<>();          //list of nodes to be drawn on ending floor
    private List<DefaultWeightedEdge> listOfEdges;

    
    // Variables for use in action listeners/key listeners/change listeners of components
    private WriteOnGlassPane wp;
    private ClearGlassPane c;
    private DrawAndWriteOnGlassPane dw;
    private DrawPathAndWriteOnGlassPane dp;
    private ChangeListener endOfPath;
    private ChangeListener startOfPath;
    
    //inner class for faculty search by image window
    class ImageSearchWindow extends JFrame {
        
        private ImageIcon[] pic;
        private JLabel[] label;
        ArrayList<String> images = new ArrayList<>();
        
        public ImageSearchWindow()
        {
            //create the frame and panels necessary for the search by image window
            Container c = this.getContentPane();
            c.setLayout(null);
            int height, width;
            
            JPanel jp = new JPanel();
            jp.setLayout(null);
            jp.setPreferredSize(new Dimension(1500,1500));
            
            JScrollPane js = new JScrollPane(jp,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            this.setContentPane(js);
            
            //get all the files from the staff image directory into an array
            File[] files = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\images\\staff").listFiles();
            for(File f:files)
            {
                if(f.isFile())
                {
                    //System.out.println(f.getName());
                    images.add(f.getName());
                }
            }
            
            pic = new ImageIcon[images.size()];
            label = new JLabel[images.size()];
            
            //create MouseListener to make the images clickable
            MouseListener ml = new MouseListener()
            {
                @Override
                public void mouseClicked(MouseEvent e) {                    
                        dispose();
                        JLabel l = (JLabel)e.getSource();
                        String[] name = l.getText().split(" ");
                        String selection = name[1]+", "+name[0];
                        nameComboBox.setSelectedItem(selection);
                    }
                //sine mouse listener is abstract, all its methods must be implemented whether they are used or not
                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            };
            
            //create an ImageIcon, JLabel and MouseListener for each image
            String[] name;
            //x and y value of position of image label
            int x = 100;
            int y = 75;
            
            for(int index = 0; index < images.size(); index++)
            {
                //add image to pic array
                pic[index] = new ImageIcon("src\\org\\cci\\mapui\\images\\staff\\" + images.get(index));
                name = images.get(index).split("_");
                //remove file type from string
                name[1] = name[1].substring(name[1].indexOf(name[1].charAt(0)),name[1].indexOf("."));
                
                //add label to label array
                label[index] = new JLabel(name[1]+" "+name[0],pic[index],JLabel.CENTER);
                label[index].setVerticalTextPosition(JLabel.BOTTOM);
                label[index].setHorizontalTextPosition(JLabel.CENTER);
                height = pic[index].getIconHeight();
                width = pic[index].getIconWidth();
                //there are 10 pictures in a row. After 10th pic is placed reset x to 100 and increase y to next row
                if(x>1225)
                {
                    x = 100;
                    y += 200;
                }
                label[index].setBounds(x,y,width,height+20);
                x += 125;
                jp.add(label[index]);
                
                //add listener to label
                label[index].addMouseListener(ml);
            }

            JLabel text = new JLabel("Click on image to get directions...",JLabel.CENTER);
            text.setBounds(20,20,200,20);           
            jp.add(text);

            this.setSize(1500,1000);
            this.setResizable(false);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }
    }

    
    public MapDisplay2() throws IOException {
        
        //create WoodwardGraph object
        graph = new WoodwardGraph();
        
        //create an ArrayList of obstacles (stairs, e.g.) that must be drawn around
        File obstacleFile = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\ObstacleList.txt");
        Scanner input = new Scanner(obstacleFile);
        String line;
        String[] obstacleInfo;
        Obstacle o;
        this.obstacleList = new ArrayList<>();
        while(input.hasNext())
        {
            line = input.nextLine();
            obstacleInfo = line.split(",");
            o = new Obstacle(obstacleInfo[0],new Coordinate(Integer.parseInt(obstacleInfo[1]),Integer.parseInt(obstacleInfo[2])),new Coordinate(Integer.parseInt(obstacleInfo[3]),Integer.parseInt(obstacleInfo[4])));
            obstacleList.add(o);
        }
        
        //create ArrayList of staff and faculty names
        this.staffNames = new ArrayList<>();
        File nameFile = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\nameList.txt");
        input = new Scanner(nameFile);
        String[] nameInfo;
        Name n;
        while(input.hasNext())
        {
            line = input.nextLine();
            nameInfo = line.split(",");
            n = new Name(nameInfo[1],nameInfo[0],nameInfo[2]);
            staffNames.add(n);
        }
        
        //create an ArrayList of room names
        this.roomNameArrayList = new ArrayList<>();
        File roomNameFile = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\RoomNameList.txt");
        input = new Scanner(roomNameFile);
        String[] roomNameInfo;
        RoomName rn;
        while(input.hasNext())
        {
            line = input.nextLine();
            roomNameInfo = line.split(",");
            rn = new RoomName(roomNameInfo[0],roomNameInfo[1]);
            roomNameArrayList.add(rn);
        }
        
        //create an ArrayList of Strings that is a copy of the WoodwardGraph vertexList
        //this will be the list of room numbers
        roomNumberArrayList = graph.getVertexList();
        
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
                             
    private void initComponents() throws IOException {
        
        //file object for list of faculty and staff
        File nameFile = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\nameList.txt");
        //create an ArrayList to read in faculty and staff names from file
        ArrayList<String> namesList = new ArrayList<>();
        //create a string array for list of faculty and staff names
        String[] namesArray;
        
        namesList.add("Faculty or Staff Name");
        Scanner input = new Scanner(nameFile);
        String line;
        String[] names;
        while(input.hasNext())
        {
            line = input.nextLine();
            names = line.split(",");
            namesList.add(names[0]+", "+names[1]);
        }
        //convert namesList to array
        namesArray = new String[namesList.size()];
        namesArray = namesList.toArray(namesArray);
        input.close();
        
        //file object for list of room names
        File roomNameFile = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\roomNameList.txt");
        //create an ArrayList to read in room names from file
        ArrayList<String> roomNamesList = new ArrayList<>();
        //create a string array for list of room names
        String[] roomNamesArray;
        
        roomNamesList.add("Lab, Center, or Room Name");
        input = new Scanner(roomNameFile);
        String[] roomNames;
        while(input.hasNext())
        {
            line = input.nextLine();
            roomNames = line.split(",");
            roomNamesList.add(roomNames[0]);
        }
        //convert roomNamesList to array
        roomNamesArray = new String[roomNamesList.size()];
        roomNamesArray = roomNamesList.toArray(roomNamesArray);
        input.close();
        
        //file object for list of room names
        File roomNumberFile = new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\AdjacencyList.txt");
        //create an ArrayList to read in room names from file
        ArrayList<String> roomNumberList = new ArrayList<>();
        //create a string array for list of room names
        String[] roomNumberArray;
        
        roomNumberList.add("Room Number");
        input = new Scanner(roomNumberFile);
        String[] roomNumbers;
        while(input.hasNext())
        {
            line = input.nextLine();
            roomNumbers = line.split(",");
            //do not add entrance or stairs to room number list
            if(!roomNumbers[0].startsWith("Stair") && !roomNumbers[0].equals("Entrance"))
            {
                roomNumberList.add(roomNumbers[0]);
            }
        }
        //convert roomNamesList to array
        roomNumberArray = new String[roomNumberList.size()];
        roomNumberArray = roomNumberList.toArray(roomNumberArray);
        input.close();
        
        //components of the map gui
        searchByPanel = new JPanel();
        nameComboBox = new JComboBox();
        roomNumberComboBox = new JComboBox();
        roomNameComboBox = new JComboBox();
        searchForLabel = new JLabel();
        searchForTextField = new JTextField();
        submitButton = new JButton();
        mapPane = new JTabbedPane();
        firstTab = new JPanel();
        firstFloorLabel = new JLabel();
        secondTab = new JPanel();
        secondFloorLabel = new JLabel();
        thirdTab = new JPanel();
        thirdFloorLabel = new JLabel();
        fourthTab = new JPanel();
        fourthFloorLabel = new JLabel();
        searchImageButton = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1500, 1100));

        searchByPanel.setBorder(BorderFactory.createTitledBorder(null, "Search By", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Times New Roman", 0, 14), new java.awt.Color(0, 0, 0))); // NOI18N
        
        nameComboBox.setModel(new DefaultComboBoxModel(namesArray));
        

        roomNumberComboBox.setModel(new DefaultComboBoxModel(roomNumberArray));

        roomNameComboBox.setModel(new DefaultComboBoxModel(roomNamesArray));

        searchForLabel.setHorizontalAlignment(SwingConstants.CENTER);
        searchForLabel.setText("Enter Text to Search");
        searchForLabel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        submitButton.setText("Submit");
        searchImageButton.setText("Search By Image");
        
        GroupLayout searchByPanelLayout = new GroupLayout(searchByPanel);
        searchByPanel.setLayout(searchByPanelLayout);
        searchByPanelLayout.setHorizontalGroup(
            searchByPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(searchByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchByPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(searchForLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameComboBox, 0, 156, Short.MAX_VALUE)
                    .addComponent(roomNumberComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roomNameComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(searchForTextField)
                    .addComponent(submitButton)

                    .addComponent(searchImageButton))
                .addContainerGap())
        );

        searchByPanelLayout.setVerticalGroup(
            searchByPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(searchByPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(roomNumberComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(roomNameComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(searchForLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchForTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(submitButton)
                .addGap(100, 100, 100) 
                .addComponent(searchImageButton)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        mapPane.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        mapPane.setTabPlacement(JTabbedPane.BOTTOM);

        firstFloorLabel.setIcon(new ImageIcon(getClass().getResource("/org/cci/mapui/images/1stfloor.png"))); // NOI18N

        GroupLayout firstTabLayout = new GroupLayout(firstTab);
        firstTab.setLayout(firstTabLayout);
        firstTabLayout.setHorizontalGroup(
            firstTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(firstTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(firstFloorLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        firstTabLayout.setVerticalGroup(
            firstTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(firstTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(firstFloorLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mapPane.addTab("First Floor", firstTab);

        secondFloorLabel.setIcon(new ImageIcon(getClass().getResource("/org/cci/mapui/images/2ndfloor.png"))); // NOI18N

        GroupLayout secondTabLayout = new GroupLayout(secondTab);
        secondTab.setLayout(secondTabLayout);
        secondTabLayout.setHorizontalGroup(
            secondTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(secondTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(secondFloorLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        secondTabLayout.setVerticalGroup(
            secondTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, secondTabLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(secondFloorLabel)
                .addGap(271, 271, 271))
        );

        mapPane.addTab("Second Floor", secondTab);

        thirdFloorLabel.setIcon(new ImageIcon(getClass().getResource("/org/cci/mapui/images/3rdfloor.png"))); // NOI18N

        GroupLayout thirdTabLayout = new GroupLayout(thirdTab);
        thirdTab.setLayout(thirdTabLayout);
        thirdTabLayout.setHorizontalGroup(
            thirdTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(thirdTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(thirdFloorLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        thirdTabLayout.setVerticalGroup(
            thirdTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, thirdTabLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(thirdFloorLabel)
                .addGap(490, 490, 490))
        );

        mapPane.addTab("Third Floor", thirdTab);

        fourthFloorLabel.setIcon(new ImageIcon(getClass().getResource("/org/cci/mapui/images/4thfloor.png"))); // NOI18N

        GroupLayout fourthTabLayout = new GroupLayout(fourthTab);
        fourthTab.setLayout(fourthTabLayout);
        fourthTabLayout.setHorizontalGroup(
            fourthTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(fourthTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fourthFloorLabel)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        fourthTabLayout.setVerticalGroup(
            fourthTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, fourthTabLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(fourthFloorLabel)
                .addGap(462, 462, 462))
        );

        mapPane.addTab("Fourth Floor", fourthTab);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchByPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mapPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(506, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(mapPane, GroupLayout.PREFERRED_SIZE, 1043, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(searchByPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        pack();
        
        //make the text field ready to accept any text entered
        searchForTextField.requestFocus();
        
        //create change listener for tabs so that glass pane can be cleared if they are clicked on
        final ChangeListener cl = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e){
                c = new ClearGlassPane();
            }
        };
        mapPane.addChangeListener(cl);
        
        //create action listener for search by image button
        searchImageButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //remove all the change listeners from the mapPane
                for(ChangeListener c:mapPane.getChangeListeners())
                {
                    mapPane.removeChangeListener(c);
                }
                //code to show new window with clickable staff pictures
                ImageSearchWindow isw = new ImageSearchWindow();
            }
        });
                
        //create action listener for nameComboBox component
        //action performed is currently a test to draw a path to the location of the office of the person selected
        nameComboBox.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                /*if the selected index is 0, then this action listener was trigged
                by being reset. Do not execute code */
                if(nameComboBox.getSelectedIndex() != 0)
                {
                    //remove any previous drawings on the glass pane
                    c = new ClearGlassPane();
                    //remove all the change listeners from the mapPane
                    for(ChangeListener c:mapPane.getChangeListeners())
                    {
                        mapPane.removeChangeListener(c);
                    }
                    //add the clear glass pane change listener back to the map pane
                    mapPane.addChangeListener(cl);
                    
                    //get source of nameComboBox
                    JComboBox comboBox = (JComboBox) e.getSource();
                    //get selection
                    Object selected = comboBox.getSelectedItem();
                    //convert selected Object to string
                    String name = selected.toString();
                    //split string around comma and get room number associated with name
                    String[] names = name.split(",");
                    String roomNumber = getRoomNumberByName(names[1].trim(),names[0].trim());

                    //create a list of edges in shortest path
		    listOfEdges = graph.getShortestPath(roomNumber);
                        
                    //create an ArrayList of Nodes in the shortest path
                    shortestPathNodes = graph.getNodeList(listOfEdges);
                    
                    //get the bounding rectangle of the mapPane object
                    //this will give the offset of the x and y coordinate on the glass pane
                    Rectangle r = mapPane.getBounds();
                    
                    //remove any previous drawings on the glass pane
                    c = new ClearGlassPane();
                    
                    //create string to write on the glass pane
                    String textToWrite = names[1].trim() + " " + names[0].trim() + "'s office (" + roomNumber + "),is located on the ";
                    switch(roomNumber.charAt(0))
                    {
                        case '1': textToWrite += "first ";
                            break;
                        case '2': textToWrite += "second ";
                            break;
                        case '3': textToWrite += "third ";
                            break;
                        case '4': textToWrite += "fourth ";
                    }
                    textToWrite += "floor. ";
                    final String text = textToWrite;
                    
                    //if the first node after the entrance and the last node are on different floors
                    //create two separate node lists to be drawn
                    //and set multiple floor flag
                    final Node end = shortestPathNodes.get(shortestPathNodes.size()-1);
                    if(shortestPathNodes.get(1).getId().charAt(0) == end.getId().charAt(0))
                    {
                        //System.out.println("Multiple floors not detected in shortest path");
                        //create a JPanel object that holds the tab associated with the roomNumber
                        JPanel f = getFloorTab(roomNumber);
                        //select the tab which is to be drawn on
                        mapPane.setSelectedComponent(f);
                        //since multiple floors were not detected in list, use shortestPathNodes as path to draw
                        //write the text and draw path on the glass pane
                        dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,shortestPathNodes);
                        
                        //remove the original clear pane change listener
                        mapPane.removeChangeListener(cl);

                        //create a ChangeListener that will draw the path again on the second floor if tabs have been changed
                        ChangeListener path = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                                //draw the next floor's path here if the correct tab is selected
                                if(index == 1)
                                {
                                    //System.out.println("Second floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text,40,375,shortestPathNodes);
                                }
                                else
                                {
                                    c = new ClearGlassPane();
                                }
                            }
                        };
                        mapPane.addChangeListener(path);
                    }
                    else
                    {
                        //System.out.println("Multiple floors detected in shortest path");
                        //since multiple floors were detected in list, use nodeListA and nodeListB as paths to draw
                        nodeListA.clear();
                        nodeListB.clear();
                        nodeListA.add(WoodwardGraph.getVertex("Entrance"));
                        nodeListA.add(WoodwardGraph.getVertex("StairB2"));
                        String startNode = "";

                        if(roomNumber.charAt(0)=='1') startNode = "StairB1";
                        else if (roomNumber.charAt(0)=='3') startNode = "StairB3";
                        else startNode = "StairB4";
                        
                        //create a list of edges in shortest path
                        listOfEdges = graph.getShortestPath(startNode,roomNumber);

                        //create an ArrayList of Nodes in the shortest path
                        nodeListB = graph.getNodeList(listOfEdges);

                        //place the Stair node as the first node in nodeListB
                        Node stair = null;
                        for(Node n:nodeListB)
                        {
                            if(n.getId().equals("StairB1") || n.getId().equals("StairB3") || n.getId().equals("StairB4"))
                            {
                              stair = n;                             
                            }
                        }
                        nodeListB.remove(stair);
                        nodeListB.add(0,stair);
                        
                        //code to test the separate node lists for each floor
                        /*
                        System.out.println("\nNodeListA:");
                        for(Node node:nodeListA)
                        {
                            System.out.println(node.getId());
                        }
                        System.out.println("\nNodeListB:");
                        for(Node node:nodeListB)
                        {
                            System.out.println(node.getId());
                        }*/
                        
                        //create a JPanel object that holds the tab associated with the second floor
                        JPanel f = getFloorTab("208");
                        //select the tab which is to be drawn on
                        mapPane.setSelectedComponent(f);
                        //clear the glass pane of the previously drawn path
                        c = new ClearGlassPane();
                        //write the text and draw path on the start floor
                        dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,nodeListA);
                        
                        final String text2 = textToWrite;
                        
                        //remove the original clear pane change listener
                        mapPane.removeChangeListener(cl);
                                               
                        //create a ChangeListener that will draw the rest of the path on the next floor when the correct tab is selected
                        ChangeListener endOfPath = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                                //draw the next floor's path here if the correct tab is selected
                                if(index == Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1)
                                {
                                    //System.out.println("Correct floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text2,40,375,nodeListB);
                                }
                                else if(index != 1)
                                {
                                    c = new ClearGlassPane();
                                }
                            }
                        };
                        mapPane.addChangeListener(endOfPath);
                        
                        //create a ChangeListener that will draw the first section of the path on the second floor from entrance to stairs
                        ChangeListener startOfPath = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();                                
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //draw the next floor's path here if the correct tab is selected
                                if(index == 1)
                                {
                                    //System.out.println("Second floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text,40,375,nodeListA);
                                }
                            }
                        };
                        mapPane.addChangeListener(startOfPath);   
                        
                    }
                    
                    //reset the other comboBoxes
                    roomNameComboBox.setSelectedIndex(0);
                    roomNumberComboBox.setSelectedIndex(0);
                    //make the text field ready to accept any text entered
                    searchForTextField.requestFocus();
                }//end if(selected.getSelectedIndex() != 0)
            }
        });
        
        //create action listener for roomNameComboBox component
        roomNameComboBox.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                /*if the selected index is 0, then this action listener was trigged
                by being reset. Do not execute code */
                if(roomNameComboBox.getSelectedIndex() != 0)
                {
                    //remove any previous drawings on the glass pane
                    c = new ClearGlassPane();
                    //remove all the change listeners from the mapPane
                    for(ChangeListener c:mapPane.getChangeListeners())
                    {
                        mapPane.removeChangeListener(c);
                    }
                    //add the clear glass pane change listener back to the map pane
                    mapPane.addChangeListener(cl);
                    
                    //get source of nameComboBox
                    JComboBox comboBox = (JComboBox) e.getSource();
                    //get selection
                    Object selected = comboBox.getSelectedItem();
                    //convert selected Object to string
                    String roomNameString = selected.toString();
                    //split string around comma and get room number associated with name
                    String[] roomName = roomNameString.split(",");
                    String roomNumber = getRoomNumberByRoomName(roomName[0].trim());
                    
                    //create a list of edges in shortest path
		    listOfEdges = graph.getShortestPath(roomNumber);
                        
                    //create an ArrayList of Nodes in the shortest path
                    shortestPathNodes = graph.getNodeList(listOfEdges);

                    //get the bounding rectangle of the mapPane object
                    //this will give the offset of the x and y coordinate on the glass pane
                    Rectangle r = mapPane.getBounds();
                    
                    //remove any previous drawings on the glass pane
                    c = new ClearGlassPane();
                    
                    //create string to write on the glass pane
                    String textToWrite = "The " + roomName[0].trim() + ",(" + getRoomNumberByRoomName(roomName[0].trim()) + ") is located on the ";
                    switch(roomNumber.charAt(0))
                    {
                        case '1': textToWrite += "first ";
                            break;
                        case '2': textToWrite += "second ";
                            break;
                        case '3': textToWrite += "third ";
                            break;
                        case '4': textToWrite += "fourth ";
                    }
                    textToWrite += "floor. ";
                    final String text = textToWrite;
                    
                    //if the first node after the entrance and the last node are on different floors
                    //create two separate node lists to be drawn
                    //and set multiple floor flag
                    final Node end = shortestPathNodes.get(shortestPathNodes.size()-1);
                    if(shortestPathNodes.get(1).getId().charAt(0) == end.getId().charAt(0))
                    {
                        //System.out.println("Multiple floors not detected in shortest path");
                        //create a JPanel object that holds the tab associated with the roomNumber
                        JPanel f = getFloorTab(roomNumber);
                        //select the tab which is to be drawn on
                        mapPane.setSelectedComponent(f);
                        //since multiple floors were not detected in list, use shortestPathNodes as path to draw
                        //write the text and draw path on the glass pane
                        dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,shortestPathNodes);
                        
                        //remove the original clear pane change listener
                        mapPane.removeChangeListener(cl);

                        //create a ChangeListener that will draw the path again on the second floor if tabs have been changed
                        ChangeListener path = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                                //draw the next floor's path here if the correct tab is selected
                                if(index == 1)
                                {
                                    //System.out.println("Second floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text,40,375,shortestPathNodes);
                                }
                                else
                                {
                                    c = new ClearGlassPane();
                                }
                            }
                        };
                        mapPane.addChangeListener(path);
                    }
                    else
                    {
                        //System.out.println("Multiple floors detected in shortest path");
                        //since multiple floors were detected in list, use nodeListA and nodeListB as paths to draw
                        nodeListA.clear();
                        nodeListB.clear();
                        nodeListA.add(WoodwardGraph.getVertex("Entrance"));
                        nodeListA.add(WoodwardGraph.getVertex("StairB2"));
                        String startNode = "";

                        if(roomNumber.charAt(0)=='1') startNode = "StairB1";
                        else if (roomNumber.charAt(0)=='3') startNode = "StairB3";
                        else startNode = "StairB4";
                        
                        //create a list of edges in shortest path
                        listOfEdges = graph.getShortestPath(startNode,roomNumber);

                        //create an ArrayList of Nodes in the shortest path
                        nodeListB = graph.getNodeList(listOfEdges);

                        //place the Stair node as the first node in nodeListB
                        Node stair = null;
                        for(Node n:nodeListB)
                        {
                            if(n.getId().equals("StairB1") || n.getId().equals("StairB3") || n.getId().equals("StairB4"))
                            {
                              stair = n;                             
                            }
                        }
                        nodeListB.remove(stair);
                        nodeListB.add(0,stair);
                        
                        //code to test the separate node lists for each floor
                        /*
                        System.out.println("\nNodeListA:");
                        for(Node node:nodeListA)
                        {
                            System.out.println(node.getId());
                        }
                        System.out.println("\nNodeListB:");
                        for(Node node:nodeListB)
                        {
                            System.out.println(node.getId());
                        }*/
                        
                        //create a JPanel object that holds the tab associated with the second floor
                        JPanel f = getFloorTab("208");
                        //select the tab which is to be drawn on
                        mapPane.setSelectedComponent(f);
                        //clear the glass pane of the previously drawn path
                        c = new ClearGlassPane();
                        //write the text and draw path on the start floor
                        dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,nodeListA);
                        
                        final String text2 = textToWrite;
                        
                        //remove the original clear pane change listener
                        mapPane.removeChangeListener(cl);
                                               
                        //create a ChangeListener that will draw the rest of the path on the next floor when the correct tab is selected
                        ChangeListener endOfPath = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                                //draw the next floor's path here if the correct tab is selected
                                if(index == Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1)
                                {
                                    //System.out.println("Correct floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text2,40,375,nodeListB);
                                }
                                else if(index != 1)
                                {
                                    c = new ClearGlassPane();
                                }
                            }
                        };
                        mapPane.addChangeListener(endOfPath);
                        
                        //create a ChangeListener that will draw the first section of the path on the second floor from entrance to stairs
                        ChangeListener startOfPath = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();                                
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //draw the next floor's path here if the correct tab is selected
                                if(index == 1)
                                {
                                    //System.out.println("Second floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text,40,375,nodeListA);
                                }
                            }
                        };
                        mapPane.addChangeListener(startOfPath);   
                        
                    }
                    //reset the other comboBoxes
                    roomNumberComboBox.setSelectedIndex(0);
                    nameComboBox.setSelectedIndex(0);
                    //make the text field ready to accept any text entered
                    searchForTextField.requestFocus();
                }//end if(selected.getSelectedIndex() != 0)
            }
        });
        
        //create action listener for the roomNumberComboBox component
        //action performed is currently a test to draw a square at the location of the office of the person selected
        roomNumberComboBox.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){ 
               /*if the selected index is 0, then this action listener was triggered
                by being reset. Do not execute code */
                if(roomNumberComboBox.getSelectedIndex() != 0)
                {              
                    //remove all the change listeners from the mapPane
                    for(ChangeListener c:mapPane.getChangeListeners())
                    {
                        mapPane.removeChangeListener(c);
                    }
                    //add the clear glass pane change listener back to the map pane
                    mapPane.addChangeListener(cl);
                    
                    //get source of nameComboBox
                    JComboBox comboBox = (JComboBox) e.getSource();
                    //get selection
                    Object selected = comboBox.getSelectedItem();
                    //convert selected Object to string
                    String roomNumber = selected.toString();               

                    //create a JPanel object that holds the tab associated with the room number
                    //JPanel f = getFloorTab(roomNumber);                                 
                    
                    //remove any previous drawings on the glass pane
                    c = new ClearGlassPane();
                    
                    //create string to write on the glass pane
                    String textToWrite;
                    
                    //if the room number has an associated staff member add that to the text to write string
                    if(getStaffNameByRoomNumber(roomNumber) != null)
                    {
                        textToWrite = getStaffNameByRoomNumber(roomNumber) + "'s office (" + roomNumber + "),is located on the ";
                    }
                    //if the room number has an associated room name add that to the text to write string
                    else if(getNameByRoomNumber(roomNumber) != null)
                    {
                        textToWrite = getNameByRoomNumber(roomNumber) + " (" + roomNumber + "),is located on the ";
                    }                 
                    else
                    {
                        textToWrite = "Room " + roomNumber + " is located on the ";
                    }
                    switch(roomNumber.charAt(0))
                    {
                        case '1': textToWrite += "first ";
                            break;
                        case '2': textToWrite += "second ";
                            break;
                        case '3': textToWrite += "third ";
                            break;
                        case '4': textToWrite += "fourth ";
                    }
                    textToWrite += "floor. ";
                    final String text = textToWrite;
                    //create a list of edges in shortest path
		    listOfEdges = graph.getShortestPath(roomNumber);
                        
                    //create an ArrayList of Nodes in the shortest path
                    shortestPathNodes = graph.getNodeList(listOfEdges);

                    //code to test that the shortest path nodes are correct
                    /*
                    for(Node node: shortestPathNodes)
                    {
                        System.out.println(node.getId());
                    }
                    */

                    //if the first node after the entrance and the last node are on different floors
                    //create two separate node lists to be drawn
                    //and set multiple floor flag
                    final Node end = shortestPathNodes.get(shortestPathNodes.size()-1);
                    if(shortestPathNodes.get(1).getId().charAt(0) == end.getId().charAt(0))
                    {
                        //System.out.println("Multiple floors not detected in shortest path");
                        //create a JPanel object that holds the tab associated with the roomNumber
                        JPanel f = getFloorTab(roomNumber);
                        //select the tab which is to be drawn on
                        mapPane.setSelectedComponent(f);
                        //since multiple floors were not detected in list, use shortestPathNodes as path to draw
                        //write the text and draw path on the glass pane
                        dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,shortestPathNodes);
                        
                        //remove the original clear pane change listener
                        mapPane.removeChangeListener(cl);

                        //create a ChangeListener that will draw the path again on the second floor if tabs have been changed
                        ChangeListener path = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                                //draw the next floor's path here if the correct tab is selected
                                if(index == 1)
                                {
                                    //System.out.println("Second floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text,40,375,shortestPathNodes);
                                }
                                else
                                {
                                    c = new ClearGlassPane();
                                }
                            }
                        };
                        mapPane.addChangeListener(path);
                    }
                    else
                    {
                        //System.out.println("Multiple floors detected in shortest path");
                        //since multiple floors were detected in list, use nodeListA and nodeListB as paths to draw
                        nodeListA.clear();
                        nodeListB.clear();
                        nodeListA.add(WoodwardGraph.getVertex("Entrance"));
                        nodeListA.add(WoodwardGraph.getVertex("StairB2"));
                        String startNode = "";

                        if(roomNumber.charAt(0)=='1') startNode = "StairB1";
                        else if (roomNumber.charAt(0)=='3') startNode = "StairB3";
                        else startNode = "StairB4";
                        
                        //create a list of edges in shortest path
                        listOfEdges = graph.getShortestPath(startNode,roomNumber);

                        //create an ArrayList of Nodes in the shortest path
                        nodeListB = graph.getNodeList(listOfEdges);

                        //place the Stair node as the first node in nodeListB
                        Node stair = null;
                        for(Node n:nodeListB)
                        {
                            if(n.getId().equals("StairB1") || n.getId().equals("StairB3") || n.getId().equals("StairB4"))
                            {
                              stair = n;                             
                            }
                        }
                        nodeListB.remove(stair);
                        nodeListB.add(0,stair);
                        
                        //code to test the separate node lists for each floor 
                        /*
                        System.out.println("\nNodeListA:");
                        for(Node node:nodeListA)
                        {
                            System.out.println(node.getId());
                        }
                        System.out.println("\nNodeListB:");
                        for(Node node:nodeListB)
                        {
                            System.out.println(node.getId());
                        }*/
                        
                        //create a JPanel object that holds the tab associated with the second floor
                        JPanel f = getFloorTab("208");
                        //select the tab which is to be drawn on
                        mapPane.setSelectedComponent(f);
                        //write the text and draw path on the start floor
                        dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,nodeListA);
                        
                        final String text2 = textToWrite;
                        
                        //remove the original clear pane change listener
                        mapPane.removeChangeListener(cl);
                                               
                        //create a ChangeListener that will draw the rest of the path on the next floor when the correct tab is selected
                        ChangeListener endOfPath = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                                //draw the next floor's path here if the correct tab is selected
                                if(index == Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1)
                                {
                                    //System.out.println("Correct floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text2,40,375,nodeListB);
                                }
                                else if(index != 1)
                                {
                                    c = new ClearGlassPane();
                                }
                            }
                        };
                        mapPane.addChangeListener(endOfPath);
                        
                        //create a ChangeListener that will draw the first section of the path on the second floor from entrance to stairs
                        ChangeListener startOfPath = new ChangeListener(){
                            @Override
                            public void stateChanged(ChangeEvent e){
                                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                                int index = sourceTabbedPane.getSelectedIndex();                                
                                //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                                //draw the next floor's path here if the correct tab is selected
                                if(index == 1)
                                {
                                    //System.out.println("Second floor tab selected");
                                    //clear the glass pane of the previously drawn path
                                    c = new ClearGlassPane();
                                    //write the text and draw path on the start floor
                                    dp = new DrawPathAndWriteOnGlassPane(text,40,375,nodeListA);
                                }
                            }
                        };
                        mapPane.addChangeListener(startOfPath);   
                        
                    }
                    //reset the other comboBoxes
                    roomNameComboBox.setSelectedIndex(0);
                    nameComboBox.setSelectedIndex(0);
                    //make the text field ready to accept any text entered
                    searchForTextField.requestFocus();
                }            
            }
        });
        
        //create action listener for submit button
        submitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //remove all the change listeners from the mapPane
                for(ChangeListener c:mapPane.getChangeListeners())
                {
                    mapPane.removeChangeListener(c);
                }
                searchFieldActions();
            }
        });
        //create new key listener for searchForTextField
        //action performed is currently a test to draw a square at the location entered in text field
        searchForTextField.addKeyListener(new KeyListener(){
            
            @Override
            public void keyPressed(KeyEvent e){}
            @Override
            public void keyReleased(KeyEvent e){
                if (e.getKeyCode()==KeyEvent.VK_ENTER)
                {
                    //remove all the change listeners from the mapPane
                    for(ChangeListener c:mapPane.getChangeListeners())
                    {
                        mapPane.removeChangeListener(c);
                    }
                    searchFieldActions();
                }
            }
            @Override
            public void keyTyped(KeyEvent e){
            }
        
        });
    }// end void initComponents  
    
    /*  this method is the set of actions that take place when either the submit button
        is pressed, or the enter key is released when user searches for text
    */
    public void searchFieldActions()
    {
        //initialize variables for use in search loops and subsequent code
        String roomNumber = null;
        //get text from searchFor text field
        String userInput = searchForTextField.getText().trim();
        if(userInput.equals(""))
        {
            //System.out.println("There is no text in the field");
        }
        else
        {
            //split userInput by spaces and capitalize first letter of each word
            String[] input = userInput.split(" ");
            String firstLetter;
            userInput = "";
            for(String word:input)
            {
                if(word.equals("cci"))
                {
                    word = "CCI";
                    userInput += word + " ";
                }
                else
                {
                    firstLetter = String.valueOf(word.charAt(0)).toUpperCase();
                    word = firstLetter + word.substring(1);
                    userInput = userInput + word + " ";
                }
            }
            userInput = userInput.trim();
            //System.out.println("Text entered: " + userInput);
            
            //create string for text to be written on glass pane
            String textToWrite = "";
            
            //search list of room numbers/names and faculty and staff names and compare to text field input
            if(isOnRoomNumberList(userInput))
            {
                //set the userInput to be the room number selected
                roomNumber = userInput;
                textToWrite = "Room " + roomNumber + " is located on the ";
                //if the room number is an office, add staff or faculty name
                if(getStaffNameByRoomNumber(roomNumber)!=(null))
                {
                    textToWrite = getStaffNameByRoomNumber(roomNumber) + "'s office (" + roomNumber + "),is located on the ";
                }
                else if (getNameByRoomNumber(roomNumber)!=(null))
                {
                    textToWrite = "The " + getNameByRoomNumber(roomNumber) + " (" + roomNumber + "),is located on the ";
                }
            }
            else if(isOnRoomNameList(userInput))
            {
                //set the room number selected to be the one matching the room name
                roomNumber = getRoomNumberByRoomName(userInput);
                textToWrite = "The " + userInput.trim() + " (" + roomNumber + ") ,is located on the ";
            }
            else if(isOnFacultyStaffList(userInput))
            {
                String[] names = userInput.split(" ");
                roomNumber = getRoomNumberByName(names[0].trim(),names[1].trim());
                textToWrite = names[0].trim() + " " + names[1].trim() + "'s office (" + roomNumber + "),is located on the ";
            }
            

            //if roomNumber is not null (i.e. there is a match) then select proper floorTab and draw
            if(roomNumber != null)
            {
                //finish the string to write info on glass pane
                switch(roomNumber.charAt(0))
                    {
                        case '1': textToWrite += "first ";
                            break;
                        case '2': textToWrite += "second ";
                            break;
                        case '3': textToWrite += "third ";
                            break;
                        case '4': textToWrite += "fourth ";
                    }
                    textToWrite += "floor. ";
                final String text = textToWrite;
                //create a list of edges in shortest path
                listOfEdges = graph.getShortestPath(roomNumber);

                //create an ArrayList of Nodes in the shortest path
                shortestPathNodes = graph.getNodeList(listOfEdges);

                //get the bounding rectangle of the mapPane object
                //this will give the offset of the x and y coordinate on the glass pane
                Rectangle r = mapPane.getBounds();

                //if the first node after the entrance and the last node are on different floors
                //create two separate node lists to be drawn
                final Node end = shortestPathNodes.get(shortestPathNodes.size()-1);
                if(shortestPathNodes.get(1).getId().charAt(0) == end.getId().charAt(0))
                {
                    //System.out.println("Multiple floors not detected in shortest path");
                    //create a JPanel object that holds the tab associated with the roomNumber
                    JPanel f = getFloorTab(roomNumber);
                    //select the tab which is to be drawn on
                    mapPane.setSelectedComponent(f);
                    //since multiple floors were not detected in list, use shortestPathNodes as path to draw
                    //write the text and draw path on the glass pane
                    c = new ClearGlassPane();
                    dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,shortestPathNodes);                     

                    //create a ChangeListener that will draw the path again on the second floor if tabs have been changed
                    ChangeListener path = new ChangeListener(){
                        @Override
                        public void stateChanged(ChangeEvent e){
                            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                            int index = sourceTabbedPane.getSelectedIndex();
                            //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                            //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                            //draw the next floor's path here if the correct tab is selected
                            if(index == 1)
                            {
                                //System.out.println("Second floor tab selected");
                                //clear the glass pane of the previously drawn path
                                c = new ClearGlassPane();
                                //write the text and draw path on the start floor
                                dp = new DrawPathAndWriteOnGlassPane(text,40,375,shortestPathNodes);
                            }
                            else
                            {
                                c = new ClearGlassPane();
                            }
                        }
                    };
                    mapPane.addChangeListener(path);
                }
                else
                {
                    //System.out.println("Multiple floors detected in shortest path");
                    //since multiple floors were detected in list, use nodeListA and nodeListB as paths to draw
                    nodeListA.clear();
                    nodeListB.clear();
                    nodeListA.add(WoodwardGraph.getVertex("Entrance"));
                    nodeListA.add(WoodwardGraph.getVertex("StairB2"));
                    String startNode = "";
                    
                    if(roomNumber.charAt(0)=='1') startNode = "StairB1";
                    else if (roomNumber.charAt(0)=='3') startNode = "StairB3";
                    else startNode = "StairB4";
 
                    //create a list of edges in shortest path
                    listOfEdges = graph.getShortestPath(startNode,roomNumber);

                    //create an ArrayList of Nodes in the shortest path
                    nodeListB = graph.getNodeList(listOfEdges);
                    
                    //place the Stair node as the first node in nodeListB
                    Node stair = null;
                    for(Node n:nodeListB)
                    {
                        if(n.getId().equals("StairB1") || n.getId().equals("StairB3") || n.getId().equals("StairB4"))
                        {
                          stair = n;                             
                        }
                    }
                    nodeListB.remove(stair);
                    nodeListB.add(0,stair);
                   
                    
                    //code to test the separate node lists for each floor
                    /*
                    System.out.println("\nNodeListA:");
                    for(Node node:nodeListA)
                    {
                        System.out.println(node.getId());
                    }
                    System.out.println("\nNodeListB:");
                    for(Node node:nodeListB)
                    {
                        System.out.println(node.getId());
                    }*/

                    //create a JPanel object that holds the tab associated with the second floor
                    JPanel f = getFloorTab("208");
                    //select the tab which is to be drawn on
                    mapPane.setSelectedComponent(f);
                    //write the text and draw path on the start floor
                    c = new ClearGlassPane();
                    dp = new DrawPathAndWriteOnGlassPane(textToWrite,40,375,nodeListA);

                    final String text2 = textToWrite;

                    //create a ChangeListener that will draw the rest of the path on the next floor when the correct tab is selected
                    ChangeListener endPath = new ChangeListener(){
                        @Override
                        public void stateChanged(ChangeEvent e){
                            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                            int index = sourceTabbedPane.getSelectedIndex();
                            //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                            //System.out.println("Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1:" + String.valueOf(Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1));
                            //draw the next floor's path here if the correct tab is selected
                            if(index == Integer.parseInt(String.valueOf(end.getId().charAt(0)))-1)
                            {
                                //System.out.println("Correct floor tab selected");
                                //clear the glass pane of the previously drawn path
                                c = new ClearGlassPane();
                                //write the text and draw path on the start floor
                                dp = new DrawPathAndWriteOnGlassPane(text2,40,375,nodeListB);
                            }
                            else if(index != 1)
                            {
                                c = new ClearGlassPane();
                            }
                        }
                    };
                    mapPane.addChangeListener(endPath);

                    //create a ChangeListener that will draw the first section of the path on the second floor from entrance to stairs
                    ChangeListener startPath = new ChangeListener(){
                        @Override
                        public void stateChanged(ChangeEvent e){
                            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                            int index = sourceTabbedPane.getSelectedIndex();                                
                            //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index) + " Index " + index);
                            //draw the next floor's path here if the correct tab is selected
                            if(index == 1)
                            {
                                //System.out.println("Second floor tab selected");
                                //clear the glass pane of the previously drawn path
                                c = new ClearGlassPane();
                                //write the text and draw path on the start floor
                                dp = new DrawPathAndWriteOnGlassPane(text,40,375,nodeListA);
                            }
                        }
                    };
                    mapPane.addChangeListener(startPath);   
                        
                }


            }
            else //let the user know their text was not found
            {
                String line = "\"" + userInput + "\" was not found.";
                c = new ClearGlassPane();
                wp = new WriteOnGlassPane(line,40,375);
            }

            //reset the other comboBoxes and set text field to null
            searchForTextField.setText(null);
            roomNameComboBox.setSelectedIndex(0);
            nameComboBox.setSelectedIndex(0);
            roomNumberComboBox.setSelectedIndex(0); 
            //make the text field ready to accept any text entered
            searchForTextField.requestFocus();
        }
    }
    /**
     * Returns a JPanel object of the tab of a floor map given a String room number
     * 
     * @param roomNum   a String containing the room Number
     * @return          a JPanel object of the tab
     */
    public JPanel getFloorTab(String roomNum)
    {
        JPanel floor = null;
        //System.out.println("roomNum = " + roomNum);
        Character rNumber = roomNum.charAt(0);
        int rNum = Character.getNumericValue(rNumber);
        
        switch(rNum)
        {
            case 1: floor = firstTab;
                break;
            case 2: floor = secondTab;
                break;
            case 3: floor = thirdTab;
                break;
            case 4: floor = fourthTab;
        }
        return floor;
    }
    /**
     * Returns a string of the room name  given a room number
     * and null if the number is not found 
     * 
     * @param rNum  a string containing the room number searched
     * @return      a string containing the room name or staff member's name
     */
    public String getNameByRoomNumber(String rNum)
    {   
        //search the room name list for the room number
        for (RoomName rn: roomNameArrayList)
        {
            if(rn.getRoom().equals(rNum))
            {
                return rn.getRoomName();
            }
        }
        return null;
    }
    /**
     * Returns a string of the staff member's name  given a room number
     * and null if the number is not found 
     * 
     * @param rNum  a string containing the room number searched
     * @return      a string containing the staff member's name
     */
    public String getStaffNameByRoomNumber(String rNum)
    {   
        //search the staffName list for the room number
        for(Name n: staffNames)
        {
            if(n.getRoom().equals(rNum))
            {
                return n.getName();
            }
        }
        return null;
    }
    /**
     * Returns a string of the room number given a first and last name
     * and null if the name is not found in staffNames
     * 
     * @param fname a String containing the first name of person searched
     * @param lname a String containing the last name of person searched
     * @return      a string containing the room number
     */
    public String getRoomNumberByName(String fname, String lname)
    {
        boolean found = false;
        String name;
        Name n;
        int index = 0;
        while(index < staffNames.size() && !found)
        {
            name = fname + " " + lname;
            n = staffNames.get(index);
            if(name.equals(n.getName()))
            {
                found = true;
                return n.getRoom();
            }
            index++;
        }
        return null;
    }
    /**
     * Returns a string of the room number given a room name, e.g. 3rd Floor Lounge
     * and null if the name is not found in roomNameArrayList
     * 
     * @param rName a String containing the room name searched
     * @return      a string containing the room number
     */
    public String getRoomNumberByRoomName(String rName)
    {
        boolean found = false;
        RoomName rn;
        int index = 0;
        while(index < roomNameArrayList.size() && !found)
        {
            rn = roomNameArrayList.get(index);
            if(rn.getRoomName().equals(rName))
            {
                found = true;
                return rn.getRoom();
            }
            index++;
        }
        return null;
    }
    
    /**
     * Returns true is the given String is on the roomNumberArrayList
     * 
     * @param input a String containing the text to be compared to the room number list
     * @return      true, if found
     */
    public boolean isOnRoomNumberList(String input)
    {
        int index = 0;
        String[] roomNumData;
        while(index < roomNumberArrayList.size())
        {
            roomNumData = roomNumberArrayList.get(index).split(",");
            if(roomNumData[0].equals(input))
            {
                return true;
            }
            index++;
        }
        return false;
    }
    
    /**
     * Returns true is the given String is on the roomNameArrayList
     * 
     * @param input a String containing the text to be compared to the room name list
     * @return      true, if found
     */
    public boolean isOnRoomNameList(String input)
    {
        int index = 0;
        while(index < roomNameArrayList.size())
        {
            if(roomNameArrayList.get(index).getRoomName().equals(input))
            {
                return true;
            }
            index++;
        }
        return false;
    }
    
    /**
     * Returns true is the given String is on the staffNames list
     * 
     * @param input a String containing the text to be compared to the staffNames list
     * @return      true, if found
     */
    public boolean isOnFacultyStaffList(String input)
    {
        int index = 0;
        while(index < staffNames.size())
        {
            //System.out.println(staffNames.get(index).getName() + "=" + input);
            if(staffNames.get(index).getName().equals(input))
            {
                return true;
            }
            index++;
        }
        return false;
    }
    /**
     * Returns an ArrayList of coordinates in the shortest path from entrance to given node
     * 
     * @param endNode   the destination Node of the path to be found
     * @return          the ArrayList of coordinates on the screen to draw the path from entrance to destination
     */
    public ArrayList<Coordinate> getShortestPathCoordinates(Node endNode)
    {
        ArrayList<Coordinate> coordinateList = new ArrayList<>();

        //create a list of edges in shortest path
        listOfEdges = graph.getShortestPath(endNode.getId());

        //create an ArrayList of Nodes in the shortest path
        shortestPathNodes = graph.getNodeList(listOfEdges);

        Coordinate coord;
        for (Node n : shortestPathNodes) 
        {          
                coord = new Coordinate(n.getX(),n.getY());
                coordinateList.add(coord);
        }
        

        //code to test that coordinate list is correct
        /*
        System.out.println();
        for(Coordinate point: coordinateList)
        {
            System.out.println(point.getX() + "," + point.getY());
        }
        */
        return coordinateList;
    }
    
    /**
     * Returns an ArrayList of coordinates in the shortest path given a list of Nodes
     * 
     * @param shortestPathNodes the list of Nodes in the path
     * @return                  the ArrayList of coordinates on the screen to draw the path from entrance to destination
     */
    public ArrayList<Coordinate> getShortestPathCoordinates(ArrayList<Node> shortestPathNodes)
    {
        ArrayList<Coordinate> coordinateList = new ArrayList<>();

        Coordinate coord;
        for (Node n : shortestPathNodes) 
        {
                coord = new Coordinate(n.getX(),n.getY());
                coordinateList.add(coord);
        }
        //code to test that coordinate list is correct
        /*
        System.out.println();
        for(Coordinate point: coordinateList)
        {
            System.out.println(point.getX() + "," + point.getY());
        }
        */
        return coordinateList;
    }
    
    final class ClearGlassPane extends JPanel
    {
        //clear the glass pane of any previous writing or drawing
        public ClearGlassPane()
        {
            this.setOpaque(true);
            setGlassPane(this);
            this.setVisible(false);
        }
    }
    final class WriteOnGlassPane extends JPanel
    {
        //coordinates where text is to be written
        private final int x,y;
        private final String text;
               
        //constructor to write text at certain coordinates
        public WriteOnGlassPane(String text, int x, int y)
        {
            this.setOpaque(false);
            setGlassPane(this);
            this.setVisible(true);
            this.x = x;
            this.y = y;
            this.text = text;
        }
        
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.setFont(new Font("TimesRoman",Font.BOLD,12));
            g.drawString(text,x,y);
        }
    }
   
    
    
    final class DrawPathAndWriteOnGlassPane extends JPanel
    {
        private final ArrayList<Node> pathToDraw;
        private final String text;
        private final int textX, textY;
        private ArrayList<Coordinate> path; //list of coordinates of path to be drawn
        
        //this contructor is used to draw something at coordinates x,y on glass pane
        public DrawPathAndWriteOnGlassPane(String text,int textX, int textY, ArrayList<Node> pathToDraw)
        {
            this.setOpaque(false);
            setGlassPane(this);
            this.setVisible(true);
            this.pathToDraw = pathToDraw;
            this.text = text;
            this.textX = textX;
            this.textY = textY;
        }
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.RED);
            /*
            System.out.println("\nNodes in path to draw:");
            for(Node n:pathToDraw)
            {
                System.out.println(n.getId());
            }
            */
            path = getShortestPathCoordinates(pathToDraw);
            //code to test that the path was correctly retreived
            /*
            System.out.println("\nCoordinate Path:");
            for(Coordinate c:path)
            {
               System.out.println(c.getX()+","+c.getY());
            }
            System.out.println();*/
            
            //draw starting icon on map
            if(path.get(1).getX() < path.get(0).getX())
                drawStartIcon(g,path.get(0).getX(),path.get(0).getY(),"left");
            else drawStartIcon(g,path.get(0).getX(),path.get(0).getY(),"right");
            //draw path
            int currentIndex = 0;
            for(Coordinate c:path)
            {
                //if this is not the first coordinate, send current coordinate and previous one to drawSegment method
                if(!c.equals(path.get(0)))
                {
                    currentIndex = path.indexOf(c);
                    drawSegment(g,path.get(currentIndex-1),c);
                }
            }
            //draw ending icon
            g.setColor(Color.BLUE);
            drawEndIcon(g,path.get(path.size()-1).getX(),path.get(path.size()-1).getY());

            g.setColor(Color.BLACK);
            g.setFont(new Font("TimesRoman",Font.BOLD,12));
            //split text around "," to write the text string on different lines because drawString method cannot handle \n
            int textHeight = g.getFontMetrics().getHeight();
            String[] lines;
            lines = text.split(",");
            for(int i = 0; i < lines.length; i++)
            {
                g.drawString(lines[i],textX,textY+textHeight*i);
            }
        }
    }
    public void drawSegment(Graphics g,Coordinate start, Coordinate end)
    {
        //System.out.println("Drawing segment from " + start.getX() + "," + start.getY() + " to " + end.getX() + "," + end.getY());
        
        //create a variable to represent coordinates that will used where segment have to turn (around obstacles, for example)
        Coordinate interCoord = null;
        //additional values by which x and y will be offset to make line segment appear correctly
        int extraX = 6;
        int extraY = 6;
        
            //if the second floor tab is enabled, check to see if segment intersects any obstacles on the second floor
            if((mapPane.getSelectedIndex()==1 && !intersectsObstacle(2,start,end)) || mapPane.getSelectedIndex()==0
                    || mapPane.getSelectedIndex()==2 || mapPane.getSelectedIndex()==3) 
            {
                //  if the two coordinates do not share an x or y value (meaning there is a straight line between them),
                //  calculate intermediate coordinate to draw between them to make line segments orthogonal
                if(start.getX() != end.getX() && start.getY() != end.getY())
                {
                    interCoord = new Coordinate(end.getX(),start.getY());
                    drawSegment(g,start,interCoord);
                    drawSegment(g,interCoord,end);
                }
                else
                {
                    g.drawLine(offsetX(start.getX(),extraX), offsetY(start.getY(),extraY), offsetX(end.getX(),extraX), offsetY(end.getY(),extraY));
                    g.drawLine(offsetX(start.getX(),extraX)+1, offsetY(start.getY(),extraY)+1, offsetX(end.getX(),extraX)+1, offsetY(end.getY(),extraY)+1);
                    g.drawLine(offsetX(start.getX(),extraX)+2, offsetY(start.getY(),extraY)+2, offsetX(end.getX(),extraX)+2, offsetY(end.getY(),extraY)+2);
                }
            }    
            else
            {
                //get the last node in the shortestPathNodes list. Which way the path is drawn around the obstacle depends on the location of this node
                Node lastNode = shortestPathNodes.get(shortestPathNodes.size()-1);
                //System.out.println("Last node is " + lastNode.getId() + " at " + lastNode.getX() + "," + lastNode.getY());
                
                //draw line segment up to the obstacle
                Obstacle o = getObstacle("StairB2");
                
                //if the x value of the start coordinate is greater than the x value of the right side of the obstacle
                //and the y value is between the upper and lower y values of the obstacle, draw the segment to the right side
                if(start.getX() > o.getLowerRight().getX()&& start.getY() < o.getLowerRight().getY())
                {
                    //System.out.println("LowerRight: " + o.getLowerRight().getX()+","+o.getLowerRight().getY() + "  Start: "+start.getX()+","+start.getY());
                    interCoord = new Coordinate(o.getLowerRight().getX()+5,start.getY());
                    drawSegment(g,start,interCoord);
                    
                    //determine which way to go around obstacle
                    //System.out.println("Last node:" + lastNode.getX() + "," + lastNode.getY() + "  start node:" + start.getX() + "," + start.getY());
                    //if the lastNode's y value is less than the start node's y value, draw up and around stairs
                    if(lastNode.getY() < start.getY())
                    {
                        start = interCoord;
                        interCoord = new Coordinate(o.getLowerRight().getX()+5,o.getUpperRight().getY()-5);
                        drawSegment(g,start,interCoord);
                        //draw to end
                        drawSegment(g,interCoord,end);
                    }
                    //if the lastNode's y value is greater than the start node's y value, draw down and around stairs
                    else
                    {
                        start = interCoord;
                        interCoord = new Coordinate(o.getLowerRight().getX()+5,o.getLowerRight().getY()+5);
                        drawSegment(g,start,interCoord);
                        //draw to end
                        drawSegment(g,interCoord,end);
                    }
                }
                //if the x value of the start coordinate is greater than the x value of the right side of the obstacle
                //and the y value is greater than the obstacle, draw line segment across the bottom of the rectangle
                else if(start.getX() > o.getLowerRight().getX() && (start.getY() > o.getLowerRight().getY()))
                {
                    interCoord = new Coordinate(o.getUpperLeft().getX()+5,start.getY());
                    drawSegment(g,start,interCoord);
                    //draw to end
                    drawSegment(g,interCoord,end);
                }
                //if the x value of the start coordinate is greater than the x value of the right side of the obstacle
                //and the y value is less than the obstacle, draw line segment across the top of the rectangle
                else if(start.getX() > o.getLowerRight().getX() && start.getY() < o.getLowerRight().getY())
                {
                    interCoord = new Coordinate(o.getLowerRight().getX()+5,start.getY());
                    drawSegment(g,start,interCoord);
                    //draw to end
                    drawSegment(g,interCoord,end);
                }
                      
            }
    }

    public boolean intersectsObstacle(int floor,Coordinate start, Coordinate end)
    {
        Obstacle o = null;
        switch(floor)
        {
            case 1: o = getObstacle("StairB1");
            case 2: o = getObstacle("StairB2");  
                break;
            case 3: o = getObstacle("StairB3");
                break;
            case 4: o = getObstacle("StairB4");
        }
        //System.out.println("Obstacle = " + o.getName());
        int width = o.getLowerRight().getX() - o.getUpperLeft().getX();
        int height = o.getLowerRight().getY() - o.getUpperLeft().getY();
        Rectangle r = new Rectangle(o.getUpperLeft().getX(),o.getUpperLeft().getY(),width,height);
        Line2D l = new Line2D.Float(start.getX(),start.getY(),end.getX(),end.getY());
        //System.out.println("l.intersects(r) = " + l.intersects(r));
        return l.intersects(r);
    }
    
    public Obstacle getObstacle(String name)
    {   
        for(Obstacle o:obstacleList)
        {
            //System.out.println(o.toString());
            if(o.getName().equals(name))
            {
                return o;
            }
        }
        return null;
    }
    
    //method to draw an arrow at the start of the path given the x and y coordinates of the beginning of path
    public void drawStartIcon(Graphics g,int x, int y, String direction)
    {
        //additional value that x and y will be offset
        int extraX = 6;
        int extraY = 7;
        //arrow will be a triangle with x,y at its apex
        //w represents the width of the base
        //h represents the height
        int w = 20;
        int h = 10;
        
        int[] pointXLeft = {offsetX(x,extraX),offsetX(x+h,extraX),offsetX(x+h,extraX)};
        int[] pointYLeft = {offsetY(y,extraY),offsetY(y-w/2,extraY),offsetY(y+w/2,extraY)};
        int[] pointXRight = {offsetX(x,extraX+4),offsetX(x-h,extraX+4),offsetX(x-h,extraX+4)};
        int[] pointYRight = {offsetY(y,extraY),offsetY(y-w/2,extraY),offsetY(y+w/2,extraY)};
        
        //create an array of x values of triangle and y values of triangle
        if(direction.equals("left"))
        {
            //create a polygon from the arrays
            Polygon p = new Polygon(pointXLeft,pointYLeft,3);
            g.fillPolygon(p); 
        }
        else
        {
            //create a polygon from the arrays
            Polygon p = new Polygon(pointXRight,pointYRight,3);
            g.fillPolygon(p); 
        }
        

    }
    //method to draw an blue circle at the end of the path given the x and y coordinates of the end of path
    public void drawEndIcon(Graphics g,int x,int y)
    {
        //circle size
        int radius = 13;
        //additional value that s and y will be offset
        int extraX = 7;
        int extraY = 7;
        
        x = x - (radius/2);
        y = y - (radius/2);
        g.fillOval(offsetX(x,extraX),offsetY(y,extraY),radius,radius);
    }
    
    public int offsetX(int x,int additionalValue)
    {
        //get bounding rectangle of mapPane for offset of x and y values
        Rectangle r = mapPane.getBounds();
        return x+(int)r.getX()+ additionalValue;
    }
    
    public int offsetY(int y,int additionalValue)
    {
        //get bounding rectangle of mapPane for offset of x and y values
        Rectangle r = mapPane.getBounds();
        return y+(int)r.getY()+ additionalValue;
    }
    
    final class DrawAndWriteOnGlassPane extends JPanel
    {
        private final int x; 
        private final int y;             //x and y coordinates of square to be drawn on
        private final String text;
        private final int textX, textY;
        
        //this contructor is used to draw something at coordinates x,y on glass pane
        public DrawAndWriteOnGlassPane(String text,int textX, int textY, int x, int y)
        {
            this.setOpaque(false);
            setGlassPane(this);
            this.setVisible(true);
            this.x = x;
            this.y = y;
            this.text = text;
            this.textX = textX;
            this.textY = textY;
        }
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillRect(x+3,y+3,10,10);
            g.setColor(Color.BLACK);
            g.setFont(new Font("TimesRoman",Font.BOLD,12));
            //split text around "," to write the text string on different lines because drawString method cannot handle \n
            int textHeight = g.getFontMetrics().getHeight();
            String[] lines;
            lines = text.split(",");
            for(int i = 0; i < lines.length; i++)
            {
                g.drawString(lines[i],textX,textY+textHeight*i);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MapDisplay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new MapDisplay2().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(MapDisplay2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

                 
}
