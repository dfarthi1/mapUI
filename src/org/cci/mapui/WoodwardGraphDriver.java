/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cci.mapui;

/* -----------------
 * WoodwardGraph.java
 * -----------------
 * 
 *
 * Author:  David Farthing
 * Date:	21 May 2014	
 *
 *
 * This program uses JGraphT 0.8.1, a free graph library that provides graph-theory objects
 * and algorithms by Barak Naveh, et al. 
 */



import org.jgrapht.graph.*;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class WoodwardGraphDriver 
{
	
	public static void main(String[] args) throws IOException
	{
            Scanner keyboard = new Scanner(System.in);
	    String endOfPath = null;
	
	    WoodwardGraph w = new WoodwardGraph();
	    
	    Node startNode = null, endNode = null;
            
            //create a copy of the vertexList from the WoodwardGraph class
            ArrayList<String> vertexList = w.getVertexList();
	    
	    //Code to test finding the shortest path by name
	    /*
	    do {
	    	System.out.print("Enter first and last name of person to find shortest path to (0 to quit): ");
	        name_to_find = keyboard.nextLine();
	        if(! name_to_find.equals("0")) {
		        //search array list of names to find index of end of path
		        boolean found = false;
		    	int i = 0;
		        while(!found)
		        {
		        	for(Name item:nameList)
		        	{
		        			
		        		if(item.getName().equals(name_to_find))
		        		{
		        			found = true;
		        			endOfPath = item.getRoom();
		        			endNode = Node.getVertex(g,endOfPath);
		        		}
		        		i++;
		        	}
		        	if(!found)
		        	{
		        		System.out.println("Could not find: " + name_to_find);
		        		System.out.print("Please enter a valid name: ");
		        		name_to_find = keyboard.nextLine();
		        		i = 0;
		        	}
		        }
		        
		        startNode = Node.getVertex(g,"Entrance");
		        //create a DijkstraShortestPath object
		        DijkstraShortestPath<Node,DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<Node,DefaultWeightedEdge>(g,startNode,endNode);
		        
		        //create a list of edges in shortest path
		        List<DefaultWeightedEdge> listOfEdges = shortestPath.getPathEdgeList();
	
		        for(DefaultWeightedEdge d1: listOfEdges)
		        {
		        	Node source = g.getEdgeSource(d1);
		        	Node target = g.getEdgeTarget(d1);
		        	System.out.println(source.getId() + " : " + target.getId());
		        }
	        }//end if(! name_to_find.equals("0"))    
	    } while (! name_to_find.equals("0"));
	    */
	    
	    //Code to test finding the shortest path by node id (room number)
	    
	    do {
	    	System.out.print("Enter node to find shortest path to (0 to quit): ");
	        endOfPath = keyboard.nextLine();
	        if(! endOfPath.equals("0")) {
		        //search array list of vertices to find index of end of path
		        boolean found = false;
		    	int i = 0;
		        while(!found)
		        {
		        	for(String vertex:vertexList)
		        	{
		        			
		        		if(vertex.startsWith(endOfPath))
		        		{
		        			found = true;
		        			endNode = WoodwardGraph.getVertex(endOfPath);
		        		}
		        		i++;
		        	}
		        	if(!found)
		        	{
		        		System.out.println("Could not find node: " + endOfPath);
		        		System.out.print("Please enter a valid node: ");
		        		endOfPath = keyboard.nextLine();
		        		i = 0;
		        	}
		        }
		        
		        startNode = WoodwardGraph.getVertex("Entrance");
		        
		        //create a list of edges in shortest path
		        List<DefaultWeightedEdge> listOfEdges = w.getShortestPath(endOfPath);
                        
                        //create an ArrayList of Nodes in the shortest path
                        ArrayList<Node> shortestPathNodes = w.getNodeList(listOfEdges);
                        
                        for(Node n: shortestPathNodes)
                        {
                            System.out.println(n.getId());
                        }
	        }//end if (! endOfPath.eq("0"))    
	    } while (! endOfPath.equals("0"));
	    
	    keyboard.close();
	

    }//end main method
	
}
